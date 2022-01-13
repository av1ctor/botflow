package com.robotikflow.core.models.filters;

public class CredentialFilter 
	extends ObjFilter
{
	private String vendor;

	public CredentialFilter()
	{
		super();
	}
	public CredentialFilter(Long id)
	{
		super(id);
	}

	public String getVendor() {
		return vendor;
	}
	public void setVendor(String vendor) {
		this.vendor = vendor;
	}
}
