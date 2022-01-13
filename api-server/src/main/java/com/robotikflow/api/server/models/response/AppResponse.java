package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.App;
import com.robotikflow.core.models.response.UserBaseResponse;
import com.robotikflow.core.util.DocumentUtil;

public class AppResponse 
	extends AppBaseResponse
{
	private final String title;
	private final String desc;
	private final String schema;
	private final long options;
	private final String icon;
	private final UserBaseResponse createdBy;
	private final String createdAt;
	private final UserBaseResponse updatedBy;
	private final String updatedAt;
	private final UserBaseResponse publishedBy;
	private final String publishedAt;

	public AppResponse(
		final App page)
	{
		super(page);
		
		title = page.getTitle();
		desc = page.getDesc();
		createdBy = new UserBaseResponse(page.getCreatedBy());
		createdAt = page.getCreatedAt().format(DocumentUtil.datePattern);
		updatedBy = page.getUpdatedBy() != null? 
			new UserBaseResponse(page.getUpdatedBy()): 
			null;
		updatedAt = page.getUpdatedAt() != null? 
			page.getUpdatedAt().format(DocumentUtil.datePattern): 
			null;
		this.schema = page.getSchema();
		publishedBy = page.getPublishedBy() != null? 
			new UserBaseResponse(page.getPublishedBy()): 
			null;
		publishedAt = page.getPublishedAt() != null? 
			page.getPublishedAt().format(DocumentUtil.datePattern): 
			null;
		icon = page.getIcon();
		options = page.getOptions();
	}

	public String getName() {
		return title;
	}

	public String getDesc() {
		return desc;
	}

	public UserBaseResponse getCreatedBy() {
		return createdBy;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public UserBaseResponse getUpdatedBy() {
		return updatedBy;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public String getSchema() {
		return schema;
	}

	public long getOptions() {
		return options;
	}

	public UserBaseResponse getPublishedBy() {
		return publishedBy;
	}

	public String getPublishedAt() {
		return publishedAt;
	}

	public String getIcon() {
		return icon;
	}
}
