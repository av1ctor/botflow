package com.robotikflow.core.services.credentials;

import java.io.InputStream;

import com.robotikflow.core.interfaces.ICredentialService;
import com.robotikflow.core.models.entities.Credential;

public class GoogleServiceAccountCredential
    implements ICredentialService
{
    public static final String name = "googleServiceAccount";

    private InputStream resource;
    
    @Override
    public void initialize(
        final Credential credential) 
    {
		var fields = credential.getFields();

        var filePath = (String)fields.get("filePath");
        resource = ClassLoader.getSystemClassLoader()
			.getResourceAsStream(filePath);
		if(resource == null)
		{
			resource = this.getClass().getClassLoader()
				.getResourceAsStream(filePath);
		}
    }

    @Override
    public Credential getCredential() {
        return null;
    }
    
    @Override
    public InputStream getClient()
    {
        return resource;
    }

    @Override
    public void authenticate() 
    {
    }
}
