package com.wqa.qioj.judge.strategy;

import com.wqa.qioj.model.dto.question.JudgeCase;
import com.wqa.qioj.judge.codesandbox.model.JudgeInfo;
import com.wqa.qioj.model.entity.Question;
import com.wqa.qioj.model.entity.QuestionSubmit;
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
