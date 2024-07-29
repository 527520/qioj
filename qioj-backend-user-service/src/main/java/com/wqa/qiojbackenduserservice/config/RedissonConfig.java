package com.wqa.qiojbackenduserservice.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类
 */
@Configuration
@Data
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonConfig {
    private String host;
    private String port;
    private Integer database;

    /**
     * 配置RedissonClient实例
     */
    @Bean
    public RedissonClient redissonClient() {
        // 1.创建配置
        Config config = new Config();
        // 构建Redis地址
        String redisAddress = String.format("redis://%s:%s", host, port);
        // 使用单节点模式配置
        config.useSingleServer().setAddress(redisAddress).setDatabase(database);
        // 设置Jackson为序列化机制
        config.setCodec(new JsonJacksonCodec());
        // 2.创建一个RedissonClient实例
        // 同步和异步API
        return Redisson.create(config);
    }
}
