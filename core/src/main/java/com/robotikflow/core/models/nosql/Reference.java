package com.robotikflow.core.models.nosql;

public class Reference 
{
	private ReferenceType type;
	private String as;
	private String local;
	private String foreign;
	private String from;
	private Filter filters;
	
	public Reference()
	{
	}
	
	public Reference(ReferenceType type, String from, String local, String foreign, String as) 
	{
		this.type = type;
		this.from = from;
		this.local = local;
		this.foreign = foreign;
		this.as = as;
	}

	public Reference(ReferenceType type, String from, Filter filters, String as) 
	{
		this.type = type;
		this.from = from;
		this.filters = filters;
		this.as = as;
	}

	public String getAs() {
		return as;
	}
	public void setAs(String as) {
		this.as = as;
	}
	public String getLocal() {
		return local;
	}
	public void setLocal(String local) {
		this.local = local;
	}
	public String getForeign() {
		return foreign;
	}
	public void setForeign(String foreign) {
		this.foreign = foreign;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public ReferenceType getType() {
		return type;
	}
	public void setType(ReferenceType type) {
		this.type = type;
	}
	public Filter getFilters() {
		return filters;
	}
	public void setFilters(Filter filters) {
		this.filters = filters;
	}
}
