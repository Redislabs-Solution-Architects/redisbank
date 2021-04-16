package com.redislabs.demos.redisbank;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class BankTransactionService {
    
    private final BankTransactionRepository repo;

    public BankTransactionService(BankTransactionRepository repo)   {
        this.repo = repo;
    }

    public List<BankTransaction> findTransactionsForAccount(String accountNumber)    {
        return repo.findByToAccountAndFromAccountOrderByTransactionDateDesc(accountNumber, accountNumber);
    }

}
