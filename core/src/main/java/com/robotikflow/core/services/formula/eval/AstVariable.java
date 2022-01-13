package com.robotikflow.core.services.formula.eval;

public class AstVariable extends AstBase 
{
	private final String name;
	
	public AstVariable(String name) 
	{
		this.name = name;
	}

	@Override
	public Object eval(EvalContext ctx) throws EvalException
	{
		if(!ctx.containsKey(name))
		{
			throw new EvalException(String.format("Variable not defined: %s", name));
		}
		
		return ctx.get(name);
	}
}

