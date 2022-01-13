package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.entities.CredentialSchema;

public class CredentialResponse
    extends ObjResponse<CredentialSchema>
{

	public CredentialResponse(
		final Credential cred) 
    {
		super(cred);
	}

	public CredentialResponse(
		final Credential cred,
		final boolean withSchema) 
    {
		super(cred, withSchema);
	}

	public CredentialResponse(
		final Credential cred,
		final boolean withSchema,
		final boolean withUser) 
    {
		super(cred, withSchema, withUser);
	}
}