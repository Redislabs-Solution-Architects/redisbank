package com.redislabs.demos.redisbank;

import com.redis.lettucemod.spring.RedisModulesAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(exclude = { RedisModulesAutoConfiguration.class })
public class RedisbankApplication {
	public static void main(String[] args) {
		SpringApplication.run(RedisbankApplication.class, args);
	}
}
