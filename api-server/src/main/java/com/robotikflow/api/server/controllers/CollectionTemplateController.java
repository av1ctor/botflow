package com.robotikflow.api.server.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.robotikflow.core.models.entities.CollectionTemplate;
import com.robotikflow.core.models.filters.CollectionTemplateFilter;
import com.robotikflow.core.services.collections.CollectionTemplateService;
import com.robotikflow.api.server.models.response.CollectionTemplateResponse;

import io.swagger.annotations.ApiOperation;

@RestController
public class CollectionTemplateController extends BaseController 
{
	@Autowired
	private CollectionTemplateService templateService;
	
	private static HashSet<String> colunasValidasDeOrdenacao = new HashSet<>(Arrays.asList
	( 
		"name", "createdAt", "updatedAt", "order"
	));
	
	@GetMapping("/templates/collections")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Listar os templates de Coleção do área de trabalho")
	List<CollectionTemplateResponse> listar(
		@RequestParam(value = "filters", required = false) String filters,
		Pageable pageable) 
		throws Exception
	{
		pageable = validateSorting(pageable, colunasValidasDeOrdenacao, "order", Direction.ASC);

		var userSession = getUserSession();
		
		List<CollectionTemplate> templates = null;
		
		if(filters == null || filters.length() == 0)
		{
			templates = templateService
				.findAllByWorkspace(userSession.getWorkspace(), pageable);
		}
		else
		{
			templates = templateService
				.findAllByWorkspace(
					userSession.getWorkspace(), 
					pageable, 
					buildFilters(filters, CollectionTemplateFilter.class));
		}
		
		return templates.stream()
				.map(t -> new CollectionTemplateResponse(t, t.getSchema(),
						userSession.getWorkspace().getId()))
				.collect(Collectors.toList());
	}
}
