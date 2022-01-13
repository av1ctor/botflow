package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ReportChartAxisY 
{
	@NotNull
	@NotEmpty
	private String[] columns;

	public String[] getColumns() {
		return columns;
	}
	public void setColumns(String[] columns) {
		this.columns = columns;
	}
}
