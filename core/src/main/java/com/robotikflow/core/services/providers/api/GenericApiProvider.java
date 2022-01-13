package com.robotikflow.core.services.providers.api;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.factories.CredentialServiceFactory;
import com.robotikflow.core.interfaces.ICredentialService;
import com.robotikflow.core.interfaces.IGenericApiProviderService;
import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.entities.Provider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class GenericApiProvider 
    implements IGenericApiProviderService
{
    public static final String name = "genericApiProvider";
    
    @Autowired
    protected CredentialServiceFactory credentialServiceFactory;
    
    private ICredentialService credentialService;
    private RestTemplate client;
    private Provider provider;
    
    @Override
    public void initialize(
        final Provider provider) 
        throws Exception
    {
        this.provider = provider;
        
        var cred = provider.getFields().getString("credential");
        if(cred != null)
        {
            credentialService = credentialServiceFactory.buildByPubId(
                cred, provider.getWorkspace());
        }
        else
        {
            credentialService = null;
        }

        client = new RestTemplate();

        client.getMessageConverters()
            .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
    }

    @Override
    public Credential getCredential() {
        return credentialService.getCredential();
    }

    @Override
    public Provider getProvider() {
        return provider;
    }
    
    @Override
    public <T> String request(
        final URI url,
        final HttpMethod method,
        final MediaType requestType,
        final MediaType responseType,
        final HttpHeaders headers,
        final T body) 
        throws Exception
    {
        headers.setContentType(requestType);
        headers.setAccept(Arrays.asList(responseType));

        var req = body != null?
            new HttpEntity<>(body, headers):
            new HttpEntity<>(headers);

        credentialService.authenticate(req);

        var res = client.exchange(url, method, req, String.class);
        
        if(!res.getStatusCode().is2xxSuccessful())
        {
            throw new ObjException(String.format("Request failed: %d", res.getStatusCodeValue()));
        }
        
        return res.getBody();
    }


}
