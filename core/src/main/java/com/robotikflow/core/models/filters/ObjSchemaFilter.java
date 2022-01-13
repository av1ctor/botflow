package com.robotikflow.core.models.filters;

public class ObjSchemaFilter 
{
	private Long id;
    private String name;
    private String category;

	public ObjSchemaFilter()
	{
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
}
