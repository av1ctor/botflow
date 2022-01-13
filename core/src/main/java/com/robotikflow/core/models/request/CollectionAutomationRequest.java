package com.robotikflow.core.models.request;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.robotikflow.core.models.entities.CollectionAutomationTrigger;
import com.robotikflow.core.models.entities.CollectionAutomationType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
	@JsonSubTypes.Type(value = CollectionAutomationFieldRequest.class, name = "FIELD"),
	@JsonSubTypes.Type(value = CollectionAutomationItemRequest.class, name = "ITEM"),
	@JsonSubTypes.Type(value = CollectionAutomationDateRequest.class, name = "DATE")
})
public abstract class CollectionAutomationRequest 
{
	private String id;
	@NotNull
	private CollectionAutomationType type;
	@NotNull
	private CollectionAutomationTrigger trigger;
	@NotNull
	private List<CollectionAutomationActivityRequest> activities;
	@NotNull
	@Size(max=1024)
	private String desc;
	
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
	public CollectionAutomationType getType() {
		return type;
	}
	public void setType(CollectionAutomationType type) {
		this.type = type;
	}
	public CollectionAutomationTrigger getTrigger() {
		return trigger;
	}
	public void setTrigger(CollectionAutomationTrigger trigger) {
		this.trigger = trigger;
	}
	public List<CollectionAutomationActivityRequest> getActivities() {
		return activities;
	}
	public void setActivities(List<CollectionAutomationActivityRequest> activities) {
		this.activities = activities;
	}
}
