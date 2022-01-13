package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionPost;

public class CollectionPostResponse 
    extends WorkspacePostResponse
{
    private final CollectionBaseResponse collection;

    public CollectionPostResponse(
        final CollectionPost post) 
    {
        super(post);
        collection = new CollectionBaseResponse(post.getCollection(), null);
    }

	public CollectionBaseResponse getCollection() {
		return collection;
	}
}
