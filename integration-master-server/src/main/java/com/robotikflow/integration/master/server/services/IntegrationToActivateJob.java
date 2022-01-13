package com.robotikflow.integration.master.server.services;

import java.time.ZonedDateTime;

import com.robotikflow.core.models.queue.IntegrationTriggeredMessage;
import com.robotikflow.core.models.repositories.CollectionIntegrationRepository;
import com.robotikflow.core.services.queue.QueueService;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
public class IntegrationToActivateJob 
	extends QuartzJobBean 
{
	@Autowired
	private CollectionIntegrationRepository integracaoRepo;
    @Autowired
    @Qualifier("integrationQueueService")
	private QueueService queueService;
	
	private static Logger logger = LoggerFactory.getLogger(IntegrationToActivateJob.class);
	
	@Override
	protected void executeInternal(
		JobExecutionContext context) 
		throws JobExecutionException 
	{
		var integrations = integracaoRepo.findAllToInitiate(ZonedDateTime.now());
		
		logger.info(String.format("Found %d integrations to activate", integrations.size()));
		
		for(var integration: integrations)
		{
			try 
			{
				integration.setStarted(true);

				var mensagem = new IntegrationTriggeredMessage(integration.getId());

				// se não for executada em horário específico do dia, iniciar imediatamente..
				if(integration.getMinOfDay() == null || 
					itIsTime(integration.getMinOfDay()))
				{
					queueService.enviar(mensagem);
				}
			} 
			catch (Exception e) 
			{
				logger.error("Failed to queue integration", e);
			}
			finally
			{
				integracaoRepo.save(integration);
			}
		}
		
	}

	private boolean itIsTime(
		final Integer atMinuteOfDay) 
	{
		var now = ZonedDateTime.now();
		var currentMinuteOfDay = now.getHour() * 60 + now.getMinute();

		return atMinuteOfDay == currentMinuteOfDay;
	}
}
