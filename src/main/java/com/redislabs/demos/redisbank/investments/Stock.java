package com.redislabs.demos.redisbank.investments;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash
public class Stock {
    
    @Id
    private long id;
    private String name;
    private String price;
}
