package com.robotikflow.core.models.schemas.obj;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
	@JsonSubTypes.Type(value = FieldTextInput.class, name = "text"),
	@JsonSubTypes.Type(value = FieldTextareaInput.class, name = "textarea"),
	@JsonSubTypes.Type(value = FieldTextInput.class, name = "email"),
	@JsonSubTypes.Type(value = FieldTextInput.class, name = "number"),
	@JsonSubTypes.Type(value = FieldTextInput.class, name = "date"),
	@JsonSubTypes.Type(value = FieldTextInput.class, name = "time"),
	@JsonSubTypes.Type(value = FieldTextInput.class, name = "datetime"),
	@JsonSubTypes.Type(value = FieldTextInput.class, name = "password"),
	@JsonSubTypes.Type(value = FieldSelectInput.class, name = "select"),
	@JsonSubTypes.Type(value = FieldSelectInput.class, name = "field"),
	@JsonSubTypes.Type(value = FieldTextInput.class, name = "script"),
	@JsonSubTypes.Type(value = FieldTextInput.class, name = "function"),
	@JsonSubTypes.Type(value = FieldTextInput.class, name = "list"),
	@JsonSubTypes.Type(value = FieldTextInput.class, name = "logtree"),
})
public class FieldInput 
{
	@NotNull
	private FieldInputType type;
	private String title;
    @Valid
    private List<@NotNull FieldInputValidation> validate;

	public FieldInput() {
		this.type = FieldInputType.text;
	}
	public FieldInput(FieldInputType type) {
		this.type = type;
	}
    
	public FieldInputType getType() {
		return type;
	}
	public void setType(FieldInputType type) {
		this.type = type;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List<FieldInputValidation> getValidate() {
		return validate;
	}
	public void setValidate(List<FieldInputValidation> validate) {
		this.validate = validate;
	}
}
