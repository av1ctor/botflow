package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotNull;

public class ReportChartAxisRange 
{
	@NotNull
	private Integer from;
	@NotNull
	private Integer to;
	public Integer getFrom() {
		return from;
	}
	public void setFrom(Integer from) {
		this.from = from;
	}
	public Integer getTo() {
		return to;
	}
	public void setTo(Integer to) {
		this.to = to;
	}
}
