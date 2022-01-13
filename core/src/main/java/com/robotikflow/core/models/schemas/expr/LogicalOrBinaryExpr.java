package com.robotikflow.core.models.schemas.expr;

import java.util.List;

public class LogicalOrBinaryExpr<V>
    extends BinaryOrValueExpr<V>
{
    private List<LogicalOrBinaryExpr<V>> and;
    private List<LogicalOrBinaryExpr<V>> or;
    private LogicalOrBinaryExpr<V> not;

    public List<LogicalOrBinaryExpr<V>> getAnd() {
        return and;
    }

    public void setAnd(List<LogicalOrBinaryExpr<V>> and) {
        this.and = and;
    }

    public List<LogicalOrBinaryExpr<V>> getOr() {
        return or;
    }

    public void setOr(List<LogicalOrBinaryExpr<V>> or) {
        this.or = or;
    }

    public LogicalOrBinaryExpr<V> getNot() {
        return not;
    }

    public void setNot(LogicalOrBinaryExpr<V> not) {
        this.not = not;
    }
}