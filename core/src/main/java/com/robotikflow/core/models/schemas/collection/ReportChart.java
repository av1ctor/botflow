package com.robotikflow.core.models.schemas.collection;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ReportChart 
{
	@NotNull
	private ReportChartType type;
	@NotNull
	@Valid
	private ReportChartAxisX x;
	@NotNull
	@Valid
	private ReportChartAxisY y;
	
	public ReportChartType getType() {
		return type;
	}
	public void setType(ReportChartType type) {
		this.type = type;
	}
	public ReportChartAxisX getX() {
		return x;
	}
	public void setX(ReportChartAxisX x) {
		this.x = x;
	}
	public ReportChartAxisY getY() {
		return y;
	}
	public void setY(ReportChartAxisY y) {
		this.y = y;
	}
	
}
