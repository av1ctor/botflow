package com.robotikflow.core.models.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class WorkspacePostRequest 
{
	private String parentId;
	@Size(min=3, max=256)
    private String title;
    @NotNull
	@Size(min=1, max=8192)
	private String message;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}