package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.nosql.FilterOperator;

public class FilterField 
{
	@NotNull
	private String label;
	private String mask;
	private FilterFieldType type;
	private FilterOperator op;
	private Object _default;
	private Long min;
	private Long max;
	private Boolean template;
	
	public FilterField() {
	}
	
	public FilterField(String label) 
	{
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getMask() {
		return mask;
	}
	public void setMask(String mask) {
		this.mask = mask;
	}
	public FilterFieldType getType() {
		return type;
	}
	public void setType(FilterFieldType type) {
		this.type = type;
	}
	public FilterOperator getOp() {
		return op;
	}
	public void setOp(FilterOperator op) {
		this.op = op;
	}
	public Object getDefault() {
		return _default;
	}
	public void setDefault(Object _default) {
		this._default = _default;
	}
	public Long getMin() {
		return min;
	}
	public void setMin(Long min) {
		this.min = min;
	}
	public Long getMax() {
		return max;
	}
	public void setMax(Long max) {
		this.max = max;
	}
	public boolean isTemplate() {
		return template != null? template: false;
	}
	public Boolean getTemplate() {
		return template;
	}
	public void setTemplate(Boolean template) {
		this.template = template;
	}
}
