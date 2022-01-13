package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.WorkspacePost;
import com.robotikflow.core.models.entities.WorkspacePostType;
import com.robotikflow.core.models.response.WorkspaceBaseResponse;
import com.robotikflow.core.models.response.UserBaseResponse;
import com.robotikflow.core.util.DocumentUtil;

public class WorkspacePostResponse 
    extends WorkspacePostBaseResponse
{
    private final WorkspacePostType type;
    private final WorkspaceBaseResponse workspace;
    private final WorkspacePostBaseResponse parent;
    private final long options;
    private final short order;
	private final String title;
	private final String message;
    private final short level;
    private final int posts;
    private final String createdAt;
    private final UserBaseResponse createdBy;
    private final String updatedAt;
    private final UserBaseResponse updatedBy;

    public WorkspacePostResponse(
        final WorkspacePost post) 
    {
        super(post);

        type = post.getType();
        workspace = new WorkspaceBaseResponse(post.getWorkspace());
        parent = post.getParent() != null?
            new WorkspacePostBaseResponse(post.getParent()):
            null;
        options = post.getOptions();
        order = post.getOrder();
        title = post.getTitle();
        message = post.getMessage();
        level = post.getLevel();
        posts = post.getPosts();
        createdAt = post.getCreatedAt().format(DocumentUtil.datePattern);
        createdBy = post.getCreatedBy() != null?
            new UserBaseResponse(post.getCreatedBy()):
            null;
        updatedAt = post.getUpdatedAt() != null?
            post.getUpdatedAt().format(DocumentUtil.datePattern):
            null;
        updatedBy = post.getUpdatedBy() != null?
            new UserBaseResponse(post.getUpdatedBy()):
            null;    
    }

    public WorkspacePostType getType() {
        return type;
    }

    public WorkspaceBaseResponse getWorkspace() {
        return workspace;
    }

    public WorkspacePostBaseResponse getParent() {
        return parent;
    }

    public long getOptions() {
        return options;
    }

    public short getOrder() {
        return order;
    }
    
    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public short getLevel() {
        return level;
    }

    public int getPosts() {
		return posts;
	}

    public String getCreatedAt() {
        return createdAt;
    }

    public UserBaseResponse getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public UserBaseResponse getUpdatedBy() {
        return updatedBy;
    }
}
