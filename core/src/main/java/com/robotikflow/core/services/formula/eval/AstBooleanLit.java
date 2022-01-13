package com.robotikflow.core.services.formula.eval;

public class AstBooleanLit extends AstBase 
{
	private final Boolean value;
	
	public AstBooleanLit(Boolean value) 
	{
		this.value = value;
	}

	@Override
	public Object eval(EvalContext ctx) throws EvalException 
	{
		return value;
	}
}

