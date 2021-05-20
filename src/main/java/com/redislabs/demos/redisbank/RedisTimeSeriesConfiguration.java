package com.redislabs.demos.redisbank;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.lettuce.core.RedisClient;
import io.lettuce.core.dynamic.RedisCommandFactory;

@Configuration
public class RedisTimeSeriesConfiguration {
    
    @Bean
    RedisCommandFactory redisCommandFactory(RedisClient redis)   {
        return new RedisCommandFactory(redis.connect());
    }
    
}
