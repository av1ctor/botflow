package com.robotikflow.integration.slave.server.services.integration;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseProcessor 
{
    protected static Logger logger = LoggerFactory.getLogger(BaseProcessor.class);
	
	protected ObjectMapper objMapper = new ObjectMapper()
		.findAndRegisterModules()
		.setSerializationInclusion(Include.NON_NULL);
    
	//FIXME: usar Redis centralizado para manter o controle do que está sendo processado
    protected static Set<Long> sendoProcessado = Collections.synchronizedSet(new HashSet<>());
}