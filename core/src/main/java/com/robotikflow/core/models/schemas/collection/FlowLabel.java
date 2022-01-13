package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public class FlowLabel 
{
    @NotBlank
    private String value;
    @Valid
    private FlowBounds bounds;

    public FlowLabel() {
    }
    
    public FlowLabel(String value) 
    {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public FlowBounds getBounds() {
        return bounds;
    }

    public void setBounds(FlowBounds bounds) {
        this.bounds = bounds;
    }
}
