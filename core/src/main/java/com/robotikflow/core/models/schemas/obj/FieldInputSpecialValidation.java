package com.robotikflow.core.models.schemas.obj;

import javax.validation.constraints.NotNull;

public class FieldInputSpecialValidation 
    extends FieldInputValidation
{
    @NotNull
    private int min;

	public int getMin() {
		return min;
	}
	public void setMin(int min) {
		this.min = min;
	}
}
