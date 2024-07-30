package com.wqa.qioj.judge.codesandbox.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.qcloud.cos.utils.Md5Utils;
import com.wqa.qioj.common.ErrorCode;
import com.wqa.qioj.exception.BusinessException;
import com.wqa.qioj.judge.codesandbox.CodeSandBox;
import com.wqa.qioj.judge.codesandbox.model.ExecuteCodeRequest;
import com.wqa.qioj.judge.codesandbox.model.ExecuteCodeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * 远程代码沙箱（实际调用接口沙箱）
 */
public class RemoteCodeSandBoxImpl implements CodeSandBox {

    // 定义鉴权请求头和密钥
    public static final String AUTH_REQUEST_HEADER = "auth";

    public static final String  AUTH_REQUEST_SECRET = "secretKey-wqa";

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");
        String url = "http://192.168.241.129:8081/executeCode";
        String respStr = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER, SecureUtil.md5(AUTH_REQUEST_SECRET))
                .body(JSONUtil.toJsonStr(executeCodeRequest))
                .execute()
                .body();
        if (StrUtil.isBlank(respStr)) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "ExecuteCode remoteSandBox error message=" + respStr);
        }
        return JSONUtil.toBean(respStr, ExecuteCodeResponse.class);
    }
}
