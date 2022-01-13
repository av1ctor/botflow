package com.robotikflow.core.util.converters;

import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class MapStringObjectToJsonConverter 
	extends MapToJsonConverter<String, Object> 
	implements AttributeConverter<Map<String, Object>, String>
{
	@Override
	public String convertToDatabaseColumn(Map<String, Object> meta) 
	{
		return super.convertToDatabaseColumn(meta);
	}
	
	@Override
	public Map<String, Object> convertToEntityAttribute(String dbData) 
	{
		return super.convertToEntityAttribute(dbData);
	}
}
