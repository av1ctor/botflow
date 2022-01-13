package com.robotikflow.core.services.formula.eval;

public class AstAddOp extends AstBase 
{
	private final Operator op;
	private final AstBase l;
	private final AstBase r;

	public AstAddOp(Operator op, AstBase l, AstBase r) 
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
		case ADD:
			if(lhs instanceof Integer)
			{
				res = ((Integer)lhs).intValue() + ((Integer)rhs).intValue();
			}					
			else if(lhs instanceof Long)
			{
				res = ((Long)lhs).longValue() + ((Long)rhs).longValue();
			}					
			else if(lhs instanceof Double)
			{
				res = ((Double)lhs).doubleValue() + ((Double)rhs).doubleValue();
			}
			else if(lhs instanceof String)
			{
				res = (String)lhs + (String)rhs;
			}
			else
			{
				throw new EvalException("Invalid operator: '+'");
			}
			break;

		case SUB:
			if(lhs instanceof Integer)
			{
				res = ((Integer)lhs).intValue() - ((Integer)rhs).intValue();
			}
			else if(lhs instanceof Long)
			{
				res = ((Long)lhs).longValue() - ((Long)rhs).longValue();
			}
			else if(lhs instanceof Double)
			{
				res = ((Double)lhs).doubleValue() - ((Double)rhs).doubleValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '-'");
			}
			break;

		case OR:
			if(lhs instanceof Integer)
			{
				res = ((Integer)lhs).intValue() | ((Integer)rhs).intValue();
			}
			else if(lhs instanceof Long)
			{
				res = ((Long)lhs).longValue() | ((Long)rhs).longValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '|'");
			}
			break;

		case AND:
			if(lhs instanceof Integer)
			{
				res = ((Integer)lhs).intValue() & ((Integer)rhs).intValue();
			}
			else if(lhs instanceof Long)
			{
				res = ((Long)lhs).longValue() & ((Long)rhs).longValue();
			}
			else
			{
				throw new EvalException("Invalid operator: '&'");
			}
			break;

		default:
			throw new EvalException("Invalid operator");
		}
		
		return res;
	}
}

