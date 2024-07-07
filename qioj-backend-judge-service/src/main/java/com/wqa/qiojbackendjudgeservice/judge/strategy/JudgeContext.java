package com.wqa.qiojbackendjudgeservice.judge.strategy;

import com.wqa.qiojbackendmodel.model.codeSandBox.JudgeInfo;
import com.wqa.qiojbackendmodel.model.dto.question.JudgeCase;
import com.wqa.qiojbackendmodel.model.entity.Question;
import com.wqa.qiojbackendmodel.model.entity.QuestionSubmit;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 上下文（用于定义在策略中传递的参数）
 */
@Data
public class JudgeContext implements Serializable {

    private JudgeInfo judgeInfo;

    private List<String> inputList;

    private List<String> outputList;

    private List<JudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmit questionSubmit;
}
