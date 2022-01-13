package com.robotikflow.core.factories;

import java.util.Map;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.interfaces.ICredentialService;
import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.CredentialRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
//import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class CredentialServiceFactory 
	extends ObjBaseServiceFactory
{
	@Autowired 
	private AutowireCapableBeanFactory beanFactory;
 	@Autowired
	private CredentialRepository credentialRepo;
	
	private static final Map<String,  Class<?>> classLut = 
		scanComponents("com.robotikflow.core.services.credentials");

	//@Cacheable(value = "credentialsFactory", key = "{#name}{#workspace.id}")
	public ICredentialService buildByName(
        final String name,
		final Workspace workspace) 
        throws Exception
	{
        if(name == null)
		{
			throw new ObjException("Invalid credential name");
		}

		var cred = credentialRepo
			.findByNameAndWorkspace(name, workspace);

		return build(name, cred);
	}

	//@Cacheable(value = "credentialsFactory", key = "{#id}")
	public ICredentialService buildByPubId(
        final String id,
		final Workspace workspace) 
        throws Exception
	{
        if(id == null)
		{
			throw new ObjException("Invalid credential id");
		}

		var cred = credentialRepo
			.findByPubIdAndWorkspace(id, workspace);

		return build(cred.getSchema().getName(), cred);
	}

	//@Cacheable(value = "credentialsFactory", key = "{#id}")
	public ICredentialService build(
		final String name, 
		final Credential credential) 
		throws Exception 
	{
		if(credential == null)
		{
			throw new ObjException("Credential not found");
		}

		var klass = classLut.get(name);
		if(klass == null)
		{
			new ObjException("Unknown credential name");
		}

		var instance = (ICredentialService)beanFactory
			.createBean(klass, AbstractBeanDefinition.AUTOWIRE_BY_TYPE, true);

		instance.initialize(credential);

		return instance;
	}    

}