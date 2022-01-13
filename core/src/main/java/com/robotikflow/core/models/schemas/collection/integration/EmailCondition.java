package com.robotikflow.core.models.schemas.collection.integration;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class EmailCondition 
{
	@NotNull
	private EmailConditionType type; 
	@NotBlank
	private String value;

	public EmailConditionType getType() {
		return type;
	}

	public void setType(EmailConditionType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
