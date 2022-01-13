package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "collections_integrations_logs")
public class CollectionIntegrationLog 
    extends CollectionLog
{
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private CollectionIntegration integration;

    public CollectionIntegration getIntegration() {
        return integration;
    }

    public void setIntegration(CollectionIntegration integration) {
        this.integration = integration;
    }
}