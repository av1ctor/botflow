package com.robotikflow.core.models.request;

import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.entities.CollectionAutomationDateRepeat;

public class CollectionAutomationDateRequest 
	extends CollectionAutomationRequest
{
	@NotNull
	private CollectionAutomationDateRepeat repeat;
	@NotNull
	private String start;
			
	public CollectionAutomationDateRepeat getRepeat() {
		return repeat;
	}

	public void setRepeat(CollectionAutomationDateRepeat repeat) {
		this.repeat = repeat;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}
}
