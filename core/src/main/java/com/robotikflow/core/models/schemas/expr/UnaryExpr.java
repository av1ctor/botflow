package com.robotikflow.core.models.schemas.expr;

public class UnaryExpr<V> 
{
    private V isnull;
    private V notnull;    

    public V getIsnull() {
        return isnull;
    }

    public void setIsnull(V isnull) {
        this.isnull = isnull;
    }

    public V getNotnull() {
        return notnull;
    }

    public void setNotnull(V notnull) {
        this.notnull = notnull;
    }
}
