package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.ObjSchemaFilter;
import com.robotikflow.core.models.filters.TriggerFilter;

@Entity
@Table(name = "triggers")
public class Trigger
	extends Obj<TriggerSchema>
{
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	protected TriggerSchema schema;

	public Trigger()
	{
		super();
	}

	public Trigger(
        final TriggerFilter filters) 
    {
        super(filters);
		if(filters != null)
		{
			var schema = filters.getSchema() != null?
				filters.getSchema():
				new ObjSchemaFilter();
			
			schema.setName(filters.getName());
			schema.setCategory(filters.getCategory());
			
			this.schema = new TriggerSchema(schema);
		}
    }

	
	@Override
	public TriggerSchema getSchema() {
		return schema;
	}

	@Override
	public void setSchema(TriggerSchema schema) {
		this.schema = schema;
	}
}
