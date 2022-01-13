package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "users_roles_workspaces")
@IdClass(UserRoleWorkspaceId.class)
public class UserRoleWorkspace
{
	@Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

	@Id
    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

	@Id
    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;
	
	public UserRoleWorkspace()
	{
		
	}

	public UserRoleWorkspace(User user, Workspace workspace, Role role)
	{
		this.user = user;
		this.workspace = workspace;
		this.role = role;
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
}
