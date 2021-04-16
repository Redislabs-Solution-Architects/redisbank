package com.redislabs.demos.redisbank;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RedisHash
public class BankTransaction {
    
    @Id
    @JsonProperty("transaction_id")
    private Long id;
    @JsonProperty("from_account")
    private String fromAccount;
    @JsonProperty("from_account_name")
    private String fromAccountName;
    @JsonProperty("to_account")
    private String toAccount;
    @JsonProperty("to_account_name")
    private String toAccountName;
    @JsonProperty("amount")
    private Double amount;
    @JsonProperty("description")
    private String description;
    @JsonProperty("transaction_date")
    private Date transactionDate;

}
