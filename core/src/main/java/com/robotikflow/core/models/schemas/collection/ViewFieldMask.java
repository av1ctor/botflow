package com.robotikflow.core.models.schemas.collection;

import java.util.Set;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ViewFieldMask 
{
	@NotNull
	private Set<@NotBlank String> fields;
	@NotNull
	private String format;
	
	public Set<String> getFields() {
		return fields;
	}
	public void setFields(Set<String> fields) {
		this.fields = fields;
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
}
