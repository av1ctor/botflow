package com.robotikflow.core.models.schemas.collection.integration;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrFieldOrValue;

public class IntegrationActionSchema 
{
	@Valid
	private Map<@NotBlank String, ScriptOrFunctionOrFieldOrValue> mapping;

	public Map<String, ScriptOrFunctionOrFieldOrValue> getMapping() {
		return mapping;
	}

	public void setMapping(Map<String, ScriptOrFunctionOrFieldOrValue> mapping) {
		this.mapping = mapping;
	}
}
