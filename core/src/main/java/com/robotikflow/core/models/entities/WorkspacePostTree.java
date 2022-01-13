package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "workspaces_posts_tree")
@IdClass(WorkspacePostTreeId.class)
public class WorkspacePostTree 
{
	@Id
	@ManyToOne
	private WorkspacePost parent;
	
	@Id
	@ManyToOne
	private WorkspacePost child;
	
	@NotNull
	private Short depth;

	public WorkspacePost getParent() {
		return parent;
	}

	public void setParent(WorkspacePost parent) {
		this.parent = parent;
	}

	public WorkspacePost getChild() {
		return child;
	}

	public void setChild(WorkspacePost child) {
		this.child = child;
	}

	public Short getDepth() {
		return depth;
	}

	public void setDepth(Short depth) {
		this.depth = depth;
	}
}
