package com.robotikflow.api.server.config;

import com.robotikflow.core.interfaces.IStorageProviderService;
import com.robotikflow.core.services.DocumentService;
import com.robotikflow.core.services.indexing.DocumentIndexService;
import com.robotikflow.core.services.indexing.ElasticDocumentIndexService;
import com.robotikflow.core.services.nosql.MongoDbService;
import com.robotikflow.core.services.nosql.NoSqlService;
import com.robotikflow.core.services.queue.QueueService;
import com.robotikflow.core.services.queue.RabbitQueueService;
import com.robotikflow.core.util.ProviderUtil;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

@Configuration
public class ServicesConfig 
{
	@Autowired 
	private AutowireCapableBeanFactory beanFactory;
	@Autowired
	private Environment env;
	
	@Bean
	public DocumentService documentService(
		IStorageProviderService internalStorageProvider) 
		throws Exception
	{
		var obj = new DocumentService(env, internalStorageProvider);
		beanFactory.autowireBean(obj);
		return obj;
	}

	@Bean("internalStorageProvider")
	@Profile("dev")
	public IStorageProviderService storageProviderLocal() 
		throws Exception
	{
		return ProviderUtil.buildInternalProvider(
			"dev", env, beanFactory);
	}

	@Bean("internalStorageProvider")
	@Profile("prod")
	public IStorageProviderService storageProviderCloud() 
		throws Exception
	{
		return ProviderUtil.buildInternalProvider(
			"prod", env, beanFactory);
	}

	@Bean
	public DocumentIndexService documentIndexService() 
		throws NumberFormatException, Exception
	{
		var indexHostAddr = env.getRequiredProperty("index.host-addr");
		var indexHostPort = env.getRequiredProperty("index.host-port");
		var obj = new ElasticDocumentIndexService(indexHostAddr, Integer.parseInt(indexHostPort));
		beanFactory.autowireBean(obj);
		return obj;
	}
	
	@Bean
	protected RabbitTemplate docsTemplate(
		ConnectionFactory connectionFactory)
	{
		return new RabbitTemplate(connectionFactory);
	}
	
	@Bean
	public QueueService docsQueueService(
		RabbitTemplate docsTemplate, 
		DirectExchange queueExchange, 
		Queue docsQueue)
	{
		var obj = new RabbitQueueService(docsTemplate, queueExchange, docsQueue);
		beanFactory.autowireBean(obj);
		return obj;
	}

	@Bean
	protected RabbitTemplate stompTemplate(
		ConnectionFactory connectionFactory)
	{
		return new RabbitTemplate(connectionFactory);
	}
	
	@Bean
	public QueueService stompQueueService(
		RabbitTemplate stompTemplate)
	{
		var obj = new RabbitQueueService(stompTemplate);
		beanFactory.autowireBean(obj);
		return obj;
	}
	
	@Bean
	protected RabbitTemplate messengerTemplate(
		ConnectionFactory connectionFactory)
	{
		return new RabbitTemplate(connectionFactory);
	}
	
	@Bean
	public QueueService messengerQueueService(
		RabbitTemplate messengerTemplate, 
		DirectExchange queueExchange, 
		Queue messengerQueue)
	{
		var obj = new RabbitQueueService(
			messengerTemplate, queueExchange, messengerQueue);
		beanFactory.autowireBean(obj);
		return obj;
	}

	@Bean
	protected RabbitTemplate activitiesTemplate(
		ConnectionFactory connectionFactory)
	{
		return new RabbitTemplate(connectionFactory);
	}
	
	@Bean
	public QueueService activitiesQueueService(
		RabbitTemplate activitiesTemplate, 
		DirectExchange queueExchange, 
		Queue activitiesQueue)
	{
		var obj = new RabbitQueueService(
			activitiesTemplate, queueExchange, activitiesQueue);
		beanFactory.autowireBean(obj);
		return obj;
	}

	@Bean
	public NoSqlService noSqlService() 
		throws Exception
	{
		var hostAddr = env.getRequiredProperty("nosql.host-addr");
		var hostPort = Integer.parseInt(env.getRequiredProperty("nosql.host-port"));
		var obj = new MongoDbService(hostAddr, hostPort);
		beanFactory.autowireBean(obj);
		return obj;
	}
}
