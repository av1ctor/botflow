package com.robotikflow.core.services.credentials;

import java.time.ZonedDateTime;

import javax.persistence.OptimisticLockException;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.interfaces.ICredentialService;
import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.repositories.CredentialRepository;
import com.robotikflow.core.models.oauth2.OAuth2Props;
import com.robotikflow.core.models.oauth2.OAuth2AuthResponse;
import com.robotikflow.core.models.oauth2.OAuth2TokenResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import reactor.netty.http.client.HttpClient;

public class GenericOAuth2Credential 
    implements ICredentialService
{
    public static final String name = "genericOAuth2";

    @Autowired
    private CredentialRepository credentialRepository;
    @Autowired
    private Environment env;

    protected Credential credential;
    protected OAuth2Props props;
    protected WebClient client;

    @Override
    public void initialize(
        final Credential credential)
    {
        initialize(credential, new OAuth2Props(credential.getFields()));
    }

    protected void initialize(
        final Credential credential,
        final OAuth2Props props)
    {
        this.props = props;
        this.credential = credential;
		client = WebClient.builder()
			.clientConnector(new ReactorClientHttpConnector(
                HttpClient.create().followRedirect(true)))
            .build();
    }
    
    protected void setProps(
        final String base) 
    {
        props = new OAuth2Props(credential.getFields());
        if(props.getClientId() == null || props.getClientId().length() == 0)
        {
            props.setClientId(env.getProperty(base + ".client_id"));
            props.setClientSecret(env.getProperty(base + ".client_secret"));
        }

        props.setRedirectUri(env.getProperty(base + ".redirect_uri"));
	}

    @Override
    public Credential getCredential() {
        return credential;
    }

    @Override
    public OAuth2Props getOAuth2Props() 
    {
        return props;
    }

    @Override
    public Object getClient()
        throws Exception
    {
		return client;	
    }

    @Override
    public void authenticate()
        throws Exception 
    {
        client.head().header("Authorization", "bearer " + getAccessToken());
    }

    @Override
    public void authenticate(
        final HttpEntity<?> request) 
        throws Exception
    {
        request.getHeaders().add("Authorization", "bearer " + getAccessToken());
    }
    
    @Override
    public String getOAuth2AuthUrl() 
    {
        return String.format(
            "%s?client_id=%s&response_type=code&scope=%s&access_type=offline&redirect_uri=%s", 
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
    public String getAccessToken()
        throws Exception
    {
        if(props.getAccessToken() == null)
        {
            throw new ObjException("The OAuth2 access token is null");
        }
        
        if(ZonedDateTime.now()
			.isAfter(props.getTokenExpiration().minusMinutes(5)))
		{
			try 
			{
				// try refreshing the token
                var res = refresh();
				
                // try saving to the DB
                if(res.getRefreshToken() != null)
				{
                    credential.getFields().put("refreshToken", res.getRefreshToken());
				}
                credential.getFields().put("accessToken", res.getAccessToken());
                credential.getFields().put("tokenExpiration", res.getTokenExpiration().toString());
                credentialRepository.save(credential);
			}
			catch(WebClientResponseException e)
            {
                // token was probably updated by another service/thread, reload it
                var current = credentialRepository
                    .findById(credential.getId()).get();

                // if the access token didn't change, some other error happened when calling OAuth2
                if((props.getAccessToken()).equals(
                    (current.getFields().getString("accessToken"))))
                {
                    throw new ObjException(
                        String.format(
                            "Invalid OAuth2 access token:\n%s", 
                            ((WebClientResponseException)e).getResponseBodyAsString()), 
                        e);
                }

                credential = current;
            }
            catch(OptimisticLockException|ObjectOptimisticLockingFailureException e) 
			{
                // token was updated by another service/thread, reload it
                credential = credentialRepository
                    .findById(credential.getId()).get();
			}
            finally
            {
                props.setAccessToken(credential.getFields().getString("accessToken"));
                props.setRefreshToken(credential.getFields().getString("refreshToken"));
                props.setTokenExpiration(ZonedDateTime.parse(credential.getFields().getString("tokenExpiration")));
            }
		}

        return props.getAccessToken();
    }
		
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
