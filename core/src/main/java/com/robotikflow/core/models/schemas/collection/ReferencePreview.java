package com.robotikflow.core.models.schemas.collection;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class ReferencePreview
{
    @NotNull
    private List<@NotBlank String> columns;

    public List<String> getColumns() {
        return columns;
    }
    public void setColumns(List<String> columns) {
        this.columns = columns;
    }
}
