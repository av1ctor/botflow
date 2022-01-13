package com.robotikflow.api.server.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.filters.CredentialFilter;
import com.robotikflow.core.models.request.CredentialRequest;
import com.robotikflow.core.models.response.CredentialResponse;
import com.robotikflow.core.models.response.OAuth2PropsResponse;
import com.robotikflow.core.models.response.OAuth2UrlResponse;
import com.robotikflow.core.services.CredentialService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class CredentialController 
	extends BaseController 
{
	@Autowired
	private CredentialService credentialService;
	
	private static HashSet<String> validColumns = 
		new HashSet<>(Arrays.asList
		( 
			"schema.name",
			"createdAt"
		));

	@GetMapping("/config/credentials")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Return the workspace's credentials")
	List<CredentialResponse> list(
		@RequestParam(value = "filters", required = false) String filters,	
		@RequestParam(value = "withSchema", required = false) Boolean withSchema,
		@RequestParam(value = "withUser", required = false) Boolean withUser,
		Pageable pageable) 
		throws Exception
	{
		pageable = validateSorting(
			pageable, validColumns, "createdAt", Direction.DESC);
		
		var ua = getUserSession();

		List<Credential> credentials = null;
		
		if(filters == null || filters.length() == 0)
		{
			credentials = credentialService
				.findAllByWorkspace(ua.getWorkspace(), pageable);
		}
		else
		{
			credentials = credentialService.findAllByWorkspace(
				ua.getWorkspace(), buildFilters(filters, CredentialFilter.class), pageable);
		}
		
		return credentials.stream()
			.map(prov -> new CredentialResponse(prov, withSchema, withUser))
				.collect(Collectors.toList());
	}

	@PostMapping("/config/credentials")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Create a credential")
	CredentialResponse create(
		@RequestBody CredentialRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var credential = credentialService.create(
			req, ua.getUser(), ua.getWorkspace());
		
		return new CredentialResponse(credential, true, true);
	}

	@PatchMapping("/config/credentials/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Update a credential")
	CredentialResponse update(
		@PathVariable String id,
		@RequestBody CredentialRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var credential = credentialService.update(
			id, req, ua.getUser(), ua.getWorkspace());
		
		return new CredentialResponse(credential, true, true);
	}

	@GetMapping("/config/credentials/{id}/oauth2")
	@ApiOperation(value = "Get credential's OAuth 2 props")
	OAuth2PropsResponse getProps(
		@PathVariable String id) 
		throws Exception
	{
		var ua = getUserSession();

		var props = credentialService.getOAuth2Props(
			id, ua.getWorkspace());
		
		return new OAuth2PropsResponse(props);
	}

	@GetMapping("/config/credentials/{id}/oauth2/url")
	@ApiOperation(value = "Get credential's OAuth 2 authorization URL")
	OAuth2UrlResponse getPropsUrl(
		@PathVariable String id) 
		throws Exception
	{
		var ua = getUserSession();

		var url = credentialService.getOAuth2AuthorizationUrl(
			id, ua.getWorkspace());
		
		return new OAuth2UrlResponse(url);
	}

	@PatchMapping("/config/credentials/{id}/oauth2/redeem")
	@ApiOperation(value = "Redeem OAuth 2 tokens")
	CredentialResponse redeem(
		@PathVariable String id,
		@RequestParam(value = "code", required = true) String code) 
		throws Exception
	{
		var ua = getUserSession();

		var credential = credentialService.redeemTokens(
			id, code, ua.getWorkspace());
		
		return new CredentialResponse(credential);
	}
}
