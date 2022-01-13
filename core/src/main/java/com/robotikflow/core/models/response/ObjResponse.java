package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Obj;
import com.robotikflow.core.models.entities.ObjSchema;
import com.robotikflow.core.util.DocumentUtil;

public class ObjResponse<T extends ObjSchema>
    extends ObjBaseResponse<T>
{
	private final String schema;
    private final UserBaseResponse createdBy;
	private final String createdAt;
	private final UserBaseResponse updatedBy;
	private final String updatedAt;

    public ObjResponse(
        final Obj<T> obj)
    {
        this(obj, false, false);
    }

    public ObjResponse(
        final Obj<T> obj,
        final boolean withSchema)
    {
        this(obj, withSchema, false);
    }
    
    public ObjResponse(
        final Obj<T> obj,
        final boolean withSchema,
        final boolean withUser)
    {
        super(obj);

        if(withSchema)
        {
            schema = ((ObjSchema)obj.getSchema()).getSchema();
        }
        else
        {
            schema = null;
        }

		if(withUser)
        {
            createdBy = new UserBaseResponse(obj.getCreatedBy());
            createdAt = obj.getCreatedAt().format(DocumentUtil.datePattern);
            updatedBy = obj.getUpdatedBy() != null? 
                new UserBaseResponse(obj.getUpdatedBy()): 
                null;
            updatedAt = obj.getUpdatedAt() != null? 
                obj.getUpdatedAt().format(DocumentUtil.datePattern): 
                null;
        }
        else
        {
            createdBy = null;
            createdAt = null;
            updatedBy = null;
            updatedAt = null;
        }
    }

    public String getSchema() {
        return schema;
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