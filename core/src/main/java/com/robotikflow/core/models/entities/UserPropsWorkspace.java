package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "users_props_workspaces")
@IdClass(UserPropsWorkspaceId.class)
public class UserPropsWorkspace
{
	@Id
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

	@Id
    @ManyToOne
    @JoinColumn(name = "workspace_id")
    private Workspace workspace;
	
	@NotNull
	private boolean active;

	private ZonedDateTime invitedAt;
	
	private ZonedDateTime acceptedAt;

	public UserPropsWorkspace()
	{
		
	}

	public UserPropsWorkspace(
		final User user, 
		final Workspace workspace, 
		final boolean active,
		final ZonedDateTime invitedAt,
		final ZonedDateTime acceptedAt)
	{
		this.user = user;
		this.workspace = workspace;
		this.active = active;
		this.invitedAt = invitedAt;
		this.acceptedAt = acceptedAt;
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

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public ZonedDateTime getInvitedAt() {
		return invitedAt;
	}

	public void setInvitedAt(ZonedDateTime invitedAt) {
		this.invitedAt = invitedAt;
	}

	public ZonedDateTime getAcceptedAt() {
		return acceptedAt;
	}

	public void setAcceptedAt(ZonedDateTime acceptedAt) {
		this.acceptedAt = acceptedAt;
	}
}
