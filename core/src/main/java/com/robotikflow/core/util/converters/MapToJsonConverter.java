package com.robotikflow.core.util.converters;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MapToJsonConverter<X, Y> 
{
	protected final static ObjectMapper objectMapper = new ObjectMapper()
		.findAndRegisterModules()
		.setSerializationInclusion(Include.NON_NULL)
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	
	protected final static Logger logger = 
		LoggerFactory.getLogger(MapToJsonConverter.class);

	public String convertToDatabaseColumn(Map<X, Y> meta) 
	{
		try 
		{
			if(meta == null)
			{
				return null;
			}
			return objectMapper.writeValueAsString(meta);
	    } 
		catch (JsonProcessingException ex) 
		{
			logger.error("Serialization failed" + ex);
			return null;
	    }
	}
	
	public Map<X, Y> convertToEntityAttribute(String dbData) 
	{
		try 
		{
			if(dbData == null)
			{
				return null;
			}
			return objectMapper.readValue(dbData, new TypeReference<Map<X, Y>>() {});
	    } 
		catch (IOException ex) 
		{
			logger.error("Deserialization failed:" + ex);
			return null;
	    }
	}
}