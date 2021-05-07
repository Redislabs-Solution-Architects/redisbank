package com.redislabs.demos.redisbank;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankTransactionRepository extends CrudRepository<BankTransaction, String>{
    
    public List<BankTransaction> findByToAccount(String toAccount);
    public List<BankTransaction> findByToAccountName(String toAccountName);
    public List<BankTransaction> findByFromAccount(String fromAccount);
    public List<BankTransaction> findByFromAccountName(String fromAccountName);

}