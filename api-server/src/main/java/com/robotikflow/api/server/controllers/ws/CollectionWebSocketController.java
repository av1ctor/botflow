package com.robotikflow.api.server.controllers.ws;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.robotikflow.core.models.UserSession;
import com.robotikflow.core.services.collections.CollectionService;
import com.robotikflow.api.server.controllers.BaseController;
import com.robotikflow.api.server.interceptors.WebSocketChannelInterceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.AntPathMatcher;

@Controller
public class CollectionWebSocketController 
	extends BaseController 
	implements PropertyChangeListener
{
	@Autowired
	private CollectionService collectionService;

	private final String PATTERN = "/topic/collections.{id}";
	private final AntPathMatcher ant = new AntPathMatcher();

	@Autowired
	public CollectionWebSocketController(
		WebSocketChannelInterceptor interceptor)
	{
		interceptor.addPropertyChangeListener(StompCommand.SUBSCRIBE.toString(), this);	
	}

	@Override
	public void propertyChange(
		PropertyChangeEvent evt) 
	{
		var accessor = (StompHeaderAccessor)evt.getNewValue();
		var path = accessor.getDestination();
	
		if(!ant.match(PATTERN, path))
		{
			return;
		}

		var variables = ant.extractUriTemplateVariables(PATTERN, path);
		var id = (String)variables.get("id");
		
		var ua = (UserSession)accessor.getUser();

		var collection = collectionService
			.findByPubIdAndUserAndWorkspace(id, ua.getUser(), ua.getWorkspace());
		if(collection == null)
		{
			throw new MessagingException("Coleção inexistente ou acesso negado");
		}
	}
}