package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotBlank;

import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrColumnOrValue;
import com.robotikflow.core.services.nosql.AggregateOperator;

public class ReportColumn
{
	@NotBlank
	private String label;
	private String mask;
	private Boolean sortable;
    private String column;
	private AggregateOperator op;
	private FilterFieldType type;
    private ScriptOrFunctionOrColumnOrValue apply;
	
	public String getMask() {
		return mask;
	}
	public void setMask(String mask) {
		this.mask = mask;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public Boolean getSortable() {
		return sortable;
	}
	public void setSortable(Boolean sortable) {
		this.sortable = sortable;
	}
    public String getColumn() {
        return column;
    }
    public void setColumn(String column) {
        this.column = column;
    }
    public AggregateOperator getOp() {
        return op;
    }
    public void setOp(AggregateOperator op) {
        this.op = op;
    }
    public ScriptOrFunctionOrColumnOrValue getApply() {
        return apply;
    }
    public void setApply(ScriptOrFunctionOrColumnOrValue apply) {
        this.apply = apply;
    }
	public FilterFieldType getType() {
		return type;
	}
	public void setType(FilterFieldType type) {
		this.type = type;
	}
}
