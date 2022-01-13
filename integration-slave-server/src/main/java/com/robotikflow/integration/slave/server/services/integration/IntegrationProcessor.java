package com.robotikflow.integration.slave.server.services.integration;

import java.util.Map;
import java.lang.management.ManagementFactory;

import com.robotikflow.core.factories.ActivityServiceFactory;
import com.robotikflow.core.factories.TriggerServiceFactory;
import com.robotikflow.core.interfaces.IActivityService;
import com.robotikflow.core.interfaces.ITriggerService;
import com.robotikflow.core.models.entities.CollectionIntegration;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.queue.IntegrationTriggeredMessage;
import com.robotikflow.core.models.repositories.CollectionIntegrationRepository;
import com.robotikflow.core.models.repositories.ObjStateRepository;
import com.robotikflow.core.services.collections.CollectionService;
import com.robotikflow.core.services.log.CollectionIntegrationLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IntegrationProcessor 
	extends BaseProcessor
{
	@Autowired
	private TriggerServiceFactory triggerServiceFactory;
	@Autowired
	private ActivityServiceFactory activityServiceFactory;
	@Autowired
	private ObjStateRepository objStateRepo;
	@Autowired
	private CollectionService collectionService;
	@Autowired
	private CollectionIntegrationRepository integrationRepo;
	@Autowired
	private CollectionIntegrationLogger integrationLogger;
	
	public void execute(
		final IntegrationTriggeredMessage mensagem) 
		throws Exception 
	{
		var id = mensagem.getId();

		if(!sendoProcessado.add(id)) 
		{
			return;
		}

		var integration = (CollectionIntegration)
			integrationRepo.findById(id).get();

		try
		{
			var trigger = integration.getTrigger();
			
			var triggerService = (ITriggerService)triggerServiceFactory
				.build(trigger.getSchema().getName(), trigger);

			var state = integration.getTriggerState();

			var start = ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());
			
			var response = triggerService.sync(
				state.getState());

			var total = ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId()) - start;

			integrationLogger.info(
				integration, 
				"sync() took %d ns and returned %d items", 
				total, 
				response != null? response.size(): 0);

			objStateRepo.save(state);

			if(response != null)
			{
				for(var item : response)
				{
					processItem(integration, item);
				}
			}
		} 
		catch (Exception e) 
		{
			if(integration != null)
			{
				integrationLogger.error(integration, "Integration failed %s", e.getMessage());
			}
			logger.error("Integration failed", e);
		}
		finally 
		{
			sendoProcessado.remove(id);
		}
	}

	private void processItem(
		final CollectionIntegration integration,
		final Map<String, Object> item)
	{
		try 
		{
			runActivities(
				integration, item, integration.getCollection().getWorkspace());
		} 
		catch (Exception e) 
		{
			integrationLogger.error(integration, "Integration activity failed", e);
			logger.error("Integration activity failed", e);
		}
	}

	private void runActivities(
		final CollectionIntegration integration, 
		final Map<String, Object> item,
		final Workspace workspace) 
		throws Exception 
	{
		var scriptContext = collectionService.configScriptContext(null, null);

		scriptContext.put("src", item);
		
		for(var act : integration.getActivities())
		{
			var activity = act.getActivity();
			var activityService = (IActivityService)activityServiceFactory.build(
				activity.getSchema().getName(),
				activity);

			var start = ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());
			
			activityService.run(
				integration.getCollection(),
				item,
				scriptContext);

			var total = ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId()) - start;
	
			integrationLogger.info(
				integration, 
				"Activity %s(%s)::run() took %d ns", 
				activity.getPubId(),
				activity.getSchema().getName(),
				total);
		}
	}	
}