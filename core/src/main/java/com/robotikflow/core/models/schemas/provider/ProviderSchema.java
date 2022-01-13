package com.robotikflow.core.models.schemas.provider;

import javax.validation.constraints.NotBlank;

import com.robotikflow.core.models.schemas.obj.ObjSchema;

public class ProviderSchema 
	extends ObjSchema
{
	@NotBlank
	private String vendor;

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
}
