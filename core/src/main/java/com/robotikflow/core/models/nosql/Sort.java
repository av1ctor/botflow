package com.robotikflow.core.models.nosql;

public class Sort 
{
	private String name;
	private boolean isAsc;
	
	public Sort() 
	{
	}
	public Sort(String name, boolean isAsc) 
	{
		this.name = name;
		this.isAsc = isAsc;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isAsc() {
		return isAsc;
	}
	public void setAsc(boolean isAsc) {
		this.isAsc = isAsc;
	}
	
}
