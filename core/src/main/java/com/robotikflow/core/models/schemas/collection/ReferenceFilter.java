package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.nosql.FilterOperator;

public class ReferenceFilter 
{
    @NotNull
    private String column;
    private FilterOperator op;

    public String getColumn() {
        return column;
    }
    public void setColumn(String column) {
        this.column = column;
    }
    public FilterOperator getOp() {
        return op;
    }
    public void setOp(FilterOperator op) {
        this.op = op;
    }
}
