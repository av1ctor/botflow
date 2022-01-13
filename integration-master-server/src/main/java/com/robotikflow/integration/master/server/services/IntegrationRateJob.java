package com.robotikflow.integration.master.server.services;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import com.robotikflow.core.models.queue.IntegrationTriggeredMessage;
import com.robotikflow.core.models.repositories.CollectionIntegrationRepository;
import com.robotikflow.core.services.queue.QueueService;

@Component
public class IntegrationRateJob 
	extends QuartzJobBean 
{
	@Autowired
	private CollectionIntegrationRepository colIntegracaoRepo;
    @Autowired
    @Qualifier("integrationQueueService")
	private QueueService queueService;
	
	private static Logger logger = LoggerFactory.getLogger(IntegrationRateJob.class);
	
	@Override
	protected void executeInternal(
		JobExecutionContext context) 
		throws JobExecutionException 
	{
		var rate = (Integer)context.getJobDetail().getJobDataMap().get("rate");
		var ids = colIntegracaoRepo.findAllIdsByFreq(rate);
		
		logger.info(String.format("Found %d integrations with frequency %d", ids.size(), rate));
		
		for(var id: ids)
		{
			try
			{
				var mensagem = new IntegrationTriggeredMessage(id);
				queueService.enviar(mensagem);
			} 
			catch (Exception e) 
			{
				logger.error("Failed to queue integration", e);
			}
		}
		
	}
}
