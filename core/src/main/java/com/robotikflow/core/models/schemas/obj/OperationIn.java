package com.robotikflow.core.models.schemas.obj;

import java.util.Map;

import javax.validation.Valid;

public class OperationIn 
{
    @Valid
    private Map<String, OperationField> fields;

    public Map<String, OperationField> getFields() {
        return fields;
    }
    public void setFields(Map<String, OperationField> fields) {
        this.fields = fields;
    }
}
