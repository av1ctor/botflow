package com.robotikflow.core.models.oauth2;

import java.time.ZonedDateTime;

public class OAuth2TokenResponse 
{
    private final String accessToken;
    private final String refreshToken;
    private final ZonedDateTime tokenExpiration;

    public OAuth2TokenResponse() {
        accessToken = null;
        refreshToken = null;
        tokenExpiration = null;
    }

    public OAuth2TokenResponse(
        final String accessToken, 
        final String refreshToken, 
        final ZonedDateTime tokenExpiration)
    {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenExpiration = tokenExpiration;
    }

    public String getAccessToken() {
        return accessToken;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public ZonedDateTime getTokenExpiration() {
        return tokenExpiration;
    }
}
