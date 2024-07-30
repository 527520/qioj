package com.wqa.qiojbackendjudgeservice.judge;

import com.wqa.qiojbackendmodel.model.entity.Question;
import com.wqa.qiojbackendmodel.model.entity.QuestionSubmit;

/**
 * 判题服务
 */
public interface JudgeService {

    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    QuestionSubmit doJudge(long questionSubmitId);

    Question doJudge(long questionId, String language);
}
