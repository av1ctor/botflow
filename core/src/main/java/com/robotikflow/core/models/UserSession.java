package com.robotikflow.core.models;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.robotikflow.core.models.entities.Group;
import com.robotikflow.core.models.entities.Role;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;

public class UserSession 
	implements Principal
{
	private final User user;
	private final Workspace workspace;
	private final ZonedDateTime logadoEm;
	
	public UserSession(
		final User user, 
		final Workspace workspace) 
	{
		this.user = user;
		this.workspace = workspace;
		this.logadoEm = ZonedDateTime.now();
	}

	public User getUser() {
		return user;
	}

	public Workspace getWorkspace() {
		return workspace;
	}
	
	public Set<Role> getRoles()
	{
		if(workspace == null)
			return new HashSet<Role>();
					
		return user.getRoles(workspace.getId());
	}

	public List<Group> getGroups()
	{
		return user.getGroups(workspace.getId());
	}

	public ZonedDateTime getLogadoEm() {
		return logadoEm;
	}

	@Override
	public String getName() {
		return user.getEmail();
	}
}
