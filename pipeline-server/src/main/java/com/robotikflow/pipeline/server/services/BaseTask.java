package com.robotikflow.pipeline.server.services;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.models.queue.Message;
import com.robotikflow.core.models.queue.MessageType;
import com.robotikflow.core.services.queue.QueueService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

public class BaseTask 
{
	protected final Logger logger = LoggerFactory.getLogger(BaseTask.class);
	protected final ObjectMapper objMapper = new ObjectMapper()
		.findAndRegisterModules()
		.setSerializationInclusion(Include.NON_NULL);
	private final QueueService queueService;
	private boolean running = true;

	public BaseTask(
		final QueueService queueService
	)
	{
		this.queueService = queueService;
		running = true;
	}
		
	protected void destroy()
	{
		running = false;
	}

	public RepeatStatus execute(
		StepContribution contribution, 
		ChunkContext chunkContext,
		MessageType type,
		Consumer<Message> handleMessage) 
	{
		return execute(
			contribution, 
			chunkContext, 
			Arrays.asList(type),
			Arrays.asList(handleMessage));
	}
	
	public RepeatStatus execute(
		StepContribution contribution, 
		ChunkContext chunkContext,
		List<MessageType> type,
		List<Consumer<Message>> handleMessage) 
	{
		while(running)
		{
			try
			{
				var mensagem = queueService.receber(3000L);
		
				if(mensagem != null)
				{
					var index = type.indexOf(mensagem.getType());
					if(index >= 0)
					{
						handleMessage.get(index).accept(mensagem);
					}
				}
			}
			catch(Exception e)
			{
				logger.error("Falha ao processar mensagem", e);
			}
		}
		
		contribution.setExitStatus(ExitStatus.COMPLETED);
		return RepeatStatus.FINISHED;
	}

	
}