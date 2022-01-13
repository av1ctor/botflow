package com.robotikflow.core.models.entities;

import java.util.EnumSet;

public enum CollectionOptions 
{
    NONE(0L),
    NO_EDIT(1L),
    NO_ITEM_UPLOAD(2L),
    NO_ITEM_COMMENT(4L),
    NO_ITEM_CREATE(8L),
    NO_ITEM_UPDATE(16L),
    NO_ITEM_DELETE(32L),
    NO_EXPORT(64L),
    NO_LOG(128L);

    private long valor;

    private CollectionOptions(long valor) 
    {
        this.valor = valor;
    }

    public static EnumSet<CollectionOptions> from(long valor) 
    {
        var res = EnumSet.noneOf(CollectionOptions.class);
        for (var code : values()) 
        {
            if ((valor & code.valor) != 0) 
            {
                res.add(code);
            }
        }
        return res;
    }
     
    public static long to(EnumSet<CollectionOptions> list) 
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
