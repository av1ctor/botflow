package com.robotikflow.api.server.models.request;

import java.util.Map; 

import javax.validation.constraints.NotNull;

public class CollectionItemRequest
{
	@NotNull
    private Map<String, Object> vars;

	public Map<String, Object> getVars() {
		return vars;
	}

	public void setVars(Map<String, Object> vars) {
		this.vars = vars;
	}
}
