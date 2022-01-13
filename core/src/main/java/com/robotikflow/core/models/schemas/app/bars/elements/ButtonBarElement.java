package com.robotikflow.core.models.schemas.app.bars.elements;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.robotikflow.core.models.schemas.app.EventAction;

public class ButtonBarElement 
    extends BarElement
{
    @NotBlank
    private String icon;
    @NotBlank
    private String title;
    private String color;
    private String size;
    @Valid
    private EventAction onClick;
    
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
    public String getColor() {
        return color;
    }
    public void setColor(String color) {
        this.color = color;
    }
    public String getSize() {
        return size;
    }
    public void setSize(String size) {
        this.size = size;
    }
    public EventAction getOnClick() {
        return onClick;
    }
    public void setOnClick(EventAction onClick) {
        this.onClick = onClick;
    }
}
