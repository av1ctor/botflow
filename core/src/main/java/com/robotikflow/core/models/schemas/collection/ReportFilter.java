package com.robotikflow.core.models.schemas.collection;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public class ReportFilter 
{
	@Valid
	private ReportFilterForm form;
	@Valid
	private Map<@NotBlank String, ReportFilterColumn> fields;

	public Map<String, ReportFilterColumn> getFields() {
		return fields;
	}
	public void setFields(Map<String, ReportFilterColumn> fields) {
		this.fields = fields;
	}
	public ReportFilterForm getForm() {
		return form;
	}
	public void setForm(ReportFilterForm form) {
		this.form = form;
	}
}
