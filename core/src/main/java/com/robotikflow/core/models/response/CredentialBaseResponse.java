package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.entities.CredentialSchema;

public class CredentialBaseResponse
    extends ObjBaseResponse<CredentialSchema>
{
	public CredentialBaseResponse(
		final Credential credential) 
    {
		super(credential);
	}
}