package com.robotikflow.api.server.models.response;

import java.util.List;
import java.util.stream.Collectors;

import com.robotikflow.core.models.entities.CollectionAutomation;
import com.robotikflow.core.models.entities.CollectionAutomationTrigger;
import com.robotikflow.core.models.entities.CollectionAutomationType;

public class CollectionAutomationBaseResponse 
{
	private final String id;
	private final String desc;
	private final CollectionAutomationType type;
	private final CollectionAutomationTrigger trigger;
	private final List<CollectionAutomationActivityResponse> activities;

	public CollectionAutomationBaseResponse(
		CollectionAutomation automation)
	{
		id = automation.getPubId();
		type = automation.getType();
		trigger = automation.getTrigger();
		activities = automation.getActivities() != null?  
			automation.getActivities().stream()
				.map(a -> new CollectionAutomationActivityResponse(a))
					.collect(Collectors.toList()):
			null;
		desc = automation.getDesc();
	}

	public String getId() {
		return id;
	}

	public String getDesc() {
		return desc;
	}

	public CollectionAutomationType getType() {
		return type;
	}

	public CollectionAutomationTrigger getTrigger() {
		return trigger;
	}

	public List<CollectionAutomationActivityResponse> getActivities() {
		return activities;
	}
}
