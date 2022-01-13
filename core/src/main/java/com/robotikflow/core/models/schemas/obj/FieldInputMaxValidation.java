package com.robotikflow.core.models.schemas.obj;

import javax.validation.constraints.NotNull;

public class FieldInputMaxValidation 
    extends FieldInputValidation
{
    @NotNull
    private Object value;

	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
}
