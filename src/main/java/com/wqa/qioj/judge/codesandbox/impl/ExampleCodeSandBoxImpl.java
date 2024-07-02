package com.wqa.qioj.judge.codesandbox.impl;

import com.wqa.qioj.judge.codesandbox.CodeSandBox;
import com.wqa.qioj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wqa.qioj.judge.codesandbox.model.ExecuteCodeResponse;
import com.wqa.qioj.model.dto.questionsubmit.JudgeInfo;
import com.wqa.qioj.model.entity.QuestionSubmit;
import com.wqa.qioj.model.enums.JudgeInfoMessageEnum;
import com.wqa.qioj.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * 示例代码沙箱
 */
public class ExampleCodeSandBoxImpl implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();

        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("测试执行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);

        executeCodeResponse.setJudgeInfo(judgeInfo);
        
        return executeCodeResponse;
    }
}
