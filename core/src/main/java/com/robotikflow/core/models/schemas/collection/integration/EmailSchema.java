package com.robotikflow.core.models.schemas.collection.integration;

import javax.validation.Valid;

import com.robotikflow.core.models.schemas.expr.LogicalExpr;

public class EmailSchema 
{
	@Valid
	private LogicalExpr<EmailCondition> condition;

	public LogicalExpr<EmailCondition> getCondition() {
		return condition;
	}

	public void setCondition(LogicalExpr<EmailCondition> condition) {
		this.condition = condition;
	}
}
