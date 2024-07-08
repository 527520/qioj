package com.wqa.qiojbackenduserservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.wqa.qiojbackenduserservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.wqa")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.wqa.qiojbackendserviceclient.service"})
public class QiojBackendUserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(QiojBackendUserServiceApplication.class, args);
	}

}
