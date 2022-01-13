package com.robotikflow.core.factories;

import java.util.Map;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.interfaces.IProviderService;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.ProviderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.Lazy;
//import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class ProviderServiceFactory
	extends ObjBaseServiceFactory
{
	@Autowired 
	private AutowireCapableBeanFactory beanFactory;
	@Autowired
	private ProviderRepository providerRepo;
	
	private static final Map<String,  Class<?>> classLut = 
		scanComponents("com.robotikflow.core.services.providers");

	//@Cacheable(value = "providersFactory", key = "{#name}{#workspace.id}")
	public IProviderService buildByName(
        final String name,
		final Workspace workspace) 
        throws Exception
	{
        if(name == null)
		{
			throw new ObjException("Invalid provider name");
		}

		var prov = providerRepo
			.findByNameAndWorkspace(name, workspace);

		return build(name, prov);
	}

	//@Cacheable(value = "providersFactory", key = "{#id}")
	public IProviderService buildByPubId(
        final String id,
		final Workspace workspace) 
        throws Exception
	{
        if(id == null)
		{
			throw new ObjException("Invalid provider id");
		}

		var prov = providerRepo
			.findByPubIdAndWorkspace(id, workspace);

		return build(prov.getSchema().getName(), prov);
	}

	//@Cacheable(value = "providersFactory", key = "{#id}")
	public IProviderService buildById(
        final Long id) 
        throws Exception
	{
        if(id == null)
		{
			throw new ObjException("Invalid provider id");
		}

		var prov = providerRepo
			.findById(id)
				.orElseThrow(() -> new ObjException("Provider not found"));

		return build(prov.getSchema().getName(), prov);
	}

	public IProviderService build(
		final String name, 
		final Provider provider) 
		throws Exception 
	{
		if(provider == null)
		{
			throw new ObjException("Provider not found");
		}

		var klass = classLut.get(name);
		if(klass == null)
		{
			new ObjException("Unknown provider name");
		}

		var instance = (IProviderService)beanFactory
			.createBean(klass, AbstractBeanDefinition.AUTOWIRE_BY_TYPE, true);
	
		instance.initialize(provider);
		
		return instance;
	}    

}