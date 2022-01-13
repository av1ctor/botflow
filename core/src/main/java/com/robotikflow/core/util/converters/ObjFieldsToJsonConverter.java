package com.robotikflow.core.util.converters;

import javax.persistence.Converter;

import com.robotikflow.core.models.misc.ObjFields;

@Converter(autoApply = true)
public class ObjFieldsToJsonConverter
	extends MapStringObjectToJsonConverter
{
	public String convertToDatabaseColumn(ObjFields meta) 
	{
		return super.convertToDatabaseColumn(meta.getFields());
	}
	
	@Override
	public ObjFields convertToEntityAttribute(String dbData) 
	{
		var map = super.convertToEntityAttribute(dbData);
		return new ObjFields(map);
	}
	
}
