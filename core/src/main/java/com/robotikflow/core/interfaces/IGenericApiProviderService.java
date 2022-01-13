package com.robotikflow.core.interfaces;

import java.net.URI;

import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.entities.Provider;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public interface IGenericApiProviderService
	extends IProviderService
{
    <T> String request(
        final URI url,
        final HttpMethod method,
        final MediaType requestType,
        final MediaType responseType,
        final HttpHeaders headers,
        final T body) 
        throws Exception;

    Credential getCredential();

    Provider getProvider();
}
