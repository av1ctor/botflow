package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.Trigger;
import com.robotikflow.core.models.entities.TriggerSchema;

public class TriggerResponse
    extends ObjResponse<TriggerSchema>
{

	public TriggerResponse(Trigger trigger) 
    {
		super(trigger);
	}
}