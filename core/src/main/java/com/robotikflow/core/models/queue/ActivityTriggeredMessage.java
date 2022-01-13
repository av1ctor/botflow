package com.robotikflow.core.models.queue;

import java.util.List;
import java.util.Map;

public class ActivityTriggeredMessage 
	extends Message 
{
	private Long collectionId;
	private List<String> activities;
	private Map<String, Object> params;
	
	public ActivityTriggeredMessage()
	{
		super(MessageType.ACTIVITY_TRIGGERED);
	}

	public ActivityTriggeredMessage(
		final Long collectionId,
		final List<String> activities,
		final Map<String, Object> params)
	{
		this();
		this.collectionId = collectionId;
		this.activities = activities;
		this.params = params;
	}

	public ActivityTriggeredMessage(
		final String activity,
		final Map<String, Object> values)
	{
		this();
		this.activities = List.of(activity);
		this.params = values;
	}

	public List<String> getActivities() {
		return activities;
	}

	public void setActivities(List<String> activities) {
		this.activities = activities;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> values) {
		this.params = values;
	}

	public Long getCollectionId() {
		return collectionId;
	}

	public void setCollectionId(Long collectionId) {
		this.collectionId = collectionId;
	}
}
