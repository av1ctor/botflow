package com.robotikflow.core.models.nosql;

public class Filter 
{
	private FilterOperator op;
	private String name;
	private Object value1;
	private Object value2;
	private Filter lhs;
	private Filter rhs;
	
	public Filter(FilterOperator op, String name)
	{
		this.op = op;
		this.name = name;
	}
	
	public Filter(FilterOperator op, String name, Object value)
	{
		this.op = op;
		this.name = name;
		this.value1 = value;
	}
	
	public Filter(FilterOperator op, String name, Object value1, Object value2)
	{
		this.op = op;
		this.name = name;
		this.value1 = value1;
		this.value2 = value2;
	}
	
	public Filter(FilterOperator op, Filter lhs) 
	{
		this.op = op;
		this.lhs = lhs;
		this.rhs = null;
	}
	
	public Filter(FilterOperator op, Filter lhs, Filter rhs) 
	{
		this.op = op;
		this.lhs = lhs;
		this.rhs = rhs;
	}
	
	public static Filter by(FilterOperator op, Filter lhs, Filter rhs)
	{
		if(lhs == null)
		{
			return rhs;
		}
		
		if(rhs == null)
		{
			return lhs;
		}
		
		return new Filter(op, lhs, rhs);
	}
	
	public FilterOperator getOp() {
		return op;
	}
	public void setOp(FilterOperator op) {
		this.op = op;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Object getValue1() {
		return value1;
	}
	public void setValue1(Object value) {
		this.value1 = value;
	}
	public Object getValue2() {
		return value2;
	}
	public void setValue2(Object value) {
		this.value2 = value;
	}
	public Filter getLhs() {
		return lhs;
	}
	public void setLhs(Filter lhs) {
		this.lhs = lhs;
	}
	public Filter getRhs() {
		return rhs;
	}
	public void setRhs(Filter rhs) {
		this.rhs = rhs;
	}
}
