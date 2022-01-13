package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.ObjSchemaFilter;

@Entity
@Table(name = "triggers_schemas")
public class TriggerSchema
	extends ObjSchema
{
	@NotNull
	private long options;
	
	public TriggerSchema()
	{
		super();
	}

	public TriggerSchema(
		final ObjSchemaFilter filters)
	{
		super(filters);
	}

	public long getOptions() {
		return options;
	}

	public void setOptions(long options) {
		this.options = options;
	}
}
