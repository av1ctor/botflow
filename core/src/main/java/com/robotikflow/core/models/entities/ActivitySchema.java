package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.ObjSchemaFilter;
import com.robotikflow.core.models.schemas.activity.ActivityDirection;

@Entity
@Table(name = "activities_schemas")
public class ActivitySchema
	extends ObjSchema
{
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private ActivityDirection dir;

	public ActivitySchema()
	{
		super();
	}

	public ActivitySchema(
		final ObjSchemaFilter filters,
		final ActivityDirection dir)
	{
		super(filters);
		this.dir = dir;
	}
	
	public ActivityDirection getDir() {
		return dir;
	}
	public void setDir(ActivityDirection dir) {
		this.dir = dir;
	}
}
