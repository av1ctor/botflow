package com.robotikflow.api.server.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.robotikflow.core.models.request.ObjSchemaRequest;
import com.robotikflow.core.models.response.ObjSchemaResponse;
import com.robotikflow.core.services.ObjSchemaService;

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
public class ObjSchemaController 
	extends BaseController 
{
	@Autowired
	private ObjSchemaService objSchemaService;
	
	private static HashSet<String> validColumns = 
		new HashSet<>(Arrays.asList
		( 
			"name",
			"createdAt"
		));

	@GetMapping("/config/objects/schemas")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Return the objects schemas")
	List<ObjSchemaResponse> list(
		Pageable pageable)
	{
		pageable = validateSorting(
			pageable, validColumns, "name", Direction.ASC);
		
		var schemas = objSchemaService
			.findAll(pageable);
		
		return schemas.stream()
			.map(prov -> new ObjSchemaResponse(prov))
				.collect(Collectors.toList());
	}

	@PostMapping("/config/objects/schemas")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR')")
	@ApiOperation(value = "Create a object schema")
	ObjSchemaResponse create(
		@RequestBody ObjSchemaRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var schema = objSchemaService.create(
			req, ua.getUser());
		
		return new ObjSchemaResponse(schema);
	}

	@PatchMapping("/config/objects/schemas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR')")
	@ApiOperation(value = "Update a object schema")
	ObjSchemaResponse update(
		@PathVariable String id,
		@RequestBody ObjSchemaRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var schema = objSchemaService.update(
			id, req, ua.getUser(), ua.getWorkspace());
		
		return new ObjSchemaResponse(schema);
	}
}
