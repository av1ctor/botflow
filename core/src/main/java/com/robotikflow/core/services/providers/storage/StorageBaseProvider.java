package com.robotikflow.core.services.providers.storage;

import java.net.URLConnection;

import com.robotikflow.core.factories.CredentialServiceFactory;
import com.robotikflow.core.interfaces.IStorageProviderService;
import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.repositories.DocumentRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StorageBaseProvider 
	implements IStorageProviderService
{
    @Autowired
    protected CredentialServiceFactory credentialServiceFactory;
	@Autowired 
	private DocumentRepository documentRepo;

	protected static Logger logger = 
		LoggerFactory.getLogger(StorageBaseProvider.class);

	protected Provider provider;
    
    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
		this.provider = provider;	
	}

	@Override
	public Document getRootDoc()
	{
		var pubId = provider.getFields().getString("root");
		return documentRepo
			.findByPubIdAndWorkspace(pubId, provider.getWorkspace().getId());
	}
	
	protected String extensionToMimeType(String extension)
	{
		return URLConnection.guessContentTypeFromName("foo." + extension);
	}

}