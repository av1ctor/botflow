package com.robotikflow.core.services.formula.eval;

public abstract class AstBase
{
    public AstBase() 
    {
    }
    
    public abstract Object eval(EvalContext ctx) throws EvalException;

	protected Object getOrCast(Object value, Object other) throws EvalException
	{
        if(value instanceof Integer)
		{
			if(other instanceof Double)
			{
				return (double)(Integer)value;
			}
			else if(other instanceof String)
			{
				return value.toString();
			}
			else if(other instanceof Boolean)
			{
				return ((Integer)value) != 0;
			}
            else if(other instanceof Long)
            {
                return (long)(Integer)value;
            }
            else if(other instanceof Integer)
            {
                return value;
            }
            else
            {
                throw new EvalException(String.format(
                    "Invalid implicit casting from %s to %s", value.getClass().getName(), other.getClass().getName()));
            }
        }
        else if(value instanceof Long)
		{
			if(other instanceof Double)
			{
				return (double)(Long)value;
			}
			else if(other instanceof String)
			{
				return value.toString();
			}
			else if(other instanceof Boolean)
			{
				return ((Long)value) != 0L;
			}
            else if(other instanceof Long || other instanceof Integer)
            {
                return value;
            }
            else
            {
                throw new EvalException(String.format(
                    "Invalid implicit casting from %s to %s", value.getClass().getName(), other.getClass().getName()));
            }
		}
        else if(value instanceof Double)
		{
			if(other instanceof String)
			{
				return value.toString();
			}
			else if(other instanceof Boolean)
			{
				return ((Double)value) != 0.0;
			}
            else if(other instanceof Double || other instanceof Long || other instanceof Integer)
            {
                return value;
            }
            else
            {
                throw new EvalException(String.format(
                    "Invalid implicit casting from %s to %s", value.getClass().getName(), other.getClass().getName()));
            }
		}
        else if(value instanceof String)
		{
            if(other instanceof Boolean)
			{
				return !((String)value).equals("false") && !((String)value).equals("0");
            }		
            else if(other instanceof String)
            {
                return value;
            }
            else
            {
                throw new EvalException(String.format(
                    "Invalid implicit casting from %s to %s", value.getClass().getName(), other.getClass().getName()));
            }
        }

		return value;
	}    
}
