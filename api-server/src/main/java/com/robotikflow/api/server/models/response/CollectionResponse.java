package com.robotikflow.api.server.models.response;

import com.robotikflow.core.models.entities.Collection;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.CollectionType;
import com.robotikflow.core.models.response.ProviderBaseResponse;
import com.robotikflow.core.models.response.UserBaseResponse;
import com.robotikflow.core.util.DocumentUtil;

public class CollectionResponse 
	extends CollectionBaseResponse
{
	private final String name;
	private final String desc;
	private final UserBaseResponse createdBy;
	private final String createdAt;
	private final UserBaseResponse updatedBy;
	private final String updatedAt;
	private final String schema;
	private final long options;
	private final String autoGenId;
	private final String positionalId;
	private final UserBaseResponse publicadoPor;
	private final String publishedAt;
	private final String icon;
	private final Short order;
	private final ProviderBaseResponse provider;
	private final CollectionParentResponse parent;

	public CollectionResponse(
		final Collection collection, 
		final String schema)
	{
		super(collection, schema);
		
		parent = new CollectionParentResponse(collection.getParent());
		name = collection.getName();
		desc = collection.getDesc();
		createdBy = new UserBaseResponse(collection.getCreatedBy());
		createdAt = collection.getCreatedAt().format(DocumentUtil.datePattern);
		updatedBy = collection.getUpdatedBy() != null? 
			new UserBaseResponse(collection.getUpdatedBy()): 
			null;
		updatedAt = collection.getUpdatedAt() != null? 
			collection.getUpdatedAt().format(DocumentUtil.datePattern): 
			null;
		this.schema = schema;
		publicadoPor = collection.getPublishedBy() != null? 
			new UserBaseResponse(collection.getPublishedBy()): 
			null;
		publishedAt = collection.getPublishedAt() != null? 
			collection.getPublishedAt().format(DocumentUtil.datePattern): 
			null;
		icon = collection.getIcon();
		order = collection.getOrder();
		if(collection.getType() == CollectionType.SCHEMA)
		{
			var ctabela = (CollectionWithSchema)collection;
			options = ctabela.getOptions();
			autoGenId = ctabela.getAutoGenId();
			positionalId = ctabela.getPositionalId();
			provider = ctabela.getProvider() != null? 
				new ProviderBaseResponse(ctabela.getProvider()): 
				null;
		}
		else
		{
			options = 0;
			autoGenId = null;
			positionalId = null;
			provider = null;
		}
	}

	public CollectionResponse(
		final Collection collection)
	{
		this(collection, collection.getType() == CollectionType.SCHEMA? 
			((CollectionWithSchema)collection).getSchema(): 
			null);
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

	public String getSchema() {
		return schema;
	}

	public long getOptions() {
		return options;
	}

	public UserBaseResponse getPublishedBy() {
		return publicadoPor;
	}

	public String getPublishedAt() {
		return publishedAt;
	}

	public String getIcon() {
		return icon;
	}

	public Short getOrder() {
		return order;
	}

	public String getAutoGenId() {
		return autoGenId;
	}

	public String getPositionalId() {
		return positionalId;
	}

	public ProviderBaseResponse getProvider() {
		return provider;
	}

	public CollectionParentResponse getParent() {
		return parent;
	}
}
