package com.robotikflow.pipeline.server.config;

import com.robotikflow.pipeline.server.services.docs.DocsContext;
import com.robotikflow.pipeline.server.services.docs.DocsTask;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

@Component
public class DocsConfig 
{
	@Autowired 
	private JobBuilderFactory jobs;
	@Autowired
	private StepBuilderFactory steps;
	@Autowired
	private DocsContext docsContext;
	
	@Bean
    public DocsTask docsTask(
		DocsContext docsContext)
    {
    	return new DocsTask(docsContext);
    }
	
	@Bean
    public Step docsStep()
    {
    	return steps
    		.get("docsStep")
    		.tasklet(docsTask(docsContext))
    		.taskExecutor(new SimpleAsyncTaskExecutor())
    		.build();
    }
	
	@Bean
    public Job docsJob() 
    {
        return jobs
          .get("docsJob")
          .start(docsStep())
          .build();
    }	
}
