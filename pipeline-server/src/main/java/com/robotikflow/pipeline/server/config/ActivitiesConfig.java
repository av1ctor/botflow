package com.robotikflow.pipeline.server.config;

import com.robotikflow.pipeline.server.services.activities.ActivitiesContext;
import com.robotikflow.pipeline.server.services.activities.ActivitiesTask;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class ActivitiesConfig 
{
	@Autowired 
	private JobBuilderFactory jobs;
	@Autowired
	private StepBuilderFactory steps;
	@Autowired
	private ActivitiesContext context;
	
	@Bean
    public ActivitiesTask activitiesTask(
		ActivitiesContext context)
    {
    	return new ActivitiesTask(context);
    }
	
	@Bean
    public Step activitiesStep()
    {
    	return steps
    		.get("activitiesStep")
    		.tasklet(activitiesTask(context))
    		.taskExecutor(new SimpleAsyncTaskExecutor())
    		.build();
    }
	
	@Bean
    public Job activitiesJob() 
    {
        return jobs
          .get("activitiesJob")
          .start(activitiesStep())
          .build();
    }	
}
