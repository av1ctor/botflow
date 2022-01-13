package com.robotikflow.core.models.entities;

import java.io.Serializable;
import java.util.Objects;

public class UserGroupWorkspaceId implements Serializable
{
	private static final long serialVersionUID = -6774160872888702131L;
	
	private Long group;
	private Long user;
	private Long workspace;
	
	public Long getGroup() {
		return group;
	}
	public void setGroup(Long group) {
		this.group = group;
	}
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
		return Objects.hash(group, workspace, user);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UserGroupWorkspaceId other = (UserGroupWorkspaceId) obj;
		return Objects.equals(group, other.group) && Objects.equals(workspace, other.workspace)
				&& Objects.equals(user, other.user);
	}

	
}
