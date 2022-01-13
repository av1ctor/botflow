package com.robotikflow.core.models.schemas.expr;

import java.util.List;

public class LogicalExpr<V>
{
    private Boolean neg;
    private List<LogicalExpr<V>> and;
    private List<LogicalExpr<V>> or;
    private V cond;

    public Boolean getNeg() {
        return neg;
    }

	public void setNeg(Boolean neg) {
		this.neg = neg;
	}

	public boolean isNeg() {
        return neg == null?
            false:
            neg.booleanValue();
    }
    
    public List<LogicalExpr<V>> getAnd() {
        return and;
    }

	public void setAnd(List<LogicalExpr<V>> and) {
        this.and = and;
    }

    public List<LogicalExpr<V>> getOr() {
        return or;
    }

    public void setOr(List<LogicalExpr<V>> or) {
        this.or = or;
    }

    public V getCond() {
		return cond;
	}

	public void setCond(V cond) {
		this.cond = cond;
	}
}