package com.robotikflow.integration.master.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.robotikflow.core.RobotikflowCore;
import com.robotikflow.integration.master.server.services.IntegrationMasterService;

@SpringBootApplication(scanBasePackageClasses = {IntegrationMasterServerApplication.class, RobotikflowCore.class})
public class IntegrationMasterServerApplication implements CommandLineRunner
{
	@Autowired
	private IntegrationMasterService service;
	
	public static void main(String[] args) 
	{
        SpringApplication.run(IntegrationMasterServerApplication.class, args);
    }
	
	@Override
    public void run(String... args) 
	{
		try 
		{
			service.run();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
