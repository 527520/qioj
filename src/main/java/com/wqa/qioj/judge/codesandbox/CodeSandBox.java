package com.wqa.qioj.judge.codesandbox;

import com.wqa.qioj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wqa.qioj.judge.codesandbox.model.ExecuteCodeResponse;

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
