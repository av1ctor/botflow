package com.robotikflow.core.services.formula.eval;

public class AstIntegerLit extends AstBase 
{
	private final Number value;
	
	public AstIntegerLit(Number value) 
	{
		this.value = value;
	}

	@Override
	public Object eval(EvalContext ctx) throws EvalException 
	{
		return value;
	}
}
