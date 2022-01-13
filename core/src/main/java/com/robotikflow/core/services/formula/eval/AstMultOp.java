package com.robotikflow.core.services.formula.eval;

public class AstMultOp extends AstBase 
{
	private final Operator op;
	private final AstBase l;
	private final AstBase r;

	public AstMultOp(Operator op, AstBase l, AstBase r) 
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
		lhs = getOrCast(lhs, rhs);
		rhs = getOrCast(rhs, lhs);

		switch(op)
		{
		case MULT:
			if(lhs instanceof Integer)
			{
				res = ((Integer)lhs).intValue() * ((Integer)rhs).intValue();
			}
			else if(lhs instanceof Long)
			{
				res = ((Long)lhs).longValue() * ((Long)rhs).longValue();
			}
			else if(lhs instanceof Double)
			{
				res = ((Double)lhs).doubleValue() * ((Double)rhs).doubleValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '*'");
			}
			break;

		case DIV:
			if(lhs instanceof Integer)
			{
				res = ((Integer)lhs).intValue() / ((Integer)rhs).intValue();
			}
			else if(lhs instanceof Long)
			{
				res = ((Long)lhs).longValue() / ((Long)rhs).longValue();
			}
			else if(lhs instanceof Double)
			{
				res = ((Double)lhs).doubleValue() / ((Double)rhs).doubleValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '/'");
			}
			break;

		case MOD:
			if(lhs instanceof Integer)
			{
				res = ((Integer)lhs).intValue() % ((Integer)rhs).intValue();
			}
			else if(lhs instanceof Long)
			{
				res = ((Long)lhs).longValue() % ((Long)rhs).longValue();
			}
			else if(lhs instanceof Double)
			{
				res = ((Double)lhs).doubleValue() % ((Double)rhs).doubleValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '%'");
			}
			break;

		case POW:
			if(lhs instanceof Integer)
			{
				res = Math.pow(((Integer)lhs).intValue(), ((Integer)rhs).intValue());
			}
			else if(lhs instanceof Long)
			{
				res = Math.pow(((Long)lhs).longValue(), ((Long)rhs).longValue());
			}
			else if(lhs instanceof Double)
			{
				res = Math.pow(((Double)lhs).doubleValue(), ((Double)rhs).doubleValue());
			}
			else
			{
				throw new EvalException("Invalid operator: '^'");
			}
			break;

		case SHL:
			if(lhs instanceof Integer)
			{
				res = ((Integer)lhs).intValue() << ((Integer)rhs).intValue();
			}
			else if(lhs instanceof Long)
			{
				res = ((Long)lhs).longValue() << ((Long)rhs).longValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '<<'");
			}
			break;

		case SHR:
			if(lhs instanceof Integer)
			{
				res = ((Integer)lhs).intValue() >> ((Integer)rhs).intValue();
			}
			else if(lhs instanceof Long)
			{
				res = ((Long)lhs).longValue() >> ((Long)rhs).longValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '<<'");
			}
			break;

		default:
			throw new EvalException("Invalid operator");
		}

		return res;
	}
}

