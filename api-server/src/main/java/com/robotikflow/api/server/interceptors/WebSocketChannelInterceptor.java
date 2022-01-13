package com.robotikflow.api.server.interceptors;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import com.robotikflow.core.web.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Service;

@Service
public class WebSocketChannelInterceptor implements ChannelInterceptor
{
	@Autowired
	protected UserService userService;
    @Value("${jwt.header}")
    private String tokenHeader;

    private PropertyChangeSupport support;

    public WebSocketChannelInterceptor()
    {
        support = new PropertyChangeSupport(this);
    }
    
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) 
    {
        var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) 
        {
            if(!accessor.containsNativeHeader(tokenHeader))
            {
                throw new MessagingException("Token obrigatório");
            }
            var bearerToken = accessor.getFirstNativeHeader(tokenHeader);
            var authToken = UserService.getToken(bearerToken);
            
            var ua = userService.autenticar(authToken);

            accessor.setUser(ua);
        }
        else
        {
            switch(accessor.getMessageType())
            {
            case MESSAGE:
            case SUBSCRIBE:
            case UNSUBSCRIBE:
                if(accessor.getUser() == null)
                {
                    throw new MessagingException("Acesso negado");
                }
                break;
            default:
                break;
            }                
        }

        switch(accessor.getMessageType())
        {
        case MESSAGE:
        case SUBSCRIBE:
        case UNSUBSCRIBE:
            support.firePropertyChange(accessor.getCommand().toString(), null, accessor);
            break;
        default:
            break;
        }

        return message;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener pcl) 
    {
        support.addPropertyChangeListener(propertyName, pcl);
    }
 
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener pcl) 
    {
        support.removePropertyChangeListener(propertyName, pcl);
    }    
}