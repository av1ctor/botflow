package com.robotikflow.core.models.schemas.obj;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
	@JsonSubTypes.Type(value = FieldInputMinValidation.class, name = "min"),
	@JsonSubTypes.Type(value = FieldInputMaxValidation.class, name = "max"),
	@JsonSubTypes.Type(value = FieldInputUppercaseValidation.class, name = "uppercase"),
	@JsonSubTypes.Type(value = FieldInputLowercaseValidation.class, name = "lowercase"),
	@JsonSubTypes.Type(value = FieldInputNumericValidation.class, name = "numeric"),
	@JsonSubTypes.Type(value = FieldInputSpecialValidation.class, name = "special"),
	@JsonSubTypes.Type(value = FieldInputEmailValidation.class, name = "email"),
	@JsonSubTypes.Type(value = FieldInputUrlValidation.class, name = "url"),
})
public class FieldInputValidation 
{
    @NotBlank
    private FieldInputValidationType type;
    @NotBlank
    private String err;
    
	public FieldInputValidationType getType() {
		return type;
	}
	public void setType(FieldInputValidationType type) {
		this.type = type;
	}
	public String getErr() {
		return err;
	}
	public void setErr(String err) {
		this.err = err;
	}
}
