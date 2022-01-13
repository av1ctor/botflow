package com.robotikflow.core.models.schemas.collection;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class View 
{
	@NotBlank
	@Size(min=3, max=64)
	private String id;
	@NotNull
	private ViewType type;
	@NotBlank
	@Size(min=3, max=64)
	private String name;
	@NotNull
	private ViewStyle style;
	@Valid
	private ViewGroupBy groupBy;
	@Valid
	private Map<@NotBlank String, @NotNull ViewFilter> filters;
	@Valid
	private Map<@NotBlank String, @NotNull ViewSort> sort;
	@Valid
	private Map<@NotBlank String, @NotNull ViewField> fields;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ViewType getType() {
		return type;
	}

	public void setType(ViewType type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ViewStyle getStyle() {
		return style;
	}

	public void setStyle(ViewStyle style) {
		this.style = style;
	}

	public ViewGroupBy getGroupBy() {
		return groupBy;
	}

	public void setGroupBy(ViewGroupBy groupBy) {
		this.groupBy = groupBy;
	}

	public Map<String, ViewFilter> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, ViewFilter> filters) {
		this.filters = filters;
	}

	public Map<String, ViewSort> getSort() {
		return sort;
	}

	public void setSort(Map<String, ViewSort> sort) {
		this.sort = sort;
	}

	public Map<String, ViewField> getFields() {
		return fields;
	}

	public void setFields(Map<String, ViewField> fields) {
		this.fields = fields;
	}
}
