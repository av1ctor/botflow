package com.robotikflow.core.models.schemas.obj;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

public class Operation 
{
    @Valid
    private OperationIn in;
    @Valid
    private OperationOut out;
    @Valid
    private Map<@NotBlank String, @Valid Method> methods;

    public OperationIn getIn() {
        return in;
    }
    public void setIn(OperationIn in) {
        this.in = in;
    }
    public OperationOut getOut() {
        return out;
    }
    public void setOut(OperationOut out) {
        this.out = out;
    }
    public Map<String, Method> getMethods() {
        return methods;
    }
    public void setMethods(Map<String, Method> methods) {
        this.methods = methods;
    }
}
