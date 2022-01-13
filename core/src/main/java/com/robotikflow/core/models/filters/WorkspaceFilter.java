package com.robotikflow.core.models.filters;

public class WorkspaceFilter 
{
	private Long id;
	private String name;

	public WorkspaceFilter()
	{
	}

	public WorkspaceFilter(Long id)
	{
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
