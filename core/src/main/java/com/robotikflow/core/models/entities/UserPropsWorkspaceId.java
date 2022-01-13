package com.robotikflow.core.models.entities;

import java.io.Serializable;
import java.util.Objects;

public class UserPropsWorkspaceId 
	implements Serializable
{
	private static final long serialVersionUID = -4489558417452230992L;
	
	private Long user;
	private Long workspace;
	
	public Long getUser() {
		return user;
	}
	public void setUser(Long user) {
		this.user = user;
	}
	public Long getWorkspace() {
		return workspace;
	}
	public void setWorkspace(Long workspace) {
		this.workspace = workspace;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(workspace, user);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
			UserPropsWorkspaceId other = (UserPropsWorkspaceId) obj;
		return Objects.equals(workspace, other.workspace)
				&& Objects.equals(user, other.user);
	}
	
}
