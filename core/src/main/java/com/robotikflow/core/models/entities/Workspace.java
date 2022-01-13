package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.WorkspaceFilter;
import com.robotikflow.core.util.IdUtil;

@Entity
@Table(name = "workspaces")
public class Workspace 
{
	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull
	private String pubId;
	
	@NotNull
	private String name;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Provider provider;

	@ManyToOne(fetch = FetchType.LAZY)
	private Document rootDoc;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Collection rootCollection;

	@ManyToOne(fetch = FetchType.LAZY)
	private User owner;
	
	@NotNull
	private ZonedDateTime createdAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	protected User updatedBy;
	
	private ZonedDateTime updatedAt;
	
	@OneToMany(mappedBy = "workspace", fetch = FetchType.LAZY)
	private Set<UserRoleWorkspace> userWorkspacePerfis = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	protected User deletedBy;
	
	protected ZonedDateTime deletedAt;

	public Workspace()
	{
		this.pubId = IdUtil.genId();
	}
	
	public Workspace(
		final String name,
		final ZonedDateTime createdAt)
	{
		this();
		this.name = name;
		this.createdAt = createdAt;
	}

	public Workspace(WorkspaceFilter filtros) 
	{
		this.id = filtros.getId();
	}
	
	public List<User> getUsers()
	{
		return userWorkspacePerfis.stream()
				.map(urp -> urp.getUser()).collect(Collectors.toList());
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

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public Document getRootDoc() {
		return rootDoc;
	}

	public void setRootDoc(Document rootDoc) {
		this.rootDoc = rootDoc;
	}

	public Collection getRootCollection() {
		return rootCollection;
	}

	public void setRootCollection(Collection rootCollection) {
		this.rootCollection = rootCollection;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
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

	public Set<UserRoleWorkspace> getUserWorkspacePerfis() {
		return userWorkspacePerfis;
	}

	public void setUserWorkspacePerfis(Set<UserRoleWorkspace> userWorkspacePerfis) {
		this.userWorkspacePerfis = userWorkspacePerfis;
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
