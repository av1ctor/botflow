package com.robotikflow.core.models.schemas.collection.valuetype;

public class ScriptOrFunctionOrValue extends ScriptOrValue
{
    private FunctionNames function;
    
    public ScriptOrFunctionOrValue()
    {
    }

    public ScriptOrFunctionOrValue(
        final Object value)
    {
        super(value);
    }

    public ScriptOrFunctionOrValue(
        final ScriptOrFunctionOrValue value)
    {
        super(value);
        this.function = value.getFunction();
    }

    public FunctionNames getFunction() {
        return function;
    }

    public void setFunction(FunctionNames function) {
        this.function = function;
    }
}
