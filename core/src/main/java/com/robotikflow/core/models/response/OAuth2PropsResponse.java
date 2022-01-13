package com.robotikflow.core.models.response;

import com.robotikflow.core.models.oauth2.OAuth2Props;

public class OAuth2PropsResponse 
{
    private final String clientId;    
    private final String clientSecret;
    private final String redirectUri;

    public OAuth2PropsResponse(
		final OAuth2Props props)
    {
        clientId = props.getClientId();
        clientSecret = props.getClientSecret();
        redirectUri = props.getRedirectUri();
    }

	public String getClientId() {
		return clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public String getRedirectUri() {
		return redirectUri;
	}
}
