package com.robotikflow.core.models.schemas.collection;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class Form 
{
    @NotBlank
    private String id;
    @NotBlank
    private String name;
    private String desc;
    @NotNull
    private FormUse use;
    private boolean active = true;
    private boolean public_ = false;
    private boolean disabled = false;
    @Valid
    private List<@NotNull FormStep> steps;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public FormUse getUse() {
        return use;
    }
    public void setUse(FormUse use) {
        this.use = use;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
    public boolean isPublic() {
        return public_;
    }
    public void setPublic(boolean public_) {
        this.public_ = public_;
    }
    public boolean isDisabled() {
        return disabled;
    }
    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
    public List<FormStep> getSteps() {
        return steps;
    }
    public void setSteps(List<FormStep> steps) {
        this.steps = steps;
    }
}
