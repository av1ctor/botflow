package com.robotikflow.core.models.request;

import javax.validation.constraints.NotBlank;

public class CollectionSchemaRequest 
{
	@NotBlank
	private String schema;
	private boolean returnSchema = false;

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public boolean isReturnSchema() {
		return returnSchema;
	}

	public void setReturnSchema(boolean returnSchema) {
		this.returnSchema = returnSchema;
	}
}
