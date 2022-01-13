package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.util.IdUtil;

import org.hibernate.annotations.DynamicUpdate;

//NOTE: @DynamicUpdate is needed here because we update parent's 'posts' field 
//      when adding a child post, what could cause a race-condition if user is 
//      updating parent at same time
@Entity
@Table(name = "workspaces_posts")
@Inheritance(strategy = InheritanceType.JOINED)
@DynamicUpdate 
public class WorkspacePost 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
	@NotNull
    private String pubId;
    
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private WorkspacePostType type;
    
	@NotNull
	private long options;

    @NotNull
    @Column(name = "\"order\"")
	private short order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    private WorkspacePost parent;

    @NotNull
    private short level;

    private String title;

    @NotNull
    private String message;

    @NotNull
    private int posts;

    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<WorkspacePost> children = new ArrayList<>();

    @NotNull
    private ZonedDateTime createdAt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private User createdBy;

    private ZonedDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User updatedBy;

	public WorkspacePost()
	{
		this.pubId = IdUtil.genId();
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

    public WorkspacePostType getType() {
        return type;
    }

    public void setType(WorkspacePostType type) {
        this.type = type;
    }

    public Workspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    public WorkspacePost getParent() {
        return parent;
    }

    public void setParent(WorkspacePost parent) {
        this.parent = parent;
    }

    public List<WorkspacePost> getChildren() {
        return children;
    }

    public short getLevel() {
        return level;
    }

    public void setLevel(short level) {
        this.level = level;
    }
        
	public long getOptions() {
		return options;
	}

	public boolean isLocked() {
		return (options & WorkspacePostOptions.NO_EDIT.asLong()) != 0;
	}

	public void setOptions(long options) {
		this.options = options;
	}

	public void setOptions(EnumSet<WorkspacePostOptions> options) {
		for(var option : options)
		{
			this.options |= option.asLong();
		}
    }

	public short getOrder() {
		return order;
    }
    
	public void setOrder(short order) {
		this.order = order;
	}
    
    public int getPosts() {
        return posts;
    }

    public void setPosts(int posts) {
        this.posts = posts;
    }
        
    public void setChildren(List<WorkspacePost> children) {
        this.children = children;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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
}