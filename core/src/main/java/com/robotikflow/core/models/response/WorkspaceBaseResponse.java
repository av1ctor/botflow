package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Workspace;

public class WorkspaceBaseResponse 
{
	private final String id;
	
	public WorkspaceBaseResponse(
		final Workspace workspace)
	{
		id = workspace.getPubId();
	}

	public String getId() {
		return id;
	}
}
