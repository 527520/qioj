package com.wqa.qiojbackendjudgeservice;

import com.wqa.qiojbackendjudgeservice.rabbitmq.InitRabbitmq;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.wqa")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.wqa.qiojbackendserviceclient.service"})
public class QiojBackendJudgeServiceApplication {

	public static void main(String[] args) {
		InitRabbitmq.doInit();
		SpringApplication.run(QiojBackendJudgeServiceApplication.class, args);
	}

}
