package com.robotikflow.api.server.models.response;

import java.time.Instant;
import java.util.Date;

import com.robotikflow.core.models.UserSession;
import com.robotikflow.core.models.response.WorkspaceResponse;
import com.robotikflow.core.models.response.UserResponse;

public class AuthLoginResponse 
{
	private final String token;
	private final Date expiration;
	private final UserResponse user;
	private final WorkspaceResponse workspace;

	public AuthLoginResponse(
		final UserSession userSession, 
		final String token, 
		final Instant expiration) 
	{
		this.token = token;
		this.expiration = Date.from(expiration);
		this.user = userSession != null? 
			new UserResponse(userSession): 
			null;
		this.workspace = userSession != null? 
			new WorkspaceResponse(userSession): 
			null;
	}

	public String getToken() 
	{
        return this.token;
	}
	
	public Date getExpiration() 
	{
		return expiration;
	}

	public UserResponse getUser() {
		return user;
	}

	public WorkspaceResponse getWorkspace() {
		return workspace;
	}
}

