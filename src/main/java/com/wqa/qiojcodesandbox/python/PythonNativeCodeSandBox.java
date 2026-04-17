package com.wqa.qiojcodesandbox.python;

import cn.hutool.core.io.resource.ResourceUtil;
import com.wqa.qiojcodesandbox.model.ExecuteCodeRequest;
import com.wqa.qiojcodesandbox.model.ExecuteCodeResponse;
import com.wqa.qiojcodesandbox.model.ExecuteMessage;
import com.wqa.qiojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Python 原生代码沙箱实现
 * 支持 Python2 和 Python3
 */
@Slf4j
public class PythonNativeCodeSandBox extends PythonCodeSandboxTemplate {

    public static void main(String[] args) {
        PythonNativeCodeSandBox pythonNativeCodeSandBox = new PythonNativeCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("python3");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.py", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        ExecuteCodeResponse executeCodeResponse = pythonNativeCodeSandBox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    @Override
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList, String language) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();

        // 获取对应的 Python 命令
        String pythonCmd = getPythonCommand(language);

        for (String input : inputList) {
            // 构建运行命令：python3 Main.py
            String runCmd = String.format("%s %s/Main.py", pythonCmd, userCodeParentPath);

            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);

                // 安全控制：限制最大运行时间，超时控制
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        runProcess.destroy();
                        System.out.println("超过程序最大运行时间，终止进程");
                    } catch (InterruptedException e) {
                        System.out.println("结束");
                    }
                }).start();

                // 执行交互式输入
                ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, input);
                System.out.println("本次运行结果：" + executeMessage);

                if (executeMessage.getExitValue() != 0) {
                    executeMessage.setExitValue(1);
                    executeMessage.setMessage("运行错误");
                    executeMessage.setErrorMessage("Runtime Error: " + executeMessage.getErrorMessage());
                }

                executeMessageList.add(executeMessage);
            } catch (IOException e) {
                // 未知错误
                ExecuteMessage executeMessage = new ExecuteMessage();
                executeMessage.setExitValue(1);
                executeMessage.setMessage(e.getMessage());
                executeMessage.setErrorMessage("系统错误: " + e.getMessage());
                executeMessageList.add(executeMessage);
            }
        }

        return executeMessageList;
    }
}
