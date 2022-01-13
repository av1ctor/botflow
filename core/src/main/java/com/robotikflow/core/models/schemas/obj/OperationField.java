package com.robotikflow.core.models.schemas.obj;

import javax.validation.constraints.NotNull;

public class OperationField 
{
    @NotNull
    private FieldType type;
    private String title;
    private Boolean hidden;

    public OperationField() {
    }
    public OperationField(FieldType type) {
        this.type = type;
    }
    public FieldType getType() {
        return type;
    }
    public void setType(FieldType type) {
        this.type = type;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Boolean getHidden() {
        return hidden;
    }
    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }
}
