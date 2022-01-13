package com.robotikflow.core.models.schemas.collection.automation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.nosql.FilterOperator;

public class ConditionField 
{
    @NotBlank
    private String name;
    @NotNull
    private FilterOperator op;
    private ConditionFieldType type;
    private Object value;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FilterOperator getOp() {
        return op;
    }

    public void setOp(FilterOperator op) {
        this.op = op;
    }

    public ConditionFieldType getType() {
        return type;
    }

    public void setType(ConditionFieldType type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
