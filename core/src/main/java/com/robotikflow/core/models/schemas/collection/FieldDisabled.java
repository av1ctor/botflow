package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;

import com.robotikflow.core.models.schemas.expr.LogicalExpr;

public class FieldDisabled 
{
	@Valid
	private LogicalExpr<FieldDisabledFor> insert;
	@Valid
	private LogicalExpr<FieldDisabledFor> update;

	public LogicalExpr<FieldDisabledFor> getInsert() {
		return insert;
	}

	public void setInsert(LogicalExpr<FieldDisabledFor> insert) {
		this.insert = insert;
	}

	public LogicalExpr<FieldDisabledFor> getUpdate() {
		return update;
	}

	public void setUpdate(LogicalExpr<FieldDisabledFor> update) {
		this.update = update;
	}
}
