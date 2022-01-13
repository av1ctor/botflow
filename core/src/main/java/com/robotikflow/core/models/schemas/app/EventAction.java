package com.robotikflow.core.models.schemas.app;

import javax.validation.constraints.NotNull;

public class EventAction 
{
    @NotNull
    private EventActionType action;
    private String target;
    
    public EventActionType getAction() {
        return action;
    }
    public void setAction(EventActionType action) {
        this.action = action;
    }
    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }
}
