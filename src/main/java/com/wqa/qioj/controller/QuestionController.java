package com.wqa.qioj.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.wqa.qioj.annotation.AuthCheck;
import com.wqa.qioj.common.BaseResponse;
import com.wqa.qioj.common.DeleteRequest;
import com.wqa.qioj.common.ErrorCode;
import com.wqa.qioj.common.ResultUtils;
import com.wqa.qioj.constant.UserConstant;
import com.wqa.qioj.exception.BusinessException;
import com.wqa.qioj.exception.ThrowUtils;
import com.wqa.qioj.model.dto.question.*;
import com.wqa.qioj.model.dto.questionsubmit.QuestionSubmitAddRequest;
import com.wqa.qioj.model.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.wqa.qioj.model.entity.Question;
import com.wqa.qioj.model.entity.QuestionSubmit;
import com.wqa.qioj.model.entity.User;
import com.wqa.qioj.model.enums.QuestionStatusEnum;
import com.wqa.qioj.model.enums.QuestionSubmitLanguageEnum;
import com.wqa.qioj.model.enums.UserRoleEnum;
import com.wqa.qioj.model.vo.QuestionSubmitVO;
import com.wqa.qioj.model.vo.QuestionVO;
import com.wqa.qioj.service.QuestionService;
import com.wqa.qioj.service.QuestionSubmitService;
import com.wqa.qioj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 题目接口
 */
@RestController
@RequestMapping("/question")
@Slf4j
public class QuestionController {

    @Resource
    private QuestionService questionService;

    @Resource
    private UserService userService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    private final static Gson GSON = new Gson();

    // region 增删改查

    /**
     * 创建
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionAddRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        questionService.validQuestion(question, true);
        User loginUser = userService.getLoginUser(request);
        question.setUserId(loginUser.getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        // 如果状态不是 1 则表示要创建题目
        String language = questionAddRequest.getLanguage();
        if (!QuestionStatusEnum.DRAFT.getValue().equals(question.getStatus())) {
            // 编程语言一定要有
            if (QuestionSubmitLanguageEnum.getEnumByValue(language) == null) {
                // 没有该编程语言或为空
                question.setStatus(QuestionStatusEnum.DRAFT.getValue());
            } else {
                question.setStatus(QuestionStatusEnum.VALIDATING_ANSWERS.getValue());
            }
        }
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        if (QuestionStatusEnum.DRAFT.getValue().equals(question.getStatus())) {
            return ResultUtils.success(newQuestionId);
        }
        // 验证答案是否正确
        CompletableFuture.runAsync(() -> {
            boolean verifyResult = questionService.verifyAnswer(newQuestionId, language);
            Question updateQuestion = new Question();
            updateQuestion.setId(newQuestionId);
            if (verifyResult) { // 正确
                updateQuestion.setStatus(QuestionStatusEnum.CREATED.getValue());
            } else { // 错误
                updateQuestion.setStatus(QuestionStatusEnum.CREATION_FAILED.getValue());
            }
            boolean updated = questionService.updateQuestionById(updateQuestion);
            ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
        });
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldQuestion.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = questionService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（管理员或本人）
     *
     * @param questionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest, HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        if (user == null || questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionUpdateRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        // 判断是否存在
        long id = questionUpdateRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        if (!UserRoleEnum.ADMIN.getValue().equals(user.getUserRole()) && !oldQuestion.getUserId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限");
        }
        if ((QuestionStatusEnum.DRAFT.getValue().equals(oldQuestion.getStatus())
                || QuestionStatusEnum.CREATION_FAILED.getValue().equals(oldQuestion.getStatus()))
                && "99".equals(question.getStatus())) {
            // 原本是草稿，这次要创建，执行判题
            question.setStatus(QuestionStatusEnum.VALIDATING_ANSWERS.getValue());
            String language = questionUpdateRequest.getLanguage();
            if (QuestionSubmitLanguageEnum.getEnumByValue(language) == null) {
                // 没有该编程语言或为空
                question.setStatus(QuestionStatusEnum.DRAFT.getValue());
            } else {
                // 验证答案是否正确
                CompletableFuture.runAsync(() -> {
                    boolean verifyResult = questionService.verifyAnswer(id, language);
                    Question updateQuestion = new Question();
                    updateQuestion.setId(id);
                    if (verifyResult) { // 正确
                        updateQuestion.setStatus(QuestionStatusEnum.CREATED.getValue());
                    } else { // 错误
                        updateQuestion.setStatus(QuestionStatusEnum.CREATION_FAILED.getValue());
                    }
                    boolean updated = questionService.updateQuestionById(updateQuestion);
                    ThrowUtils.throwIf(!updated, ErrorCode.OPERATION_ERROR);
                });
            }
        } else {
            question.setStatus(oldQuestion.getStatus());
        }
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取（封装类：脱敏）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Question> getQuestionById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        // 不是本人或管理员,不能直接获取所有信息
        if (!question.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return ResultUtils.success(question);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);
        queryWrapper.eq("status", "3");
        Page<Question> questionPage = questionService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        questionQueryRequest.setUserId(loginUser.getId());
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取题目列表（仅管理员）
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                           HttpServletRequest request) {
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        QueryWrapper<Question> queryWrapper = questionService.getQueryWrapper(questionQueryRequest);
        queryWrapper.eq("status", "3");
        Page<Question> questionPage = questionService.page(new Page<>(current, size), queryWrapper);
        return ResultUtils.success(questionPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            question.setTags(GSON.toJson(tags));
        }
        List<JudgeCase> judgeCase = questionEditRequest.getJudgeCase();
        if (judgeCase != null) {
            question.setJudgeCase(GSON.toJson(judgeCase));
        }
        JudgeConfig judgeConfig = questionEditRequest.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        User loginUser = userService.getLoginUser(request);
        long id = questionEditRequest.getId();
        // 判断是否存在
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldQuestion.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return resultNum 本次点赞变化数
     */
    @PostMapping("/question_submit/do")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能点赞
        final User loginUser = userService.getLoginUser(request);
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(questionSubmitId);
    }

    /**
     * 分页获取题目提交列表（仅管理员和本人能查看所有信息，其他用户看不到题目答案、提交代码）
     *
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/question_submit/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 从数据库查询原始的题目提交分页信息
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        // 返回脱敏信息
        final User loginUser = userService.getLoginUser(request);

        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser));
    }

}
