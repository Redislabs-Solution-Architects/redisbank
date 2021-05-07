package com.redislabs.demos.redisbank;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash
public class BankTransaction {
    
    @Id
    private Long id;
    private String fromAccount;
    private String fromAccountName;
    @Indexed
    private String toAccount;
    @Indexed
    private String toAccountName;
    private Double amount;
    private String description;
    private Date transactionDate;
    private String transactionType;

}
