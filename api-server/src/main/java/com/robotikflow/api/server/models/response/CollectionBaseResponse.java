package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.Collection;
import com.robotikflow.core.models.entities.CollectionType;

public class CollectionBaseResponse 
{
	private final String id;
	private final CollectionType type;

	public CollectionBaseResponse(
		final Collection collection, 
		final String schema)
	{
		id = collection.getPubId();
		type = collection.getType();
	}

	public String getId() {
		return id;
	}

	public CollectionType getType() {
		return type;
	}
}
