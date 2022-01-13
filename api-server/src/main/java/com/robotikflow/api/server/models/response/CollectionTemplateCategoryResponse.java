package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.CollectionTemplateCategory;
import com.robotikflow.core.models.response.UserBaseResponse;
import com.robotikflow.core.util.DocumentUtil;

public class CollectionTemplateCategoryResponse 
{
	private final String id;
	private final String name;
	private final String desc;
	private final UserBaseResponse createdBy;
	private final String createdAt;
	private final UserBaseResponse updatedBy;
	private final String updatedAt;
	private final String icon;
	private final String thumb;
	private final Short order;

	public CollectionTemplateCategoryResponse(
		final CollectionTemplateCategory template, 
		final Long idWorkspace)
	{
		id = template.getPubId();
		name = template.getName();
		desc = template.getDesc();
		createdBy = new UserBaseResponse(template.getCreatedBy());
		createdAt = template.getCreatedAt().format(DocumentUtil.datePattern);;
		updatedBy = template.getUpdatedBy() != null? 
			new UserBaseResponse(template.getUpdatedBy()):
			null;
		updatedAt = template.getUpdatedAt() != null? 
			template.getUpdatedAt().format(DocumentUtil.datePattern): 
			null;
		icon = template.getIcon();
		thumb = template.getThumb();
		order = template.getOrder();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
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

	public String getIcon() {
		return icon;
	}

	public String getThumb() {
		return thumb;
	}

	public Short getOrder() {
		return order;
	}
}
