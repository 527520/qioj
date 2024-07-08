package com.wqa.qiojbackendgateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class QiojBackendGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(QiojBackendGatewayApplication.class, args);
	}

}
