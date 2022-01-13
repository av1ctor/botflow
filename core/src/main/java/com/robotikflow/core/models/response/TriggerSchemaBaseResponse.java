package com.robotikflow.core.models.response;

import com.robotikflow.core.models.entities.TriggerSchema;

public class TriggerSchemaBaseResponse
    extends ObjSchemaBaseResponse
{
	public final long options;
		
	public TriggerSchemaBaseResponse(
		TriggerSchema trig) 
    {
		super(trig);
		options = trig.getOptions();
	}

	public long getOptions() {
		return options;
	}
}