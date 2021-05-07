package com.redislabs.demos.redisbank;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BankTransactionGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankTransactionGenerator.class);
    private static final int TRANSACTION_RATE_MS = 10000;
    private static final String TRANSACTION_KEY = "transaction";
    private static final String TRANSACTIONS_STREAM = "transactions";
    private final List<TransactionSource> transactionSources;
    private final SecureRandom random;

    private final StringRedisTemplate redis;

    public BankTransactionGenerator(StringRedisTemplate redis)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        this.redis = redis;
        transactionSources = CsvUtil.loadObjectList(TransactionSource.class, "transaction_sources.csv");
        random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed("lars".getBytes("UTF-8"));

        // Don't cross the streams! ;)
        redis.delete(TRANSACTIONS_STREAM);
        List<BankTransaction> transactions = CsvUtil.loadObjectList(BankTransaction.class, "transactions.csv");
        for (BankTransaction bankTransaction : transactions) {
            bankTransaction.setToAccountName("lars");
            bankTransaction.setToAccount(FakeIbanUtil.generateFakeIbanFrom("lars"));
            bankTransaction.setFromAccount(FakeIbanUtil.generateFakeIbanFrom(bankTransaction.getFromAccountName()));
            updateBankTransactions(bankTransaction);
        }
        ;

    }

    @Scheduled(fixedDelay = TRANSACTION_RATE_MS)
    public void generateNewTransaction() {
        BankTransaction bankTransaction = generateBankTransaction();
        updateBankTransactions(bankTransaction);
    }

    private void updateBankTransactions(BankTransaction bankTransaction) {
        Map<String, String> update = new HashMap<>();
        String transactionString;
        try {
            transactionString = CsvUtil.serializeObject(bankTransaction);
            LOGGER.info("Update: {}", transactionString);
            update.put(TRANSACTION_KEY, transactionString);
            redis.opsForStream().add(TRANSACTIONS_STREAM, update);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error serialising object to JSON", e.getMessage());
        }
    }

    private BankTransaction generateBankTransaction() {
        BankTransaction transaction = new BankTransaction();
        transaction.setId(random.nextLong());
        transaction.setToAccountName("lars");
        transaction.setToAccount(FakeIbanUtil.generateFakeIbanFrom("lars"));
        TransactionSource ts = transactionSources.get(random.nextInt(transactionSources.size()));
        transaction.setFromAccountName(ts.getFromAccountName());
        transaction.setFromAccount(FakeIbanUtil.generateFakeIbanFrom(ts.getFromAccountName()));
        transaction.setDescription(ts.getDescription());
        transaction.setTransactionType(ts.getType());
        Double bandwidth = (1 + random.nextInt(3)) * 100.00;
        Double amount = random.nextDouble() * bandwidth % 300.0;
        Double roundedAmount = Math.floor(amount * 100) / 100;
        transaction.setAmount(roundedAmount);
        transaction.setTransactionDate(new Date());
        return transaction;
    }

}
