package com.robotikflow.core.models.request;

public class TriggerSchemaRequest
    extends ObjSchemaRequest
{
    private long options;

    public long getOptions() {
        return options;
    }

    public void setOptions(long options) {
        this.options = options;
    }
}