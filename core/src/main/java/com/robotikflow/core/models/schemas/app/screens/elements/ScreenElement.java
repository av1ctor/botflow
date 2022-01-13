package com.robotikflow.core.models.schemas.app.screens.elements;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BlockScreenElement.class, name = "block"),
    @JsonSubTypes.Type(value = TextScreenElement.class, name = "text"),
    @JsonSubTypes.Type(value = EmptyScreenElement.class, name = "empty"),
    @JsonSubTypes.Type(value = FormScreenElement.class, name = "form"),
})
public class ScreenElement 
{
    @NotNull
    private ScreenElementType type;
    @Valid
    private ScreenElementWidths widths = new ScreenElementWidths();
    private String height;
    @Valid
    private List<ScreenElement> elements;

    public ScreenElementType getType() {
        return type;
    }
    public void setType(ScreenElementType type) {
        this.type = type;
    }
    public ScreenElementWidths getWidths() {
        return widths;
    }
    public void setWidths(ScreenElementWidths widths) {
        this.widths = widths;
    }
    public String getHeight() {
        return height;
    }
    public void setHeight(String height) {
        this.height = height;
    }
    public List<ScreenElement> getElements() {
        return elements;
    }
    public void setElements(List<ScreenElement> elements) {
        this.elements = elements;
    }
}
