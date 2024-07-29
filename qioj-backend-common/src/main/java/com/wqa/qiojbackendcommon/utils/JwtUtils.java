package com.wqa.qiojbackendcommon.utils;

import cn.hutool.crypto.asymmetric.RSA;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

/**
 * 生成和解析JWT的工具
 */
public class JwtUtils {

    // 签名秘钥
    public static final String SECRET = "6fUFp5!2GJXgO3IU4IU@1az6U9#M5h2GE3K$LqzB9Y%chGyqPV_wuQiAn" +
                                        "Popcorn}(K9XLrQMNLd(}>.lq75hKM9m9gS_eAIkZmy>^JAyArsV5WCQ";

    /**
     * 生成token
     *
     * @param payload
     * @return
     */
    public static String getToken(Map<String, Object> payload) {
        // 指定token过期时间为7天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 7);
        JWTCreator.Builder builder = JWT.create();
        // 构建 payload
        payload.forEach((key, value) -> builder.withClaim(key, value.toString()));
        // 指定过期时间和签名算法
        return builder.withExpiresAt(calendar.getTime()).sign(Algorithm.HMAC256(SECRET));
    }

    /**
     * 解析 token
     *
     * @param token
     * @return
     */
    public static DecodedJWT decoded(String token) {
        JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
        return jwtVerifier.verify(token);
    }

    /**
     * 验证JWT的有效性
     *
     * @param token
     * @return
     */
    public static boolean validateToken(String token) {
        try {
            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            DecodedJWT decodedJWT = jwtVerifier.verify(token);
            // 检查是否过期
            Date expiresAt = decodedJWT.getExpiresAt();
            return expiresAt != null && expiresAt.after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
