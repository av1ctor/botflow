package com.robotikflow.core.util.converters;

import java.io.IOException;
import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.robotikflow.core.models.schemas.obj.Field;

@Converter
public class MapStringFieldToJsonConverter
	extends MapToJsonConverter<String, Field>
	implements AttributeConverter<Map<String, Field>, String> 
{
	@Override
	public String convertToDatabaseColumn(Map<String, Field> meta) 
	{
		return super.convertToDatabaseColumn(meta);
	}
	
	@Override
	public Map<String, Field> convertToEntityAttribute(String dbData) 
	{
		try 
		{
			if(dbData == null)
			{
				return null;
			}
			return objectMapper.readValue(dbData, new TypeReference<Map<String, Field>>() {});
	    } 
		catch (IOException ex) 
		{
			logger.error("Deserialization failed:" + ex);
			return null;
	    }
	}
}