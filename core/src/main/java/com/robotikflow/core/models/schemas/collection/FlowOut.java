package com.robotikflow.core.models.schemas.collection;

import java.util.List;

import javax.validation.Valid;

import com.robotikflow.core.models.schemas.expr.LogicalExpr;

public class FlowOut 
{
    @Valid
    private FlowLabel label;
    @Valid
    private LogicalExpr<FlowCondition> condition;
    @Valid
    private List<FlowEdges> edges;

    public FlowLabel getLabel() {
        return label;
    }

    public void setLabel(FlowLabel label) {
        this.label = label;
    }
    
    public LogicalExpr<FlowCondition> getCondition() {
        return condition;
    }

    public void setCondition(LogicalExpr<FlowCondition> condition) {
        this.condition = condition;
    }

    public List<FlowEdges> getEdges() {
        return edges;
    }

    public void setEdges(List<FlowEdges> edges) {
        this.edges = edges;
    }
}
