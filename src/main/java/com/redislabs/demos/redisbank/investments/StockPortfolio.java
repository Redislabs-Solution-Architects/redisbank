package com.redislabs.demos.redisbank.investments;

import java.util.HashMap;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash
public class StockPortfolio {
    
    @Id
    private Long id;
    private HashMap<Stock, Double> stocks = new HashMap<>();
    
}
