package com.robotikflow.core.models.request;

import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.entities.CollectionAuthRole;

public class CollectionAuthRequest 
{
    private String id;
	private UserRequest user;
	private GroupRequest group;
	@NotNull
	private CollectionAuthRole role;
	private boolean reverse;
    
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public UserRequest getUser() {
		return user;
	}

	public void setUser(UserRequest user) {
		this.user = user;
	}

	public GroupRequest getGroup() {
		return group;
	}

	public void setGroup(GroupRequest group) {
		this.group = group;
	}

	public CollectionAuthRole getRole() {
		return role;
	}

	public void setRole(CollectionAuthRole role) {
		this.role = role;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}
	
}

