package com.wqa.qiojcodesandbox.cpp;

import cn.hutool.core.io.resource.ResourceUtil;
import com.wqa.qiojcodesandbox.model.ExecuteCodeRequest;
import com.wqa.qiojcodesandbox.model.ExecuteCodeResponse;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CppDockerCodeSandBox extends CppCodeSandboxTemplate {

    public static Boolean firstInit = true;
    private static final long TIME_OUT = 5000L;

    public static void main(String[] args) {
        CppDockerCodeSandBox cDockerCodeSandBox = new CppDockerCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("c");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.cpp", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        ExecuteCodeResponse executeCodeResponse = cDockerCodeSandBox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
