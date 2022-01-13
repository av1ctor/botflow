package com.robotikflow.core;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource({"classpath:core.properties", "classpath:core-${spring.profiles.active}.properties"})
public class RobotikflowCore
{
	@Bean
	public RestTemplate restTemplate() 
	{
	    return new RestTemplate();
	}	
}