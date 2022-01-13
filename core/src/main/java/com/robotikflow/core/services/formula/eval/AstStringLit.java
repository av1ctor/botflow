package com.robotikflow.core.services.formula.eval;

public class AstStringLit extends AstBase 
{
	private final String value;
	
	public AstStringLit(String value) 
	{
		this.value = value;
	}

	@Override
	public Object eval(EvalContext ctx) throws EvalException 
	{
		return value;
	}
}

