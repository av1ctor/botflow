package com.robotikflow.core.models.schemas.collection.valuetype;

public class ScriptOrValueOrRegex extends ScriptOrFunctionOrValue
{
    private String regex;

    public ScriptOrValueOrRegex()
    {
    }

    public ScriptOrValueOrRegex(Object value)
    {
        super(value);
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
}
