package com.robotikflow.api.server.models.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class CollectionAdicionarColunaRequest
{
	@NotNull
	@NotBlank
    private String type;
	private int index;
	private Boolean nullable;
	private Boolean sortable;
	private Boolean unique;

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public Boolean getSortable() {
		return sortable;
	}
	public void setSortable(boolean sortable) {
		this.sortable = sortable;
	}
	public Boolean getUnique() {
		return unique;
	}
	public void setUnique(boolean unique) {
		this.unique = unique;
	}
	public Boolean getNullable() {
		return nullable;
	}
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
}
