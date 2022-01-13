package com.robotikflow.core.models.schemas.collection;

import java.util.Map;

public class ViewFieldColor 
{
	private String value;
	private Map<Object, String> criteria;
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Map<Object, String> getCriteria() {
		return criteria;
	}
	public void setCriteria(Map<Object, String> criteria) {
		this.criteria = criteria;
	}
	
}
