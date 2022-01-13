package com.robotikflow.core.services.credentials;

import java.time.ZonedDateTime;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.oauth2.OAuth2AuthResponse;
import com.robotikflow.core.models.oauth2.OAuth2TokenResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class GoogleOAuth2Credential 
    extends GenericOAuth2Credential
{
    public static final String name = "googleOAuth2";

    private GoogleCredential googleCredential;

    @Override
    public void initialize(
        final Credential credential) 
    {
		super.initialize(credential);
		setProps("oauth2.google");

        googleCredential = new GoogleCredential.Builder()
			.setTransport(new NetHttpTransport())
			.setJsonFactory(JacksonFactory.getDefaultInstance())
			.setClientSecrets(
				props.getClientId(), 
				props.getClientSecret())
			.build();
    }
    
    @Override
    public GoogleCredential getClient()
    {
        return googleCredential;
    }

    @Override
    public void authenticate() 
        throws Exception
    {
        googleCredential.setAccessToken(getAccessToken());
    }
    
    @Override
    public String getOAuth2AuthUrl() 
    {
        return String.format(
            "%s?client_id=%s&response_type=code&scope=%s&access_type=offline&prompt=select_account consent&redirect_uri=%s", 
            props.getAuthUrl(), 
            props.getClientId(), 
            props.getScopes(),
			props.getRedirectUri());
    }

	@Override
	public OAuth2TokenResponse redeemTokens(
        final String code)
        throws Exception 
    {
        var res = new GoogleAuthorizationCodeTokenRequest(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(), 
            props.getClientId(), 
            props.getClientSecret(), 
            code, 
            props.getRedirectUri()
        ).execute();

        return new OAuth2TokenResponse(
            res.getAccessToken(), 
            res.getRefreshToken(), 
            ZonedDateTime.now().plusSeconds(res.getExpiresInSeconds())
        );
	}

    @Override
    protected OAuth2TokenResponse refresh() 
        throws WebClientResponseException
    {
        var res = client.post()
            .uri(GoogleOAuthConstants.TOKEN_SERVER_URL)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(BodyInserters
                .fromFormData("client_id", props.getClientId())
                    .with("client_secret", props.getClientSecret())
                    .with("refresh_token", props.getRefreshToken())
                    .with("grant_type", "refresh_token"))
            .retrieve() 
            .bodyToFlux(OAuth2AuthResponse.class)
            .blockLast();

        return new OAuth2TokenResponse(
            res.getAccess_token(), 
            res.getRefresh_token(), 
            ZonedDateTime.now().plusSeconds(res.getExpires_in())
        );
    }	    
    
}
