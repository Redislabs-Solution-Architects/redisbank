package com.redislabs.demos.redisbank;

import com.redislabs.redistimeseries.RedisTimeSeries;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisTimeSeriesConfiguration {
    
    @Bean
    RedisTimeSeries redisTimeSeries(@Value("${spring.redis.host}") String host, @Value("${spring.redis.port}") int port)  {
        RedisTimeSeries rts = new RedisTimeSeries(host, port);
        return rts;
    }

}
