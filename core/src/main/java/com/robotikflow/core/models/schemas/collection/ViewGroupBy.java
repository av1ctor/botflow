package com.robotikflow.core.models.schemas.collection;

import java.util.Map;
import java.util.List;
import javax.validation.constraints.NotBlank;

public class ViewGroupBy 
{
    @NotBlank
    private String column;
    private Map<@NotBlank String, List<ViewGroupByAggregateType>> aggregates;

    public String getColumn() {
        return column;
    }
    public void setColumn(String column) {
        this.column = column;
    }
    public Map<String, List<ViewGroupByAggregateType>> getAggregates() {
        return aggregates;
    }
    public void setAggregates(Map<String, List<ViewGroupByAggregateType>> aggregates) {
        this.aggregates = aggregates;
    }
}