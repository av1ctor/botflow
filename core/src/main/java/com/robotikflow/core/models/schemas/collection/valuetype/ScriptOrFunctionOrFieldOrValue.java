package com.robotikflow.core.models.schemas.collection.valuetype;

public class ScriptOrFunctionOrFieldOrValue 
    extends ScriptOrFunctionOrValue
{
    private String field;

    public ScriptOrFunctionOrFieldOrValue()
    {
    }

    public ScriptOrFunctionOrFieldOrValue(
        final Object value)
    {
        super(value);
    }

    public ScriptOrFunctionOrFieldOrValue(
        final ScriptOrFunctionOrValue value)
    {
        super(value);
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }
}
