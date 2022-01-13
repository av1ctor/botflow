package com.robotikflow.core.models.schemas.collection.valuetype;

public class ScriptOrFunctionOrColumnOrValue extends ScriptOrFunctionOrValue
{
    private String column;

    public ScriptOrFunctionOrColumnOrValue()
    {
    }

    public ScriptOrFunctionOrColumnOrValue(Object value)
    {
        super(value);
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }
}
