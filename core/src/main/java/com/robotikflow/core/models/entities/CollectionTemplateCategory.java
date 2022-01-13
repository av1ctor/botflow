package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.CollectionTemplateCategoryFilter;
import com.robotikflow.core.models.filters.UserFilter;
import com.robotikflow.core.util.IdUtil;

@Entity
@Table(name = "collections_templates_categories")
public class CollectionTemplateCategory
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	protected String pubId;
	
	@ManyToOne
	private Workspace workspace;
	
	@NotNull
	private String name;
	
	@NotNull
	@Column(name = "\"desc\"")
	private String desc;
	
	private String icon;
	
	private String thumb;

    @Column(name = "\"order\"")
	private Short order;

	@NotNull
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

	@OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
	private Set<CollectionTemplate> templates = new HashSet<>();

	public CollectionTemplateCategory() 
	{
		this.pubId = IdUtil.genId();
	}

	public CollectionTemplateCategory(CollectionTemplateCategoryFilter filtros) 
	{
		this.workspace = new Workspace(filtros.getWorkspace());
		this.pubId = filtros.getPubId();
		this.name = filtros.getName();
		this.createdBy = filtros.getCreatedBy() == null? null: new User(new UserFilter() {{ setEmail(filtros.getCreatedBy()); }});
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

	public String getThumb() {
		return thumb;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

	public Short getOrder() {
		return order;
	}

	public void setOrder(Short order) {
		this.order = order;
	}

	public Set<CollectionTemplate> getTemplates() {
		return templates;
	}

	public void setTemplates(Set<CollectionTemplate> templates) {
		this.templates = templates;
	}
}
