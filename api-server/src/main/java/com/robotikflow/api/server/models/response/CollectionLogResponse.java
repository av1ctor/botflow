package com.robotikflow.api.server.models.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.models.entities.CollectionLog;

public class CollectionLogResponse 
    extends WorkspaceLogResponse
{
    private final CollectionBaseResponse collection;

    public CollectionLogResponse(
        final CollectionLog log, 
        final ObjectMapper objectMapper) 
    {
        super(log, objectMapper);
        collection = new CollectionBaseResponse(log.getCollection(), null);
    }

	public CollectionBaseResponse getCollection() {
		return collection;
	}
}
