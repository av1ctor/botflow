package com.robotikflow.core.models.request;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.robotikflow.core.models.nosql.FilterOperator;

public class CollectionFilterRequest
{
    @NotBlank
    private String name;
    @Valid
    private FilterOperator op;
    private Object value;

    public CollectionFilterRequest() {
    }

    public CollectionFilterRequest(String name, Object value) {
        this.name = name;
        this.op = FilterOperator.eq;
        this.value = value;
    }
    
    public CollectionFilterRequest(String name, FilterOperator op, Object value) {
        this.name = name;
        this.op = op;
        this.value = value;
    }

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
    public Object getValue() {
        return value;
    }
    public void setValue(Object value) {
        this.value = value;
    }
}