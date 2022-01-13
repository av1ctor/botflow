package com.robotikflow.upload.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.robotikflow.core.RobotikflowCore;
import com.robotikflow.core.web.RobotikflowCoreWeb;

@SpringBootApplication(scanBasePackageClasses = {UploadServerApplication.class, RobotikflowCore.class, RobotikflowCoreWeb.class})
public class UploadServerApplication
{
	public static void main(String[] args) 
	{
		SpringApplication.run(UploadServerApplication.class, args);
	}
}
