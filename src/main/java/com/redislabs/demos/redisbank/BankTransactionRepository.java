package com.redislabs.demos.redisbank;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankTransactionRepository extends CrudRepository<BankTransaction, String>{
    
    public List<BankTransaction> findByToAccountAndFromAccountOrderByTransactionDateDesc(String toAccount, String fromAccount);

}