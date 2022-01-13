package com.robotikflow.core.util.expr;

import java.util.function.Function;

import com.robotikflow.core.models.schemas.expr.LogicalExpr;

public class Evaluator 
{
    public static <V> boolean logicalEval(
        final LogicalExpr<V> expr,
        final Function<V, Boolean> eval)
    {
        if(expr == null)
        {
            return false;
        }

        if(expr.getAnd() != null)
        {
            for(var child : expr.getAnd())
            {
                if(!logicalEval(child, eval))
                {
                    return expr.isNeg()? 
                        true: 
                        false;
                }
            }

            return expr.isNeg()? 
                false: 
                true;
        }
        else if(expr.getOr() != null)
        {
            for(var child : expr.getOr())
            {
                if(logicalEval(child, eval))
                {
                    return expr.isNeg()? 
                        false:
                        true;
                }
            }            

            return expr.isNeg()?
                true:
                false;
        }
        else
        {
            return eval.apply(expr.getCond());
        }
    }
}