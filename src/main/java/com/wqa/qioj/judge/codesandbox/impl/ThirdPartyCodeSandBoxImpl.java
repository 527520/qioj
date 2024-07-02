package com.wqa.qioj.judge.codesandbox.impl;

import com.wqa.qioj.judge.codesandbox.CodeSandBox;
import com.wqa.qioj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wqa.qioj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 第三方代码沙箱
 */
public class ThirdPartyCodeSandBoxImpl implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("第三方代码沙箱");
        return null;
    }
}
