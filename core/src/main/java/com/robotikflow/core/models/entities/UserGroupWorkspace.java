package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "users_groups_workspaces")
@IdClass(UserGroupWorkspaceId.class)
public class UserGroupWorkspace 
{
	@Id
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Id
	@ManyToOne
	@JoinColumn(name = "group_id")
	private Group group;
	
	@Id
	@ManyToOne
	@JoinColumn(name = "workspace_id")
	private Workspace workspace;
	
	public UserGroupWorkspace()
	{
		
	}
	
	public UserGroupWorkspace(
		final Group group, 
		final User user, 
		final Workspace workspace) 
	{
		this.group = group;
		this.user = user;
		this.workspace = workspace;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}
}
