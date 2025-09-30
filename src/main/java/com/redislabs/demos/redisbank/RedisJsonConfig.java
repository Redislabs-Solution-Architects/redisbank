package com.redislabs.demos.redisbank;

import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.sync.RedisModulesCommands;
import io.lettuce.core.RedisURI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

@Configuration
public class RedisJsonConfig {

    @Bean(destroyMethod = "shutdown")
    public RedisModulesClient redisModulesClient(RedisConnectionFactory cf) {
        LettuceConnectionFactory lcf = (LettuceConnectionFactory) cf;
        RedisURI.Builder b = RedisURI.Builder.redis(lcf.getHostName(), lcf.getPort())
                .withDatabase(lcf.getDatabase());
        if (lcf.getPassword() != null && !lcf.getPassword().isEmpty()) {
            b.withPassword(lcf.getPassword().toCharArray());
        }
        return RedisModulesClient.create(b.build());
    }

    @Bean(destroyMethod = "close")
    public StatefulRedisModulesConnection<String,String> redisModulesConnection(RedisModulesClient client) {
        return client.connect();
    }

    @Bean
    public RedisModulesCommands<String,String> redisModulesCommands(StatefulRedisModulesConnection<String,String> conn) {
        return conn.sync();
    }
}