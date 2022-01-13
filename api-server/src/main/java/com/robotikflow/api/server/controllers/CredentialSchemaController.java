package com.robotikflow.api.server.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.robotikflow.core.models.request.CredentialSchemaRequest;
import com.robotikflow.core.models.response.CredentialSchemaResponse;
import com.robotikflow.core.services.CredentialSchemaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class CredentialSchemaController 
	extends BaseController 
{
	@Autowired
	private CredentialSchemaService credentialSchemaService;
	
	private static HashSet<String> validColumns = 
		new HashSet<>(Arrays.asList
		( 
			"name",
			"createdAt"
		));

	@GetMapping("/config/credentials/schemas")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Return the credentials schemas")
	List<CredentialSchemaResponse> list(
		Pageable pageable)
	{
		pageable = validateSorting(
			pageable, validColumns, "name", Direction.ASC);
		
		var schemas = credentialSchemaService
			.findAll(pageable);
		
		return schemas.stream()
			.map(prov -> new CredentialSchemaResponse(prov))
				.collect(Collectors.toList());
	}

	@PostMapping("/config/credentials/schemas")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR')")
	@ApiOperation(value = "Create a credential schema")
	CredentialSchemaResponse create(
		@RequestBody CredentialSchemaRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var schema = credentialSchemaService.create(
			req, ua.getUser());
		
		return new CredentialSchemaResponse(schema);
	}

	@PatchMapping("/config/credentials/schemas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR')")
	@ApiOperation(value = "Update a credential schema")
	CredentialSchemaResponse update(
		@PathVariable String id,
		@RequestBody CredentialSchemaRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var schema = credentialSchemaService.update(
			id, req, ua.getUser(), ua.getWorkspace());
		
		return new CredentialSchemaResponse(schema);
	}
}
