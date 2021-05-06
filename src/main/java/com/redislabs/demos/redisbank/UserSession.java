package com.redislabs.demos.redisbank;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash
public class UserSession {

    @Id
    private String userName;
    private String accountNumber;

    public UserSession(String userName, String accountNumber)   {
        this.userName = userName;
        this.accountNumber = accountNumber;
    }

    public String getUserName() {
        return userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getAccountNumber() {
        return accountNumber;
    }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

}
