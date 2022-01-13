package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionAutomation;
import com.robotikflow.core.models.response.UserBaseResponse;
import com.robotikflow.core.util.DocumentUtil;

public class CollectionAutomationResponse
	extends CollectionAutomationBaseResponse
{
	private final UserBaseResponse createdBy;
	private final String createdAt;
	private final UserBaseResponse updatedBy;
	private final String updatedAt;

	public CollectionAutomationResponse(
		CollectionAutomation automacao)
	{
		super(automacao);

		createdBy = new UserBaseResponse(automacao.getCreatedBy());
		createdAt = automacao.getCreatedAt().format(DocumentUtil.datePattern);
		updatedBy = automacao.getUpdatedBy() != null? 
			new UserBaseResponse(automacao.getUpdatedBy()): 
			null;
		updatedAt = automacao.getUpdatedAt() != null? 
			automacao.getUpdatedAt().format(DocumentUtil.datePattern): 
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
