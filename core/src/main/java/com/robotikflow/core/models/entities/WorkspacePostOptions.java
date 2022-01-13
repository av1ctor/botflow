package com.robotikflow.core.models.entities;

import java.util.EnumSet;

public enum WorkspacePostOptions 
{
    NONE(0L),
    NO_EDIT(1L);

    private long value;

    private WorkspacePostOptions(
        long value) 
    {
        this.value = value;
    }

    public static EnumSet<WorkspacePostOptions> from(
        long value) 
    {
        var res = EnumSet.noneOf(WorkspacePostOptions.class);
        for (var code : values()) 
        {
            if ((value & code.value) != 0) 
            {
                res.add(code);
            }
        }
        return res;
    }
     
    public static long to(
        EnumSet<WorkspacePostOptions> list) 
    {
        long res = 0;
        for (var code : list) 
        {
            res |= code.value;
        }
        return res;
    }

    public long asLong() {
        return value;
    }
}
