package com.robotikflow.core.services.credentials;

import java.time.ZonedDateTime;

import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.oauth2.OAuth2AuthResponse;
import com.robotikflow.core.models.oauth2.OAuth2TokenResponse;
import com.robotikflow.core.services.credentials.graph.AuthenticationProvider;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class MsGraphOAuth2Credential 
    extends GenericOAuth2Credential
{
    public static final String name = "msGraphOAuth2";

    private IGraphServiceClient graphCredential;

    @Override
    public void initialize(
        final Credential credential) 
    {
		super.initialize(credential);
		setProps("oauth2.ms.graph");

        graphCredential = GraphServiceClient
            .builder()
            .authenticationProvider(new AuthenticationProvider(this))
            .buildClient();	
    }
    
    @Override
    public IGraphServiceClient getClient()
    {
        return graphCredential;
    }
    
    @Override
    public void authenticate() 
    {
    }
        
	@Override
	public OAuth2TokenResponse redeemTokens(
        final String code) 
    {
        var res = client.post()
            .uri(props.getTokenUrl())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(BodyInserters
                .fromFormData("client_id", props.getClientId())
                    .with("redirect_uri", props.getRedirectUri())
                    .with("client_secret", props.getClientSecret())
                    .with("grant_type", "authorization_code")
                    .with("code", code))
            .retrieve()
            .bodyToFlux(OAuth2AuthResponse.class)
            .blockLast();

        return new OAuth2TokenResponse(
            res.getAccess_token(), 
            res.getRefresh_token(), 
            ZonedDateTime.now().plusSeconds(res.getExpires_in())
        );
	}
    
    @Override
    protected OAuth2TokenResponse refresh()
        throws WebClientResponseException 
    {
        var res = client.post()
            .uri(props.getTokenUrl())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(BodyInserters
                .fromFormData("client_id", props.getClientId())
                    .with("redirect_uri", props.getRedirectUri())
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
