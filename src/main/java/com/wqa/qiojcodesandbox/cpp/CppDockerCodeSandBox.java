package com.wqa.qiojcodesandbox.cpp;

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
 * C++ 语言 Docker 代码沙箱实现
 */
@Slf4j
public class CppDockerCodeSandBox extends CppCodeSandboxTemplate {

    /**
     * Docker 镜像名称
     */
    private static final String IMAGE_NAME = "gcc:latest";

    /**
     * 是否首次初始化（需要拉取镜像）
     */
    public static Boolean firstInit = true;

    public static void main(String[] args) {
        CppDockerCodeSandBox cppDockerCodeSandBox = new CppDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("cpp");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.cpp", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        ExecuteCodeResponse executeCodeResponse = cppDockerCodeSandBox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    /**
     * 3) 使用 Docker 容器运行代码
     *
     * @param userCodeFile 用户代码文件
     * @param inputList    输入用例列表
     * @return 执行结果列表
     */
    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();

        // 获取 Docker 客户端
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();

        // 拉取镜像（仅首次）
        if (firstInit) {
            pullImage(dockerClient);
            firstInit = false;
        }

        // 创建容器
        String containerId = createContainer(dockerClient, userCodeParentPath);

        // 启动容器
        dockerClient.startContainerCmd(containerId).exec();
        log.info("容器启动成功，容器ID: {}", containerId);

        // 在容器中编译 C++ 代码
        ExecuteMessage compileMessage = compileInContainer(dockerClient, containerId, userCodeFile.getName());
        if (compileMessage.getExitValue() != 0) {
            // 编译失败
            List<ExecuteMessage> result = new ArrayList<>();
            result.add(compileMessage);
            // 清理容器
            cleanContainer(dockerClient, containerId);
            return result;
        }

        // 执行代码并获取结果
        List<ExecuteMessage> executeMessageList = executeInContainer(dockerClient, containerId, inputList);

        // 清理容器
        cleanContainer(dockerClient, containerId);

        return executeMessageList;
    }

    /**
     * 拉取 Docker 镜像
     *
     * @param dockerClient Docker 客户端
     */
    private void pullImage(DockerClient dockerClient) {
        PullImageCmd pullImageCmd = dockerClient.pullImageCmd(IMAGE_NAME);
        PullImageResultCallback pullImageResultCallback = new PullImageResultCallback() {
            @Override
            public void onNext(PullResponseItem item) {
                System.out.println("下载镜像：" + item.getStatus());
                super.onNext(item);
            }
        };
        try {
            pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
            log.info("镜像下载完成: {}", IMAGE_NAME);
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
     * @return 容器ID
     */
    private String createContainer(DockerClient dockerClient, String userCodeParentPath) {
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(IMAGE_NAME);
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
     * 在容器中编译 C++ 代码
     *
     * @param dockerClient  Docker 客户端
     * @param containerId   容器ID
     * @param codeFileName  代码文件名
     * @return 编译结果
     */
    private ExecuteMessage compileInContainer(DockerClient dockerClient, String containerId, String codeFileName) {
        // 编译命令: g++ -o Main Main.cpp
        String[] compileCmd = {"g++", "-o", "/app/Main", "/app/" + codeFileName};

        ExecCreateCmdResponse execCreateCmdResponse = dockerClient
                .execCreateCmd(containerId)
                .withCmd(compileCmd)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .exec();

        ExecuteMessage executeMessage = new ExecuteMessage();
        final String[] errorMessage = {null};
        final String[] message = {null};
        final int[] exitCode = {0};

        ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback() {
            @Override
            public void onNext(Frame frame) {
                StreamType streamType = frame.getStreamType();
                if (StreamType.STDERR.equals(streamType)) {
                    errorMessage[0] = new String(frame.getPayload()).trim();
                    System.out.println("编译错误：" + errorMessage[0]);
                } else {
                    message[0] = new String(frame.getPayload()).trim();
                }
                super.onNext(frame);
            }

            @Override
            public void onComplete() {
                super.onComplete();
            }
        };

        try {
            dockerClient.execStartCmd(execCreateCmdResponse.getId())
                    .exec(execStartResultCallback)
                    .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);

            // 获取退出码
            exitCode[0] = dockerClient.inspectExecCmd(execCreateCmdResponse.getId()).exec().getExitCodeLong().intValue();
        } catch (InterruptedException e) {
            log.error("编译过程异常", e);
            executeMessage.setErrorMessage("编译超时");
            executeMessage.setExitValue(1);
            return executeMessage;
        }

        executeMessage.setMessage(message[0]);
        executeMessage.setErrorMessage(errorMessage[0]);
        executeMessage.setExitValue(exitCode[0]);

        return executeMessage;
    }

    /**
     * 在容器中执行编译后的程序
     *
     * @param dockerClient Docker 客户端
     * @param containerId  容器ID
     * @param inputList    输入用例列表
     * @return 执行结果列表
     */
    private List<ExecuteMessage> executeInContainer(DockerClient dockerClient, String containerId, List<String> inputList) {
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String[] arg = inputArgs.split(" ");
            long time = 0L;
            final boolean[] timeout = {true};

            // 执行命令: /app/Main arg1 arg2
            String[] cmdArray = ArrayUtil.append(new String[]{"/app/Main"}, arg);

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
