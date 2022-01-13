package com.robotikflow.core.models.schemas.activity;

import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.schemas.obj.ObjSchema;

public class ActivitySchema 
	extends ObjSchema
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
