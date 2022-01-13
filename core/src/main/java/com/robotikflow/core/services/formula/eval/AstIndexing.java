package com.robotikflow.core.services.formula.eval;

import java.util.ArrayList;

public class AstIndexing extends AstBase 
{
	private final AstBase index;
	private final AstBase base;
	
	public AstIndexing(AstBase index, AstBase base) 
	{
		this.index = index;
		this.base = base;
	}

	public Object eval(EvalContext ctx) throws EvalException
	{
		var obj = base.eval(ctx);
		if(!(obj instanceof ArrayList<?>))
		{
			throw new EvalException("Trying to index a non array");
		}
	
		var i = index.eval(ctx);
		if(i == null || !(i instanceof Integer || i instanceof Long))
		{
			throw new EvalException("Invalid indexing");
		}

		@SuppressWarnings("unchecked")
		var arr = ((ArrayList<Object>)obj);
		var idx = ((Number)i).intValue();
		if(idx < 0 || idx >= arr.size())
		{
			throw new EvalException("Index out of bounds");
		}

		obj = arr.get(idx);
		
		return obj;
	}
}

