package com.redislabs.demos.redisbank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BankTransactionGenerator implements InitializingBean {
    
    private static final int TRANSACTION_RATE_MS = 3000;
    private static final String TRANSACTION_KEY = "transaction";
    private static final String TRANSACTIONS_STREAM = "transactions";

    private final StringRedisTemplate redis;

    public BankTransactionGenerator(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Scheduled(fixedDelay = TRANSACTION_RATE_MS)
    public void generateNewTransaction()    {
        BankTransaction bankTransaction = generateBankTransaction();
        updateBankTransactions(bankTransaction);
    }

    private void updateBankTransactions(BankTransaction bankTransaction) {
        Map<String, BankTransaction> update = new HashMap<>();
        update.put(TRANSACTION_KEY, bankTransaction);
        redis.opsForStream().add(TRANSACTIONS_STREAM, update);
    }

    private BankTransaction generateBankTransaction()   {
        BankTransaction transaction = new BankTransaction();
        return transaction;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // Don't cross the streams! ;)
        redis.delete(TRANSACTIONS_STREAM);
        List<BankTransaction> transactions = CsvLoader.loadObjectList(BankTransaction.class, "transactions.csv");
        for (BankTransaction bankTransaction : transactions) {
            updateBankTransactions(bankTransaction);
        };

    }

}
