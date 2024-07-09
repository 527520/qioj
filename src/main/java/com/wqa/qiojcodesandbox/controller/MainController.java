package com.wqa.qiojcodesandbox.controller;

import cn.hutool.crypto.SecureUtil;
import com.wqa.qiojcodesandbox.JavaDockerCodeSandBox;
import com.wqa.qiojcodesandbox.JavaNativeCodeSandBox;
import com.wqa.qiojcodesandbox.model.ExecuteCodeRequest;
import com.wqa.qiojcodesandbox.model.ExecuteCodeResponse;
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

    @Resource
    private JavaDockerCodeSandBox javaDockerCodeSandBox;

    @Resource
    private JavaNativeCodeSandBox javaNativeCodeSandBox;

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
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        if ("docker".equals(codeSandBoxType)) {
            executeCodeResponse =  javaDockerCodeSandBox.executeCode(executeCodeRequest);
        } else if ("native".equals(codeSandBoxType)) {
            executeCodeResponse =  javaNativeCodeSandBox.executeCode(executeCodeRequest);
        }
        return executeCodeResponse;
    }
}
