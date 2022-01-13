package com.robotikflow.core.models.entities;

import java.io.Serializable;
import java.util.Objects;

public class MenuRoleId implements Serializable 
{
	private static final long serialVersionUID = -7941317171234939015L;
	
	private Long menu;
	private Long role;
	public Long getMenu() {
		return menu;
	}
	public void setMenu(Long menu) {
		this.menu = menu;
	}
	public Long getRole() {
		return role;
	}
	public void setRole(Long role) {
		this.role = role;
	}
	@Override
	public int hashCode() {
		return Objects.hash(role, menu);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MenuRoleId other = (MenuRoleId) obj;
		return Objects.equals(role, other.role) && Objects.equals(menu, other.menu);
	}
	
	
}
