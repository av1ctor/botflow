package com.robotikflow.core.services.queue;

import com.robotikflow.core.models.queue.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.core.AbstractExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.core.ParameterizedTypeReference;

public class RabbitQueueService 
	implements QueueService 
{
	public static final String AMQP_TOPIC_EXCHANGE = "amq.topic";
	private final RabbitTemplate template;	
	private final Queue queue;
	private ObjectMapper mapper = new ObjectMapper()
		.findAndRegisterModules();
	
	public RabbitQueueService(
		final RabbitTemplate template) 
	{
		this.template = template;
		this.queue = null;
		
		template.setMessageConverter(new Jackson2JsonMessageConverter(mapper));
	}

	public RabbitQueueService(
		final RabbitTemplate template, 
		final AbstractExchange exchange, 
		final Queue queue) 
	{
		this.template = template;
		this.queue = queue;
		
		template.setExchange(exchange.getName());
		template.setDefaultReceiveQueue(queue.getName());
		template.setRoutingKey(queue.getName());
		template.setMessageConverter(new Jackson2JsonMessageConverter(mapper));
	}
	
	@Override
	public void enviar(
		final Message mensagem) 
		throws Exception
	{
		template.convertAndSend(queue.getName(), mensagem);
	}

	@Override
	public void enviar(
		final String exchange, 
		final String destino, 
		final Message mensagem) 
		throws Exception
	{
		template.convertAndSend(exchange, destino, mensagem);
	}
	
	@Override
	public Message receber(
		final long timeoutEmMs) 
		throws Exception
	{
		return template.receiveAndConvert(
			timeoutEmMs, ParameterizedTypeReference.forType(Message.class));
	}

	@Override
	public Message receber() 
		throws Exception
	{
		return receber(-1L);
	}
}
