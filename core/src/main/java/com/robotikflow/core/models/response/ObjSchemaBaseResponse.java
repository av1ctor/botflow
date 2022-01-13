package com.robotikflow.core.models.response;

import java.util.Map;

import com.robotikflow.core.models.entities.ObjSchema;
import com.robotikflow.core.models.schemas.obj.Field;
import com.robotikflow.core.models.schemas.obj.ObjType;

public class ObjSchemaBaseResponse 
{
    private final String id;
    private final ObjType type;
    private final String name;
    private final String title;
    private final String desc;
    private final String icon;
	private final String schema;
    private final String category;
    private final Map<String, Field> fields;

    public ObjSchemaBaseResponse(ObjSchema obj)
    {
        id = obj.getPubId();
        type = obj.getType();
        name = obj.getName();
        title = obj.getTitle();
        desc = obj.getDesc();
        icon = obj.getIcon();
		schema = obj.getSchema();
        category = obj.getCategory();
        fields = obj.getFields();
    }

	public String getId() {
		return id;
	}

	public ObjType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public String getTitle() {
		return title;
	}

	public String getDesc() {
		return desc;
	}

	public String getIcon() {
		return icon;
	}

	public String getSchema() {
		return schema;
	}

	public String getCategory() {
		return category;
	}

	public Map<String, Field> getFields() {
		return fields;
	}
}
