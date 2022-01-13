package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.ProviderSchema;

public class ProviderResponse
    extends ObjResponse<ProviderSchema>
{
	public ProviderResponse(
		final Provider prov) 
    {
		super(prov);
	}

	public ProviderResponse(
		final Provider prov,
		final boolean withSchema) 
    {
		super(prov, withSchema);
	}

	public ProviderResponse(
		final Provider prov,
		final boolean withSchema,
		final boolean withUser) 
    {
		super(prov, withSchema, withUser);
	}
}