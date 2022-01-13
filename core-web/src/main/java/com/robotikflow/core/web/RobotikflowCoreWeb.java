package com.robotikflow.core.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource({"classpath:core-web.properties", "classpath:core-web-${spring.profiles.active}.properties"})
public class RobotikflowCoreWeb
{
}