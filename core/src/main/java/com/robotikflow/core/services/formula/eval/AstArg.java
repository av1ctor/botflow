package com.robotikflow.core.services.formula.eval;

public class AstArg extends AstBase
{
    private final AstBase arg;
    private final AstArg prev;

    public AstArg(AstBase arg) 
    {
        this.arg = arg;
        this.prev = null;
    }
    
    public AstArg(AstArg prev, AstBase arg) 
    {
        this.arg = arg;
        this.prev = prev;
    }

    @Override
    public Object eval(EvalContext ctx) throws EvalException 
    {
        return arg.eval(ctx);
    }

    public AstArg getPrev() {
        return prev;
    }
}
