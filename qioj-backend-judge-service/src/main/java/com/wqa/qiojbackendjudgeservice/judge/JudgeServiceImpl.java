package com.wqa.qiojbackendjudgeservice.judge;

import cn.hutool.json.JSONUtil;
import com.wqa.qiojbackendcommon.common.ErrorCode;
import com.wqa.qiojbackendcommon.exception.BusinessException;
import com.wqa.qiojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.wqa.qiojbackendjudgeservice.judge.codesandbox.CodeSandBoxFactory;
import com.wqa.qiojbackendjudgeservice.judge.codesandbox.CodeSandBoxProxy;
import com.wqa.qiojbackendjudgeservice.judge.strategy.JudgeContext;
import com.wqa.qiojbackendmodel.model.codeSandBox.ExecuteCodeRequest;
import com.wqa.qiojbackendmodel.model.codeSandBox.ExecuteCodeResponse;
import com.wqa.qiojbackendmodel.model.codeSandBox.JudgeInfo;
import com.wqa.qiojbackendmodel.model.dto.question.JudgeCase;
import com.wqa.qiojbackendmodel.model.entity.Question;
import com.wqa.qiojbackendmodel.model.entity.QuestionSubmit;
import com.wqa.qiojbackendmodel.model.enums.JudgeInfoMessageEnum;
import com.wqa.qiojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.wqa.qiojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService {

    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private JudgeManager judgeManager;

    @Value("${codesandbox.type:example}")
    private String type;

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        // 1) 传入题目的提交id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交信息不存在");
        }
        Integer status = questionSubmit.getStatus();
        Long questionId = questionSubmit.getQuestionId();
        String code = questionSubmit.getCode();
        String language = questionSubmit.getLanguage();

        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        // 2) 如果不为等待状态
        if (!QuestionSubmitStatusEnum.WAITING.getValue().equals(status)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "题目正在判题中");
        }
        // 3) 更改判题状态为 判题中 ，防止重复执行
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean result = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "判题状态更新错误");
        }
        // 4) 调用沙箱，获取到执行结果
        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        codeSandBox = new CodeSandBoxProxy(codeSandBox);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandBox.executeCode(executeCodeRequest);
        // 5) 根据沙箱执行结果，设置题目的判题状态和信息
        List<String> outputList = executeCodeResponse.getOutputList();
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(outputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);

        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);

        // 6) 修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
        result = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);

        // 7) 修改数据库中的成功数
        if (JudgeInfoMessageEnum.ACCEPTED.getValue().equals(judgeInfo.getMessage())) {
            // 表示成功
            Question question1 = new Question();
            question1.setId(questionId);
            question1.setAcceptedNum(question.getAcceptedNum() + 1);
            result = questionFeignClient.updateQuestionById(question1);
        }
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "判题状态更新错误");
        }
        return questionFeignClient.getQuestionSubmitById(questionSubmitId);
    }
}
