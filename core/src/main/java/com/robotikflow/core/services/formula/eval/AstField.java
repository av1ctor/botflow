package com.robotikflow.core.services.formula.eval;

import java.util.Map;

public class AstField extends AstBase 
{
	private final AstBase base;
	private final String name;
	
	public AstField(AstBase base, String name) 
	{
		this.base = base;
		this.name = name;
	}

	@Override
	public Object eval(EvalContext ctx) throws EvalException
	{
		var obj = base.eval(ctx);
		if((obj instanceof Map<?, ?>))
		{
			@SuppressWarnings("unchecked")
			var map = (Map<String, Object>)obj;

			if(!map.containsKey(name))
			{
				throw new EvalException(String.format("Field not defined: %s", name));
			}
			
			return map.get(name);
		}

		try
		{
			var field = obj.getClass().getField(name);
			if(field == null)
			{
				throw new EvalException(String.format("Field not defined: %s", name));
			}

			return field.get(obj);
		}
		catch(NoSuchFieldException ex)
		{
			throw new EvalException(String.format("Field not defined: %s", name));
		}
		catch(IllegalAccessException ex)
		{
			throw new EvalException(String.format("Illegal field access: %s", name));
		}
	}
}

