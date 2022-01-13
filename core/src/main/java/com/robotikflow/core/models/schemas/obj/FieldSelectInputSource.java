package com.robotikflow.core.models.schemas.obj;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import com.drew.lang.annotations.NotNull;

public class FieldSelectInputSource 
{
    @NotBlank
    private String name;
    @NotNull
    @Valid
    private FieldSelectInputOption option;
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public FieldSelectInputOption getOption() {
        return option;
    }
    public void setOption(FieldSelectInputOption option) {
        this.option = option;
    }
}
