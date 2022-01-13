package com.robotikflow.core.models.schemas.obj;

import javax.validation.constraints.NotBlank;

public class FieldSelectInputOption 
{
    @NotBlank
    private String title;
    @NotBlank
    private Object value;
    
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
}
