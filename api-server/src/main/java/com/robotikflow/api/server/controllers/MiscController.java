package com.robotikflow.api.server.controllers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.robotikflow.core.models.entities.Timezone;
import com.robotikflow.core.services.MiscService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class MiscController extends BaseController 
{
	@Autowired
	private MiscService miscService;
	
	private static HashSet<String> iconsSortCols = new HashSet<>(Arrays.asList
	( 
		"id"
	));
	
	private static HashSet<String> timezonesSortCols = new HashSet<>(Arrays.asList
	( 
		"id", "diff"
	));
	
	public MiscController(Environment env) 
	{
		super();
	}
	
	@GetMapping("/misc/icons")
	@ApiOperation(value = "Listar os ícones")
	List<String> listarIcones(
		Pageable pageable)
	{
		pageable = verificarOrdenacaoOffsetLimit(pageable, iconsSortCols, "id", Direction.ASC);

		var icons = miscService.findAllIcons(pageable);
		
		return icons;
	}

	@GetMapping("/misc/timezones")
	@ApiOperation(value = "Listar os timezones")
	List<Timezone> listarTimezones(
		Pageable pageable)
	{
		pageable = verificarOrdenacaoOffsetLimit(pageable, timezonesSortCols, "id", Direction.ASC);

		var timezones = miscService.findAllTimezones(pageable);
		
		return timezones;
	}
}
