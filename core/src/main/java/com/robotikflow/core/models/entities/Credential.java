package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.CredentialFilter;
import com.robotikflow.core.models.filters.ObjSchemaFilter;

@Entity
@Table(name = "credentials")
public class Credential
	extends Obj<CredentialSchema>
{
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	protected CredentialSchema schema;
	
	public Credential()
	{
		super();
	}

	public Credential(
        final CredentialFilter filters) 
    {
        super(filters);
		if(filters != null)
		{
			var schema = filters.getSchema() != null?
				filters.getSchema():
				new ObjSchemaFilter();
			
			schema.setName(filters.getName());
			schema.setCategory(filters.getCategory());
			
        	this.schema = new CredentialSchema(schema, filters.getVendor());
		}
    }

	@Override
	public CredentialSchema getSchema() {
		return schema;
	}

	@Override
	public void setSchema(CredentialSchema schema) {
		this.schema = schema;
	}
}
