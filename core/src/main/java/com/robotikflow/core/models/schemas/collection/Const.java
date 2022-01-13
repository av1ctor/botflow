package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrValue;

public class Const 
{
    @NotBlank
    private String label;
    @NotNull
    @Valid
    private ScriptOrFunctionOrValue value;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ScriptOrFunctionOrValue getValue() {
        return value;
    }

    public void setValue(ScriptOrFunctionOrValue value) {
        this.value = value;
    }
}
