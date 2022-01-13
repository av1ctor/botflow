package com.robotikflow.core.services.formula.eval;

public class AstDecimalLit extends AstBase 
{
	private final Number value;
	
	public AstDecimalLit(Number value) 
	{
		this.value = value;
	}

	@Override
	public Object eval(EvalContext ctx) throws EvalException 
	{
		return value;
	}
}

