package com.robotikflow.api.server.models.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.models.entities.CollectionIntegrationLog;
import com.robotikflow.core.models.entities.WorkspaceLogType;
import com.robotikflow.core.util.DocumentUtil;

public class CollectionIntegrationLogResponse 
{
    private final WorkspaceLogType type;
	private final String message;
    private final String extra;
    private final String date;

    public CollectionIntegrationLogResponse(
        final CollectionIntegrationLog log, 
        final ObjectMapper objectMapper) 
    {
        type = log.getType();
        message = log.getMessage();
        String ex = null;
        try 
        {
			ex = log.getExtra() != null?
			    objectMapper.writeValueAsString(log.getExtra()):
			    null;
        } 
        catch (JsonProcessingException e) 
        {
        }
        extra = ex;
        date = log.getDate().format(DocumentUtil.datePattern);
    }

    public WorkspaceLogType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getExtra() {
        return extra;
    }

    public String getDate() {
        return date;
    }
}
