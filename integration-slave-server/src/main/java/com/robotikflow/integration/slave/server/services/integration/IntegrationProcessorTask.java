package com.robotikflow.integration.slave.server.services.integration;

import javax.annotation.PreDestroy;

import com.robotikflow.core.models.queue.IntegrationTriggeredMessage;
import com.robotikflow.core.services.queue.QueueService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;

public class IntegrationProcessorTask 
	implements Tasklet 
{
	@Autowired
	private QueueService integrationQueueService;
	@Autowired
	private IntegrationProcessor integrationProcessor;
	
	private static Logger logger = LoggerFactory.getLogger(IntegrationProcessorTask.class);
	private boolean running = true;
	
	public IntegrationProcessorTask()
	{
		running = true;
	}

	@PreDestroy
	public void destroy()
	{
		running = false;
	}
	
	
	@Override
	public RepeatStatus execute(
		StepContribution contribution, 
		ChunkContext chunkContext) 
		throws Exception 
	{
		while(running)
		{
			try
			{
				var mensagem = integrationQueueService.receber(3000L);
		
				if(mensagem != null)
				{
					switch(mensagem.getType())
					{
					case INTEGRATION_TRIGGERED:
						integrationProcessor
							.execute((IntegrationTriggeredMessage)mensagem);
						break;
					default:
						break;
					}
				}
			}
			catch(Exception e)
			{
				logger.error("Integration processing failed", e);
			}
		}
		
		contribution.setExitStatus(ExitStatus.COMPLETED);
		return RepeatStatus.FINISHED;
	}
	
}
