package com.robotikflow.core.models.request;

public class PageAuthRequest 
{
    private String id;
	private UserRequest user;
	private GroupRequest group;
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

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}
	
}

