package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionAutomationField;

public class CollectionAutomationFieldResponse 
	extends CollectionAutomationResponse 
{
	private final String schema;

	public CollectionAutomationFieldResponse(
		final CollectionAutomationField automacao)
	{
		super(automacao);

		schema = automacao.getSchema() != null?
			automacao.getSchema():
			null;
	}

	public String getSchema() {
		return schema;
	}
}
