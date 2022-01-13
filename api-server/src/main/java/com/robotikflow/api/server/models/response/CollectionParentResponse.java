package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.Collection;

public class CollectionParentResponse 
    extends CollectionBaseResponse
{
	private final String name;

    public CollectionParentResponse(Collection collection) 
    {
        super(collection, null);
        name = collection.getName();
    }

    public String getName() {
        return name;
    }
}
