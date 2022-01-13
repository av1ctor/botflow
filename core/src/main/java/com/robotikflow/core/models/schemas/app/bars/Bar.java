package com.robotikflow.core.models.schemas.app.bars;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.schemas.app.bars.elements.BarElement;

public class Bar 
{
    @NotBlank
    private String id;
    @NotNull
    private BarType type;
    private String color;
    private String bgColor;
    private Short size;
    @Valid
    private List<BarElement> elements;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public BarType getType() {
        return type;
    }
    public void setType(BarType type) {
        this.type = type;
    }
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public String getBgColor() {
        return bgColor;
    }
    public void setBgColor(String bgColor) {
        this.bgColor = bgColor;
    }
    public Short getSize() {
        return size;
    }
    public void setSize(Short size) {
        this.size = size;
    }
    public List<BarElement> getElements() {
        return elements;
    }
    public void setElements(List<BarElement> elements) {
        this.elements = elements;
    }
}
