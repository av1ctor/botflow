package com.robotikflow.core.models.filters;

import java.time.ZonedDateTime;

import javax.persistence.Column;

public class WorkspaceLogFilter 
{
	public WorkspaceFilter workspace;
	
	public Long id;
	
	public String type;

	@FilterOperation(FilterOperationType.GE)
	@Column(name="date")
	public ZonedDateTime fromDate;
	
	@FilterOperation(FilterOperationType.LE)
	@Column(name="date")
	public ZonedDateTime toDate;
}
