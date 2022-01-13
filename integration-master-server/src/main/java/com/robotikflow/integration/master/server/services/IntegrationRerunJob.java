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
import com.robotikflow.core.util.DocumentUtil;

@Component
public class IntegrationRerunJob extends QuartzJobBean 
{
	@Autowired
	private CollectionIntegrationRepository colIntegracaoRepo;
    @Autowired
    @Qualifier("integrationQueueService")
	private QueueService queueService;
	
	private static Logger logger = LoggerFactory.getLogger(IntegrationRerunJob.class);
	
	@Override
	protected void executeInternal(
		JobExecutionContext context) 
		throws JobExecutionException 
	{
		var atDate = ZonedDateTime.now().withSecond(0).withNano(0);
		var ids = colIntegracaoRepo.findAllToRerun(atDate);
		
		logger.info(String.format(
			"Encontradas %d integrações para reexecutar em %s", 
			ids.size(), 
			atDate.format(DocumentUtil.datePattern)));
		
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
