package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionVersion;
import com.robotikflow.core.util.DocumentUtil;

public class CollectionVersionResponse 
{
	private final String id;
	private final String desc;
	private final String diff;
	private final String createdAt;

	public CollectionVersionResponse(CollectionVersion version)
	{
		id = version.getPubId();
		desc = version.getChange().getDesc();
		diff = version.getDiff();
		createdAt = version.getCreatedAt().format(DocumentUtil.datePattern);
	}

	public String getId() {
		return id;
	}

	public String getDesc() {
		return desc;
	}

	public String getDiff() {
		return diff;
	}

	public String getCreatedAt() {
		return createdAt;
	}
}
