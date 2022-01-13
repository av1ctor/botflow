package com.robotikflow.core.models.entities;

import java.io.Serializable;
import java.util.Objects;

public class GroupTreeId implements Serializable
{
	private static final long serialVersionUID = 8330214427393737686L;
	
	private Long parent;
	private Long child;
	
	public Long getParent() {
		return parent;
	}
	public void setParent(Long parent) {
		this.parent = parent;
	}
	public Long getChild() {
		return child;
	}
	public void setChild(Long child) {
		this.child = child;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(child, parent);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GroupTreeId other = (GroupTreeId) obj;
		return Objects.equals(child, other.child) && Objects.equals(parent, other.parent);
	}
}
