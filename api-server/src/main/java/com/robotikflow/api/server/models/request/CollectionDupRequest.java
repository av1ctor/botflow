package com.robotikflow.api.server.models.request;

public class CollectionDupRequest 
{
    private boolean withData;
    private boolean withPermissions;
    private boolean withIntegrations;
    private boolean withAutomations;
    private boolean withAuxs;

    public boolean isWithData() {
        return withData;
    }

    public void setWithData(boolean withData) {
        this.withData = withData;
    }

    public boolean isWithPermissions() {
        return withPermissions;
    }

    public void setWithPermissions(boolean withPermissions) {
        this.withPermissions = withPermissions;
    }

    public boolean isWithIntegrations() {
        return withIntegrations;
    }

    public void setWithIntegrations(boolean withIntegrations) {
        this.withIntegrations = withIntegrations;
    }

    public boolean isWithAutomations() {
        return withAutomations;
    }

    public void setWithAutomations(boolean withAutomations) {
        this.withAutomations = withAutomations;
    }

    public boolean isWithAuxs() {
        return withAuxs;
    }

    public void setWithAuxs(boolean withAuxs) {
        this.withAuxs = withAuxs;
    }
}