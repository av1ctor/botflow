package com.robotikflow.core.models.filters;

public class CollectionTemplateFilter 
{
	private WorkspaceFilter workspace;
	private String createdBy;
	private String pubId;
	private String name;
	private CollectionTemplateCategoryFilter category;

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
	public String getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}
	public CollectionTemplateCategoryFilter getCategory() {
		return category;
	}
	public void setCategory(CollectionTemplateCategoryFilter category) {
		this.category = category;
	}
}
