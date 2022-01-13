package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.nosql.FilterOperator;

public class FieldDisabledValue 
{
    @NotNull
    private FilterOperator op;
    private Object value;

    public FilterOperator getOp() {
        return op;
    }
    public void setOp(FilterOperator op) {
        this.op = op;
    }
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
}
