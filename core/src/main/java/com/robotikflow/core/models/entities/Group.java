package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.robotikflow.core.models.filters.GroupFilter;
import com.robotikflow.core.util.IdUtil;

@Entity
@Table(name = "groups")
public class Group 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull
	private String pubId;
	
	@NotNull
	@Size(min=3, max=64)
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Group parent;
	
	@NotNull
	private boolean deletable;
	
	@NotNull
	@ManyToOne(fetch=FetchType.EAGER)
	private Workspace workspace;
	
	@ManyToOne(fetch = FetchType.LAZY)
	protected User createdBy;
	
	protected ZonedDateTime createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	protected User updatedBy;
	
	protected ZonedDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	protected User deletedBy;
	
	protected ZonedDateTime deletedAt;

	public Group()
	{
		this.pubId = IdUtil.genId();
	}
	
	public Group(
		final String name, 
		final Workspace workspace, 
		final Group parente,
		final boolean deletable)
	{
		this();
		this.name = name;
		this.workspace = workspace;
		this.parent = parente;
		this.deletable = deletable;
	}

	public Group(
		final GroupFilter filtros)
	{
		this.pubId = filtros.getPubId();
		this.name = filtros.getName();
		this.workspace = new Workspace(filtros.getWorkspace());
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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public boolean isDeletable() {
		return deletable;
	}

	public void setDeletable(boolean deletavel) {
		this.deletable = deletavel;
	}

	public Group getParent() {
		return parent;
	}

	public void setParent(Group parent) {
		this.parent = parent;
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
}
