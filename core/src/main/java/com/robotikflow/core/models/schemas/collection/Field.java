package com.robotikflow.core.models.schemas.collection;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrValue;
import com.robotikflow.core.models.schemas.expr.LogicalExpr;

public class Field 
{
	@NotNull
	@Size(min=3, max=32)
	private String label;
	@NotNull
	private FieldType type;
	private FieldType subtype;
	private FieldComponent component;
	private String mask;
	private String _class;
	private Boolean auto;
	private Boolean positional;
	private Boolean nullable;
	private Boolean sortable;
	private Boolean unique;
	private ScriptOrFunctionOrValue _default;
	private List<String> options;
	@Valid
	private FieldHidden hidden;
	@Valid
	private FieldDisabled disabled;
	@Valid
	private LogicalExpr<FieldDependency> depends;
	@Valid
	private FieldReference ref;
	@Valid
	private Map<@NotBlank @Size(min=3, max=32) String, Method> methods;

	public Field() {
	}

	public Field(
		String label, 
		FieldType type, 
		FieldComponent component, 
		Boolean nullable,
		Boolean sortable,
		Boolean unique)
	{
		this.label = label;
		this.type = type;
		this.component = component;
		this.nullable = nullable;
		this.sortable = sortable;
		this.unique = unique;
	}
	
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public FieldType getType() {
		return type;
	}
	public void setType(FieldType type) {
		this.type = type;
	}
	public FieldType getSubtype() {
		return subtype;
	}
	public void setSubtype(FieldType subtype) {
		this.subtype = subtype;
	}
	public FieldComponent getComponent() {
		return component;
	}
	public void setComponent(FieldComponent component) {
		this.component = component;
	}
	public String getMask() {
		return mask;
	}
	public void setMask(String mask) {
		this.mask = mask;
	}
	public boolean isAuto() {
		return auto != null? auto: false;
	}
	public Boolean getAuto() {
		return auto;
	}
	public void setAuto(Boolean auto) {
		this.auto = auto;
	}
	public boolean isNullable() {
		return nullable != null? nullable: false;
	}
	public Boolean getNullable() {
		return nullable;
	}
	public void setNullable(Boolean nullable) {
		this.nullable = nullable;
	}
	public boolean isSortable() {
		return sortable != null? sortable: false;
	}
	public Boolean getSortable() {
		return sortable;
	}
	public void setSortable(Boolean sortable) {
		this.sortable = sortable;
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
	public ScriptOrFunctionOrValue getDefault() {
		return _default;
	}
	public void setDefault(ScriptOrFunctionOrValue _default) {
		this._default = _default;
	}
	public List<String> getOptions() {
		return options;
	}
	public void setOptions(List<String> options) {
		this.options = options;
	}
	public boolean isPositional() {
		return positional != null? positional: false;
	}
	public Boolean getPositional() {
		return positional;
	}
	public void setPositional(Boolean positional) {
		this.positional = positional;
	}
	@JsonProperty("class")
	public String getClass_() {
		return _class;
	}
	@JsonProperty("class")
	public void setClass_(String _class) {
		this._class = _class;
	}
	public FieldHidden getHidden() {
		return hidden;
	}
	public void setHidden(FieldHidden hidden) {
		this.hidden = hidden;
	}
	public FieldDisabled getDisabled() {
		return disabled;
	}
	public void setDisabled(FieldDisabled disabled) {
		this.disabled = disabled;
	}
	public LogicalExpr<FieldDependency> getDepends() {
		return depends;
	}
	public void setDepends(LogicalExpr<FieldDependency> depends) {
		this.depends = depends;
	}
	public FieldReference getRef() {
		return ref;
	}
	public void setRef(FieldReference ref) {
		this.ref = ref;
	}
	public Map<@NotBlank String, Method> getMethods() {
		return methods;
	}
	public void setMethods(Map<@NotBlank String, Method> methods) {
		this.methods = methods;
	}
}
