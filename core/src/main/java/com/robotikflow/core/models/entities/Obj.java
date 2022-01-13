package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.ObjFilter;
import com.robotikflow.core.models.misc.ObjFields;
import com.robotikflow.core.util.IdUtil;
import com.robotikflow.core.util.converters.ObjFieldsToJsonConverter;

@Entity
@Table(name = "objects")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Obj<T extends ObjSchema>
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Long id;
	
	@NotNull
	protected String pubId;
	
	@NotNull
	@ManyToOne
	protected Workspace workspace;
	
	@NotNull
	@Convert(converter = ObjFieldsToJsonConverter.class)
	protected ObjFields fields;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	protected User createdBy;
	
	@NotNull
	protected ZonedDateTime createdAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	protected User updatedBy;
	
	protected ZonedDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	private User deletedBy;
	
	private ZonedDateTime deletedAt;
	
	// add optimistic locking
	@Version
	private Long version;

	protected Obj()
	{
		this.pubId = IdUtil.genId();
	}

	protected Obj(
		final ObjFilter filters)
	{
        if(filters != null)
		{
			this.id = filters.getId();
			this.workspace = new Workspace(filters.getWorkspace());
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

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public ObjFields getFields() {
		return fields;
	}

	public void setFields(ObjFields fields) {
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

	public abstract T getSchema();
	public abstract void setSchema(T schema);
}
