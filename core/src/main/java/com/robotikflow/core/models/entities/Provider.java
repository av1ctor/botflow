package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.ObjSchemaFilter;
import com.robotikflow.core.models.filters.ProviderFilter;

@Entity
@Table(name = "providers")
public class Provider
	extends Obj<ProviderSchema>
{
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	protected ProviderSchema schema;

	public Provider()
	{
		super();
	}

	public Provider(
        final ProviderFilter filters) 
    {
        super(filters);
		if(filters != null)
		{
			var schema = filters.getSchema() != null?
				filters.getSchema():
				new ObjSchemaFilter();
			
			schema.setName(filters.getName());
			schema.setCategory(filters.getCategory());
			
			this.schema = new ProviderSchema(schema, filters.getVendor());
		}
    }

	@Override
	public ProviderSchema getSchema() {
		return schema;
	}

	@Override
	public void setSchema(ProviderSchema schema) {
		this.schema = schema;
	}
}
