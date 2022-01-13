package com.robotikflow.core.models.response;

import com.robotikflow.core.models.UserSession;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;

public class WorkspaceResponse 
	extends WorkspaceBaseResponse
{
	private final String name;
	private final String idRootDoc;
	private final String idRootCol;
	private final UserBaseResponse owner;
	private final WorkspaceUserResponse user;
	
	public WorkspaceResponse(
		final Workspace workspace)
	{
		this(workspace, null);
	}

	public WorkspaceResponse(
		final Workspace workspace,
		final User user)
	{
		super(workspace);

		name = workspace.getName();
		idRootDoc = workspace.getRootDoc().getPubId();
		idRootCol = workspace.getRootCollection().getPubId();
		owner = new UserBaseResponse(workspace.getOwner());
		if(user != null)
		{
			this.user = new WorkspaceUserResponse(workspace, user);
		}
		else
		{
			this.user = null;
		}
	}

	public WorkspaceResponse(UserSession ua)
	{
		this(ua.getWorkspace(), ua.getUser());
	}

	public String getName() {
		return name;
	}

	public String getIdRootDoc() {
		return idRootDoc;
	}

	public String getIdRootCol() {
		return idRootCol;
	}

	public UserBaseResponse getOwner() {
		return owner;
	}

	public WorkspaceUserResponse getUser() {
		return user;
	}
}
