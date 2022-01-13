package com.robotikflow.core.util;

import java.time.ZonedDateTime;

import com.robotikflow.core.interfaces.IStorageProviderService;
import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.misc.ObjFields;
import com.robotikflow.core.models.repositories.ProviderSchemaRepository;
import com.robotikflow.core.services.providers.storage.GcloudStorageProvider;
import com.robotikflow.core.services.providers.storage.LocalStorageProvider;

import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.core.env.Environment;

public class ProviderUtil 
{
	private static final String internalStorageProviderName = "internalStorageProvider";

    public static String getInternalStorageProviderName()
    {
        return internalStorageProviderName;
    }

    public static String getRoot(
        final Provider provider)
    {
        return provider.getFields().getString("root");
    }
    
    public static boolean isInternalStorage(
        final Provider provider) 
    {
		return provider.getSchema().getName().equals(internalStorageProviderName);
	}

    public static IStorageProviderService buildInternalProvider(
        final String profile,
        final Environment env,
        final AutowireCapableBeanFactory beanFactory) 
        throws Exception
    {
		switch(profile)
        {
        case "dev":
        {
            var provider = new Provider();
            var fields = new ObjFields() 
            {
                {
                    put("basePath", "../pipeline-server/blobs");
                    put("baseUrl", env.getRequiredProperty("cloud.host-addr"));
                }
            };
            provider.setFields(fields);

            var obj = new LocalStorageProvider();
            beanFactory.autowireBean(obj);
            obj.initialize(provider);
            return obj;
        }

        case "prod":
        {
            var credential = new Credential();
            credential.setFields(new ObjFields() 
            {
                {
                    put("filePath", env.getRequiredProperty("cloud.credentials-filename"));
                }
            });
            
            var provider = new Provider();
            provider.setFields(new ObjFields() 
            {
                {
                    put("credential", credential);
                    put("corsOrigin", env.getRequiredProperty("cors.allowed-origins"));
                    put("location", "us-east1");
                }
            });
            
            var obj = new GcloudStorageProvider();
            beanFactory.autowireBean(obj);
            obj.initialize(provider);
            return obj;
        }

        default:
            throw new Exception("Unknown profile");
        }
    }

	public static Provider createDefault(
        final User user, 
		final Workspace workspace,
        final ProviderSchemaRepository providerSchemaRepo) 
	{
		var provider = new Provider();

		var fields = new ObjFields();
		if(workspace.getRootDoc() != null)
        {
            fields.put("root", workspace.getRootDoc().getPubId());
        }

        var schema = providerSchemaRepo.findByName(internalStorageProviderName);
		
        provider.setSchema(schema);
		provider.setFields(fields);
		provider.setWorkspace(workspace);
		provider.setCreatedBy(user);
		provider.setCreatedAt(ZonedDateTime.now());

		return provider;
	}

}
