package com.redislabs.demos.redisbank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RedisbankApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisbankApplication.class, args);
	}


}
