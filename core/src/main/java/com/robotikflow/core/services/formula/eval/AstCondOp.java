package com.robotikflow.core.services.formula.eval;

public class AstCondOp extends AstBase 
{
	private final Operator op;
	private final AstBase l;
	private final AstBase r;

	public AstCondOp(Operator op, AstBase l, AstBase r) 
	{
		this.op = op;
		this.l = l; 
		this.r = r;
	}

	@Override
	public Object eval(EvalContext ctx) throws EvalException
	{
		Object res = null;

		var lhs = l.eval(ctx);
		var rhs = r.eval(ctx);

		switch(op)
		{
		case LAND:
			if(lhs instanceof Boolean && rhs instanceof Boolean)
			{
				res = ((Boolean)lhs).booleanValue() && ((Boolean)rhs).booleanValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '&&'");
			}
			break;

		case LOR:
			if(lhs instanceof Boolean && rhs instanceof Boolean)
			{
				res = ((Boolean)lhs).booleanValue() || ((Boolean)rhs).booleanValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '||'");
			}
			break;

		default:
			throw new EvalException("Invalid operator");
		}

		return res;
	}
}

