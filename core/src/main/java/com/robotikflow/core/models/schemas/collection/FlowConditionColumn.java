package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.nosql.FilterOperator;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrColumnOrValue;

public class FlowConditionColumn 
{
    @NotBlank
    private String name;
    @NotNull
    private FilterOperator op;
    @Valid
    private ScriptOrFunctionOrColumnOrValue value;

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
    public ScriptOrFunctionOrColumnOrValue getValue() {
        return value;
    }
    public void setValue(ScriptOrFunctionOrColumnOrValue value) {
        this.value = value;
    }
}
