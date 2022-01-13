package com.robotikflow.api.server.models.response;

import java.util.List;
import java.util.stream.Collectors;

import com.robotikflow.core.models.entities.Menu;

public class MenuResponse 
{
	private final Long id;
	private final String label;
	private final String alias;
	private final Short order;
	private final String icon;
	private final String command;
	private final List<MenuResponse> items;

	public MenuResponse(
		final Menu menu) 
	{
		id = menu.getId();
		label = menu.getLabel();
		alias = menu.getAlias();
		order = menu.getOrder();
		icon = menu.getIcon();
		command = menu.getCommand();
		items = menu.getItems() != null
				? menu.getItems().stream().map(i -> new MenuResponse(i)).collect(Collectors.toList())
				: null;
	}

	public Long getId() {
		return id;
	}
	public String getLabel() {
		return label;
	}
	public Short getOrder() {
		return order;
	}
	public String getIcon() {
		return icon;
	}
	public String getCommand() {
		return command;
	}
	public List<MenuResponse> getItems() {
		return items;
	}
	public String getAlias() {
		return alias;
	}
}
