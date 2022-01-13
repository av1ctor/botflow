package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.ProviderSchema;

public class ProviderSchemaBaseResponse
    extends ObjSchemaBaseResponse
{
	private String vendor;

	public ProviderSchemaBaseResponse(ProviderSchema prov) 
    {
		super(prov);
		vendor = prov.getVendor();
	}

	public String getVendor() {
		return vendor;
	}
}