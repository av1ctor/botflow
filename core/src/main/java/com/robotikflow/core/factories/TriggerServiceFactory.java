package com.robotikflow.core.factories;

import java.util.Map;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.interfaces.ITriggerService;
import com.robotikflow.core.models.entities.Trigger;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.TriggerRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.context.annotation.Lazy;
//import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class TriggerServiceFactory
	extends ObjBaseServiceFactory
{
	@Autowired 
	private AutowireCapableBeanFactory beanFactory;
	@Autowired
	private TriggerRepository triggerRepo;
	
	private static final Map<String,  Class<?>> classLut = 
		scanComponents("com.robotikflow.core.services.triggers");

	//@Cacheable(value = "triggersFactory", key = "{#name}{#workspace.id}")
	public ITriggerService buildByName(
        final String name,
		final Workspace workspace) 
        throws Exception
	{
        if(name == null)
		{
			throw new ObjException("Invalid trigger name");
		}

		var prov = triggerRepo
			.findByNameAndWorkspace(name, workspace);

		return build(name, prov);
	}

	//@Cacheable(value = "triggersFactory", key = "{#id}")
	public ITriggerService buildByPubId(
        final String id,
		final Workspace workspace) 
        throws Exception
	{
        if(id == null)
		{
			throw new ObjException("Invalid trigger id");
		}

		var prov = triggerRepo
			.findByPubIdAndWorkspace(id, workspace);

		return build(prov.getSchema().getName(), prov);
	}

	//@Cacheable(value = "triggersFactory", key = "{#id}")
	public ITriggerService buildById(
        final Long id) 
        throws Exception
	{
        if(id == null)
		{
			throw new ObjException("Invalid trigger id");
		}

		var prov = triggerRepo
			.findById(id)
				.orElseThrow(() -> new ObjException("Trigger not found"));

		return build(prov.getSchema().getName(), prov);
	}

	public ITriggerService build(
		final String name, 
		final Trigger trigger) 
		throws Exception 
	{
		if(trigger == null)
		{
			throw new ObjException("Trigger not found");
		}

		var klass = classLut.get(name);
		if(klass == null)
		{
			new ObjException("Unknown trigger name");
		}

		var instance = (ITriggerService)beanFactory
			.createBean(klass, AbstractBeanDefinition.AUTOWIRE_BY_TYPE, true);
	
		instance.initialize(trigger);
		
		return instance;
	}    

}