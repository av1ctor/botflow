package com.robotikflow.core.models.request;

import javax.validation.constraints.NotBlank;

public class AppRequest 
{
	private String id;
	@NotBlank
	private String title;
	@NotBlank
	private String desc;
	private boolean active;
	private long options;
	@NotBlank
	private String icon;
	@NotBlank
	private String schema;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
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
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public boolean getActive() {
        return active;
    }
}
