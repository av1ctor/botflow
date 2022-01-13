package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.CredentialSchema;
import com.robotikflow.core.models.schemas.credential.CredentialMode;

public class CredentialSchemaBaseResponse
    extends ObjSchemaBaseResponse
{
	private final String vendor;
	private final CredentialMode mode;

	public CredentialSchemaBaseResponse(
		CredentialSchema cred) 
    {
		super(cred);
		vendor = cred.getVendor();
		mode = cred.getMode();
	}

	public String getVendor() {
		return vendor;
	}

	public CredentialMode getMode() {
		return mode;
	}
}