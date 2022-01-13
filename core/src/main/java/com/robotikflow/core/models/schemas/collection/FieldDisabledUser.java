package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.nosql.FilterOperator;

public class FieldDisabledUser 
{
	@NotNull
	private FilterOperator op;
	@NotNull
	private String email;
	
	public FilterOperator getOp() {
		return op;
	}
	public void setOp(FilterOperator op) {
		this.op = op;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
