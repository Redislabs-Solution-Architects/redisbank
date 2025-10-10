package com.redislabs.demos.redisbank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private Config config;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(config.getStomp().getDestinationPrefix()); // ex: "/topic"
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(config.getStomp().getEndpoint()) // ex: "/ws"
                // origin du front (Vite) + éventuellement 8080 pour debug direct
                .setAllowedOriginPatterns("http://localhost:5173", "http://localhost:8080", "*");
        // PAS de withSockJS()  → WebSocket natif
    }
}