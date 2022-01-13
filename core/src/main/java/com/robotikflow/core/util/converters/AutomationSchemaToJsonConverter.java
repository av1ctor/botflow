package com.robotikflow.core.util.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.robotikflow.core.models.schemas.collection.automation.AutomationSchema;

@Converter
public class AutomationSchemaToJsonConverter 
	extends ObjectToJsonConverter<AutomationSchema> 
	implements AttributeConverter<AutomationSchema, String>
{
	@Override
	public String convertToDatabaseColumn(AutomationSchema meta) 
	{
		return super.convertToDatabaseColumn(meta);
	}
	
	@Override
	public AutomationSchema convertToEntityAttribute(String dbData) 
	{
		return super.convertToEntityAttribute(dbData, AutomationSchema.class);
	}
	
}
