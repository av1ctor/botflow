package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.App;

public class AppBaseResponse 
{
	private final String id;
	private final String title;

	public AppBaseResponse(
		final App page)
	{
		id = page.getPubId();
		title = page.getTitle();
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}
}
