package com.redislabs.demos.redisbank;

import java.time.Duration;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions;
import org.springframework.data.redis.stream.Subscription;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
public class BankTransactionForwarder
        implements InitializingBean, DisposableBean, StreamListener<String, MapRecord<String, String, String>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BankTransactionForwarder.class);
    private static final String TRANSACTIONS_STREAM = "transactions";

    private final Config config;
    private final StringRedisTemplate redis;
    private final SimpMessageSendingOperations smso;
    private final BankTransactionRepository btr;
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private Subscription subscription;

    public BankTransactionForwarder(Config config, StringRedisTemplate redis, SimpMessageSendingOperations smso,
            BankTransactionRepository btr) {
        this.config = config;
        this.redis = redis;
        this.smso = smso;
        this.btr = btr;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.container = StreamMessageListenerContainer.create(redis.getConnectionFactory(),
                StreamMessageListenerContainerOptions.builder().pollTimeout(Duration.ofMillis(100)).build());
        container.start();
        this.subscription = container.receive(StreamOffset.latest(TRANSACTIONS_STREAM), this);
        subscription.await(Duration.ofSeconds(2));
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        // Update dataset
        String messageString = message.getValue().get("transaction");
        try {
            BankTransaction bankTransaction = CsvUtil.deserializeObject(messageString, BankTransaction.class);
            btr.save(bankTransaction);
            LOGGER.info("Saved transaction: {}", messageString);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error parsing JSON: {}", e.getMessage());
        }

        // Stream message to websocket connection topic
        smso.convertAndSend(config.getStomp().getTransactionsTopic(), message.getValue());
    }

    @Override
    public void destroy() throws Exception {
        if (subscription != null) {
            subscription.cancel();
        }
        if (container != null) {
            container.stop();
        }
    }

}
