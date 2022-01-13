package com.robotikflow.core.models.request;

import javax.validation.constraints.NotNull;

public class CollectionIntegrationActivityRequest 
{
	private String id;
	@NotNull
	private ActivityRequest activity;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public ActivityRequest getActivity() {
		return activity;
	}
	public void setActivity(ActivityRequest activity) {
		this.activity = activity;
	}
}
