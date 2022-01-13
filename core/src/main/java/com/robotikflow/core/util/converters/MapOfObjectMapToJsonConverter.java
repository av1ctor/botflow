package com.robotikflow.core.util.converters;

import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class MapOfObjectMapToJsonConverter 
	extends MapToJsonConverter<String, Map<String, Object>> 
	implements AttributeConverter<Map<String, Map<String, Object>>, String>
{
	@Override
	public String convertToDatabaseColumn(Map<String, Map<String, Object>> meta) 
	{
		return super.convertToDatabaseColumn(meta);
	}
	
	@Override
	public Map<String, Map<String, Object>> convertToEntityAttribute(String dbData) 
	{
		return super.convertToEntityAttribute(dbData);
	}
	
}
