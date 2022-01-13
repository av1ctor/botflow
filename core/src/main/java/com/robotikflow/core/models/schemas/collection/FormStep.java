package com.robotikflow.core.models.schemas.collection;

import java.util.List;

import javax.validation.Valid;

public class FormStep 
{
    @Valid
    private List<FormElement> elements;

    public List<FormElement> getElements() {
        return elements;
    }
    public void setElements(List<FormElement> elements) {
        this.elements = elements;
    }
}
