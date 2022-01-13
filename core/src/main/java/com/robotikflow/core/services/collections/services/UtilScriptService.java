package com.robotikflow.core.services.collections.services;

import java.util.List;
import java.util.Set;

public class UtilScriptService
{
	public String format(final String pattern, final Object... args)
	{
		return String.format(pattern, args);
	}
	
	public Boolean isEmpty(final Object value)
	{
		if(value == null)
		{
			return true;
		}
		
		if(value instanceof String)
		{
			return ((String)value).isEmpty();
		}
		else if(value instanceof Set)
		{
			return ((Set<?>)value).isEmpty();
		}
		else if(value instanceof List)
		{
			return ((List<?>)value).isEmpty();
		}
		else if(value instanceof Object[])
		{
			return ((Object[])value).length == 0;
		}
		
		return false;
	}
	
	public String addDv(final String value)
	{
		var sum = 0;
		var factor = 1;
		for(var i = value.length() - 1; i >= 0; i--)
		{
			var c = value.charAt(i) - '0';
			sum += factor * c;
			factor += 1;
		}
		
		var rem = sum % 10;
		
		return value + String.valueOf((char)('0' + (rem == 0? 0: 10 - rem)));
	}
}

