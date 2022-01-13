package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.nosql.FilterOperator;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrValue;

public class ReportFilterColumn 
{
	@NotNull
	private FilterOperator op;
	private ScriptOrFunctionOrValue value;
	private ScriptOrFunctionOrValue from;
	private ScriptOrFunctionOrValue to;
	
	public FilterOperator getOp() {
		return op;
	}
	public void setOp(FilterOperator op) {
		this.op = op;
	}
	public ScriptOrFunctionOrValue getValue() {
		return value;
	}
	public void setValue(ScriptOrFunctionOrValue value) {
		this.value = value;
	}
	public ScriptOrFunctionOrValue getfrom() {
		return from;
	}
	public void setFrom(ScriptOrFunctionOrValue from) {
		this.from = from;
	}
	public ScriptOrFunctionOrValue getTo() {
		return to;
	}
	public void setTo(ScriptOrFunctionOrValue to) {
		this.to = to;
	}
}
