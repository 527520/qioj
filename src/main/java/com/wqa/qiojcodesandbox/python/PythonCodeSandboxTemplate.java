package com.wqa.qiojcodesandbox.python;

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

/**
 * Python 代码沙箱模板方法实现
 */
@Slf4j
public abstract class PythonCodeSandboxTemplate implements CodeSandBox {

    private static final String GLOBAL_CODE_DIR_NAME = "tempCode";

    /**
     * 代码统一名称
     */
    private static final String GLOBAL_PYTHON_CLASS_NAME = "Main.py";

    /**
     * 代码运行超时时间
     */
    static final Long TIME_OUT = 5000L;

    public static final WordTree WORD_TREE;

    static {
        // 初始化字典树
        WORD_TREE = new WordTree();
        WORD_TREE.addWords(CodeBlackList.PYTHON_SENSITIVE_WORD_LIST.getSensitiveWords());
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        String code = executeCodeRequest.getCode();

        // 校验代码是否包含黑名单中的命令
        FoundWord foundWord = WORD_TREE.matchWord(code);
        if (foundWord != null && StrUtil.isNotBlank(foundWord.getFoundWord())) {
            System.out.println("敏感词：" + foundWord.getFoundWord());
            return new ExecuteCodeResponse(null, "代码包含敏感词：" + foundWord.getFoundWord(), 3, new JudgeInfo());
        }

        List<String> inputList = executeCodeRequest.getInputList();
        String language = executeCodeRequest.getLanguage();

        // 保存用户代码文件
        File userCodeFile = saveCodeToFile(code, language, GLOBAL_CODE_DIR_NAME, GLOBAL_PYTHON_CLASS_NAME);

        // Python 是解释型语言，不需要编译，直接运行
        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList, language);

        // 收集结果
        ExecuteCodeResponse executeCodeResponse = getOutputResponse(executeMessageList);

        // 删除文件
        boolean deleteResult = delCodeFile(userCodeFile);
        if (deleteResult) {
            log.info("删除文件成功");
        }

        return executeCodeResponse;
    }

    /**
     * 1. 保存用户代码
     *
     * @param code          代码
     * @param language      语言
     * @param globalCodePath 全局代码路径
     * @param fileName      文件名
     * @return 代码文件
     */
    public File saveCodeToFile(String code, String language, String globalCodePath, String fileName) {
        String projectPath = System.getProperty("user.dir");
        String globalCodePathName = projectPath + File.separator + globalCodePath;
        if (!FileUtil.exist(globalCodePathName)) {
            FileUtil.mkdir(globalCodePathName);
        }
        // 把用户代码隔离
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        String userCodePath = userCodeParentPath + File.separator + fileName;
        return FileUtil.writeUtf8String(code, userCodePath);
    }

    /**
     * 2. 检查 Python 语法（可选）
     *
     * @param userCodeFile 用户代码文件
     * @param language     语言（python2 或 python3）
     * @return 检查结果
     */
    public ExecuteMessage checkSyntax(File userCodeFile, String language) {
        // 使用 python -m py_compile 检查语法
        String pythonCmd = getPythonCommand(language);
        String checkCmd = String.format("%s -m py_compile %s", pythonCmd, userCodeFile.getAbsolutePath());

        try {
            Process checkProcess = Runtime.getRuntime().exec(checkCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(checkProcess, "语法检查");

            if (executeMessage.getExitValue() != 0) {
                executeMessage.setExitValue(1);
                executeMessage.setErrorMessage("Syntax Error: " + executeMessage.getErrorMessage());
            }

            return executeMessage;
        } catch (IOException e) {
            ExecuteMessage executeMessage = new ExecuteMessage();
            executeMessage.setExitValue(1);
            executeMessage.setMessage(e.getMessage());
            executeMessage.setErrorMessage("系统错误");
            return executeMessage;
        }
    }

    /**
     * 3. 运行代码
     *
     * @param userCodeFile 用户代码文件
     * @param inputList    输入用例
     * @param language     语言（python2 或 python3）
     * @return 执行结果列表
     */
    public abstract List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList, String language);

    /**
     * 4) 收集运行结果
     *
     * @param executeMessageList 执行结果列表
     * @return 响应对象
     */
    public ExecuteCodeResponse getOutputResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        long maxTime = 0;
        long maxMemory = 0;

        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                outputList.add(executeMessage.getMessage());
                // 执行中出现错误
                executeCodeResponse.setStatus(3);
                executeCodeResponse.setJudgeInfo(new JudgeInfo(errorMessage, null, null));
                break;
            }

            // 如果没有错误信息就正常添加
            outputList.add(executeMessage.getMessage());
            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
            Long memory = executeMessage.getMemory();
            if (memory != null) {
                maxMemory = Math.max(maxMemory, memory);
            }
        }

        // 没有错误信息
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(2);
            executeCodeResponse.setMessage("成功");
        }

        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        return executeCodeResponse;
    }

    /**
     * 5) 删除文件
     *
     * @param userCodeFile 用户代码文件
     * @return 是否删除成功
     */
    public boolean delCodeFile(File userCodeFile) {
        if (userCodeFile.getParentFile() != null) {
            boolean del = FileUtil.del(userCodeFile.getParentFile().getAbsolutePath());
            System.out.println("删除" + (del ? "成功" : "失败"));
            return del;
        }
        return true;
    }

    /**
     * 获取 Python 命令
     *
     * @param language 语言（python2 或 python3）
     * @return Python 命令
     */
    protected String getPythonCommand(String language) {
        if ("python2".equalsIgnoreCase(language)) {
            return "python2";
        } else {
            // 默认使用 python3
            return "python3";
        }
    }
}
