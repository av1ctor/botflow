package com.robotikflow.pipeline.server.services.messenger;

import javax.annotation.PreDestroy;

import com.robotikflow.core.interfaces.IEmailProviderService;
import com.robotikflow.core.models.queue.EmailMessengerMessage;
import com.robotikflow.core.models.queue.MessageType;
import com.robotikflow.pipeline.server.services.BaseTask;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class MessengerTask 
	extends BaseTask
	implements Tasklet 
{
	private final MessengerContext context;
	
	public MessengerTask(
		MessengerContext context)
	{
		super(
			context.getMessengerQueueService());
		this.context = context;
	}
	
	@PreDestroy
	public void destroy()
	{
		super.destroy();
	}
	
	@Override
	public RepeatStatus execute(
		StepContribution contribution, 
		ChunkContext chunkContext) 
	{
		return super.execute(
			contribution, 
			chunkContext, 
			MessageType.MESSENGER_EMAIL, 
			(msg) -> 
			{
				handleSendEmail((EmailMessengerMessage)msg);
			});
    }

	private void handleSendEmail(
		final EmailMessengerMessage mensagem) 
	{
		var provider = context.getProviderRepo()
			.findById(mensagem.getProviderId())
				.orElseThrow();

		try 
		{
			var providerService = (IEmailProviderService)context
				.getProviderFactory()	
					.build(provider.getSchema().getName(), provider);
			
			providerService.sendMessage(
				null, 
				mensagem.getTo(),
				mensagem.getSubject(),
				mensagem.getBody());
		} 
		catch (Exception e) 
		{
			context.getLogger().error(
				provider.getWorkspace(), null, "Falha ao enviar e-mail", e);
		}
	}
}
