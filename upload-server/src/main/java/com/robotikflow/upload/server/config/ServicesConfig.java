package com.robotikflow.upload.server.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.robotikflow.core.interfaces.IStorageProviderService;
import com.robotikflow.core.services.DocumentService;
import com.robotikflow.core.services.indexing.DocumentIndexService;
import com.robotikflow.core.services.indexing.NullDocumentIndexService;
import com.robotikflow.core.services.providers.storage.NullStorageProvider;
import com.robotikflow.core.services.queue.QueueService;
import com.robotikflow.core.services.queue.RabbitQueueService;

import me.desair.tus.server.TusFileUploadService;

@Configuration
public class ServicesConfig 
{
	@Autowired 
	private AutowireCapableBeanFactory beanFactory;
	@Autowired
	private Environment env;
    
	@Value("${tus.server.data.directory}")
    private String tusDataPath;
    
    @Value("${server.servlet.contextPath}")
    private String apiPath;
	
    @Bean
    public TusFileUploadService tusFileUploadService() {
        return new TusFileUploadService()
            .withStoragePath(tusDataPath)
            .withUploadURI(apiPath + "/docs/upload")
            .withThreadLocalCache(false);
    }
    
    @Bean
    public DocumentService documentService(
        final IStorageProviderService internalStorageProvider) 
        throws Exception
    {
    	var obj = new DocumentService(env, internalStorageProvider);
		beanFactory.autowireBean(obj);
    	return obj;
    }

	@Bean("internalStorageProvider")
	public IStorageProviderService storageProvider() 
		throws Exception
	{
		return new NullStorageProvider();
	}

    @Bean
    public DocumentIndexService documentIndexService() 
        throws NumberFormatException, Exception
    {
    	return new NullDocumentIndexService();
    }
    
    @Bean
    protected RabbitTemplate docsTemplate(
        final ConnectionFactory connectionFactory)
    {
    	return new RabbitTemplate(connectionFactory);
    }
    
    @Bean
    public QueueService docsQueueService(
        final RabbitTemplate docsTemplate, 
        final DirectExchange queueExchange, 
        final Queue docsQueue)
    {
		var obj = new RabbitQueueService(docsTemplate, queueExchange, docsQueue);
		beanFactory.autowireBean(obj);
    	return obj;
    }
}
