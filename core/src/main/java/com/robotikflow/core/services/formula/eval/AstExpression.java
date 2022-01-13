package com.robotikflow.core.services.formula.eval;

public class AstExpression extends AstBase 
{
	private final AstBase expr;
	
	public AstExpression(AstBase expr) 
	{
		this.expr = expr;
	}

	@Override
	public Object eval(EvalContext ctx) throws EvalException
	{
		return expr.eval(ctx);
	}
}

