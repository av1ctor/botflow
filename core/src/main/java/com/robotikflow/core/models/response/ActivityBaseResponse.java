package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.ActivitySchema;

public class ActivityBaseResponse
    extends ObjBaseResponse<ActivitySchema>
{
	public ActivityBaseResponse(Activity activity) 
    {
		super(activity);
	}
}