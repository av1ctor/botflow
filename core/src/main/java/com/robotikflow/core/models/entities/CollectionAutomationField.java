package com.robotikflow.core.models.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.schemas.collection.automation.AutomationSchema;
import com.robotikflow.core.util.converters.AutomationSchemaToJsonConverter;

@Entity
@Table(name = "collections_automations_fields")
public class CollectionAutomationField 
	extends CollectionAutomation
{
	@Column(name = "schema", insertable = false, updatable = false)
	@Convert(converter = AutomationSchemaToJsonConverter.class)
	private AutomationSchema schemaObj;

	@NotNull
	private String schema;
    
	public CollectionAutomationField()
	{
		super(CollectionAutomationType.FIELD);
	}

	public CollectionAutomationField(
		final CollectionAutomationField cac, 
		final CollectionWithSchema collection)
	{
		super(cac, collection);
		this.schema = cac.schema;
	}
	
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public AutomationSchema getSchemaObj() {
		return schemaObj;
	}

	public void setSchemaObj(AutomationSchema schemaObj) {
		this.schemaObj = schemaObj;
	}
}
