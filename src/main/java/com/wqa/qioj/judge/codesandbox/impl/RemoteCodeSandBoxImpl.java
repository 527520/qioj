package com.wqa.qioj.judge.codesandbox.impl;

import com.wqa.qioj.judge.codesandbox.CodeSandBox;
import com.wqa.qioj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wqa.qioj.judge.codesandbox.model.ExecuteCodeResponse;

/**
 * 远程代码沙箱（实际调用接口沙箱）
 */
public class RemoteCodeSandBoxImpl implements CodeSandBox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");
        return null;
    }
}
