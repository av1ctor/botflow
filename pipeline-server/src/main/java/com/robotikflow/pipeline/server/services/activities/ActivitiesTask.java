package com.robotikflow.pipeline.server.services.activities;

import java.lang.management.ManagementFactory;
import java.util.Map;

import javax.annotation.PreDestroy;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.interfaces.IActivityService;
import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.misc.ObjFields;
import com.robotikflow.core.models.queue.ActivityTriggeredMessage;
import com.robotikflow.core.models.queue.MessageType;
import com.robotikflow.pipeline.server.services.BaseTask;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class ActivitiesTask 
	extends BaseTask
	implements Tasklet 
{
	private final ActivitiesContext context;

	public ActivitiesTask(
		ActivitiesContext context)
	{
		super(
			context.getActivitiesQueueService());

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
			MessageType.ACTIVITY_TRIGGERED, 
			(msg) -> runActivities(
				(ActivityTriggeredMessage)msg));
	}

	private Object runActivities(
		final ActivityTriggeredMessage msg)
	{
		var collectionService = context.getCollectionService();
		
		var collection = (CollectionWithSchema)collectionService
			.findById(msg.getCollectionId());
		if(collection == null)
		{
			return null;
		}

		try
		{
			var workspace = collection.getWorkspace();
			
			var scriptContext = collectionService.configScriptContext(collection, null);

			// no activities id's? create one..
			if(msg.getActivities() == null)
			{
				var params = msg.getParams();

				var schema = context.getActivitySchemaRepository()
					.findByPubId((String)params.get("schemaId"));

				@SuppressWarnings(value = {"unused", "unchecked"})
				var fields = (Map<String, Object>)params.get("fields");

				var activity = new Activity() {{
					setSchema(schema);
					setFields(new ObjFields(fields));
					setWorkspace(collection.getWorkspace());
				}};
	
				var activityService = (IActivityService)context.getActivityServiceFactory()
					.build(schema.getName(), activity);

				@SuppressWarnings("unchecked")
				var values = (Map<String, Object>)params.get("values");

				var start = ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());

				activityService.run(
					collection,
					values,
					scriptContext);

				var total = ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId()) - start;

				context.getCollectionLogger().info(
					collection, 
					collection.getCreatedBy(), 
					"Activity anonymous(%s)::run() took %d ns", 
					schema.getName(),
					total);
			}
			else
			{
				for(var actId : msg.getActivities())
				{
					var activity = context.getActivityRepository()
						.findByPubIdAndWorkspace(actId, workspace);

					if(activity == null)
					{
						throw new ObjException("Activity not found");
					}

					var activityService = (IActivityService)context.getActivityServiceFactory()
						.build(activity.getSchema().getName(), activity);

					var start = ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());
					
					activityService.run(
						collection,
						msg.getParams(),
						scriptContext);

					var total = ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId()) - start;

					context.getCollectionLogger().info(
						collection, 
						collection.getCreatedBy(), 
						"Activity %s(%s)::run() took %d ns", 
						activity.getPubId(),
						activity.getSchema().getName(),
						total);
				}
			}
		}
		catch(Exception e)
		{
			context.getCollectionLogger().error(
				collection, 
				collection.getCreatedBy(), 
				"Activity execution failed: %s", 
				e.getMessage());
		}

		
		return null;
	}


}
