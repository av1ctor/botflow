package com.robotikflow.core.models.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.schemas.obj.ObjType;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes({
	@JsonSubTypes.Type(value = CredentialSchemaRequest.class, name = "credential"),
	@JsonSubTypes.Type(value = ProviderSchemaRequest.class, name = "provider"),
	@JsonSubTypes.Type(value = ActivitySchemaRequest.class, name = "activity"),
})
public class ObjSchemaRequest 
	extends ObjBaseRequest
{
	private String id;
	@NotNull
    private float version;
	@NotNull
    private ObjType type;
	@NotBlank
	private String name;
	@NotBlank
	private String title;
	@NotBlank
	private String desc;
	@NotBlank
	private String icon;
	@NotBlank
	private String category;
	@NotBlank
	private String schema;

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public float getVersion() {
		return version;
	}
	public void setVersion(float version) {
		this.version = version;
	}	
	public ObjType getType() {
		return type;
	}
	public void setType(ObjType type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getCategory() {
		return category;
	}
	public void setCategory(String category) {
		this.category = category;
	}
	public String getSchema() {
		return schema;
	}
	public void setSchema(String schema) {
		this.schema = schema;
	}
}
