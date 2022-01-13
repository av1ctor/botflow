package com.robotikflow.core.models.schemas.collection.automation;

public class Condition 
{
    private ConditionType type;
    private ConditionField field;

    public ConditionType getType() {
        return type;
    }

    public void setType(ConditionType type) {
        this.type = type;
    }

    public ConditionField getField() {
        return field;
    }

    public void setField(ConditionField field) {
        this.field = field;
    }
}
