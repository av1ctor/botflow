package com.robotikflow.core.models.response;

import java.util.List;
import java.util.stream.Collectors;

import com.robotikflow.core.models.UserSession;
import com.robotikflow.core.models.entities.User;

public class UserResponse 
	extends UserBaseResponse
{
	private final String lang;
	private final String timezone;
	private final String theme;
	private final boolean isAdmin;
	private final RoleResponse role;
	private final List<GroupResponse> groups;
	private final UserPropsWorkspaceResponse props;

	public UserResponse(
		final User user, 
		final Long idWorkspace)
	{
		super(user);

		this.lang = user.getLang();
		this.timezone = user.getTimezone();
		this.theme = user.getTheme();
		this.isAdmin = user.isAdmin(idWorkspace);
		this.role = new RoleResponse(user.getRole(idWorkspace));
		this.groups = user.getGroups(idWorkspace).stream()
				.map(g -> new GroupResponse(g)).collect(Collectors.toList());
		this.props = new UserPropsWorkspaceResponse(user.getProps(idWorkspace));
	}

	public UserResponse(
		final UserSession ua)
	{
		this(ua.getUser(), ua.getWorkspace().getId()); 
	}

	public String getLang() {
		return lang;
	}
	
	public String getTimezone() {
		return timezone;
	}
	
	public String getTheme() {
		return theme;
	}
	
	public boolean isAdmin() {
		return isAdmin;
	}

	public RoleResponse getRole() {
		return role;
	}

	public List<GroupResponse> getGroups() {
		return groups;
	}

	public UserPropsWorkspaceResponse getProps() {
		return props;
	}
}
