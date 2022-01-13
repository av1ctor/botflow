package com.robotikflow.core.services.credentials;

import com.robotikflow.core.interfaces.ICredentialService;
import com.robotikflow.core.models.entities.Credential;

public class KeySecretCredential 
    implements ICredentialService
{
    public static final String name = "keySecretCredential";

    private Credential credential;

    @Override
    public void initialize(
        Credential credential) 
        throws Exception 
    {
        this.credential = credential;
    }

    @Override
    public Object getClient() 
        throws Exception 
    {
        return null;
    }

    @Override
    public void authenticate() 
        throws Exception 
    {
    }

    @Override
    public Credential getCredential() 
    {
        return credential;
    }
}
