package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionAutomationActivity;
import com.robotikflow.core.models.response.ActivityResponse;

public class CollectionAutomationActivityResponse
{
	private final String id;
    private final ActivityResponse activity;

    public CollectionAutomationActivityResponse(
        final CollectionAutomationActivity act)
    {
        this.id = act.getPubId();
        this.activity = new ActivityResponse(act.getActivity());
    }

    public String getId() {
        return id;
    }
    public ActivityResponse getActivity() {
        return activity;
    }
}