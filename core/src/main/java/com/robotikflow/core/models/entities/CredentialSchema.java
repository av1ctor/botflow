package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.ObjSchemaFilter;
import com.robotikflow.core.models.schemas.credential.CredentialMode;

@Entity
@Table(name = "credentials_schemas")
public class CredentialSchema
	extends ObjSchema
{
	@NotNull
	private String vendor;
	
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private CredentialMode mode;

	public CredentialSchema()
	{
		super();
	}

	public CredentialSchema(
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

	public CredentialMode getMode() {
		return mode;
	}

	public void setMode(CredentialMode mode) {
		this.mode = mode;
	}
}
