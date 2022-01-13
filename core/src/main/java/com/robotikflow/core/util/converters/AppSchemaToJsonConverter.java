package com.robotikflow.core.util.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.robotikflow.core.models.schemas.app.AppSchema;

@Converter
public class AppSchemaToJsonConverter 
	extends ObjectToJsonConverter<AppSchema> 
	implements AttributeConverter<AppSchema, String>
{
	@Override
	public String convertToDatabaseColumn(AppSchema meta) 
	{
		return super.convertToDatabaseColumn(meta);
	}
	
	@Override
	public AppSchema convertToEntityAttribute(String dbData) 
	{
		return super.convertToEntityAttribute(dbData, AppSchema.class);
	}
	
}
