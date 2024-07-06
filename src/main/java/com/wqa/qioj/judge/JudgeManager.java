package com.wqa.qioj.judge;

import com.wqa.qioj.judge.strategy.DefaultJudgeStrategyImpl;
import com.wqa.qioj.judge.strategy.JavaLanguageJudgeStrategyImpl;
import com.wqa.qioj.judge.strategy.JudgeContext;
import com.wqa.qioj.judge.strategy.JudgeStrategy;
import com.wqa.qioj.judge.codesandbox.model.JudgeInfo;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化调用）
 */
@Service
public class JudgeManager {

    JudgeInfo doJudge(JudgeContext judgeContext) {
        String language = judgeContext.getQuestionSubmit().getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategyImpl();
        if ("java".equals(language)) {
            judgeStrategy = new JavaLanguageJudgeStrategyImpl();
        }
        return judgeStrategy.doJudge(judgeContext);
    }
}
