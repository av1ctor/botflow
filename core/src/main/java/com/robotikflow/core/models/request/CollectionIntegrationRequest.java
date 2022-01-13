package com.robotikflow.core.models.request;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CollectionIntegrationRequest 
{
	private String id;
	@NotNull
	@Size(max=1024)
	private String desc;
	private Integer freq;
	private Integer minOfDay;
	@NotNull
	private boolean active;
	private String start;
	@NotNull
	private TriggerRequest trigger;
	@NotNull
	private List<CollectionIntegrationActivityRequest> activities;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public Integer getFreq() {
		return freq;
	}
	public void setFreq(Integer freq) {
		this.freq = freq;
	}
	public Integer getMinOfDay() {
		return minOfDay;
	}
	public void setMinOfDay(Integer minOfDay) {
		this.minOfDay = minOfDay;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	public TriggerRequest getTrigger() {
		return trigger;
	}
	public void setTrigger(TriggerRequest trigger) {
		this.trigger = trigger;
	}
	public List<CollectionIntegrationActivityRequest> getActivities() {
		return activities;
	}
	public void setActivities(List<CollectionIntegrationActivityRequest> activities) {
		this.activities = activities;
	}
}
