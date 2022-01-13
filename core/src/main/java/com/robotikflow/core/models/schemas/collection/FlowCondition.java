package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;

public class FlowCondition 
{
    @Valid
    private FlowConditionColumn column;
    @Valid
    private FlowConditionAutomation automation;

    public FlowConditionColumn getColumn() {
        return column;
    }
    public void setColumn(FlowConditionColumn column) {
        this.column = column;
    }
    public FlowConditionAutomation getAutomation() {
        return automation;
    }
    public void setAutomation(FlowConditionAutomation automation) {
        this.automation = automation;
    }
}
