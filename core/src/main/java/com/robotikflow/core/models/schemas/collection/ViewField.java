package com.robotikflow.core.models.schemas.collection;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ViewField 
{
	private int index = 99;
	@Valid
	private ViewFieldColor color;
	private Short width;
	private Boolean frozen;
	private Boolean hidden;
	@Valid
	private Map<@NotBlank String, @NotNull ViewField> fields;

	public ViewField() {
	}

	public ViewField(int index) 
	{
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public ViewFieldColor getColor() {
		return color;
	}
	public void setColor(ViewFieldColor color) {
		this.color = color;
	}
	public Short getWidth() {
		return width;
	}
	public void setWidth(Short width) {
		this.width = width;
	}
	public boolean isFrozen() {
		return frozen != null? frozen: false;
	}
	public Boolean getFrozen() {
		return frozen;
	}
	public void setFrozen(Boolean frozen) {
		this.frozen = frozen;
	}
	public boolean isHidden() {
		return hidden != null? hidden: false;
	}
	public Boolean getHidden() {
		return hidden;
	}
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
	public Map<String, ViewField> getFields() {
		return fields;
	}
	public void setFields(Map<String, ViewField> fields) {
		this.fields = fields;
	}
}
