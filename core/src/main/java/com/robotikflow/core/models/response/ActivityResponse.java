package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.ActivitySchema;

public class ActivityResponse
    extends ObjResponse<ActivitySchema>
{

	public ActivityResponse(
		final Activity activity) 
    {
		super(activity);
	}

	public ActivityResponse(
		final Activity activity,
		final boolean withSchema) 
    {
		super(activity, withSchema);
	}
}