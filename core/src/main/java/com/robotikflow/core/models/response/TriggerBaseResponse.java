package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Trigger;
import com.robotikflow.core.models.entities.TriggerSchema;

public class TriggerBaseResponse
    extends ObjBaseResponse<TriggerSchema>
{
	public TriggerBaseResponse(Trigger activity) 
    {
		super(activity);
	}
}