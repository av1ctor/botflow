package com.robotikflow.api.server.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.filters.ProviderFilter;
import com.robotikflow.core.models.request.ProviderRequest;
import com.robotikflow.core.models.response.ProviderResponse;
import com.robotikflow.core.models.response.ProviderSchemaResponse;
import com.robotikflow.core.services.ProviderService;

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
public class ProviderController 
	extends BaseController 
{
	@Autowired
	private ProviderService providerService;
	
	private static HashSet<String> validColumns = 
		new HashSet<>(Arrays.asList
		( 
			"schema.name",
			"createdAt"
		));

	@GetMapping("/config/providers/schemas")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Return the workspace's providers schemas")
	List<ProviderSchemaResponse> listSchemas(
		Pageable pageable)
	{
		pageable = validateSorting(
			pageable, validColumns, "name", Direction.ASC);
		
		var schemas = providerService
			.findAllSchemas(pageable);
		
		return schemas.stream()
			.map(prov -> new ProviderSchemaResponse(prov))
				.collect(Collectors.toList());
	}

	@GetMapping("/config/providers")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Return the workspace's providers")
	List<ProviderResponse> list(
		@RequestParam(value = "filters", required = false) String filters,	
		@RequestParam(value = "withSchema", required = false) Boolean withSchema,
		@RequestParam(value = "withUser", required = false) Boolean withUser,
		Pageable pageable) 
		throws Exception
	{
		pageable = validateSorting(
			pageable, validColumns, "createdAt", Direction.DESC);
		
		var ua = getUserSession();

		List<Provider> providers = null;
		
		if(filters == null || filters.length() == 0)
		{
			providers = providerService
				.findAllByWorkspace(ua.getWorkspace(), pageable);
		}
		else
		{
			providers = providerService.findAllByWorkspace(
				ua.getWorkspace(), buildFilters(filters, ProviderFilter.class), pageable);
		}
		
		return providers.stream()
			.map(prov -> new ProviderResponse(prov, withSchema, withUser))
				.collect(Collectors.toList());
	}

	@PostMapping("/config/providers")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Create a provider")
	ProviderResponse create(
		@RequestBody ProviderRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var provider = providerService.create(
			req, ua.getUser(), ua.getWorkspace());
		
		return new ProviderResponse(provider, true, true);
	}

	@PatchMapping("/config/providers/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Update a provider")
	ProviderResponse update(
		@PathVariable String id,
		@RequestBody ProviderRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var provider = providerService.update(
			id, req, ua.getUser(), ua.getWorkspace());
		
		return new ProviderResponse(provider, true, true);
	}
}
