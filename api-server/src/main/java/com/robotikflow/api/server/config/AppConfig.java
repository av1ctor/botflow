package com.robotikflow.api.server.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.robotikflow.api.server.config.custom.ObjectIdSerializer;

import org.bson.types.ObjectId;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class AppConfig
{
    @Bean
    public SimpleModule objectIdSerializer() 
    {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ObjectId.class, new ObjectIdSerializer());
        return module;
    }
}