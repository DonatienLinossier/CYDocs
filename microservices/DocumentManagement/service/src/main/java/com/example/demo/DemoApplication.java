package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import main.java.com.cyFramework.core.Logger;

@SpringBootApplication
@EnableDiscoveryClient
public class DemoApplication {
	private static final Logger logger = new Logger("DemoApplication");

	public static void main(String[] args) {
		logger.info("azertyiop");
		SpringApplication.run(DemoApplication.class, args);
		logger.info("azertyiop1");
	}

}
