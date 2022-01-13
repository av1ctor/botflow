package com.robotikflow.pipeline.server;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.robotikflow.core.RobotikflowCore;

@SpringBootApplication(scanBasePackageClasses = {PipelineServerApplication.class, RobotikflowCore.class})
public class PipelineServerApplication 
	implements CommandLineRunner
{
	@Autowired
	private JobLauncher jobLaucher;
	@Autowired
	private Job docsJob;
	@Autowired
	private Job messengerJob;
	@Autowired
	private Job activitiesJob;
	
	public static void main(String[] args) 
	{
        SpringApplication.run(PipelineServerApplication.class, args);
    }
	
	@Override
    public void run(String... args) throws Exception 
	{
		jobLaucher.run(docsJob, new JobParameters());
		jobLaucher.run(messengerJob, new JobParameters());
		jobLaucher.run(activitiesJob, new JobParameters());
	}
}
