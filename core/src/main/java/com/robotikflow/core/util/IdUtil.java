package com.robotikflow.core.util;

import org.bson.types.ObjectId;

public class IdUtil
{
    public static String genId()
    {
        return new ObjectId().toHexString();
    }
}