package com.robotikflow.integration.master.server.services;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

import com.robotikflow.core.models.queue.IntegrationTriggeredMessage;
import com.robotikflow.core.models.repositories.CollectionIntegrationRepository;
import com.robotikflow.core.services.queue.QueueService;

@Component
public class IntegrationMinuteOfDayJob extends QuartzJobBean 
{
	@Autowired
	private CollectionIntegrationRepository colIntegracaoRepo;
    @Autowired
    @Qualifier("integrationQueueService")
	private QueueService queueService;
	
	private static Logger logger = LoggerFactory.getLogger(IntegrationMinuteOfDayJob.class);
	
	@Override
	protected void executeInternal(
		JobExecutionContext context) 
		throws JobExecutionException 
	{
		var now = ZonedDateTime.now();
		var minuteOfDay = now.getHour() * 60 + now.getMinute();
		var ids = colIntegracaoRepo.findAllByMinOfDay(minuteOfDay);
		
		logger.info(String.format("Found %d integrations with minute of day %d", ids.size(), minuteOfDay));
		
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
