package com.robotikflow.core.models.schemas.expr;

public class EqExpr<V>
{
    private V left;
    private V right;

    public V getLeft() {
        return left;
    }

    public void setLeft(V left) {
        this.left = left;
    }

    public V getRight() {
        return right;
    }

    public void setRight(V right) {
        this.right = right;
    }
}
