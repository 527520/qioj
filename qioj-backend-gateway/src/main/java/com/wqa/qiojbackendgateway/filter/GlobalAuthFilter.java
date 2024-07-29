package com.wqa.qiojbackendgateway.filter;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wqa.qiojbackendcommon.common.ErrorCode;
import com.wqa.qiojbackendcommon.exception.BusinessException;
import com.wqa.qiojbackendcommon.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class GlobalAuthFilter implements GlobalFilter, Ordered {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("#{'${gateway.excludedUrls}'.split(',')}")
    private List<String> excludedUrls; //配置不需要校验的链接

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        String path = serverHttpRequest.getURI().getPath();
        // 判断路径中是否包含inner（只允许内部调用）
        ServerHttpResponse response = exchange.getResponse();
        if (antPathMatcher.match("/**/inner/**", path)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);
            DataBufferFactory dataBufferFactory = response.bufferFactory();
            DataBuffer dataBuffer = dataBufferFactory.wrap("无权限".getBytes(StandardCharsets.UTF_8));
            return response.writeWith(Mono.just(dataBuffer));
        }
        //1.排除不需要权限校验的连接
        //如果 当前链接不需要校验则直接放行
        if (excludedUrls.contains(path)) {
            return chain.filter(exchange);
        }
        String token = exchange.getRequest().getHeaders().getFirst("token");
        if (token == null) {
            return getNotLoginVoidMono(response);
        }
        try {
            boolean result = JwtUtils.validateToken(token);
            if (!result) {
                return getNotLoginVoidMono(response);
            }
        } catch (SignatureVerificationException e) {
            log.error("无效签名：" + e.getMessage());
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无效签名");
        } catch (TokenExpiredException e) {
            log.error("token已经过期：" + e.getMessage());
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "token已经过期");
        } catch (AlgorithmMismatchException e) {
            log.error("算法不一致：" + e.getMessage());
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "算法不一致");
        } catch (Exception e) {
            log.info("：" + e);
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "token无效");
        }
        return chain.filter(exchange);
    }

    @NotNull
    private Mono<Void> getNotLoginVoidMono(ServerHttpResponse response) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("errCode", 401);
        responseData.put("errMessage", "用户未登录");
        return responseError(response, responseData);
    }

    //响应错误数据 这个方法就是将这个map转化为JSON
    private Mono<Void> responseError(ServerHttpResponse response, Map<String, Object> responseData) {
        // 将信息转换为 JSON
        ObjectMapper objectMapper = new ObjectMapper();
        byte[] data = new byte[0];
        try {
            data = objectMapper.writeValueAsBytes(responseData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        // 输出错误信息到页面
        DataBuffer buffer = response.bufferFactory().wrap(data);
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 优先级提到最高
     *
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
