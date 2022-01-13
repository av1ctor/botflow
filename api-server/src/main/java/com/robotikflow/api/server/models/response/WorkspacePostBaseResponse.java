package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.WorkspacePost;

public class WorkspacePostBaseResponse 
{
    private final String id;

    public WorkspacePostBaseResponse(
        final WorkspacePost post) 
    {
        id = post.getPubId();
    }

	public String getId() {
        return id;
    }

}
