package com.redislabs.demos.redisbank.transactions;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankTransactionRepository extends CrudRepository<BankTransaction, String>{
    
}