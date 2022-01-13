package com.robotikflow.core.models.oauth2;

import java.time.ZonedDateTime;

import com.robotikflow.core.models.misc.ObjFields;

public class OAuth2Props 
{
    private final String authUrl;
	private final String scopes;
    private final String tokenUrl;
	private String clientId;    
    private String clientSecret;
	private String redirectUri;
    private String authorizationCode;
    private String accessToken;
    private String refreshToken;
    private ZonedDateTime tokenExpiration;

    public OAuth2Props(ObjFields fields)
    {
        authUrl = fields.getString("authUrl");
		scopes = fields.getString("scopes");
        tokenUrl = fields.getString("tokenUrl");
        clientId = fields.getString("clientId");
        clientSecret = fields.getString("clientSecret");
        redirectUri = fields.getString("redirectUri");
        authorizationCode = fields.getString("authorizationCode");
        accessToken = fields.getString("accessToken");
        refreshToken = fields.getString("refreshToken");
        tokenExpiration = fields.getString("tokenExpiration") != null? 
			ZonedDateTime.parse(fields.getString("tokenExpiration")):
			ZonedDateTime.now();
    }

    public String getAuthUrl() {
		return authUrl;
	}
    public String getTokenUrl() {
		return tokenUrl;
	}
	public String getScopes() {
		return scopes;
	}
	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	public String getClientSecret() {
		return clientSecret;
	}
	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}
	public String getRedirectUri() {
		return redirectUri;
	}
	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}
	public String getAuthorizationCode() {
		return authorizationCode;
	}
	public void setAuthorizationCode(String authorizationCode) {
		this.authorizationCode = authorizationCode;
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	public ZonedDateTime getTokenExpiration() {
		return tokenExpiration;
	}
	public void setTokenExpiration(ZonedDateTime tokenExpiration) {
		this.tokenExpiration = tokenExpiration;
	}
       
}
