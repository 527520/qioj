package com.wqa.qiojbackendjudgeservice.judge.codesandbox;

import com.wqa.qiojbackendmodel.model.codeSandBox.ExecuteCodeRequest;
import com.wqa.qiojbackendmodel.model.codeSandBox.ExecuteCodeResponse;

/**
 * 代码沙箱接口定义
 */
public interface CodeSandBox {

    /**
     * 执行代码
     * @param executeCodeRequest
     * @return
     */
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
