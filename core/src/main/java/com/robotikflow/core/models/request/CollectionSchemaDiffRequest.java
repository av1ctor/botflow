package com.robotikflow.core.models.request;

import javax.validation.constraints.NotBlank;

public class CollectionSchemaDiffRequest 
{
	@NotBlank
	private String diff;
	private boolean returnSchema = false;

	public String getDiff() {
		return diff;
	}

	public void setDiff(String diff) {
		this.diff = diff;
	}

	public boolean isReturnSchema() {
		return returnSchema;
	}

	public void setReturnSchema(boolean returnSchema) {
		this.returnSchema = returnSchema;
	}
}
