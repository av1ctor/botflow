package com.robotikflow.integration.master.server.services;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class IntegrationMasterService 
{
	@Autowired
    private Scheduler scheduler;
    
	private static Logger logger = LoggerFactory.getLogger(IntegrationMasterService.class);
	
	private final String GROUP_NAME = "integration-jobs";

	private final String TOACTIVATE_JOB_NAME = "activate-job";
	
	private final String RERUN_JOB_NAME = "rerun-job";
	
	private final String RATE_GEN_JOB_NAME = "rate-%s-job";
	private static final List<Integer> rates = Arrays
		.asList(1, 3, 5, 10, 15, 30, 60, 60*6, 60*12, 60*24, 60*24*7, 60*24*15, 60*24*30);
	
	private final String MINUTELY_JOB_NAME = "minutely-job";
	
	private final String POLLING_JOB_NAME = "polling-job";
	private final int POLLING_JOB_RATE = 1;
	
	@Autowired
	public IntegrationMasterService(Environment env)
	{
	}
	
	private <T extends Job> JobDetail buildIntegracaoRateJob(
		final String key, 
		final Class<T> klass,
		final int rate)
	{
		return JobBuilder.newJob(klass)
			.withIdentity(key, GROUP_NAME)
			.usingJobData("rate", rate)
			.storeDurably()
			.build();
	}

	private Trigger buildIntegracaoRateTrigger(
		final JobDetail job, 
		final int rateInMinutes)
	{
		return TriggerBuilder.newTrigger()
			.forJob(job)
			.withIdentity(job.getKey().getName(), "integration-triggers")
			.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(rateInMinutes))
			.startNow()
			.build();
	}

	private Date getNextMinute()
	{
		return Date.from(ZonedDateTime.now().plusMinutes(1).withSecond(0).withNano(0).toInstant());
	}

	private Trigger buildIntegracaoMinutelyTrigger(
		final JobDetail job)
	{
		return TriggerBuilder.newTrigger()
			.forJob(job)
			.withIdentity(job.getKey().getName(), "integration-triggers")
			.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever())
			.startAt(getNextMinute())	
			.build();
	}

	private <T extends Job> void scheduleJob(
		final String name,
		final Class<T> klass,
		final int rate)
		throws SchedulerException
	{
		var key = JobKey.jobKey(name, GROUP_NAME);
		if(scheduler.checkExists(key))
		{
			scheduler.deleteJob(key);
		}
		
		var job = buildIntegracaoRateJob(name, klass, rate);
		scheduler.scheduleJob(job, rate != 0? 
			buildIntegracaoRateTrigger(job, rate):
			buildIntegracaoMinutelyTrigger(job));
	}

	private <T extends Job> void scheduleJob(
		final String name,
		final Class<T> klass)
		throws SchedulerException
	{	
		scheduleJob(name, klass, 0);
	}

	private void iniciarJobs() 
		throws SchedulerException 
	{
		scheduleJob(TOACTIVATE_JOB_NAME, IntegrationToActivateJob.class);
		scheduleJob(RERUN_JOB_NAME, IntegrationRerunJob.class);
		scheduleJob(MINUTELY_JOB_NAME, IntegrationMinuteOfDayJob.class);
		for(var rate : rates)
		{
			var name = String.format(RATE_GEN_JOB_NAME, rate);
			scheduleJob(name, IntegrationRateJob.class, rate);
		}
		scheduleJob(POLLING_JOB_NAME, IntegrationPollingJob.class, POLLING_JOB_RATE);
	}

	public void run() 
		throws Exception
	{
		logger.info("Serviço iniciando...");
		
		iniciarJobs();
		
		logger.info("Serviço iniciado! Digite exit ou quit para sair");
		
		// main loop
		if(System.in != null)
		{
			try(var scanner = new Scanner(System.in))
			{
				var isRunning = true;
				while(isRunning && Thread.currentThread().isAlive())
				{
					var cmd = scanner.nextLine();
					switch(cmd)
					{
						case "exit":
						case "quit":
							isRunning = false;
							break;
					}
				}

				// shutdown
				logger.info("Serviço finalizando...");
				
				scheduler.shutdown(true);
				
				System.exit(0);
			}
			catch (Exception e) 
			{
			}
		}
	}
}
