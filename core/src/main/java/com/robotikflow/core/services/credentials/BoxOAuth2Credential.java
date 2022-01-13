package com.robotikflow.core.services.credentials;

import com.robotikflow.core.models.entities.Credential;

public class BoxOAuth2Credential 
    extends GenericOAuth2Credential
{
    public static final String name = "boxOAuth2";

    @Override
    public void initialize(
        final Credential credential) 
    {
		super.initialize(credential);
		setProps("oauth2.box");
    }

    @Override
    public void authenticate()
        throws Exception 
    {
    }

    @Override
    public String getOAuth2AuthUrl() 
    {
        return String.format(
            "%s?client_id=%s&response_type=code&token_access_type=admin_readwrite&state=1234&redirect_uri=%s", 
            props.getAuthUrl(), 
            props.getClientId(), 
			props.getRedirectUri());
    }    
}
