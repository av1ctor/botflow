package com.robotikflow.core.models.schemas.expr;

public class BinaryOrValueExpr<V> 
    extends BinaryExpr<V>
{
    private V val;
    
    public V getVal() {
        return val;
    }

    public void setVal(V val) {
        this.val = val;
    }
}
