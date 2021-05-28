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
public class Trade {
    
    @Id
    private Long id;
    private Stock stock;
    private int amount;
    private String fromPortfolio;
    private String fromPortfolioName;
    private String toPortfolio;
    private String toPortfolioName;
    private String createdDate;
    private String settledDate;
    private String settledBy;
}
