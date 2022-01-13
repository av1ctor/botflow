package com.robotikflow.core.services.collections.services;

import java.util.Arrays;

import org.bson.Document;

public class DbScriptService
{
	private Object getValueOrField(Object value)
	{
		if(value == null)
			return null;
		
		if(!(value instanceof String))
			return value;
		
		var valueStr = (String)value;
		
		switch(valueStr.charAt(0))
		{
		case '$':
		case '-':
		case '+':
		case '*':
		case '/':
		case '.':
		case ',':
		case '(':
		case ')':
		case '=':
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		case ' ':
		case '\t':
		case '\r':
		case '\n':
		case '\'':
		case '\"':
		case '\\':
			return value;
		}
			
		return '$' + valueStr;
	}

	public Document toDate(Object value)
	{
		return new Document("$toDate", getValueOrField(value));
	}

	public Document toDate(String value)
	{
		return toDate((Object)value);
	}

	public Document year(Object value)
	{
		return new Document("$year", getValueOrField(value));
	}

	public Document year(String value)
	{
		return year((Object)value);
	}

	public Document year(Object value, String timeZone)
	{
		return new Document("$year", new Document("date", getValueOrField(value)).append("timezone", timeZone));
	}

	public Document year(String value, String timeZone)
	{
		return year((Object)value, timeZone);
	}

	public Document month(Object value)
	{
		return new Document("$month", getValueOrField(value));
	}

	public Document month(String value)
	{
		return month((Object)value);
	}

	public Document month(Object value, String timeZone)
	{
		return new Document("$month", new Document("date", getValueOrField(value)).append("timezone", timeZone));
	}

	public Document month(String value, String timeZone)
	{
		return month((Object)value, timeZone);
	}

	public Document day(Object value)
	{
		return new Document("$dayOfMonth", getValueOrField(value));
	}

	public Document day(String value)
	{
		return day((Object)value);
	}

	public Document day(Object value, String timeZone)
	{
		return new Document("$dayOfMonth", new Document("date", getValueOrField(value)).append("timezone", timeZone));
	}

	public Document day(String value, String timeZone)
	{
		return day((Object)value, timeZone);
	}

	public Document hour(Object value)
	{
		return new Document("$hour", getValueOrField(value));
	}

	public Document hour(String value)
	{
		return hour((Object)value);
	}

	public Document hour(Object value, String timeZone)
	{
		return new Document("$hour", new Document("date", getValueOrField(value)).append("timezone", timeZone));
	}

	public Document hour(String value, String timeZone)
	{
		return hour((Object)value, timeZone);
	}

	public Document minute(Object value)
	{
		return new Document("$minute", getValueOrField(value));
	}

	public Document minute(String value)
	{
		return minute((Object)value);
	}

	public Document minute(Object value, String timeZone)
	{
		return new Document("$minute", new Document("date", getValueOrField(value)).append("timezone", timeZone));
	}

	public Document minute(String value, String timeZone)
	{
		return minute((Object)value, timeZone);
	}

	public Document second(Object value)
	{
		return new Document("$second", getValueOrField(value));
	}

	public Document second(String value)
	{
		return second((Object)value);
	}

	public Document second(Object value, String timeZone)
	{
		return new Document("$second", new Document("date", getValueOrField(value)).append("timezone", timeZone));
	}

	public Document second(String value, String timeZone)
	{
		return second((Object)value, timeZone);
	}

	public Document divide(Object lvalue, Object rvalue)
	{
		return new Document("$divide", Arrays.asList(getValueOrField(lvalue), getValueOrField(rvalue)));
	}

	public Document subtract(Object lvalue, Object rvalue)
	{
		return new Document("$subtract", Arrays.asList(getValueOrField(lvalue), getValueOrField(rvalue)));
	}

	public Document dateDiff(Object lvalue, Object rvalue, String type)
	{
		var divisor = 1L;
		switch(type)
		{
			case "millis":
				divisor = 1L;
				break;
			case "seconds":
				divisor = 1000L;
				break;
			case "minutes":
				divisor = 1000L*60;
				break;
			case "hours":
				divisor = 1000L*60*60;
				break;
			case "days":
				divisor = 1000L*60*60*24;
				break;
		}
		return divide(subtract(lvalue, rvalue), divisor);
	}
}
