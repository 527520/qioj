package com.wqa.qiojbackendjudgeservice.judge;

import com.wqa.qiojbackendjudgeservice.judge.strategy.DefaultJudgeStrategyImpl;
import com.wqa.qiojbackendjudgeservice.judge.strategy.JavaLanguageJudgeStrategyImpl;
import com.wqa.qiojbackendjudgeservice.judge.strategy.JudgeContext;
import com.wqa.qiojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.wqa.qiojbackendmodel.model.codeSandBox.JudgeInfo;
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
