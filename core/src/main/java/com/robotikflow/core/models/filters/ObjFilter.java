package com.robotikflow.core.models.filters;

public class ObjFilter 
{
	private Long id;
	private String name;
	private String category;
	private ObjSchemaFilter schema;
	private WorkspaceFilter workspace;

	public ObjFilter()
	{
	}
	public ObjFilter(Long id)
	{
		this.id = id;
	}

	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public ObjSchemaFilter getSchema() {
		return schema;
	}
	public void setSchema(ObjSchemaFilter schema) {
		this.schema = schema;
	}
	public WorkspaceFilter getWorkspace() {
		return workspace;
	}
	public void setWorkspace(WorkspaceFilter workspace) {
		this.workspace = workspace;
	}
}
