package com.robotikflow.core.services.factories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.robotikflow.core.factories.ProviderServiceFactory;
import com.robotikflow.core.interfaces.IStorageProviderService;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.services.DocumentService;
import com.robotikflow.core.util.ProviderUtil;

@Component
@Lazy
public class DocumentServiceFactory
{
    @Autowired
    private Environment env;
    @Autowired
    private AutowireCapableBeanFactory beanFactory;
    @Autowired
    private DocumentService documentService;
    @Autowired
    private ProviderServiceFactory providerFactory;
    
    @Cacheable(value = "documentServices", key = "{#provider.id}")
    public DocumentService build(
        final Provider provider) 
        throws Exception
	{
        if(provider == null || 
            ProviderUtil.isInternalStorage(provider))
		{
			return documentService;
		}

        var service = (IStorageProviderService)providerFactory
            .build(provider.getSchema().getName(), provider);
        
        var res = new DocumentService(env, service);
        beanFactory.autowireBean(res);
        return res;
	}
}