package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.ActivitySchema;
import com.robotikflow.core.models.schemas.activity.ActivityDirection;

public class ActivitySchemaBaseResponse
    extends ObjSchemaBaseResponse
{
	private final ActivityDirection dir;

	public ActivitySchemaBaseResponse(ActivitySchema act) 
    {
		super(act);
		dir = act.getDir();
	}

	public ActivityDirection getDir() {
		return dir;
	}
}