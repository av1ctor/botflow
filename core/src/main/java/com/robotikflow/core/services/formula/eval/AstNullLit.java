package com.robotikflow.core.services.formula.eval;

public class AstNullLit extends AstBase 
{
	public AstNullLit()
	{
	}

	@Override
	public Object eval(EvalContext ctx) throws EvalException 
	{
		return null;
	}
}

