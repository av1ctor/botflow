package com.robotikflow.core.models.schemas.obj;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Ref 
{
    @NotBlank
	private String type;
	@Valid
    private Map<@NotBlank String, @NotBlank Object> filters;

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Map<@NotBlank String, @NotNull Object> getFilters() {
		return filters;
	}
	public void setFilters(Map<@NotBlank String, @NotNull Object> filters) {
		this.filters = filters;
	}
}
