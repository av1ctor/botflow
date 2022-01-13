package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.InheritanceType;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.util.DocumentUtil;
import com.robotikflow.core.util.IdUtil;

@Entity
@Table(name = "documents")
@Inheritance(strategy = InheritanceType.JOINED)
public class Document
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	protected Long id;

	@NotNull
	protected String pubId;
	
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private Provider provider;

	@NotNull
	@Enumerated(EnumType.ORDINAL)
	protected DocumentType type;
	
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	protected Workspace workspace;
	
	@ManyToOne(fetch = FetchType.LAZY)
	protected Document parent;
	
	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	protected Set<Document> children = new HashSet<>();

	@NotNull
	protected String name;

	protected String extension;
	
	protected Long size;
	
	protected Short version;
	
	@ManyToOne(fetch = FetchType.EAGER)
	protected User owner;

	@ManyToOne(fetch = FetchType.EAGER)
	protected Group group;
	
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	protected DocumentAuthType ownerAuth;
	
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	protected DocumentAuthType groupAuth;
	
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	protected DocumentAuthType othersAuth;

	@ManyToOne(fetch = FetchType.EAGER)
	protected User createdBy;
	
	@NotNull
	protected ZonedDateTime createdAt;
	
	@ManyToOne(fetch = FetchType.EAGER)
	protected User updatedBy;
	
	protected ZonedDateTime updatedAt;
	
	@ManyToOne(fetch = FetchType.LAZY)
	protected User deletedBy;
	
	protected ZonedDateTime deletedAt;

	public Document()
	{
		this.pubId = IdUtil.genId();
	}

	public Document(
		Provider provider,
		String name, 
		DocumentType type,
		Long tamanho,
		Document parent,
		User user,
		Group group,
		DocumentAuthType ownerAuth, 
		DocumentAuthType groupAuth,
		DocumentAuthType othersAuth,
		ZonedDateTime createdAt,
		Workspace workspace)
	{
		this();
		this.provider = provider;
		this.workspace = workspace;
		this.name = name;
		this.type = type;
		this.extension = DocumentUtil.getExtension(name, type);
		this.size = tamanho;
		this.parent = parent;
		this.owner = user;
		this.group = group;
		this.ownerAuth = ownerAuth;
		this.groupAuth = groupAuth;
		this.othersAuth = othersAuth;
		this.createdBy = this.updatedBy = user;
		this.createdAt = this.updatedAt = createdAt;
		this.version = 0;
	}

	public Document(
		Provider provider,
		String name, 
		DocumentType type,
		Long tamanho,
		Document parent,
		User user,
		Group group,
		DocumentAuthType ownerAuth, 
		DocumentAuthType groupAuth,
		DocumentAuthType othersAuth,
		Workspace workspace)
	{
		this(
			provider, 
			name, 
			type, 
			tamanho, 
			parent, 
			user, 
			group, 
			ownerAuth, 
			groupAuth, 
			othersAuth, 
			ZonedDateTime.now(), 
			workspace);
	}

	public Document(
		Provider provider,
		String name, 
		DocumentType type, 
		Long tamanho, 
		Document parent, 
		User user, 
		Group group, 
		Workspace workspace)
	{
		this(
			provider,
			name, 
			type, 
			tamanho, 
			parent, 
			user, 
			group, 
			parent.getOwnerAuth(), 
			parent.getGroupAuth(),
			parent.getOthersAuth(),
			workspace);
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

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public DocumentType getType() {
		return type;
	}

	public void setType(DocumentType type) {
		this.type = type;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public Document getParent() {
		return parent;
	}

	public void setParent(Document parent) {
		this.parent = parent;
	}

	public Set<Document> getChildren() {
		return children;
	}

	public void setChildren(Set<Document> children) {
		this.children = children;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public Short getVersion() {
		return version;
	}

	public void setVersion(Short version) {
		this.version = version;
	}

	public User getOwner() {
		return owner;
	}

	public void setOwner(User owner) {
		this.owner = owner;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public DocumentAuthType getOwnerAuth() {
		return ownerAuth;
	}

	public void setOwnerAuth(DocumentAuthType ownerAuth) {
		this.ownerAuth = ownerAuth;
	}

	public DocumentAuthType getGroupAuth() {
		return groupAuth;
	}

	public void setGroupAuth(DocumentAuthType groupAuth) {
		this.groupAuth = groupAuth;
	}

	public DocumentAuthType getOthersAuth() {
		return othersAuth;
	}

	public void setOthersAuth(DocumentAuthType othersAuth) {
		this.othersAuth = othersAuth;
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

