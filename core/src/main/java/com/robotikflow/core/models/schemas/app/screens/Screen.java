package com.robotikflow.core.models.schemas.app.screens;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.robotikflow.core.models.schemas.app.EventAction;
import com.robotikflow.core.models.schemas.app.screens.elements.ScreenElement;

public class Screen 
{
    @NotBlank
    private String id;
    private Short index;
    @NotBlank
    private String title;
    private String desc;
    @Valid
    private Map<ScreenEventType, EventAction> events;
    @Valid
    private List<ScreenElement> elements;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Short getIndex() {
        return index;
    }
    public void setIndex(Short index) {
        this.index = index;
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
    public List<ScreenElement> getElements() {
        return elements;
    }
    public void setElements(List<ScreenElement> elements) {
        this.elements = elements;
    }
    public Map<ScreenEventType, EventAction> getEvents() {
        return events;
    }
    public void setEvents(Map<ScreenEventType, EventAction> events) {
        this.events = events;
    }
}
