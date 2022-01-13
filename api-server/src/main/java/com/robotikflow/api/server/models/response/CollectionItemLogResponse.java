package com.robotikflow.api.server.models.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.models.entities.CollectionItemLog;

public class CollectionItemLogResponse 
    extends CollectionLogResponse
{
    private final String itemId;

    public CollectionItemLogResponse(
        final CollectionItemLog log, 
        final ObjectMapper objectMapper) 
    {
        super(log, objectMapper);
        itemId = log.getItemId();
    }

	public String getItemId() {
		return itemId;
	}
}
