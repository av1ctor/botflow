package com.robotikflow.core.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AmqpConfig 
{
	private static final String DOCS_QUEUE = "ROBOTIKFLOW_DOCS";
    private static final String INTEGRATION_QUEUE = "ROBOTIKFLOW_INTEGRATION";
    private static final String ACTIVITIES_QUEUE = "ROBOTIKFLOW_ACTIVITIES";
    private static final String MESSENGER_QUEUE = "ROBOTIKFLOW_MESSENGER";
    private static final String EXCHANGE = "ROBOTIKFLOW_EXCHANGE";
	
    @Bean
    public DirectExchange queueExchange() 
    {
        return new DirectExchange(EXCHANGE);
    }
    
    @Bean
    public Queue docsQueue() 
	{
        return new Queue(DOCS_QUEUE);
    }
	
	@Bean
    public Queue integrationQueue() 
	{
        return new Queue(INTEGRATION_QUEUE);
    }
	
    @Bean
    public Queue messengerQueue() 
	{
        return new Queue(MESSENGER_QUEUE);
	}
	
	@Bean
    public Queue activitiesQueue() 
	{
        return new Queue(ACTIVITIES_QUEUE);
	}
	
	@Bean
	public Binding bindingDocsQueue(
		DirectExchange queueExchange, 
		Queue docsQueue) 
	{
	    return BindingBuilder.bind(docsQueue)
	        .to(queueExchange)
	        .withQueueName();
	}	

	@Bean
	public Binding bindingIntegrationQueue(
		DirectExchange queueExchange, 
		Queue integrationQueue) 
	{
	    return BindingBuilder.bind(integrationQueue)
	        .to(queueExchange)
	        .withQueueName();
	}	

	@Bean
	public Binding bindingMessengerQueue(
		DirectExchange queueExchange, 
		Queue messengerQueue) 
	{
	    return BindingBuilder.bind(messengerQueue)
	        .to(queueExchange)
	        .withQueueName();
	}	

	@Bean
	public Binding bindingActivitiesQueue(
		DirectExchange queueExchange, 
		Queue activitiesQueue) 
	{
	    return BindingBuilder.bind(activitiesQueue)
	        .to(queueExchange)
	        .withQueueName();
	}}
