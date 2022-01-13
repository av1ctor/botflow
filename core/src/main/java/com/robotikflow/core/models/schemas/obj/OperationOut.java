package com.robotikflow.core.models.schemas.obj;

import java.util.Map;

import javax.validation.Valid;

public class OperationOut 
{
    private Boolean multiple;
    @Valid
    private Map<String, OperationField> fields;

    public boolean isMultiple() {
        return multiple != null && multiple.booleanValue()?
            true:
            false;
    }
    public Boolean getMultiple() {
        return multiple;
    }
    public void setMultiple(Boolean multiple) {
        this.multiple = multiple;
    }
    public Map<String, OperationField> getFields() {
        return fields;
    }
    public void setFields(Map<String, OperationField> fields) {
        this.fields = fields;
    }
}
