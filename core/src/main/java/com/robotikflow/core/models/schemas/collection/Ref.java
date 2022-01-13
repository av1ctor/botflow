package com.robotikflow.core.models.schemas.collection;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.nosql.ReferenceType;

public class Ref 
{
	@NotNull
	private String collection;
	private ReferenceType type;
	@NotNull
	@Valid
	private List<ReferenceFilter> filters;
	@NotNull
	@Valid
	private ReferencePreview preview;
	private Boolean preload;

	public String getCollection() {
		return collection;
	}
	public void setCollection(String collection) {
		this.collection = collection;
	}
	public ReferenceType getType() {
		return type;
	}
	public void setType(ReferenceType type) {
		this.type = type;
	}
	public List<ReferenceFilter> getFilters() {
		return filters;
	}
	public void setFilters(List<ReferenceFilter> filters) {
		this.filters = filters;
	}
	public ReferencePreview getPreview() {
		return preview;
	}
	public void setPreview(ReferencePreview preview) {
		this.preview = preview;
	}
	public boolean isPreload() {
		return preload != null? preload: false;
	}
	public Boolean getPreload() {
		return preload;
	}
	public void setPreload(Boolean preload) {
		this.preload = preload;
	}
}
