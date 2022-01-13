package com.robotikflow.core.models.filters;

public class AppFilter 
{
	private String pubId;
	private String title;
	private WorkspaceFilter workspace;
	
	public String getPubId() {
		return pubId;
	}
	public void setPubId(String pubId) {
		this.pubId = pubId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public WorkspaceFilter getWorkspace() {
		return workspace;
	}
	public void setWorkspace(WorkspaceFilter workspace) {
		this.workspace = workspace;
	}
}
