package com.robotikflow.core.models.schemas.collection;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
	@JsonSubTypes.Type(value = FormEmptyElement.class, name = "empty"),
	@JsonSubTypes.Type(value = FormTextElement.class, name = "text"),
    @JsonSubTypes.Type(value = FormFieldElement.class, name = "field"),
    @JsonSubTypes.Type(value = FormRowElement.class, name = "row"),
    @JsonSubTypes.Type(value = FormHlineElement.class, name = "hline"),
})
public class FormElement 
{
    @NotNull
    private FormElementType type;
    @Valid
    private FormElementWidths widths = new FormElementWidths();
    private String height;
    @Valid
    private List<@NotNull FormElement> elements;

    public FormElementType getType() {
        return type;
    }
    public void setType(FormElementType type) {
        this.type = type;
    }
    public FormElementWidths getWidths() {
        return widths;
    }
    public void setWidths(FormElementWidths widths) {
        this.widths = widths;
    }
    public String getHeight() {
        return height;
    }
    public void setHeight(String height) {
        this.height = height;
    }
    public List<FormElement> getElements() {
        return elements;
    }
    public void setElements(List<FormElement> elements) {
        this.elements = elements;
    }
}
