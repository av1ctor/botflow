package com.robotikflow.core.models.response;

public class OAuth2UrlResponse 
{
    private final String url;    

    public OAuth2UrlResponse(
		final String url)
    {
        this.url = url;
    }

	public String getUrl() {
		return url;
	}
}
