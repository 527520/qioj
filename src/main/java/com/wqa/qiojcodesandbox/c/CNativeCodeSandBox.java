package com.wqa.qiojcodesandbox.c;

import cn.hutool.core.io.resource.ResourceUtil;
import com.wqa.qiojcodesandbox.model.ExecuteCodeRequest;
import com.wqa.qiojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CNativeCodeSandBox extends CCodeSandboxTemplate {

    public static void main(String[] args) {
        CNativeCodeSandBox cNativeCodeSandBox = new CNativeCodeSandBox();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setLanguage("c");
        executeCodeRequest.setInputList(Arrays.asList("1 2", "3 4"));
        String code = ResourceUtil.readStr("testCode/Main.c", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        ExecuteCodeResponse executeCodeResponse = cNativeCodeSandBox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        return super.executeCode(executeCodeRequest);
    }
}
