package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionAuth;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.response.UserBaseResponse;
import com.robotikflow.core.util.DocumentUtil;

public class CollectionAuthResponse
	extends CollectionAuthBaseResponse
{
	private final UserBaseResponse createdBy;
	private final String createdAt;
	private final UserBaseResponse updatedBy;
	private final String updatedAt;

	public CollectionAuthResponse(
		final CollectionAuth auth, 
		final Workspace workspace)
	{
		super(auth, workspace);
		
		createdBy = new UserBaseResponse(auth.getCreatedBy());
		createdAt = auth.getCreatedAt().format(DocumentUtil.datePattern);
		updatedBy = auth.getUpdatedBy() != null? 
			new UserBaseResponse(auth.getUpdatedBy()): 
			null;
		updatedAt = auth.getUpdatedAt() != null? 
			auth.getUpdatedAt().format(DocumentUtil.datePattern): 
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
