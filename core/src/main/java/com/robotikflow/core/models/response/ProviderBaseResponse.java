package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.ProviderSchema;

public class ProviderBaseResponse
    extends ObjBaseResponse<ProviderSchema>
{
	public ProviderBaseResponse(Provider provider) 
    {
		super(provider);
	}
}