package com.robotikflow.core.models.filters;

public class ProviderFilter 
	extends ObjFilter
{
	private String vendor;

	public ProviderFilter()
	{
		super();
	}
	public ProviderFilter(Long id)
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
