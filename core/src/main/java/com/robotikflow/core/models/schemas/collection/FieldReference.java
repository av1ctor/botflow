package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.NotNull;

public class FieldReference 
{
    @NotNull
    private String name;
	@NotNull
	private String display;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDisplay() {
        return display;
    }
    public void setDisplay(String display) {
        this.display = display;
    }
}
