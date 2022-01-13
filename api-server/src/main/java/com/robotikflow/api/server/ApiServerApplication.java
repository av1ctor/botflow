package com.robotikflow.api.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.robotikflow.core.RobotikflowCore;
import com.robotikflow.core.web.RobotikflowCoreWeb;

@SpringBootApplication(scanBasePackageClasses = {ApiServerApplication.class, RobotikflowCore.class, RobotikflowCoreWeb.class})
public class ApiServerApplication
{
	public static void main(String[] args) 
	{
		SpringApplication.run(ApiServerApplication.class, args);
	}

}
