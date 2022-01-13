package com.robotikflow.pipeline.server.config;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Profile("dev")
@EnableBatchProcessing(modular = true)
public class JobsConfigDev extends DefaultBatchConfigurer 
{
	@Override
    public PlatformTransactionManager getTransactionManager() 
	{
       return new ResourcelessTransactionManager();
    }
	
	@Override
    protected JobRepository createJobRepository() throws Exception
    {
		var factory = new MapJobRepositoryFactoryBean();
        factory.setTransactionManager(getTransactionManager());
        return (JobRepository) factory.getObject();
    }
	
	@Override
	protected JobLauncher createJobLauncher() throws Exception 
	{
        var jobLauncher = new SimpleJobLauncher();
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.setJobRepository(getJobRepository());
        return jobLauncher;
    }	
}
