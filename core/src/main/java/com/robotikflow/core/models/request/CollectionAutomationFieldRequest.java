package com.robotikflow.core.models.request;

import javax.validation.constraints.NotNull;

public class CollectionAutomationFieldRequest 
	extends CollectionAutomationRequest
{
	@NotNull
	private String schema;
			
	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}
}
