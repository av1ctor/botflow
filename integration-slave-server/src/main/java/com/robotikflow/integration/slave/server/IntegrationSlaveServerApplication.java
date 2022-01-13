package com.robotikflow.integration.slave.server;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.robotikflow.core.RobotikflowCore;

@SpringBootApplication(scanBasePackageClasses = {IntegrationSlaveServerApplication.class, RobotikflowCore.class})
public class IntegrationSlaveServerApplication 
	implements CommandLineRunner
{
	@Autowired
	private JobLauncher jobLaucher;
	@Autowired
	private Job integrationJob;
	
	public static void main(String[] args) 
	{
        SpringApplication.run(IntegrationSlaveServerApplication.class, args);
    }
	
	@Override
    public void run(String... args) throws Exception 
	{
		jobLaucher.run(integrationJob, new JobParameters());
	}
}
