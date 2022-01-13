package com.robotikflow.api.server.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.robotikflow.core.models.request.TriggerSchemaRequest;
import com.robotikflow.core.models.response.TriggerSchemaResponse;
import com.robotikflow.core.services.TriggerSchemaService;

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
public class TriggerSchemaController 
	extends BaseController 
{
	@Autowired
	private TriggerSchemaService triggerSchemaService;
	
	private static HashSet<String> validColumns = 
		new HashSet<>(Arrays.asList
		( 
			"name",
			"createdAt"
		));

	@GetMapping("/config/triggers/schemas")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Return the triggers schemas")
	List<TriggerSchemaResponse> list(
		Pageable pageable)
	{
		pageable = validateSorting(
			pageable, validColumns, "name", Direction.ASC);
		
		var schemas = triggerSchemaService
			.findAll(pageable);
		
		return schemas.stream()
			.map(prov -> new TriggerSchemaResponse(prov))
				.collect(Collectors.toList());
	}

	@PostMapping("/config/triggers/schemas")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR')")
	@ApiOperation(value = "Create a trigger schema")
	TriggerSchemaResponse create(
		@RequestBody TriggerSchemaRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var schema = triggerSchemaService.create(
			req, ua.getUser());
		
		return new TriggerSchemaResponse(schema);
	}

	@PatchMapping("/config/triggers/schemas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR')")
	@ApiOperation(value = "Update a trigger schema")
	TriggerSchemaResponse update(
		@PathVariable String id,
		@RequestBody TriggerSchemaRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var schema = triggerSchemaService.update(
			id, req, ua.getUser(), ua.getWorkspace());
		
		return new TriggerSchemaResponse(schema);
	}
}
