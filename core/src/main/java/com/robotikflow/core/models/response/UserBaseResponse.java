package com.robotikflow.core.models.response;

import com.robotikflow.core.models.UserSession;
import com.robotikflow.core.models.entities.User;

public class UserBaseResponse 
{
	private final String id;
	private final String email;
	private final String name;
	private final String nick;
	private final String icon;

	public UserBaseResponse(
		final User user)
	{
		this.id = user.getPubId();
		this.email = user.getEmail();
		this.name = user.getName();
		this.nick = user.getNick();
		this.icon = user.getIcon();
	}

	public UserBaseResponse(
		final UserSession userSession)
	{
		this(userSession.getUser());
	}

	public String getId() {
		return id;
	}
	
	public String getEmail() {
		return email;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNick() {
		return nick;
	}
	
	public String getIcon() {
		return icon;
	}
}
