package com.robotikflow.core.factories;

import java.util.Map;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.interfaces.IActivityService;
import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.ActivityRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
//import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Lazy
public class ActivityServiceFactory 
	extends ObjBaseServiceFactory
{
	@Autowired 
	private AutowireCapableBeanFactory beanFactory;
 	@Autowired
	private ActivityRepository activityRepo;
	
	private static final Map<String,  Class<?>> classLut = 
		scanComponents("com.robotikflow.core.services.activities");

	//@Cacheable(value = "activitiesFactory", key = "{#name}{#workspace.id}")
	public IActivityService buildByName(
        final String name,
		final Workspace workspace) 
        throws Exception
	{
        if(name == null)
		{
			throw new ObjException("Invalid activity name");
		}

		var cred = activityRepo
			.findByNameAndWorkspace(name, workspace);

		return build(name, cred);
	}

	//@Cacheable(value = "activitiesFactory", key = "{#id}")
	public IActivityService buildByPubId(
        final String id,
		final Workspace workspace) 
        throws Exception
	{
        if(id == null)
		{
			throw new ObjException("Invalid activity id");
		}

		var cred = activityRepo
			.findByPubIdAndWorkspace(id, workspace);

		return build(cred.getSchema().getName(), cred);
	}

	//@Cacheable(value = "activitiesFactory", key = "{#id}")
	public IActivityService build(
		final String name, 
		final Activity activity) 
		throws Exception 
	{
		if(activity == null)
		{
			throw new ObjException("Activity not found");
		}

		var klass = classLut.get(name);
		if(klass == null)
		{
			new ObjException("Unknown activity name");
		}

		var instance = (IActivityService)beanFactory
			.createBean(klass, AbstractBeanDefinition.AUTOWIRE_BY_TYPE, true);
	
		instance.initialize(activity);
	
		return instance;
	}    

}