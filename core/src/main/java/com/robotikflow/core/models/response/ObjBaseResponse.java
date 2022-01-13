package com.robotikflow.core.models.response;

import java.util.Map;

import com.robotikflow.core.models.entities.Obj;
import com.robotikflow.core.models.entities.ObjSchema;

public class ObjBaseResponse<T extends ObjSchema> 
{
    private final String id;
    private final String name;
    private final String schemaId;
    private final Map<String, Object> fields;

    public ObjBaseResponse(
        final Obj<T> obj)
    {
        id = obj.getPubId();
        name = obj.getSchema().getName();
        schemaId = ((ObjSchema)obj.getSchema()).getPubId();
        fields = obj.getFields();
    }

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSchemaId() {
		return schemaId;
	}

	public Map<String, Object> getFields() {
		return fields;
	}
}
