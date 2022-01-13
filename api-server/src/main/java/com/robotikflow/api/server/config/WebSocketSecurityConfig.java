package com.robotikflow.api.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer 
{
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) 
    {
        messages
            .simpMessageDestMatchers("/topic/**").denyAll()
            .anyMessage().permitAll();
	}

	@Override
    protected boolean sameOriginDisabled() 
    {
		//disable CSRF for websockets for now...
		return true;
	}
}