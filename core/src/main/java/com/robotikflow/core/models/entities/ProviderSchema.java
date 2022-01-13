package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.ObjSchemaFilter;

@Entity
@Table(name = "providers_schemas")
public class ProviderSchema
	extends ObjSchema
{
	@NotNull
	private String vendor;

	public ProviderSchema()
	{
		super();
	}

	public ProviderSchema(
		final ObjSchemaFilter filters,
		final String vendor)
	{
		super(filters);
		this.vendor = vendor;
	}
	
	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
}
