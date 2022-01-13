package com.robotikflow.core.services.formula.eval;

import java.util.LinkedList;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;

public class AstFunction extends AstBase 
{
	private final String name;
	private final AstBase base;
	private final AstArg arg;
	
	public AstFunction(String name, AstArg arg) 
	{
		this.base = null;
		this.name = name;
		this.arg = arg;
	}

	public AstFunction(AstBase base, AstArg arg) 
	{
		this.base = base;
		this.name = null;
		this.arg = arg;
	}

	public AstFunction(AstBase base, String name, AstArg arg) 
	{
		this.base = base;
		this.name = name;
		this.arg = arg;
	}

	private Object[] evalArgs(EvalContext ctx) throws EvalException 
	{
		var res = new LinkedList<Object>();

		var n = arg;
		while(n != null)
		{
			var expr = n.eval(ctx);
			res.addFirst(expr);
			n = n.getPrev();
		};
		
		return res.toArray();
	}

	private Object callGlobal(EvalContext ctx) throws EvalException
	{
		var function = (EvalFunction)ctx.get(name);
		if (function == null) 
		{
			throw new EvalException(String.format("Function not defined: %s", name));
		}

		var args = evalArgs(ctx);

		return function.exec(args);
	}

	private Object callMethod(EvalContext ctx) throws EvalException 
	{
		var args = evalArgs(ctx);

		var obj = base.eval(ctx);
		if (obj instanceof Map<?, ?>) 
		{
			@SuppressWarnings("unchecked")
			var function = (EvalFunction) ((Map<String, Object>)obj).get(name);
			if (function == null) 
			{
				throw new EvalException(String.format("Function not defined: %s", name));
			}

			return function.exec(args);
		}

		var parameterType = new Class[args.length];
		for (var i = 0; i < args.length; i++) 
		{
			parameterType[i] = args[i].getClass();
		}

		try 
		{
			var function = obj.getClass().getMethod(name, parameterType);
			if (function == null) 
			{
				throw new EvalException(String.format("Function not defined: %s", name));
			}

			return function.invoke(obj, args);
		} 
		catch (IllegalAccessException ex) 
		{
			throw new EvalException(String.format("Illegal access: %s", name));
		} 
		catch (NoSuchMethodException ex) 
		{
			throw new EvalException(String.format("Function not defined: %s", name));
		} 
		catch (InvocationTargetException ex) 
		{
			throw new EvalException(String.format("Exception while calling: %s", name));
		}
	}

	@Override
	public Object eval(EvalContext ctx) throws EvalException 
	{
		if(base == null)
		{
			return callGlobal(ctx);
		}
		else if(name == null)
		{
			throw new EvalException("Calling anonymous function not supported yet");
		}
		else
		{
			return callMethod(ctx);
		}
	}
}
