package com.robotikflow.pipeline.server.config;

import com.robotikflow.pipeline.server.services.messenger.MessengerContext;
import com.robotikflow.pipeline.server.services.messenger.MessengerTask;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class MessengerConfig 
{
	@Autowired 
	private JobBuilderFactory jobs;
	@Autowired
	private StepBuilderFactory steps;
	@Autowired
	private MessengerContext context;
	
	@Bean
    public MessengerTask messengerTask(
		MessengerContext context)
    {
    	return new MessengerTask(context);
    }
	
	@Bean
    public Step messengerStep()
    {
    	return steps
    		.get("messengerStep")
    		.tasklet(messengerTask(context))
    		.taskExecutor(new SimpleAsyncTaskExecutor())
    		.build();
    }
	
	@Bean
    public Job messengerJob() 
    {
        return jobs
          .get("messengerJob")
          .start(messengerStep())
          .build();
    }	
}
