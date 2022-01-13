package com.robotikflow.core.models.request;

import javax.validation.constraints.NotBlank;

public class GroupRequest 
{
	private String id;
	@NotBlank
	private String name;
	private String parentId;
    
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
}

