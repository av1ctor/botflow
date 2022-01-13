package com.robotikflow.core.models.entities;

import java.io.Serializable;
import java.util.Objects;

public class MenuGroupId implements Serializable 
{
	private static final long serialVersionUID = -7941317174598939015L;
	
	private Long menu;
	private Long group;
	public Long getMenu() {
		return menu;
	}
	public void setMenu(Long menu) {
		this.menu = menu;
	}
	public Long getGroup() {
		return group;
	}
	public void setGroup(Long group) {
		this.group = group;
	}
	@Override
	public int hashCode() {
		return Objects.hash(group, menu);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MenuGroupId other = (MenuGroupId) obj;
		return Objects.equals(group, other.group) && Objects.equals(menu, other.menu);
	}
	
	
}
