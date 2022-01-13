package com.robotikflow.core.models.schemas.collection;

import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Index  
{
	@NotNull
	private List<@NotBlank String> columns;
	@NotNull
	private FieldIndexDir dir;
	private Boolean unique;
	
	public Index() {
	}
	
	public Index(List<String> columns, FieldIndexDir dir, Boolean unique)
	{
		this.columns = columns;
		this.dir = dir;
		this.unique = unique;
	}
	
	public List<String> getColumns() {
		return columns;
	}
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}
	public FieldIndexDir getDir() {
		return dir;
	}
	public void setDir(FieldIndexDir dir) {
		this.dir = dir;
	}
	public boolean isUnique() {
		return unique != null? unique: false;
	}
	public Boolean getUnique() {
		return unique;
	}
	public void setUnique(Boolean unique) {
		this.unique = unique;
	}
	@Override
	public int hashCode() {
		return Objects.hash(columns, dir, unique);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Index other = (Index) obj;
		return Objects.equals(columns, other.columns) && dir == other.dir && unique == other.unique;
	}
	
}
