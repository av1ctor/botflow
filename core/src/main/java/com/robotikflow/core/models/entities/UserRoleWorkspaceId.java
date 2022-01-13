package com.robotikflow.core.models.entities;

import java.io.Serializable;
import java.util.Objects;

public class UserRoleWorkspaceId 
	implements Serializable
{
	private static final long serialVersionUID = -5398558417452230993L;
	
	private Long user;
	private Long workspace;
	private Long role;
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
	public Long getRole() {
		return role;
	}
	public void setRole(Long role) {
		this.role = role;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(role, workspace, user);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserRoleWorkspaceId other = (UserRoleWorkspaceId) obj;
		return Objects.equals(role, other.role) && Objects.equals(workspace, other.workspace)
				&& Objects.equals(user, other.user);
	}
	
}
