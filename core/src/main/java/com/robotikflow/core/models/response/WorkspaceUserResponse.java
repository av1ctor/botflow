package com.robotikflow.core.models.response;

import java.time.ZonedDateTime;

import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;

public class WorkspaceUserResponse 
{
	private final ZonedDateTime invitedAt; 
	private final ZonedDateTime acceptedAt;
	private final boolean active;
	
	public WorkspaceUserResponse(
		final Workspace workspace,
		final User user)
	{
		var props = user.getProps(workspace.getId());
		invitedAt = props.getInvitedAt();
		acceptedAt = props.getAcceptedAt();
		active = props.isActive();
	}

	public ZonedDateTime getInvitedAt() {
		return invitedAt;
	}

	public ZonedDateTime getAcceptedAt() {
		return acceptedAt;
	}

	public boolean isActive() {
		return active;
	}
}
