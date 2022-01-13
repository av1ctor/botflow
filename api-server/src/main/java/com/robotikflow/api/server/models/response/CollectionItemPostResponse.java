package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionItemPost;

public class CollectionItemPostResponse 
    extends CollectionPostResponse
{
    private final String itemId;

    public CollectionItemPostResponse(
        final CollectionItemPost log, 
        final boolean includeParent) 
    {
        super(log);
        itemId = log.getItemId();
    }

	public String getItemId() {
		return itemId;
	}
}
