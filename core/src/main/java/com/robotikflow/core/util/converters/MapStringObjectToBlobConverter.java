package com.robotikflow.core.util.converters;

import java.util.Map;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class MapStringObjectToBlobConverter
	extends MapToBlobConverter<String, Object>
	implements AttributeConverter<Map<String, Object>, byte[]> 
{
	@Override
	public byte[] convertToDatabaseColumn(Map<String, Object> meta) 
	{
		return super.convertToDatabaseColumn(meta);
	}
	
	@Override
	public Map<String, Object> convertToEntityAttribute(byte[] dbData) 
	{
		return super.convertToEntityAttribute(dbData);
	}
}