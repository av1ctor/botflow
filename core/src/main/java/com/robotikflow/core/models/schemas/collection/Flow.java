package com.robotikflow.core.models.schemas.collection;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Flow 
{
    @NotNull
    private FlowType type;
    private String part;
    @NotNull
    private FlowLabel label;
    @Valid
    private FlowBounds bounds;
    @Valid
    private Set<@NotBlank String> in;
    @Valid
    private Map<@NotBlank String, FlowOut> out;
    @Valid
    private List<FlowActivity> activities;

    public FlowType getType() {
        return type;
    }

    public void setType(FlowType type) {
        this.type = type;
    }

    public String getPart() {
        return part;
    }

    public void setPart(String part) {
        this.part = part;
    }

    public FlowLabel getLabel() {
        return label;
    }

    public void setLabel(FlowLabel label) {
        this.label = label;
    }

    public FlowBounds getBounds() {
        return bounds;
    }

    public void setBounds(FlowBounds bounds) {
        this.bounds = bounds;
    }

    public Set<String> getIn() {
        return in;
    }

    public void setIn(Set<String> in) {
        this.in = in;
    }

    public Map<String, FlowOut> getOut() {
        return out;
    }

    public void setOut(Map<String, FlowOut> out) {
        this.out = out;
    }

    public List<FlowActivity> getActivities() {
        return activities;
    }

    public void setActivities(List<FlowActivity> activities) {
        this.activities = activities;
    }
}
