package com.wqa.qiojcodesandbox.controller;

import cn.hutool.crypto.SecureUtil;
import com.wqa.qiojcodesandbox.CodeSandBox;
import com.wqa.qiojcodesandbox.c.CCodeSandboxTemplate;
import com.wqa.qiojcodesandbox.factory.CodeSandBoxFactory;
import com.wqa.qiojcodesandbox.factory.DockerCodeSandBoxFactory;
import com.wqa.qiojcodesandbox.factory.NativeCodeSandBoxFactory;
import com.wqa.qiojcodesandbox.java.JavaCodeSandBoxTemplate;
import com.wqa.qiojcodesandbox.java.JavaDockerCodeSandBox;
import com.wqa.qiojcodesandbox.java.JavaNativeCodeSandBox;
import com.wqa.qiojcodesandbox.model.ExecuteCodeRequest;
import com.wqa.qiojcodesandbox.model.ExecuteCodeResponse;
import com.wqa.qiojcodesandbox.model.JudgeInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("/")
public class MainController {

    // 定义鉴权请求头和密钥
    public static final String AUTH_REQUEST_HEADER = "auth";

    public static final String  AUTH_REQUEST_SECRET = "secretKey-wqa";

    @Value("${codeSandBox.type}")
    private String codeSandBoxType;

    @GetMapping("/health")
    public String healthCheck() {
        return "ok";
    }

    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest,
                                           HttpServletRequest request,
                                           HttpServletResponse response) {
        // 基本的认证
        String authHeader = request.getHeader(AUTH_REQUEST_HEADER);
        if (!SecureUtil.md5(AUTH_REQUEST_SECRET).equals(authHeader)) {
            response.setStatus(403);
            return null;
        }
        if (executeCodeRequest == null) {
            throw new RuntimeException("参数为空");
        }
        ExecuteCodeResponse executeCodeResponse;
        String language = executeCodeRequest.getLanguage();
        CodeSandBoxFactory codeSandBoxFactory;
        if ("docker".equals(codeSandBoxType)) {
            codeSandBoxFactory = new DockerCodeSandBoxFactory();
        } else if ("native".equals(codeSandBoxType)) {
            codeSandBoxFactory = new NativeCodeSandBoxFactory();
        } else {
            codeSandBoxFactory = new NativeCodeSandBoxFactory();
        }
        CodeSandBox codeSandBox;
        if ("java".equals(language)) {
            codeSandBox = codeSandBoxFactory.createJavaCodeSandBox();
        } else if ("c".equals(language)) {
            codeSandBox = codeSandBoxFactory.createCCodeSandBox();
        } else if ("cpp".equals(language)) {
            codeSandBox = codeSandBoxFactory.createCppCodeSandBox();
        }
        else {
            return new ExecuteCodeResponse(null, "暂无此语言的代码沙箱", 3, new JudgeInfo());
        }
        executeCodeResponse =  codeSandBox.executeCode(executeCodeRequest);
        return executeCodeResponse;
    }
}
