package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ReportChartAxisX
{
	@NotNull
	private String column;
	@Valid
	private ReportChartAxisRange range;
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public ReportChartAxisRange getRange() {
		return range;
	}
	public void setRange(ReportChartAxisRange range) {
		this.range = range;
	}
}
