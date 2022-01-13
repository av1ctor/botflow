package com.robotikflow.core.models.schemas.obj;

import javax.validation.constraints.NotNull;

public class FieldTextareaInput
	extends FieldInput
{
    @NotNull
    private int rows;

    public FieldTextareaInput()
    {
        super(FieldInputType.textarea);
    }

	public int getRows() {
		return rows;
	}
	public void setRows(int rows) {
		this.rows = rows;
	}
}
