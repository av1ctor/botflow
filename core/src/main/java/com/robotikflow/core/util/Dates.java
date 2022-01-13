package com.robotikflow.core.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Dates 
{
    public static ZonedDateTime parse(
        final String date)
    {
        if(date == null || date.trim().length() == 0)
        {
            return null;
        }
        
        return ZonedDateTime.parse(
            addTimezone(removeMilliseconds(date)), 
            DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    private static String addTimezone(
        final String date 
    )
    {
        return date.length() < 25?
            date + "-03:00":
            date;
    }

    private static String removeMilliseconds(
        final String date 
    )
    {
        var dot = date.indexOf('.');
        return dot >= 0?
            date.substring(0, dot):
            date;
    }    
}