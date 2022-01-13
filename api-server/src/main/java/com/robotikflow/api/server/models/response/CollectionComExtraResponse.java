package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.Collection;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.CollectionType;

public class CollectionComExtraResponse 
{
	private final CollectionResponse collection;
	private final Object extra;
	
	public CollectionComExtraResponse (
		final Collection collection, 
		final String schema,
		final Object extra)
	{
		this.collection = new CollectionResponse(collection, schema);
		this.extra = extra;
	}

	public CollectionComExtraResponse (
		final Collection collection, 
		final Object extra)
	{
		this(
			collection, 
			collection.getType() == CollectionType.SCHEMA? 
				((CollectionWithSchema)collection).getSchema(): 
				null,
			extra);
	}

	public CollectionResponse getCollection() {
		return collection;
	}
	public Object getExtra() {
		return extra;
	}
}
