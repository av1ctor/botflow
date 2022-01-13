package com.robotikflow.core.models.schemas.collection;

import com.robotikflow.core.models.nosql.FilterOperator;

public class AuthColumn 
{
	private String name;
	private FilterOperator op;
	private Object value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FilterOperator getOp() {
		return op;
	}

	public void setOp(FilterOperator op) {
		this.op = op;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
}
