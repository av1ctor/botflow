package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotBlank;

public class FlowConditionAutomation 
{
    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
