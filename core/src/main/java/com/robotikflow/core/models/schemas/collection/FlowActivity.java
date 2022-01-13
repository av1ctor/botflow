package com.robotikflow.core.models.schemas.collection;

import java.util.Map;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class FlowActivity 
{
    @NotBlank
    private String schemaId;
    @NotNull
    private Map<@NotBlank String, Object> fields;

    public String getSchemaId() {
        return schemaId;
    }
    public void setSchemaId(String schemaId) {
        this.schemaId = schemaId;
    }
    public Map<String, Object> getFields() {
        return fields;
    }
    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }
}
