package com.robotikflow.core.services.formula.eval;

public class AstUop extends AstBase 
{
	private final Operator op;
	private final AstBase l;
	
	public AstUop(Operator op, AstBase l) 
	{
		this.op = op;
		this.l = l;
	}

	@Override
	public Object eval(EvalContext ctx) throws EvalException
	{
		Object res = null;

		var lhs = l.eval(ctx);
		switch(op)
		{
		case SUB:
			if(lhs instanceof Integer)
			{
				res = -((Integer)lhs).intValue();
			}
			else if(lhs instanceof Long)
			{
				res = -((Long)lhs).longValue();
			}
			else if(lhs instanceof Double)
			{
				res = -((Double)lhs).doubleValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '-'");
			}
			break;

		case NOT:
			if(lhs instanceof Boolean)
			{
				res = !((Boolean)lhs).booleanValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '!'");
			}
			break;

		default:
			throw new EvalException("Invalid operator");
		}
				
		return res;
	}
}

