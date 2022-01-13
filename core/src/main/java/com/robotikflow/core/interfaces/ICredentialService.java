package com.robotikflow.core.interfaces;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.oauth2.OAuth2Props;
import com.robotikflow.core.models.oauth2.OAuth2TokenResponse;

import org.springframework.http.HttpEntity;

public interface ICredentialService 
    extends IObjService<Credential>
{
    Object getClient()
        throws Exception;

    void authenticate()
        throws Exception;
            
    default void authenticate(
        HttpEntity<?> entity) 
        throws Exception
    {
    }

    default OAuth2Props getOAuth2Props()
    {
        throw new ObjException("Unsupported method");
    }

    default String getOAuth2AuthUrl()
    {
        throw new ObjException("Unsupported method");
    }

    default OAuth2TokenResponse redeemTokens(
        final String code) 
        throws Exception
    {
        throw new ObjException("Unsupported method");
    }

    default String getAccessToken()
        throws Exception
    {
        throw new ObjException("Unsupported method");
    }

    Credential getCredential();
}
