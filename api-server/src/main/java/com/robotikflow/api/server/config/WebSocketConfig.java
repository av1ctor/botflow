package com.robotikflow.api.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.robotikflow.api.server.interceptors.WebSocketChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer 
{
	@Autowired 
	private WebSocketChannelInterceptor wsChannelInterceptor;
    @Value("${websocket.path}")
    private String websocketPath;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry)
    {
        registry
            .setApplicationDestinationPrefixes("/app", "/topic")
            .enableStompBrokerRelay("/topic");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) 
    {
        registry
            .addEndpoint(websocketPath)
                .setAllowedOrigins("*");
    }

	@Override
    public void configureClientInboundChannel(ChannelRegistration registration) 
    {
        registration.interceptors(wsChannelInterceptor);
	}    
}