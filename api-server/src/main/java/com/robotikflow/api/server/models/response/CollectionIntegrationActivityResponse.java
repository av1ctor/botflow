package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionIntegrationActivity;
import com.robotikflow.core.models.response.ActivityResponse;

public class CollectionIntegrationActivityResponse
{
	private final String id;
    private final ActivityResponse activity;

    public CollectionIntegrationActivityResponse(
        final CollectionIntegrationActivity activity)
    {
        this.id = activity.getPubId();
        this.activity = new ActivityResponse(activity.getActivity(), true);
    }

    public String getId() {
        return id;
    }
    public ActivityResponse getActivity() {
        return activity;
    }
}