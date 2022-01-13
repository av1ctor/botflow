package com.robotikflow.api.server.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.robotikflow.core.models.request.ActivitySchemaRequest;
import com.robotikflow.core.models.response.ActivitySchemaResponse;
import com.robotikflow.core.services.ActivitySchemaService;

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
public class ActivitySchemaController 
	extends BaseController 
{
	@Autowired
	private ActivitySchemaService activitySchemaService;
	
	private static HashSet<String> validColumns = 
		new HashSet<>(Arrays.asList
		( 
			"name",
			"createdAt"
		));

	@GetMapping("/config/activities/schemas")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Return the activities schemas")
	List<ActivitySchemaResponse> list(
		Pageable pageable)
	{
		pageable = validateSorting(
			pageable, validColumns, "name", Direction.ASC);
		
		var schemas = activitySchemaService
			.findAll(pageable);
		
		return schemas.stream()
			.map(prov -> new ActivitySchemaResponse(prov))
				.collect(Collectors.toList());
	}

	@PostMapping("/config/activities/schemas")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR')")
	@ApiOperation(value = "Create a activity schema")
	ActivitySchemaResponse create(
		@RequestBody ActivitySchemaRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var schema = activitySchemaService.create(
			req, ua.getUser());
		
		return new ActivitySchemaResponse(schema);
	}

	@PatchMapping("/config/activities/schemas/{id}")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR')")
	@ApiOperation(value = "Update a activity schema")
	ActivitySchemaResponse update(
		@PathVariable String id,
		@RequestBody ActivitySchemaRequest req) 
		throws Exception
	{
		var ua = getUserSession();

		var schema = activitySchemaService.update(
			id, req, ua.getUser(), ua.getWorkspace());
		
		return new ActivitySchemaResponse(schema);
	}
}
