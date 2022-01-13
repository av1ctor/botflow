package com.robotikflow.api.server.models.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.models.entities.WorkspaceLog;
import com.robotikflow.core.models.entities.WorkspaceLogType;
import com.robotikflow.core.models.response.WorkspaceBaseResponse;
import com.robotikflow.core.models.response.UserBaseResponse;
import com.robotikflow.core.util.DocumentUtil;

public class WorkspaceLogResponse 
{
    private final Long id;
    private final WorkspaceBaseResponse workspace;
    private final WorkspaceLogType type;
	private final String message;
    private final String extra;
    private final String date;
    private final UserBaseResponse user;

    public WorkspaceLogResponse(
        final WorkspaceLog log, 
        final ObjectMapper objectMapper) 
    {
        id = log.getId();
        workspace = new WorkspaceBaseResponse(log.getWorspace());
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
        user = log.getUser() != null?
            new UserBaseResponse(log.getUser()):
            null;
    }

    public Long getId() {
		return id;
	}

	public WorkspaceBaseResponse getWorkspace() {
        return workspace;
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

    public UserBaseResponse getUser() {
        return user;
    }
}
