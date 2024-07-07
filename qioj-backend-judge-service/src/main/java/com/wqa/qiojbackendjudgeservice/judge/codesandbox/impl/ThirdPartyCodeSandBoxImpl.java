package com.wqa.qiojbackendjudgeservice.judge.codesandbox.impl;

import com.wqa.qiojbackendjudgeservice.judge.codesandbox.CodeSandBox;
import com.wqa.qiojbackendmodel.model.codeSandBox.ExecuteCodeRequest;
import com.wqa.qiojbackendmodel.model.codeSandBox.ExecuteCodeResponse;

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
