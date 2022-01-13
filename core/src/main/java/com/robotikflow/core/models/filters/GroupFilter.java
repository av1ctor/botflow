package com.robotikflow.core.models.filters;

public class GroupFilter 
{
	private String pubId;
	private String name;
	private WorkspaceFilter workspace;
	
	public String getPubId() {
		return pubId;
	}
	public void setPubId(String pubId) {
		this.pubId = pubId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public WorkspaceFilter getWorkspace() {
		return workspace;
	}
	public void setWorkspace(WorkspaceFilter workspace) {
		this.workspace = workspace;
	}

}
