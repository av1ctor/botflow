package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "collections_automations_logs")
public class CollectionAutomationLog 
    extends CollectionLog
{
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private CollectionAutomation automation;

    public CollectionAutomation getAutomation() {
        return automation;
    }

    public void setAutomation(CollectionAutomation automation) {
        this.automation = automation;
    }
}