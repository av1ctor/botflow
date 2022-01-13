package com.robotikflow.core.models.entities;

import java.util.EnumSet;

public enum TriggerOptions 
{
    NONE(0L),
    ALLOW_FREQ(1L);

    private long valor;

    private TriggerOptions(long valor) 
    {
        this.valor = valor;
    }

    public static EnumSet<TriggerOptions> from(long valor) 
    {
        var res = EnumSet.noneOf(TriggerOptions.class);
        for (var code : values()) 
        {
            if ((valor & code.valor) != 0) 
            {
                res.add(code);
            }
        }
        return res;
    }
     
    public static long to(EnumSet<TriggerOptions> list) 
    {
        long res = 0;
        for (var code : list) 
        {
            res |= code.valor;
        }
        return res;
    }

    public long asLong() {
        return valor;
    }
}
