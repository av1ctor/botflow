package com.robotikflow.core.models.schemas.app.screens.elements;

import javax.validation.constraints.NotBlank;

public class FormScreenElement 
    extends ScreenElement
{
    @NotBlank
    private String collection;
    @NotBlank
    private String form;
    
    public String getCollection() {
        return collection;
    }
    public void setCollection(String collection) {
        this.collection = collection;
    }
    public String getForm() {
        return form;
    }
    public void setForm(String form) {
        this.form = form;
    }
}
