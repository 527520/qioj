package com.wqa.qioj.judge.strategy;

import com.wqa.qioj.model.dto.questionsubmit.JudgeInfo;

/**
 * 判题策略（策略模式）
 */
public interface JudgeStrategy {

    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
