package com.robotikflow.core.services.credentials;

import java.time.ZonedDateTime;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.oauth2.OAuth2TokenResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClientResponseException;

public class DropboxOAuth2Credential 
    extends GenericOAuth2Credential
{
    public static final String name = "dropboxOAuth2";

    private static ObjectMapper objMapper = 
        new ObjectMapper();

    private static Logger logger = 
		LoggerFactory.getLogger(DropboxOAuth2Credential.class);

    @Override
    public void initialize(
        final Credential credential) 
    {
		super.initialize(credential);
		setProps("oauth2.dropbox");
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
            "%s?client_id=%s&response_type=code&token_access_type=offline&state=1234&redirect_uri=%s", 
            props.getAuthUrl(), 
            props.getClientId(), 
			props.getRedirectUri());
    }

	@Override
	public OAuth2TokenResponse redeemTokens(
        final String code) 
    {
        try 
        {
            var res = client.post()
                .uri(props.getTokenUrl())
                .headers(headers -> headers.setBasicAuth(props.getClientId(), props.getClientSecret()))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(BodyInserters
                    .fromFormData("redirect_uri", props.getRedirectUri())
                        .with("grant_type", "authorization_code")
                        .with("code", code))
                .retrieve()
                .bodyToFlux(String.class)
                .blockLast();

            var map = objMapper.readValue(res, new TypeReference<Map<String, String>>() {});

            return new OAuth2TokenResponse(
                map.get("access_token"), 
                map.get("refresh_token"), 
                ZonedDateTime.now().plusSeconds(Long.parseLong(map.get("expires_in")))
            );
        } 
        catch (Exception e) 
        {
            logger.error("Falha ao tentar autorização de acesso ao Microsoft Graph", e);
            return null;
        }
	}

    @Override
    protected OAuth2TokenResponse refresh()
        throws WebClientResponseException 
    {
        var res = client.post()
            .uri(props.getTokenUrl())
            .headers(headers -> headers.setBasicAuth(props.getClientId(), props.getClientSecret()))
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .body(BodyInserters
                .fromFormData("refresh_token", props.getRefreshToken())
                    .with("grant_type", "refresh_token"))
            .retrieve() 
            .bodyToFlux(String.class)
            .blockLast();

		try 
        {
			var map = objMapper.readValue(res, new TypeReference<Map<String, String>>() {});

            return new OAuth2TokenResponse(
                map.get("access_token"), 
                map.get("refresh_token"), 
                ZonedDateTime.now().plusSeconds(Long.parseLong(map.get("expires_in")))
            );
		} 
        catch (JsonProcessingException e) 
        {
			throw new WebClientResponseException(500, e.getMessage(), null, null, null, null);
		}
    }
}
