package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.ObjSchema;
import com.robotikflow.core.util.DocumentUtil;

public class ObjSchemaResponse
    extends ObjSchemaBaseResponse
{
	private final UserBaseResponse createdBy;
	private final String createdAt;
	private final UserBaseResponse updatedBy;
	private final String updatedAt;

    public ObjSchemaResponse(
        final ObjSchema obj)
    {
        super(obj);

		createdBy = new UserBaseResponse(obj.getCreatedBy());
		createdAt = obj.getCreatedAt().format(DocumentUtil.datePattern);
        updatedBy = obj.getUpdatedBy() != null? 
            new UserBaseResponse(obj.getUpdatedBy()): 
            null;
        updatedAt = obj.getUpdatedAt() != null? 
            obj.getUpdatedAt().format(DocumentUtil.datePattern): 
            null;
    }

    public UserBaseResponse getCreatedBy() {
        return createdBy;
    }
    public String getCreatedAt() {
        return createdAt;
    }
    public UserBaseResponse getUpdatedBy() {
        return updatedBy;
    }
    public String getUpdatedAt() {
        return updatedAt;
    }	
}