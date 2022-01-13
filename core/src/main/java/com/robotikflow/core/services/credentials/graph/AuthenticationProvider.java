package com.robotikflow.core.services.credentials.graph;

import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.options.HeaderOption;
import com.robotikflow.core.services.credentials.MsGraphOAuth2Credential;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthenticationProvider
    implements IAuthenticationProvider 
{
    private final MsGraphOAuth2Credential credentialService;
 
    private static Logger logger = 
        LoggerFactory.getLogger(AuthenticationProvider.class);

    public AuthenticationProvider(
        final MsGraphOAuth2Credential credentialService)
    {
        this.credentialService = credentialService;
    }
    
    @Override
    public void authenticateRequest(
        IHttpRequest request) 
    {
        try 
        {
            var accessToken = credentialService.getAccessToken();
            request.getHeaders().add(
                new HeaderOption("Authorization", "bearer " + accessToken));
        } 
        catch (Exception e) 
        {
            logger.error("Token de acesso da API do MS Graph inválido", e);
        }
    }
}