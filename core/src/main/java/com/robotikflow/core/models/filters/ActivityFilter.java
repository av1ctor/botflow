package com.robotikflow.core.models.filters;

import com.robotikflow.core.models.schemas.activity.ActivityDirection;

public class ActivityFilter 
	extends ObjFilter
{
	private ActivityDirection dir;

	public ActivityFilter()
	{
		super();
	}
	public ActivityFilter(Long id)
	{
		super(id);
	}

	public ActivityDirection getDir() {
		return dir;
	}
	public void setDir(ActivityDirection dir) {
		this.dir = dir;
	}
}
