package com.robotikflow.core.models.schemas.collection.automation;

import javax.validation.Valid;

import com.robotikflow.core.models.schemas.expr.LogicalExpr;

public class AutomationSchema
{
    @Valid
    private LogicalExpr<Condition> condition;

    public LogicalExpr<Condition> getCondition() {
        return condition;
    }

    public void setCondition(LogicalExpr<Condition> condition) {
        this.condition = condition;
    }
}