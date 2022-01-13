package com.robotikflow.core.services.collections.services;

import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.repositories.UserRepository;
import com.robotikflow.core.models.response.UserResponse;

public class UserScriptService
{
	private final UserRepository userRepo;
	private final Workspace workspace;
	private final User user;
	
	public UserScriptService(final UserRepository userRepo, final Workspace workspace, User user)
	{
		this.userRepo = userRepo;
		this.workspace = workspace;
		this.user = user;
	}

	public User getCurrent() {
		return user;
	}

	public String getTimeZone()
	{
		return user.getTimezone();
	}
	
	public UserResponse findByEmail(final String email)
	{
		var idWorkspace = workspace.getId();
		return new UserResponse(userRepo.findByEmailAndIdWorkspace(email, idWorkspace), idWorkspace);
	}
}
