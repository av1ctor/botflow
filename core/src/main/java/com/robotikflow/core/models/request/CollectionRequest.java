package com.robotikflow.core.models.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class CollectionRequest 
{
	private String id;
	@NotNull
	@Size(min=3, max=64)
	private String name;
	@Size(max=1024)
	private String desc;
	private long options;
	private String schema;
	private String icon;
	private Short order;
	private ProviderBaseRequest provider;
	private boolean published;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public long getOptions() {
		return options;
	}
	public void setOptions(long options) {
		this.options = options;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public Short getOrder() {
		return order;
	}
	public void setOrder(Short order) {
		this.order = order;
	}
	public ProviderBaseRequest getProvider() {
		return provider;
	}
	public void setProvider(ProviderBaseRequest provider) {
		this.provider = provider;
	}

	public boolean isPublished() {
		return published;
	}

	public void setPublished(boolean published) {
		this.published = published;
	}
}
