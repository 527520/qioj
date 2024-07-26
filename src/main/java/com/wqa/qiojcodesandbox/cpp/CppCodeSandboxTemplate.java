package com.wqa.qiojcodesandbox.cpp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.FoundWord;
import cn.hutool.dfa.WordTree;
import com.wqa.qiojcodesandbox.CodeSandBox;
import com.wqa.qiojcodesandbox.constant.CodeBlackList;
import com.wqa.qiojcodesandbox.model.ExecuteCodeRequest;
import com.wqa.qiojcodesandbox.model.ExecuteCodeResponse;
import com.wqa.qiojcodesandbox.model.ExecuteMessage;
import com.wqa.qiojcodesandbox.model.JudgeInfo;
import com.wqa.qiojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class CppCodeSandboxTemplate implements CodeSandBox {

    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    /**
     * 代码统一名称
     */
    private static final String GLOBAL_C_CLASS_NAME = "Main.c";

    /**
     * 代码运行超时时间
     */
    static final Long TIME_OUT = 5000L;

    public static final WordTree WORD_TREE;

    static {
        // 初始化字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(CodeBlackList.C_SENSITIVE_WORD_LIST.getSensitiveWords());
    }


    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();

        // 校验代码是否包含黑名单中的命令
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null && StrUtil.isNotBlank(foundWord.getFoundWord())) {
            System.out.println("敏感词：" + foundWord.getFoundWord());
            return null;
        }

        List<String> inputList = executeCodeRequest.getInputList();
        String language = executeCodeRequest.getLanguage();

        //保存用户代码文件
        File userCodeFile = saveCodeToFile(code, language, GLOBAL_CODE_DIR_NAME, GLOBAL_C_CLASS_NAME);

        //编译文件
        ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        System.out.println("编译结果：" + compileFileExecuteMessage);

        if (compileFileExecuteMessage.getErrorMessage() != null) {
            // 返回编译错误信息
            return new ExecuteCodeResponse(null, "失败", 3, new JudgeInfo(compileFileExecuteMessage.getErrorMessage(), null, null));
        }

        //运行文件
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);

        //收集结果
        ExecuteCodeResponse executeCodeResponse = getOutputResponse(executeMessageList);

        // 删除文件
        boolean deleteResult = delCodeFile(userCodeFile);
        if (deleteResult) {
            log.info("删除文件成功");
        }

        return executeCodeResponse;
    }

    /**
     * 1.保存用户代码
     *
     * @param code     代码
     * @param language 语言
     * @return
     */
    public File saveCodeToFile(String code, String language , String globalCodePath, String fileName) {
        String projectPath = System.getProperty("user.dir");
        String globalCodePathName = projectPath + File.separator + globalCodePath;
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        //把用户代码隔离
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + fileName;
        return FileUtil.writeUtf8String(code, userCodePath);
    }


    /**
     * 2.编译代码
     *
     * @param userCodeFile 用户代码文件
     * @return
     */
    public ExecuteMessage compileFile(File userCodeFile) {

        String osName = System.getProperty("os.name").toLowerCase();
        //2.编译程序命令
        String compileCmd = String.format("g++ %s -o %sMain.exe", userCodeFile.getAbsolutePath(), userCodeFile.getParent() + File.separator);
        if (osName.contains("nix") || osName.contains("nux")) {
            compileCmd = String.format("g++ %s -o %sMain", userCodeFile.getAbsolutePath(), userCodeFile.getParent() + File.separator);
        }
        try {
            Process complieProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(complieProcess, "编译");
            if (executeMessage.getExitValue() != 0) {
                executeMessage.setExitValue(1);
                executeMessage.setMessage("编译错误");
                executeMessage.setErrorMessage("Compile Error");
            }
            //返回执行结果
            return executeMessage;
        } catch (Exception e) {
            // 未知错误
            ExecuteMessage executeMessage = new ExecuteMessage();
            executeMessage.setExitValue(1);
            executeMessage.setMessage(e.getMessage());
            executeMessage.setErrorMessage("系统错误");
            return executeMessage;
        }
    }

    /**
     * 3.运行代码
     *
     * @param userCodeFile 用户代码文件
     * @param inputList    输入用例
     * @return
     */
    public List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String input : inputList) {
            String runCmd = String.format("%sMain.exe", userCodeParentPath + File.separator);

            String osName = System.getProperty("os.name").toLowerCase();

            if (osName.contains("nix") || osName.contains("nux")) {
                runCmd = String.format("%sMain", userCodeParentPath + File.separator);
            }
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
                ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, input);
                System.out.println("本次运行结果：" + executeMessage);
                if (executeMessage.getExitValue() != 0) {
                    executeMessage.setExitValue(1);
                    executeMessage.setMessage("运行错误");
                    executeMessage.setErrorMessage("Runtime Error");
                }
                executeMessageList.add(executeMessage);
            } catch (IOException e) {
                // 未知错误
                ExecuteMessage executeMessage = new ExecuteMessage();
                executeMessage.setExitValue(1);
                executeMessage.setMessage(e.getMessage());
                executeMessage.setErrorMessage("系统错误");
                executeMessageList.add(executeMessage);
            }
        }
        return executeMessageList;
    }

    /**
     * 4) 收集运行结果
     * @param executeMessageList 执行结果列表
     * @return
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        //4. 收集整理输出结果
        //准备返回信息对象
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0;
        long maxMemory = 0;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                outputList.add(executeMessage.getMessage());
                //执行中出现错误
                // 用户提交的代码执行中存在错误,直接返回
                executeCodeResponse.setStatus(3);
                executeCodeResponse.setJudgeInfo(new JudgeInfo(errorMessage, null, null));
                break;
            }
            //如果没有错误信息就正常添加
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);

            }
            Long memory = executeMessage.getMemory();
            if (memory != null)
            {
                maxMemory += memory;
            }
        }
        //没有错误信息
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(2);
            executeCodeResponse.setMessage("成功");
        }
        executeCodeResponse.setOutputList(outputList);
        //正常运行
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setTime(maxTime);
        // 运行正常完成则不设置message，交由判题机判题
        executeCodeResponse.setJudgeInfo(judgeInfo);
        System.out.println(executeCodeResponse);
        return executeCodeResponse;
    }

    /**
     * 5) 删除文件
     * @param userCodeFile 用户代码文件
     * @return
     */
    public boolean delCodeFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeFile.getParentFile().getAbsolutePath());
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }
}
