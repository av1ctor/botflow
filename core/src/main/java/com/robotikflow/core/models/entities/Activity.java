package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.ActivityFilter;
import com.robotikflow.core.models.filters.ObjSchemaFilter;

@Entity
@Table(name = "activities")
public class Activity
	extends Obj<ActivitySchema>
{
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	protected ActivitySchema schema;

	public Activity()
	{
		super();
	}
	
	public Activity(
		final Activity activity)
	{
		super();
		schema = activity.getSchema();
	}

	public Activity(
        final ActivityFilter filters) 
    {
        super(filters);
		if(filters != null)
		{
			var schema = filters.getSchema() != null?
				filters.getSchema():
				new ObjSchemaFilter();
			
			schema.setName(filters.getName());
			schema.setCategory(filters.getCategory());
			
        	this.schema = new ActivitySchema(schema, filters.getDir());
		}
    }

	@Override
	public ActivitySchema getSchema() {
		return schema;
	}

	@Override
	public void setSchema(ActivitySchema schema) {
		this.schema = schema;
	}
}
