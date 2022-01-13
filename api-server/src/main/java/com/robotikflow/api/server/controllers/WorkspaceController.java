package com.robotikflow.api.server.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.robotikflow.api.server.models.response.AuthLoginResponse;
import com.robotikflow.api.server.models.response.WorkspaceLogResponse;
import com.robotikflow.core.exception.WorkspaceException;
import com.robotikflow.core.models.UserSession;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.WorkspaceLog;
import com.robotikflow.core.models.filters.WorkspaceFilter;
import com.robotikflow.core.models.filters.WorkspaceLogFilter;
import com.robotikflow.core.models.request.AccessType;
import com.robotikflow.core.models.response.WorkspaceResponse;
import com.robotikflow.core.web.security.JwtTokenUtil;
import com.robotikflow.core.web.security.JwtUserFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class WorkspaceController 
	extends BaseController 
{
	@Autowired
    private JwtTokenUtil jwtTokenUtil;

	private static HashSet<String> logsSortColumns = new HashSet<>(Arrays.asList
	( 
		"date"
	));

	private static HashSet<String> sortColumns = new HashSet<>(Arrays.asList
	( 
		"id", "name"
	));
	
	@GetMapping("/workspaces")
	@ApiOperation(value = "Find all user's workspaces")
	List<WorkspaceResponse> findAll(
		@RequestParam(value = "filters", required = false) String filters,
		Pageable pageable) throws Exception
	{
		pageable = validateSorting(pageable, sortColumns, "name", Direction.ASC);
		
		var ua = getUserSession();

		List<Workspace> workspaces = null;

		if(filters == null || filters.length() == 0)
		{
			workspaces = workspaceService.findAllByUser(ua.getUser(), pageable);
		}
		else
		{
			workspaces = workspaceService.findAllByUser(
				ua.getUser(), pageable, buildFilters(filters, WorkspaceFilter.class));
		}
		
		return workspaces.stream()
			.map(w -> new WorkspaceResponse(w, ua.getUser()))
				.collect(Collectors.toList());
	}
	
	@GetMapping("/workspaces/{id}")
	@ApiOperation(value = "Find a single workspace by id")
	WorkspaceResponse findOne(
		@PathVariable String id) 
	{
		var ua = getUserSession();
		
		var workspace = workspaceService.findByPubId(id);
		if(workspace == null)
		{
			throw new WorkspaceException("Área de trabalho inexistente");
		}
		
		workspaceService.validarAcesso(AccessType.READ, workspace, ua.getUser());
		
		return new WorkspaceResponse(workspace);
	}

	@GetMapping("/workspaces/switch/{id}")
	@ApiOperation(value = "Switch to another workspace")
	AuthLoginResponse switchTo(
		@PathVariable String id) 
	{
		var workspace = workspaceService.findByPubId(id);
		if(workspace == null)
		{
			throw new WorkspaceException("Área de trabalho inexistente");
		}
		
		var ua = getUserSession();
		
		workspaceService.validarAcesso(AccessType.READ, workspace, ua.getUser());

    	ua = new UserSession(ua.getUser(), workspace);
		var jwtUser = JwtUserFactory.create(ua);

        var pair = jwtTokenUtil.generateToken(jwtUser);
		
		return new AuthLoginResponse(ua, pair.getKey(), pair.getValue());
	}

	@PatchMapping("/workspaces/accept/{id}")
	@ApiOperation(value = "Accept invitation to join another workspace")
	WorkspaceResponse accept(
		@PathVariable String id)
	{
		var ua = getUserSession();
		
		var workspace = workspaceService.findByPubId(id);
		if(workspace == null)
		{
			throw new WorkspaceException("Área de trabalho inexistente");
		}

		var user = userService.aceitarConvite(
			ua.getUser(), workspace.getPubId());

		return new WorkspaceResponse(workspace, user.getUser());
	}

	@PatchMapping("/workspaces/refuse/{id}")
	@ApiOperation(value = "Refuse invitation to join another workspace")
	WorkspaceResponse refuse(
		@PathVariable String id)
	{
		var ua = getUserSession();
		
		var workspace = workspaceService.findByPubId(id);
		if(workspace == null)
		{
			throw new WorkspaceException("Área de trabalho inexistente");
		}

		userService.recusarConvite(ua.getUser(), workspace);

		return new WorkspaceResponse(workspace);
	}

	@PatchMapping("/workspaces/leave/{id}")
	@ApiOperation(value = "Leave a workspace")
	WorkspaceResponse leave(
		@PathVariable String id)
	{
		var ua = getUserSession();
		
		var workspace = workspaceService.findByPubId(id);
		if(workspace == null)
		{
			throw new WorkspaceException("Área de trabalho inexistente");
		}

		userService.abandonar(ua.getUser(), workspace);

		return new WorkspaceResponse(workspace);
	}

	@GetMapping("/workspaces/{id}/logs")
	@ApiOperation(value = "Find workspace's logs")
	List<WorkspaceLogResponse> findAllLogs(
		@PathVariable String id,
		@RequestParam(value = "filters", required = false) String filters,
		Pageable pageable) throws Exception
	{
		pageable = validateSorting(pageable, logsSortColumns, "date", Direction.ASC);
		
		var ua = getUserSession();

		var workspace = workspaceService.findByPubId(id);
		if(workspace == null)
		{
			throw new WorkspaceException("Área de trabalho inexistente");
		}

		workspaceService.validarAcesso(
			AccessType.CREATE, workspace, ua.getUser());
		
		List<WorkspaceLog> logs;
		if(filters == null || filters.length() == 0)
		{
			logs = workspaceService.findAllLogs(
				workspace, pageable);
		}
		else
		{
			logs = workspaceService.findAllLogs(
				workspace, pageable, buildFilters(filters, WorkspaceLogFilter.class));
		}
			
		return logs.stream()
				.map(l -> new WorkspaceLogResponse(l, objMapper))
				.collect(Collectors.toList());
	}
}
