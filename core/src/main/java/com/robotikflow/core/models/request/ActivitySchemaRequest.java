package com.robotikflow.core.models.request;

import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.schemas.activity.ActivityDirection;

public class ActivitySchemaRequest
    extends ObjSchemaRequest
{
	@NotNull
    private ActivityDirection dir;

	public ActivityDirection getDir() {
		return dir;
	}
	public void setDir(ActivityDirection dir) {
		this.dir = dir;
	}
}