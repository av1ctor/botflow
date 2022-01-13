package com.robotikflow.api.server.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.robotikflow.core.models.entities.Menu;
import com.robotikflow.core.models.repositories.MenuRepository;
import com.robotikflow.api.server.models.response.MenuResponse;

import io.swagger.annotations.ApiOperation;

@RestController
public class MenuController extends BaseController 
{
	@Autowired
	private MenuRepository menuRepo;
	
	@GetMapping("/menus")
	@ApiOperation(value = "Carregar menu a depender do role do usuário logado")
	List<MenuResponse> ler()
	{
		var ua = getUserSession();
		
		var menus = menuRepo.findAllTopByWorkspace(ua.getWorkspace());
		
		var rolesIds = ua.getRoles().stream()
			.map(p -> p.getId())
				.collect(Collectors.toSet());

		var groupsIds = ua.getGroups().stream()
			.filter(g -> g.getWorkspace().getId() == ua.getWorkspace().getId())
				.map(g -> g.getId())
					.collect(Collectors.toSet());
		
		for(var menu : menus)
		{
			removeIfForbidden(menu, rolesIds, groupsIds);
		}
		
		return menus.stream().map(m -> new MenuResponse(m)).collect(Collectors.toList());
	}
	
	private void removeIfForbidden(
		final Menu menu, 
		final Set<Long> rolesIds,
		final Set<Long> groupsIds)
	{
		var toRemove = new HashSet<Menu>();
		for(var item : menu.getItems())
		{
			var removed = false;
			var menuRoles = item.getRoles();
			if(menuRoles != null && menuRoles.size() > 0) 
			{
				if(!menuRoles.stream().anyMatch(r -> rolesIds.contains(r.getRole().getId())))
				{
					toRemove.add(item);
					removed = true;
				}
			}

			if(!removed)
			{
				var menuGroups = item.getGroups();
				if(menuGroups != null && menuGroups.size() > 0) 
				{
					if(!menuGroups.stream().anyMatch(g -> groupsIds.contains(g.getGroup().getId())))
					{
						toRemove.add(item);
					}
				}
			}
			
			if(item.getItems() != null)
			{
				removeIfForbidden(item, rolesIds, groupsIds);
			}
		}
		
		menu.getItems().removeAll(toRemove);
	}
}
