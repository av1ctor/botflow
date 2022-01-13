package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;

import com.robotikflow.core.models.schemas.expr.LogicalExpr;

public class Auth 
{
	@Valid
	private LogicalExpr<AuthAction> create;
	@Valid
	private LogicalExpr<AuthAction> read;
	@Valid
	private LogicalExpr<AuthAction> edit;

	public LogicalExpr<AuthAction> getCreate() {
		return create;
	}

	public void setCreate(LogicalExpr<AuthAction> create) {
		this.create = create;
	}

	public LogicalExpr<AuthAction> getRead() {
		return read;
	}

	public void setRead(LogicalExpr<AuthAction> read) {
		this.read = read;
	}

	public LogicalExpr<AuthAction> getEdit() {
		return edit;
	}

	public void setEdit(LogicalExpr<AuthAction> edit) {
		this.edit = edit;
	}

}
