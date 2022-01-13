package com.robotikflow.core.models.schemas.credential;

import javax.validation.constraints.NotBlank;

import com.robotikflow.core.models.schemas.obj.ObjSchema;

public class CredentialSchema 
	extends ObjSchema
{
	@NotBlank
	private String vendor;
	@NotBlank
	private CredentialMode mode;

	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	public CredentialMode getMode() {
		return mode;
	}

	public void setMode(CredentialMode mode) {
		this.mode = mode;
	}
}
