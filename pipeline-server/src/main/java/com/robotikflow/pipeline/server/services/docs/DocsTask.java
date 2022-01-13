package com.robotikflow.pipeline.server.services.docs;

import java.util.Arrays;

import javax.annotation.PreDestroy;

import com.robotikflow.core.models.queue.DocExtCreatedMessage;
import com.robotikflow.core.models.queue.DocIntCopiedMessage;
import com.robotikflow.core.models.queue.DocIntCreatedMessage;
import com.robotikflow.core.models.queue.DocIntDeletedMessage;
import com.robotikflow.core.models.queue.DocIntUpdatedMessage;
import com.robotikflow.core.models.queue.MessageType;
import com.robotikflow.pipeline.server.services.BaseTask;
import com.robotikflow.pipeline.server.services.docs.external.DocExtTask;
import com.robotikflow.pipeline.server.services.docs.internal.DocIntTask;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class DocsTask 
	extends BaseTask
	implements Tasklet 
{
	private final DocIntTask docIntTask;
	private final DocExtTask docExtTask;

	public DocsTask(
		DocsContext context)
	{
		super(
			context.getDocsQueueService());

		docIntTask = new DocIntTask(context.getDocIntContext());
		docExtTask = new DocExtTask(context.getDocExtContext());
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
			Arrays.asList(
				MessageType.DOC_INT_UPDATED, 
				MessageType.DOC_INT_CREATED, 
				MessageType.DOC_INT_DELETED, 
				MessageType.DOC_INT_COPIED,
				MessageType.DOC_EXT_CREATED), 
			Arrays.asList(
				(msg) -> docIntTask.handleOnDocUpdated((DocIntUpdatedMessage)msg),
				(msg) -> docIntTask.handleOnDocCreated((DocIntCreatedMessage)msg),
				(msg) -> docIntTask.handleOnDocDeleted((DocIntDeletedMessage)msg),
				(msg) -> docIntTask.handleOnDocCopied((DocIntCopiedMessage)msg),
				(msg) -> docExtTask.handleOnDocCreated((DocExtCreatedMessage)msg)
			));
	}


}
