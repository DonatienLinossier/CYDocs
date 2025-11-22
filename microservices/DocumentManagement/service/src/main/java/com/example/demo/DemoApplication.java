package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class DemoApplication {

	public static void main(String[] args) {
		System.out.println("testpls");
		SpringApplication.run(DemoApplication.class, args);
	}

}
