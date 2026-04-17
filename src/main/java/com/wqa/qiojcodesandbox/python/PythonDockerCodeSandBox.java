package com.wqa.qiojcodesandbox.python;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.PullImageCmd;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PullResponseItem;
import com.github.dockerjava.api.model.Statistics;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.api.model.Volume;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.wqa.qiojcodesandbox.model.ExecuteCodeRequest;
import com.wqa.qiojcodesandbox.model.ExecuteCodeResponse;
import com.wqa.qiojcodesandbox.model.ExecuteMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Python Docker 代码沙箱实现
 * 支持 Python2 和 Python3
 */
@Slf4j
public class PythonDockerCodeSandBox extends PythonCodeSandboxTemplate {

    /**
     * Python3 Docker 镜像名称
     */
    private static final String PYTHON3_IMAGE_NAME = "python:3.9-slim";

    /**
     * Python2 Docker 镜像名称
     */
    private static final String PYTHON2_IMAGE_NAME = "python:2.7-slim";

    /**
     * 是否首次初始化 Python3 镜像
     */
    private static volatile boolean python3FirstInit = true;

    /**
     * 是否首次初始化 Python2 镜像
     */
    private static volatile boolean python2FirstInit = true;

    public static void main(String[] args) {
        PythonDockerCodeSandBox pythonDockerCodeSandBox = new PythonDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("python3");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.py", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        ExecuteCodeResponse executeCodeResponse = pythonDockerCodeSandBox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList, String language) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        // 获取 Docker 客户端
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();

        // 根据语言版本选择镜像
        String imageName = getImageName(language);

        // 拉取镜像（仅首次）
        if ("python2".equalsIgnoreCase(language)) {
            if (python2FirstInit) {
                pullImage(dockerClient, imageName);
                python2FirstInit = false;
            }
        } else {
            if (python3FirstInit) {
                pullImage(dockerClient, imageName);
                python3FirstInit = false;
            }
        }

        // 创建容器
        String containerId = createContainer(dockerClient, userCodeParentPath, imageName);

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        log.info("容器启动成功，容器ID: {}", containerId);

        // 执行代码并获取结果
        List<ExecuteMessage> executeMessageList = executeInContainer(dockerClient, containerId, inputList, language);

        // 清理容器
        cleanContainer(dockerClient, containerId);

        return executeMessageList;
    }

    /**
     * 获取镜像名称
     *
     * @param language 语言版本
     * @return 镜像名称
     */
    private String getImageName(String language) {
        if ("python2".equalsIgnoreCase(language)) {
            return PYTHON2_IMAGE_NAME;
        } else {
            return PYTHON3_IMAGE_NAME;
        }
    }

    /**
     * 拉取 Docker 镜像
     *
     * @param dockerClient Docker 客户端
     * @param imageName    镜像名称
     */
    private void pullImage(DockerClient dockerClient, String imageName) {
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(imageName);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("下载镜像：" + item.getStatus());
                super.onNext(item);
            }
        };
        try {
            pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
            log.info("镜像下载完成: {}", imageName);
        } catch (InterruptedException e) {
            log.error("拉取镜像异常", e);
            throw new RuntimeException("拉取镜像失败", e);
        }
    }

    /**
     * 创建 Docker 容器
     *
     * @param dockerClient       Docker 客户端
     * @param userCodeParentPath 用户代码父目录路径
     * @param imageName          镜像名称
     * @return 容器ID
     */
    private String createContainer(DockerClient dockerClient, String userCodeParentPath, String imageName) {
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(imageName);
        HostConfig hostConfig = new HostConfig();
        // 挂载代码目录到容器
        hostConfig.setBinds(new Bind(userCodeParentPath, new Volume("/app")));
        // 资源限制
        hostConfig.withMemory(100 * 1000 * 1000L); // 100MB 内存
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);

        CreateContainerResponse createContainerResponse = containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true) // 禁用网络
                .withReadonlyRootfs(true)  // 只读文件系统
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();

        String containerId = createContainerResponse.getId();
        log.info("容器创建成功，容器ID: {}", containerId);
        return containerId;
    }

    /**
     * 在容器中执行 Python 程序
     *
     * @param dockerClient Docker 客户端
     * @param containerId  容器ID
     * @param inputList    输入用例列表
     * @param language     语言版本（python2 或 python3）
     * @return 执行结果列表
     */
    private List<ExecuteMessage> executeInContainer(DockerClient dockerClient, String containerId, List<String> inputList, String language) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        // 获取容器内的 Python 命令
        String pythonCmd = "python2".equalsIgnoreCase(language) ? "python2" : "python3";

        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String[] arg = inputArgs.split(" ");
            long time = 0L;
            final boolean[] timeout = {true};

            // 执行命令: python3 Main.py arg1 arg2
            String[] cmdArray = ArrayUtil.append(new String[]{pythonCmd, "/app/Main.py"}, arg);

            ExecCreateCmdResponse execCreateCmdResponse = dockerClient
                    .execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStdin(true)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .exec();

            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] message = {null};
            final String[] errorMessage = {null};
            String execId = execCreateCmdResponse.getId();

            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
                @Override
                public void onComplete() {
                    timeout[0] = false;
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessage[0] = new String(frame.getPayload()).trim();
                        System.out.println("输出错误结果：" + errorMessage[0]);
                    } else {
                        message[0] = new String(frame.getPayload()).trim();
                        System.out.println("输出结果：" + message[0]);
                    }
                    super.onNext(frame);
                }
            };

            // 获取内存占用
            final long[] maxMemory = {0L};
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> resultCallback = new ResultCallback<Statistics>() {
                @Override
                public void onStart(Closeable closeable) {
                }

                @Override
                public void onNext(Statistics statistics) {
                    if (statistics.getMemoryStats() != null && statistics.getMemoryStats().getUsage() != null) {
                        Long usage = statistics.getMemoryStats().getUsage();
                        maxMemory[0] = Math.max(usage, maxMemory[0]);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    log.error("获取内存统计出错", throwable);
                }

                @Override
                public void onComplete() {
                }

                @Override
                public void close() throws IOException {
                }
            };
            statsCmd.exec(resultCallback);

            try {
                stopWatch.start();
                dockerClient.execStartCmd(execId)
                        .exec(execStartResultCallback)
                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
                stopWatch.stop();
                time = stopWatch.getLastTaskTimeMillis();
                statsCmd.close();
            } catch (InterruptedException e) {
                log.error("程序执行异常", e);
                throw new RuntimeException("程序执行异常", e);
            }

            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setMessage(message[0]);
            executeMessage.setTime(time);
            executeMessage.setMemory(maxMemory[0]);
            executeMessageList.add(executeMessage);
        }

        return executeMessageList;
    }

    /**
     * 清理容器
     *
     * @param dockerClient Docker 客户端
     * @param containerId  容器ID
     */
    private void cleanContainer(DockerClient dockerClient, String containerId) {
        try {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();
            log.info("容器清理完成，容器ID: {}", containerId);
        } catch (Exception e) {
            log.error("清理容器失败", e);
        }
    }
}