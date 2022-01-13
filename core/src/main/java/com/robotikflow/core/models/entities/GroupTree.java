package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "groups_tree")
@IdClass(GroupTreeId.class)
public class GroupTree 
{
	@Id
	@NotNull
	@ManyToOne
	private Group parent;
	
	@Id
	@NotNull
	@ManyToOne
	private Group child;
	
	@NotNull
	private Short depth;

	public Group getParent() {
		return parent;
	}

	public void setParent(Group parent) {
		this.parent = parent;
	}

	public Group getChild() {
		return child;
	}

	public void setChild(Group child) {
		this.child = child;
	}

	public Short getDepth() {
		return depth;
	}

	public void setDepth(Short depth) {
		this.depth = depth;
	}
	
	
}
