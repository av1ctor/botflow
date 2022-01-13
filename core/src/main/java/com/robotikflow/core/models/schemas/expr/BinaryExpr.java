package com.robotikflow.core.models.schemas.expr;

public class BinaryExpr<V>
    extends UnaryExpr<V>
{
    private EqExpr<V> eq;
    private NeExpr<V> ne;

    public EqExpr<V> getEq() {
        return eq;
    }

    public void setEq(EqExpr<V> eq) {
        this.eq = eq;
    }

    public NeExpr<V> getNe() {
        return ne;
    }

    public void setNe(NeExpr<V> ne) {
        this.ne = ne;
    }
}