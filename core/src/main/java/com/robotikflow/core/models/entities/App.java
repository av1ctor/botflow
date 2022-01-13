package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.robotikflow.core.models.filters.AppFilter;
import com.robotikflow.core.models.schemas.app.AppSchema;
import com.robotikflow.core.util.IdUtil;
import com.robotikflow.core.util.converters.AppSchemaToJsonConverter;

@Entity
@Table(name = "apps")
public class App 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
	@NotNull
    private String pubId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Workspace workspace;

	@NotNull
	private boolean active;

	@NotNull
	@Size(max=24)
	private String icon;

	@NotNull
	@Size(max=64)
    private String title;

	@NotNull
	@Size(max=512)
	@Column(name = "\"desc\"")
    private String desc;

	@NotNull
	private long options;

	@Column(name = "schema", insertable = false, updatable = false)
	@Convert(converter = AppSchemaToJsonConverter.class)
	private AppSchema schemaObj;

	@NotNull
	private String schema;

    @NotNull
    private ZonedDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;

    private ZonedDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User updatedBy;

	private ZonedDateTime publishedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	private User publishedBy;

	private ZonedDateTime deletedAt;
    
	@ManyToOne(fetch = FetchType.LAZY)
	private User deletedBy;
	
	public App()
	{
		this.pubId = IdUtil.genId();
	}

	public App(AppFilter filters)
	{
		this.pubId = filters.getPubId();
		this.title = filters.getTitle();
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

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
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
    
    public AppSchema getSchemaObj() {
		return schemaObj;
	}

	public void setSchemaObj(AppSchema schemaObj) {
		this.schemaObj = schemaObj;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(ZonedDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

	public void setPublishedAt(ZonedDateTime publishedAt) {
		this.publishedAt = publishedAt;
	}

	public ZonedDateTime getPublishedAt() {
		return publishedAt;
	}

	public User getPublishedBy() {
		return publishedBy;
	}

	public void setPublishedBy(User publishedBy) {
		this.publishedBy = publishedBy;
	}
	
    public ZonedDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(ZonedDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public User getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(User deletedBy) {
		this.deletedBy = deletedBy;
	}
}