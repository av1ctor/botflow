package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.nosql.FilterOperator;

public class CollectionQuery 
{
	@NotNull
	private String at;
	@NotNull
	private String localKey;
	@NotNull
	private String foreignKey;
	private String criteria;
	private FilterOperator op;
	
	public String getAt() {
		return at;
	}
	public void setAt(String at) {
		this.at = at;
	}
	public String getLocalKey() {
		return localKey;
	}
	public void setLocalKey(String localKey) {
		this.localKey = localKey;
	}
	public String getForeignKey() {
		return foreignKey;
	}
	public void setForeignKey(String foreignKey) {
		this.foreignKey = foreignKey;
	}
	public String getCriteria() {
		return criteria;
	}
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
	public FilterOperator getOp() {
		return op;
	}
	public void setOp(FilterOperator op) {
		this.op = op;
	}
}
