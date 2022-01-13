package com.robotikflow.core.models.schemas.collection;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class ItemMeta 
{
    private Map<String, FlowItemMeta> flows;
    private Set<String> automations = new HashSet<String>();

    public Map<String, FlowItemMeta> getFlows() {
        return flows;
    }
    public void setFlows(Map<String, FlowItemMeta> flows) {
        this.flows = flows;
    }
    public Set<String> getAutomations() {
        return automations;
    }
    public void setAutomations(Set<String> automations) {
        this.automations = automations;
    }
}