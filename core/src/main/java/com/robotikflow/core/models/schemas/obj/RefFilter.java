package com.robotikflow.core.models.schemas.obj;

import javax.validation.constraints.NotNull;

public class RefFilter 
{
    private FilterOp op;
    @NotNull
    private Object value;
    
	public FilterOp getOp() {
		return op;
	}
	public void setOp(FilterOp op) {
		this.op = op;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
}
