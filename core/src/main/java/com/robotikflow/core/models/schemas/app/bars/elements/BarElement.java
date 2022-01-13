package com.robotikflow.core.models.schemas.app.bars.elements;

import java.util.List;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = BlockBarElement.class, name = "block"),
    @JsonSubTypes.Type(value = EmptyBarElement.class, name = "empty"),
    @JsonSubTypes.Type(value = ButtonBarElement.class, name = "button"),
    @JsonSubTypes.Type(value = MenuBarElement.class, name = "menu"),
    @JsonSubTypes.Type(value = MenuItemBarElement.class, name = "menuitem"),
})
public class BarElement 
{
    private BarElementType type;
    @Valid
    private List<BarElement> elements;
    
    public BarElementType getType() {
        return type;
    }
    public void setType(BarElementType type) {
        this.type = type;
    }
    public List<BarElement> getElements() {
        return elements;
    }
    public void setElements(List<BarElement> elements) {
        this.elements = elements;
    }
}
