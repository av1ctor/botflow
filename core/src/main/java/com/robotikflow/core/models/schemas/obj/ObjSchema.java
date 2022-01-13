package com.robotikflow.core.models.schemas.obj;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ObjSchema 
{
	@NotNull
	private float version;
	@NotNull
	private ObjType type;
	@NotBlank
	private String name;
	@Valid
	private List<@NotBlank String> categories;
	@NotBlank
	private String title;
	private String desc;
	private String icon;	
	private Boolean hidden;	
	@Valid
	private Map<@NotBlank String, @NotNull Ref> refs;
	@NotNull
	private Map<@NotBlank String, @Valid Field> fields;
	private Map<@NotBlank String, @Valid Method> methods;
	private Map<@NotBlank String, @Valid Operation> operations;

	public float getVersion() {
		return version;
	}
	public void setVersion(float version) {
		this.version = version;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ObjType getType() {
		return type;
	}
	public void setType(ObjType type) {
		this.type = type;
	}
	public List<String> getCategories() {
		return categories;
	}
	public void setCategories(List<String> categories) {
		this.categories = categories;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public boolean isHidden() {
		return hidden != null && hidden.booleanValue()?
			true:
			false;
	}
	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}
	public Map<String, Ref> getRefs() {
		return refs;
	}
	public void setRefs(Map<String, Ref> refs) {
		this.refs = refs;
	}
	public Map<String, Field> getFields() {
		return fields;
	}
	public void setFields(Map<String, Field> fields) {
		this.fields = fields;
	}
	public Map<String, Method> getMethods() {
		return methods;
	}
	public void setMethods(Map<String, Method> methods) {
		this.methods = methods;
	}
	public Map<String, Operation> getOperations() {
		return operations;
	}
	public void setOperations(Map<String, Operation> operations) {
		this.operations = operations;
	}
}
