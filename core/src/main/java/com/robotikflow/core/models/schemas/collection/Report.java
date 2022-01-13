package com.robotikflow.core.models.schemas.collection;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrValue;

public class Report 
{
	@NotNull
	private String id;
	@NotNull
	private String name;
	@NotNull
	private String description;
	@Valid
	private Map<String, ScriptOrFunctionOrValue> consts;
	@NotNull
	@Valid
	private Map<@NotBlank String, ReportColumn> columns;
	@Valid
	private ReportFilter filter;
	@Valid
	private Map<@NotBlank String, FieldIndexDir> order;
	@Valid
	private ReportChart chart;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Map<String, ScriptOrFunctionOrValue> getConsts() {
		return consts;
	}
	public void setConsts(Map<String, ScriptOrFunctionOrValue> consts) {
		this.consts = consts;
	}
	public Map<String, FieldIndexDir> getOrder() {
		return order;
	}
	public void setOrder(Map<String, FieldIndexDir> order) {
		this.order = order;
	}
	public ReportChart getChart() {
		return chart;
	}
	public void setChart(ReportChart chart) {
		this.chart = chart;
	}
	public Map<String, ReportColumn> getColumns() {
		return columns;
	}
	public void setColumns(Map<String, ReportColumn> columns) {
		this.columns = columns;
	}
	public ReportFilter getFilter() {
		return filter;
	}
	public void setFilter(ReportFilter filter) {
		this.filter = filter;
	}
}
