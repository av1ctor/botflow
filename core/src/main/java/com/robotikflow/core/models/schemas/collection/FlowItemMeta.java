package com.robotikflow.core.models.schemas.collection;

import java.util.List;

public class FlowItemMeta 
{
    private FlowItemMetaStatus status;
    private List<String> pending;            // outs
    private int count = 0;                  // ins

    public FlowItemMeta() {
    }

    public FlowItemMeta(FlowItemMetaStatus status)
    {
        this.status = status;
    }

    public FlowItemMeta(FlowItemMetaStatus status, int count)
    {
        this.status = status;
        this.count = count;
    }

    public FlowItemMetaStatus getStatus() {
        return status;
    }

    public void setStatus(FlowItemMetaStatus status) {
        this.status = status;
    }

    public List<String> getPending() {
        return pending;
    }

    public void setPending(List<String> pending) {
        this.pending = pending;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int inCount) {
        this.count = inCount;
    }
}