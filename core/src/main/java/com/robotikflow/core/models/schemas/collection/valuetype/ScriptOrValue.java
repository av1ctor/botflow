package com.robotikflow.core.models.schemas.collection.valuetype;

public class ScriptOrValue 
{
    private String script;
    private Object value;

    public ScriptOrValue()
    {
    }

    public ScriptOrValue(Object value)
    {
        this.value = value;
    }

    public ScriptOrValue(
        final ScriptOrValue value)
    {
        this.script = value.getScript();
        this.value = value.getValue();
    }

    public String getScript() {
        return script;
    }

	public void setScript(String script) {
        this.script = script;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
