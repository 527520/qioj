package com.wqa.qioj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.wqa.qioj.model.dto.question.JudgeCase;
import com.wqa.qioj.model.dto.question.JudgeConfig;
import com.wqa.qioj.model.dto.questionsubmit.JudgeInfo;
import com.wqa.qioj.model.entity.Question;
import com.wqa.qioj.model.enums.JudgeInfoMessageEnum;

import java.util.List;

/**
 * JAVA程序判题策略
 */
public class JavaLanguageJudgeStrategyImpl implements JudgeStrategy {
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {

        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        Long memory = judgeInfo.getMemory();
        Long time = judgeInfo.getTime();
        JudgeInfo judgeInfoResp = new JudgeInfo();
        judgeInfoResp.setMemory(memory);
        judgeInfoResp.setTime(time);
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();

        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;

        // 判断输出数量与预期数量是否相等
        if (outputList.size() != inputList.size()) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResp.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResp;
        }
        // 判断每一项输出和预期输出是否相等
        for (int i = 0; i < judgeCaseList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            if (!judgeCase.getOutput().equals(outputList.get(i))) {
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResp.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResp;
            }
        }
        // 判断题目限制

        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfig = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        Long needMemoryLimit = judgeConfig.getMemoryLimit();
        Long needTimeLimit = judgeConfig.getTimeLimit();
        if (memory > needMemoryLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResp.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResp;
        }
        // 假设JAVA程序要额外执行一些时间（1s）
        long JAVA_PROGRAM_TIME_COST = 1000L;
        if ((time - JAVA_PROGRAM_TIME_COST) > needTimeLimit) {
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResp.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResp;
        }

        judgeInfoResp.setMessage(judgeInfoMessageEnum.getValue());
        return judgeInfoResp;
    }
}
