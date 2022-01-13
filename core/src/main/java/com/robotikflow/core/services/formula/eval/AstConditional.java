package com.robotikflow.core.services.formula.eval;

public class AstConditional extends AstBase
{
    private final AstBase c;
    private final AstBase t;
    private final AstBase f;

    public AstConditional(AstBase c, AstBase t, AstBase f) 
    {
        this.c = c;
        this.t = t;
        this.f = f;
    }
    
    @Override
    public Object eval(EvalContext ctx) throws EvalException 
    {
        return ((Boolean)c.eval(ctx))? t: f;
    }
}
