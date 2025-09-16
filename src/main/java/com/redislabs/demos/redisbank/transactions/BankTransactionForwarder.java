package com.redislabs.demos.redisbank.transactions;

import java.time.Duration;
import java.util.Objects;

import io.lettuce.core.json.DefaultJsonParser;
import io.lettuce.core.json.JsonParser;
import io.lettuce.core.json.JsonPath;

import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RedisModulesCommands;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.redislabs.demos.redisbank.Config;
import com.redislabs.demos.redisbank.SerializationUtil;

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


    private final RedisModulesCommands<String, String> json;


    private final BankTransactionRepository btr;
    private StreamMessageListenerContainer<String, MapRecord<String, String, String>> container;
    private Subscription subscription;

    public BankTransactionForwarder(
            Config config,
            StringRedisTemplate redis,
            SimpMessageSendingOperations smso,
            BankTransactionRepository btr,
            StatefulRedisModulesConnection<String, String> json) {
        this.config = config;
        this.redis = redis;
        this.smso = smso;
        this.btr = btr;
        this.json = json.sync();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        var connectionFactory = Objects.requireNonNull(
                redis.getConnectionFactory(),
                "RedisConnectionFactory must not be null"
        );

        this.container = StreamMessageListenerContainer.create(
                connectionFactory,
                StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofMillis(1000))
                        .build()
        );
        container.start();
        this.subscription = container.receive(StreamOffset.latest(TRANSACTIONS_STREAM), this);
        subscription.await(Duration.ofSeconds(10));
    }

    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        LOGGER.info("Message received from stream: {}", message);
        // Update search index whenever a new transaction arrives via the stream
        String messageString = message.getValue().get("transaction");
        try {
            BankTransaction bankTransaction = SerializationUtil.deserializeObject(messageString, BankTransaction.class);
            btr.save(bankTransaction);

            JsonParser parser = new DefaultJsonParser();
            json.jsonSet(
                    "jsontx:" + bankTransaction.getId(),
                    JsonPath.ROOT_PATH,
                    parser.createJsonValue(messageString)
            );

        } catch (JsonProcessingException e) {
            LOGGER.error("Error parsing JSON: {}", e.getMessage());
        }

        // Stream message to websocket connection topic
        smso.convertAndSend(config.getStomp().getTransactionsTopic(), message.getValue());
        LOGGER.info("Websocket message: {}", messageString);

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
