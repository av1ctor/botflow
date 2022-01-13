package com.robotikflow.api.server.models.response;

import java.util.List;
import java.util.stream.Collectors;

import com.robotikflow.core.models.entities.CollectionIntegration;
import com.robotikflow.core.models.response.TriggerBaseResponse;
import com.robotikflow.core.util.DocumentUtil;

public class CollectionIntegrationBaseResponse 
{
	private final String id;
	private final String desc;
	private final Integer freq;
	private final Integer minOfDay;
	private final boolean active;
	private final boolean started;
	private final String start;
	private final TriggerBaseResponse trigger;
	private final List<CollectionIntegrationActivityResponse> activities;

	public CollectionIntegrationBaseResponse(
		final CollectionIntegration integration)
	{
		id = integration.getPubId();
		freq = integration.getFreq();
		minOfDay = integration.getMinOfDay();
		active = integration.isActive();
		started = integration.isStarted();
		start = integration.getStart() != null?
			integration.getStart().format(DocumentUtil.datePattern):
			null;
		trigger = new TriggerBaseResponse(integration.getTrigger());
		activities = integration.getActivities() != null?  
			integration.getActivities().stream()
				.map(a -> new CollectionIntegrationActivityResponse(a))
					.collect(Collectors.toList()):
			null;
		desc = integration.getDesc();
	}

	public String getId() {
		return id;
	}

	public String getDesc() {
		return desc;
	}

	public Integer getFreq() {
		return freq;
	}

	public Integer getMinOfDay() {
		return minOfDay;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isStarted() {
		return started;
	}

	public String getStart() {
		return start;
	}
	
	public TriggerBaseResponse getTrigger() {
		return trigger;
	}

	public List<CollectionIntegrationActivityResponse> getActivities() {
		return activities;
	}
}
