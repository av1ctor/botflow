package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.ObjSchemaFilter;
import com.robotikflow.core.models.schemas.obj.Field;
import com.robotikflow.core.models.schemas.obj.ObjType;
import com.robotikflow.core.util.IdUtil;
import com.robotikflow.core.util.converters.MapStringFieldToJsonConverter;

@Entity
@Table(name = "objects_schemas")
@Inheritance(strategy = InheritanceType.JOINED)
public class ObjSchema 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	private String pubId;

	@NotNull
	private Float version;
	
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private ObjType type;
	
	@NotNull
	protected String name;

	@NotNull
	private String title;

	@NotNull
	@Column(name = "\"desc\"")
	private String desc;

	@NotNull
	private String icon;

	@NotNull
	private String schema;

	private Boolean hidden;

	@NotNull
	private Boolean statefull;

	@NotNull
	private String category;

	@NotNull
	@Convert(converter = MapStringFieldToJsonConverter.class)
	private Map<String, Field> fields;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private User createdBy;
	
	@NotNull
	private ZonedDateTime createdAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private User updatedBy;
	
	private ZonedDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	private User deletedBy;
	
	private ZonedDateTime deletedAt;
	
	protected ObjSchema()
	{
		this.pubId = IdUtil.genId();
	}
	
	public ObjSchema(ObjType type)
	{
		this();
		this.type = type;
	}

	public ObjSchema(
		final ObjSchemaFilter filters)
	{
		if(filters != null)
		{
			this.id = filters.getId();
			this.name = filters.getName();
			this.category = filters.getCategory();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPubId() {
		return pubId;
	}

	public void setPubId(String pubId) {
		this.pubId = pubId;
	}

	public Float getVersion() {
		return version;
	}

	public void setVersion(Float version) {
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

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public Boolean getHidden() {
		return hidden;
	}

	public void setHidden(Boolean hidden) {
		this.hidden = hidden;
	}

	public Boolean getStatefull() {
		return statefull;
	}

	public void setStatefull(Boolean statefull) {
		this.statefull = statefull;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public Map<String, Field> getFields() {
		return fields;
	}

	public void setFields(Map<String, Field> fields) {
		this.fields = fields;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public User getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}

	public ZonedDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(ZonedDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public User getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(User deletedBy) {
		this.deletedBy = deletedBy;
	}

	public ZonedDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(ZonedDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}
}
