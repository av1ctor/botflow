package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;

public class FieldDependency 
{
	@Valid
	private FieldDependencyColumn column;
	@Valid
	private FieldDependencyFlow flow;

	public FieldDependencyColumn getColumn() {
		return column;
	}

	public void setColumn(FieldDependencyColumn column) {
		this.column = column;
	}

	public FieldDependencyFlow getFlow() {
		return flow;
	}

	public void setFlow(FieldDependencyFlow flow) {
		this.flow = flow;
	}
}
