package com.robotikflow.core.models.schemas.collection;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ReportFilterForm 
{
	@Valid
	@NotNull
	private Map<@NotBlank String, FilterField> fields;
    
    public Map<String, FilterField> getFields() {
		return fields;
	}
	public void setFields(Map<String, FilterField> fields) {
		this.fields = fields;
    }
}
