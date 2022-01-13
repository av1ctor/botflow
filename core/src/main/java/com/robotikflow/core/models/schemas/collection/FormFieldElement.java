package com.robotikflow.core.models.schemas.collection;

import javax.validation.constraints.Size;

public class FormFieldElement 
    extends FormElement
{
    private String name;
    private Boolean disabled;
    private Boolean required;
    @Size(min=0, max=128)
    private String label;
    @Size(min=0, max=1024)
    private String desc;
    private Object default_;
    private String placeholder;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Boolean getDisabled() {
        return disabled;
    }
    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }
    public Boolean getRequired() {
        return required;
    }
    public void setRequired(Boolean required) {
        this.required = required;
    }
    public String getLabel() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public Object getDefault_() {
        return default_;
    }
    public void setDefault_(Object default_) {
        this.default_ = default_;
    }
    public String getPlaceholder() {
        return placeholder;
    }
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }
}
