package com.robotikflow.api.server.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.robotikflow.api.server.models.response.EmptyResponse;
import com.robotikflow.api.server.models.response.AppResponse;
import com.robotikflow.core.exception.WorkspaceException;
import com.robotikflow.core.models.entities.App;
import com.robotikflow.core.models.filters.AppFilter;
import com.robotikflow.core.models.request.AccessType;
import com.robotikflow.core.models.request.AppRequest;
import com.robotikflow.core.services.apps.AppService;

import io.swagger.annotations.ApiOperation;

@RestController
public class AppController 
	extends BaseController 
{
	@Autowired
	protected AppService appService;
	
	private static HashSet<String> sortingColumns = new HashSet<>(Arrays.asList
	( 
		"id", "title", "createdAt"
	));
	
	@GetMapping("/apps")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Find all workspace's apps")
	List<AppResponse> findAll(
		@RequestParam(value = "filters", required = false) String filters,
		Pageable appable) throws Exception
	{
		appable = validateSorting(appable, sortingColumns, "createdAt", Direction.DESC);
		
		var ua = getUserSession();

		List<App> apps = null;

		if(filters == null || filters.length() == 0)
		{
			apps = appService.findAllByWorkspace(
				ua.getWorkspace(), appable);
		}
		else
		{
			apps = appService.findAllByWorkspace(
				ua.getWorkspace(),
				buildFilters(filters, AppFilter.class),
				appable);
		}
		
		return apps.stream()
			.map(a -> new AppResponse(a))
			.collect(Collectors.toList());
	}
	
	@GetMapping("/apps/{id}")
	@PreAuthorize("hasRole('ROLE_USER_PARTNER')")
	@ApiOperation(value = "Find a single app by id in the current workspace")
	AppResponse findOne(
		@PathVariable String id) 
	{
		var ua = getUserSession();
		
		var app = appService.findByPubIdAndWorkspace(id, ua.getWorkspace());
		
		appService.authorize(AccessType.READ, app, ua.getUser(), ua.getWorkspace());
		
		return new AppResponse(app);
	}

	@PostMapping("/apps")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Add an app to current workspace")
	AppResponse add(
		@Valid @RequestBody AppRequest req) 
		throws Exception
	{
		var ua = getUserSession();
		
		return new AppResponse(
			appService.create(req, ua.getUser(), ua.getWorkspace()));
	}

	@PatchMapping("/apps/{id}")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Update app")
	AppResponse update(
		@PathVariable String id,	
		@Valid @RequestBody AppRequest req) 
		throws Exception
	{
		var ua = getUserSession();
		
		var app = appService.findByPubIdAndWorkspace(id, ua.getWorkspace());
		if(app == null)
		{
			throw new WorkspaceException("Unknown app");
		}

		var res = appService.update(
			app, 
			req, 
			ua.getUser(),
			ua.getWorkspace());

		return new AppResponse(res);
	}
	
	@DeleteMapping("/apps/{id}")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Delete app")
	EmptyResponse remove(
		@PathVariable String id)
	{
		var ua = getUserSession();

		var app = appService.findByPubIdAndWorkspace(id, ua.getWorkspace());
		if(app == null)
		{
			throw new WorkspaceException("Unknown app");
		}

		appService.delete(app, ua.getUser());

		return new EmptyResponse();
	}
}
