package com.redislabs.demos.redisbank;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.redislabs.lettusearch.Field;
import com.redislabs.lettusearch.RediSearchCommands;
import com.redislabs.lettusearch.StatefulRediSearchConnection;
import com.redislabs.lettusearch.Field.Text.PhoneticMatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import io.lettuce.core.RedisCommandExecutionException;

@Component
public class BankTransactionGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankTransactionGenerator.class);
    private static Double balance = 100000.00;
    private static final int TRANSACTION_RATE_MS = 10000;
    private static final String TRANSACTION_KEY = "transaction";
    private static final String TRANSACTIONS_STREAM = "transactions";
    private static final String ACCOUNT_INDEX = "transaction_account_idx";
    private static final String SEARCH_INDEX = "transaction_description_idx";
    private final List<TransactionSource> transactionSources;
    private final SecureRandom random;
    private final DateFormat df = new SimpleDateFormat("yyyy.MM.dd 'at' HH:mm:ss");
    private final NumberFormat nf = NumberFormat.getCurrencyInstance();

    private final StringRedisTemplate redis;
    private final StatefulRediSearchConnection<String, String> connection;

    public BankTransactionGenerator(StringRedisTemplate redis, StatefulRediSearchConnection<String, String> connection)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        this.redis = redis;
        this.connection = connection;
        transactionSources = SerializationUtil.loadObjectList(TransactionSource.class, "transaction_sources.csv");
        random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed("lars".getBytes("UTF-8")); // Prime the RNG so it always generates the same pseudorandom set

        createSearchIndices();
        createInitialStream();

    }

    private void createSearchIndices() {
        RediSearchCommands<String, String> commands = connection.sync();
        try {
            commands.dropIndex(ACCOUNT_INDEX);
            commands.dropIndex(SEARCH_INDEX);
        } catch (RedisCommandExecutionException e) {
            if (!e.getMessage().equals("Unknown Index name")) {
                LOGGER.error("Error dropping index: {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }
        LOGGER.info("Creating {} index", ACCOUNT_INDEX);
        commands.create(ACCOUNT_INDEX, Field.text("toAccountName").build());

        LOGGER.info("Creating {} index", SEARCH_INDEX);
        commands.create(SEARCH_INDEX, Field.text("description").matcher(PhoneticMatcher.English).build(),
                Field.text("fromAccountName").matcher(PhoneticMatcher.English).build(),
                Field.text("transactionType").matcher(PhoneticMatcher.English).build());
    }

    private void createInitialStream() {
        redis.delete(TRANSACTIONS_STREAM);
        for (int i = 0; i < 10; i++) {
            BankTransaction bankTransaction = createBankTransaction();
            streamBankTransaction(bankTransaction);
        }
        ;
    }

    @Scheduled(fixedDelay = TRANSACTION_RATE_MS)
    public void generateNewTransaction() {
        BankTransaction bankTransaction = createBankTransaction();
        streamBankTransaction(bankTransaction);
    }

    private void streamBankTransaction(BankTransaction bankTransaction) {
        Map<String, String> update = new HashMap<>();
        String transactionString;
        try {
            transactionString = SerializationUtil.serializeObject(bankTransaction);
            LOGGER.info("Update: {}", transactionString);
            update.put(TRANSACTION_KEY, transactionString);
            redis.opsForStream().add(TRANSACTIONS_STREAM, update);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error serialising object to JSON", e.getMessage());
        }
    }

    private BankTransaction createBankTransaction() {
        BankTransaction transaction = new BankTransaction();
        transaction.setId(random.nextLong());
        transaction.setToAccountName("lars");
        transaction.setToAccount(FakeIbanUtil.generateFakeIbanFrom("lars"));
        TransactionSource ts = transactionSources.get(random.nextInt(transactionSources.size()));
        transaction.setFromAccountName(ts.getFromAccountName());
        transaction.setFromAccount(FakeIbanUtil.generateFakeIbanFrom(ts.getFromAccountName()));
        transaction.setDescription(ts.getDescription());
        transaction.setTransactionType(ts.getType());
        transaction.setAmount(createTransactionAmount());
        transaction.setTransactionDate(df.format(new Date()));
        transaction.setBalanceAfter(nf.format(balance));
        return transaction;
    }

    private String createTransactionAmount() {
        Double bandwidth = (1 + random.nextInt(3)) * 100.00;
        Double amount = random.nextDouble() * bandwidth % 300.0;
        Double roundedAmount = Math.floor(amount * 100) / 100;
        balance = balance - roundedAmount;
        return nf.format(roundedAmount);
    }

}
