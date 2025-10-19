package com.bharat.springbootsocial.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(25000)
                .setDisconnectDelay(5000);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Set application destination prefix for @MessageMapping endpoints
        registry.setApplicationDestinationPrefixes("/app");
        
        // Enable simple broker for specific topics
        registry.enableSimpleBroker(
            "/group", 
            "/user", 
            "/chat", 
            "/notifications", 
            "/calls", 
            "/room-events", 
            "/room", 
            "/queue", 
            "/group-call", 
            "/group-call-notifications",
            "/call-signaling",
            "/call-invitations"
        );
        
        // Set user destination prefix for user-specific messages
        registry.setUserDestinationPrefix("/user");
    }
}
