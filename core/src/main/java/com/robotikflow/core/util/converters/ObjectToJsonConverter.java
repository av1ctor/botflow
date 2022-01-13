package com.robotikflow.core.util.converters;
import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ObjectToJsonConverter<T> 
{
	private final static ObjectMapper objectMapper = new ObjectMapper()		
		.findAndRegisterModules()
		.setSerializationInclusion(Include.NON_NULL)
		.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
	
	protected final static Logger logger = 
		LoggerFactory.getLogger(ObjectToJsonConverter.class);

	public String convertToDatabaseColumn(T meta) 
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
	
	public T convertToEntityAttribute(String dbData, Class<T> clazz) 
	{
		try 
		{
			if(dbData == null)
			{
				return null;
			}
			return objectMapper.readValue(dbData, clazz);
	    } 
		catch (IOException ex) 
		{
			logger.error("Deserialization failed:" + ex);
			return null;
	    }
	}
}