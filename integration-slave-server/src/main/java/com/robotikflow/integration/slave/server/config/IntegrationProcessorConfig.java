package com.robotikflow.integration.slave.server.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Component;

import com.robotikflow.integration.slave.server.services.integration.IntegrationProcessorTask;

@Component
public class IntegrationProcessorConfig
{
	@Autowired 
	private JobBuilderFactory jobs;
	@Autowired
	private StepBuilderFactory steps;
	@Autowired
	private AutowireCapableBeanFactory beanFactory;
	
	@Bean
    public IntegrationProcessorTask integrationProcessorTask()
    {
    	var obj = new IntegrationProcessorTask();
		beanFactory.autowireBean(obj);
		return obj;
    }
	
	@Bean
    public Step integrationProcessStep()
    {
    	return steps
    		.get("integrationStep")
    		.tasklet(integrationProcessorTask())
    		.taskExecutor(new SimpleAsyncTaskExecutor())
    		.build();
    }
	
	@Bean
    public Job integrationJob() 
    {
        return jobs
          .get("integrationJob")
          .start(integrationProcessStep())
          .build();
    }	
}
