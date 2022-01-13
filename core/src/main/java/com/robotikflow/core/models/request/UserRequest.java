package com.robotikflow.core.models.request;

import java.util.List;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UserRequest 
	extends UserBaseRequest
{
	@NotBlank
	@Size(min=5, max=128)
	@Email
	private String email;
	@Size(max=128)
	private String name;
	@Size(max=64)
	private String nick;
	@Size(max=64)
	private String icon;
	@Size(max=64)
	private String password;
	private String lang;
	private String timezone;
	private String theme;
	private RoleRequest role;
	private List<GroupRequest> groups;
	private UserPropsWorkspaceRequest props;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public UserPropsWorkspaceRequest getProps() {
		return props;
	}

	public void setProps(UserPropsWorkspaceRequest props) {
		this.props = props;
	}

	public RoleRequest getRole() {
		return role;
	}

	public void setRole(RoleRequest role) {
		this.role = role;
	}

	public List<GroupRequest> getGroups() {
		return groups;
	}

	public void setGroups(List<GroupRequest> groups) {
		this.groups = groups;
	}
}
