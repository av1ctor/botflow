package com.robotikflow.integration.slave.server.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("prod")
@EnableBatchProcessing
public class JobsConfigProd 
{
}
