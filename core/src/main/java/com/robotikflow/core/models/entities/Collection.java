package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
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
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.robotikflow.core.models.filters.CollectionFilter;
import com.robotikflow.core.models.filters.UserFilter;
import com.robotikflow.core.util.IdUtil;

@Entity
@Table(name = "collections")
@Inheritance(strategy = InheritanceType.JOINED)
public class Collection 
{
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private CollectionType type;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	private String pubId;
	
	@NotNull
	@ManyToOne
	private Workspace workspace;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Collection parent;
	
	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	private Set<Collection> children = new HashSet<>();

	@NotNull
	@Size(max=64)
	private String name;
	
	@Size(max=512)
	@Column(name = "\"desc\"")
	private String desc;
	
	@Size(max=24)
	private String icon;
	
    @Column(name = "\"order\"")
	private Short order;

	@ManyToOne(fetch = FetchType.EAGER)
	private User publishedBy;
	
	private ZonedDateTime publishedAt;

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	private User createdBy;
	
	@NotNull
	private ZonedDateTime createdAt;
	
	@ManyToOne(fetch = FetchType.EAGER)
	private User updatedBy;
	
	private ZonedDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	private User deletedBy;
	
	private ZonedDateTime deletedAt;
	
	@OneToMany(mappedBy = "collection", fetch = FetchType.LAZY)
	private Set<CollectionAuth> auths;

	@OneToMany(mappedBy = "collection", 
		fetch = FetchType.LAZY, 
		cascade = {CascadeType.MERGE, CascadeType.PERSIST}, 
		orphanRemoval = true)
	@OrderBy("id desc")
	private List<CollectionVersion> versions = new ArrayList<>();

	protected Collection()
	{
	}
	
	public Collection(CollectionType type)
	{
		this.pubId = IdUtil.genId();
		this.type = type;
	}

	public Collection(
		CollectionFilter filtros, 
		String idParent) 
	{
		this.workspace = new Workspace(filtros.getWorkspace());
		this.pubId = filtros.getPubId();
		this.name = filtros.getName();
		this.parent = idParent != null? new Collection() {{ setPubId(idParent); }}: null;
		this.createdBy = filtros.getCreatedBy() == null? null: new User(new UserFilter() {{ setEmail(filtros.getCreatedBy()); }});
	}

	public Collection(
		Collection collection, 
		User user, 
		EnumSet<CollectionDupOptions> with)
	{
		this(collection.type);
		this.workspace = collection.workspace;
		this.parent = collection.parent;
		this.createdBy = user;
		this.createdAt = ZonedDateTime.now();
		this.name = collection.name;
		this.desc = collection.desc;
		this.icon = collection.icon;
		this.order = collection.order;
		this.publishedBy = collection.publishedBy != null? user: null;
		this.publishedAt = collection.publishedAt != null? this.createdAt: null;

		if(with.contains(CollectionDupOptions.PERMISSIONS))
		{
			if(collection.auths != null)
			{
				this.auths = new HashSet<>();
				
				for(var cup : collection.auths)
				{
					var cupDup = new CollectionAuth(cup, this);
					this.auths.add(cupDup);
				}
			}
		}
	}

	public Collection(
		CollectionType type, 
		String name, 
		Collection parent, 
		User user, 
		Workspace workspace) 
	{
		this(type);
		this.parent = parent;
		this.workspace = workspace;
		this.name = name;
		this.createdBy = user;
		this.createdAt = ZonedDateTime.now();
	}

	public CollectionType getType() {
		return type;
	}

	public void setType(CollectionType type) {
		this.type = type;
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

	public void setWorspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public Collection getParent() {
		return parent;
	}

	public void setParent(Collection parent) {
		this.parent = parent;
	}

	public Set<Collection> getChildren() {
		return children;
	}

	public void setChildren(Set<Collection> children) {
		this.children = children;
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

	public User getPublishedBy() {
		return publishedBy;
	}

	public void setPublishedBy(User publishedBy) {
		this.publishedBy = publishedBy;
	}

	public ZonedDateTime getPublishedAt() {
		return publishedAt;
	}

	public void setPublishedAt(ZonedDateTime publishedAt) {
		this.publishedAt = publishedAt;
	}

	public Set<CollectionAuth> getAuthorizations() {
		return auths;
	}

	public void setAuthorizations(Set<CollectionAuth> auths) {
		this.auths = auths;
	}

	public List<CollectionVersion> getVersions() {
		return versions;
	}

	public void setVersions(List<CollectionVersion> versions) {
		this.versions = versions;
	}
}
