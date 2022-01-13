package com.robotikflow.core.util.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import com.robotikflow.core.models.schemas.collection.CollectionSchema;

@Converter
public class CollectionSchemaToJsonConverter 
	extends ObjectToJsonConverter<CollectionSchema> 
	implements AttributeConverter<CollectionSchema, String>
{
	@Override
	public String convertToDatabaseColumn(CollectionSchema meta) 
	{
		return super.convertToDatabaseColumn(meta);
	}
	
	@Override
	public CollectionSchema convertToEntityAttribute(String dbData) 
	{
		return super.convertToEntityAttribute(dbData, CollectionSchema.class);
	}
	
}
