package com.robotikflow.pipeline.server.services.docs.external;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.factories.ProviderServiceFactory;
import com.robotikflow.core.models.repositories.DocumentRepository;
import com.robotikflow.core.models.repositories.ProviderRepository;
import com.robotikflow.core.services.log.DocumentLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class DocExtContext 
{
	@Autowired
	private DocumentRepository documentRepo;
	@Autowired
	private DocumentLogger docLogger;
	@Autowired
	private ProviderServiceFactory providerServiceFactory;
    @Autowired
    private ProviderRepository providerRepo;
	
	private final ExecutorService threadPool;
	private final ObjectMapper mapper;

	@Autowired
	public DocExtContext(Environment env)
	{
		//
		threadPool = Executors.newFixedThreadPool(10);
		
		//
		mapper = new ObjectMapper()
			.findAndRegisterModules();
	}

	public ExecutorService getThreadPool() {
		return threadPool;
	}

	public DocumentRepository getDocumentRepo() {
		return documentRepo;
	}

	public DocumentLogger getDocLogger() {
		return docLogger;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

	public ProviderServiceFactory getProviderServiceFactory() {
		return providerServiceFactory;
	}

	public ProviderRepository getProviderRepo() {
		return providerRepo;
	}
}
