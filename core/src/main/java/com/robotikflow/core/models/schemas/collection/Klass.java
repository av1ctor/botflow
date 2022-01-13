package com.robotikflow.core.models.schemas.collection;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class Klass 
{
	@NotNull
	@Size(min=3, max=32)
	private String label;
	@Size(min=3, max=32)
	private String icon;
	@NotNull
	@Valid
	private Map<@NotBlank @Size(min=3, max=32) String, Field> props;
	@Valid
	private Map<@NotBlank @Size(min=3, max=32) String, Method> methods;

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public Map<@NotBlank String, Field> getProps() {
		return props;
	}
	public void setProps(Map<String, Field> props) {
		this.props = props;
	}
	public Map<@NotBlank String, Method> getMethods() {
		return methods;
	}
	public void setMethods(Map<@NotBlank String, Method> methods) {
		this.methods = methods;
	}
}
