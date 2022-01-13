package com.robotikflow.api.server.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

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
import com.robotikflow.core.exception.UserException;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.filters.UserFilter;
import com.robotikflow.core.models.request.AccessType;
import com.robotikflow.core.models.request.UserRequest;
import com.robotikflow.core.models.response.UserResponse;

import io.swagger.annotations.ApiOperation;

@RestController
public class UserController extends BaseController 
{
	private static HashSet<String> colunasValidasDeOrdenacao = new HashSet<>(Arrays.asList
	( 
		"id", "email", "createdAt", "updatedAt"
	));
	
	@GetMapping("/users")
	@ApiOperation(value = "Find all workspace's users")
	List<UserResponse> findAll(
		@RequestParam(value = "filters", required = false) String filters,
		Pageable pageable) throws Exception
	{
		pageable = validateSorting(pageable, colunasValidasDeOrdenacao, "email", Direction.ASC);
		
		var ua = getUserSession();

		//TODO: validar acesso
		
		List<User> users = null;

		var idWorkspace = ua.getWorkspace().getId();
		if(filters == null || filters.length() == 0)
		{
			users = userService.findAllByWorkspace(
				idWorkspace, pageable);
		}
		else
		{
			users = userService.findAllByWorkspace(
				idWorkspace, pageable, buildFilters(filters, UserFilter.class));
		}
		
		return users.stream()
				.map(w -> new UserResponse(w, idWorkspace))
				.collect(Collectors.toList());
	}
	
	@GetMapping("/users/{id}")
	@ApiOperation(value = "Find a single user by id in the current workspace")
	UserResponse findOne(
		@PathVariable String id) 
	{
		var ua = getUserSession();
		
		var user = userService.findByPubIdAndWorkspace(id, ua.getWorkspace());
		
		userService.validarAcesso(AccessType.READ, user, ua.getUser(), ua.getWorkspace());
		
		return new UserResponse(user, ua.getWorkspace().getId());
	}

	@PostMapping("/users")
	@PreAuthorize("hasRole('USER_ADMIN')")
	@ApiOperation(value = "Add an user to current workspace")
	UserResponse add(
		@Valid @RequestBody UserRequest req
	)
	{
		var ua = getUserSession();
		
		userService.validarAcesso(AccessType.CREATE, ua.getUser(), ua.getWorkspace());
		
		return new UserResponse(
			userService.adicionar(req, ua.getWorkspace(), ua.getUser()), ua.getWorkspace().getId());
	}

	@PatchMapping("/users/{id}")
	@ApiOperation(value = "Update user")
	UserResponse update(
		@PathVariable String id,	
		@Valid @RequestBody UserRequest req)
	{
		var ua = getUserSession();
		
		var user = userService.findByPubId(id);
		if(user == null)
		{
			throw new UserException("Usuário inexistente");
		}

		userService.validarAcesso(AccessType.UPDATE, user, ua.getUser(), ua.getWorkspace());

		var res = userService.atualizar(
			user, 
			req, 
			ua.getWorkspace(), 
			ua.getUser().isAdmin(ua.getWorkspace().getId()),
			user.getId() == ua.getUser().getId());

		return new UserResponse(res, ua.getWorkspace().getId());
	}
	
	@DeleteMapping("/users/{id}")
	@ApiOperation(value = "Delete user")
	EmptyResponse remove(
		@PathVariable String id)
	{
		var ua = getUserSession();

		var user = userService.findByPubIdAndWorkspace(id, ua.getWorkspace());
		if(user == null)
		{
			throw new UserException("Usuário inexistente");
		}

		userService.validarAcesso(AccessType.DELETE, user, ua.getUser(), ua.getWorkspace());

		if(user.getId() == ua.getUser().getId())
		{
			userService.remover(user, ua.getUser());
		}
		else
		{
			userService.remover(user, ua.getWorkspace());
		}

		return new EmptyResponse();
	}
}
