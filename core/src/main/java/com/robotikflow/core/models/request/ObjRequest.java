package com.robotikflow.core.models.request;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ObjRequest 
	extends ObjBaseRequest
{
	@NotNull
	private String schemaId;
	@Valid
	private Map<String, Object> fields;

	public String getSchemaId() {
		return schemaId;
	}
	public void setSchemaId(String schemaId) {
		this.schemaId = schemaId;
	}
	public Map<String, Object> getFields() {
		return fields;
	}
	public void setFields(Map<String, Object> fields) {
		this.fields = fields;
	}
}
