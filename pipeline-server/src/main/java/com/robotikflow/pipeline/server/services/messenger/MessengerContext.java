package com.robotikflow.pipeline.server.services.messenger;

import com.robotikflow.core.factories.CredentialServiceFactory;
import com.robotikflow.core.factories.ProviderServiceFactory;
import com.robotikflow.core.models.repositories.ProviderRepository;
import com.robotikflow.core.services.log.WorkspaceLogger;
import com.robotikflow.core.services.queue.QueueService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessengerContext 
{
    @Autowired
    private QueueService messengerQueueService;
	@Autowired
	private ProviderServiceFactory providerFactory;
    @Autowired
    private ProviderRepository providerRepo;
    @Autowired
    private CredentialServiceFactory credentialFactory;
	@Autowired
	private WorkspaceLogger logger;

	public ProviderRepository getProviderRepo() {
        return providerRepo;
    }

    public ProviderServiceFactory getProviderFactory() {
		return providerFactory;
	}

	public CredentialServiceFactory getCredentialFactory() {
		return credentialFactory;
	}

	public QueueService getMessengerQueueService() {
        return messengerQueueService;
    }

    public WorkspaceLogger getLogger() {
        return logger;
    }
}
