package com.robotikflow.core.models.filters;

import java.time.ZonedDateTime;

public class CollectionFilter 
{
	private WorkspaceFilter workspace;
	private String createdBy;
	private String pubId;
	private String name;
	private ZonedDateTime publishedAt;

	public void setPublishedAt(ZonedDateTime publishedAt) {
		this.publishedAt = publishedAt;
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
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public String getPubId() {
		return pubId;
	}
	public ZonedDateTime getPublishedAt() {
		return publishedAt;
	}
}
