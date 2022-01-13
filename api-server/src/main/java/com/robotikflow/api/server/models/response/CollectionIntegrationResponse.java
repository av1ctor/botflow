package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionIntegration;
import com.robotikflow.core.models.response.UserBaseResponse;
import com.robotikflow.core.util.DocumentUtil;

public class CollectionIntegrationResponse 
	extends CollectionIntegrationBaseResponse
{
	private final UserBaseResponse createdBy;
	private final String createdAt;
	private final UserBaseResponse updatedBy;
	private final String updatedAt;

	public CollectionIntegrationResponse(
		final CollectionIntegration integration)
	{
		super(integration);
		
		createdBy = new UserBaseResponse(integration.getCreatedBy());
		createdAt = integration.getCreatedAt().format(DocumentUtil.datePattern);
		updatedBy = integration.getUpdatedBy() != null? 
			new UserBaseResponse(integration.getUpdatedBy()): 
			null;
		updatedAt = integration.getUpdatedAt() != null? 
			integration.getUpdatedAt().format(DocumentUtil.datePattern): 
			null;
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
}
