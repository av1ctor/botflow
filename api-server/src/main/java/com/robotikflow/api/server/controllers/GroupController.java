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
import com.robotikflow.core.models.entities.Group;
import com.robotikflow.core.models.filters.GroupFilter;
import com.robotikflow.core.models.request.GroupMultiRequest;
import com.robotikflow.core.models.request.GroupRequest;
import com.robotikflow.core.models.request.AccessType;
import com.robotikflow.core.models.response.GroupResponse;
import com.robotikflow.core.services.GroupService;

import io.swagger.annotations.ApiOperation;
import io.undertow.util.BadRequestException;

@RestController
public class GroupController extends BaseController 
{
	@Autowired
	protected GroupService groupService;

	private static HashSet<String> sortColumns = new HashSet<>(Arrays.asList
	( 
		"id", "name", "createdAt", "updatedAt"
	));
	
	@GetMapping("/groups")
	@ApiOperation(value = "Encontrar os groups de usuários de um área de trabalho")
	List<GroupResponse> listar(
		@RequestParam(value = "filters", required = false) String filters,
		Pageable pageable) throws Exception
	{
		pageable = validateSorting(pageable, sortColumns, "name", Direction.ASC);
		
		var ua = getUserSession();

		//TODO: validar acesso
		
		List<Group> groups = null;
		if(filters == null || filters.length() == 0)
		{
			groups = groupService.findAllByWorkspace(
				ua.getWorkspace(), pageable);
		}
		else
		{
			groups = groupService.findAllByWorkspace(
				ua.getWorkspace(), pageable, buildFilters(filters, GroupFilter.class));
		}
		
		return groups.stream()
			.map(g -> new GroupResponse(g, (parent) -> groupService.findAllByParent(parent)))
			.collect(Collectors.toList());
	}

	@PostMapping("/groups")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Criar um group no área de trabalho")
	GroupResponse create(
		@Valid @RequestBody GroupRequest req
	)
	{
		var ua = getUserSession();
		
		groupService.validarAcesso(AccessType.CREATE, ua.getUser(), ua.getWorkspace());
		
		return new GroupResponse(
			groupService.criar(req, ua.getUser(), ua.getWorkspace()), 
			(parent) -> groupService.findAllByParent(parent));
	}

	@PatchMapping("/groups/{id}")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Atualizar um group do área de trabalho")
	GroupResponse update(
		@PathVariable String id,
		@Valid @RequestBody GroupRequest req
	) throws BadRequestException
	{
		var ua = getUserSession();

		var group = groupService
			.findByPubIdAndWorkspace(id, ua.getWorkspace());
		if(group == null)
		{
			throw new BadRequestException("Group inexistente");
		}
		
		groupService.validarAcesso(AccessType.UPDATE, group, ua.getUser(), ua.getWorkspace());
		
		return new GroupResponse(
			groupService.atualizar(group, req, ua.getUser(), ua.getWorkspace()), 
			(parent) -> groupService.findAllByParent(parent));
	}

	@DeleteMapping("/groups/{id}")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Remover um group do área de trabalho")
	EmptyResponse delete(
		@PathVariable String id
	) throws BadRequestException
	{
		var ua = getUserSession();

		var group = groupService
			.findByPubIdAndWorkspace(id, ua.getWorkspace());
		if(group == null)
		{
			throw new BadRequestException("Group inexistente");
		}
			
		groupService.validarAcesso(AccessType.DELETE, group, ua.getUser(), ua.getWorkspace());
		
		groupService.remover(group, ua.getUser(), ua.getWorkspace());

		return new EmptyResponse();
	}

	@PatchMapping("/groups")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Realizar operações em lote nos groups do área de trabalho")
	List<GroupResponse> multiOp(
		@Valid @RequestBody List<GroupMultiRequest> req
	)
	{
		var ua = getUserSession();
		
		groupService.validarAcesso(AccessType.CREATE, ua.getUser(), ua.getWorkspace());

		List<Group> res = groupService.multiOp(req, ua.getUser(), ua.getWorkspace());
		
		return res.stream()
			.map(g -> 
				new GroupResponse(
					g, 
					(parent) -> groupService.findAllByParent(parent)))
			.collect(Collectors.toList());
	}

}
