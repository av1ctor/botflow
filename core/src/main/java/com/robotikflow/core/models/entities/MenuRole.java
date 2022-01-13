package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "menus_roles")
@IdClass(MenuRoleId.class)
public class MenuRole 
{
	@Id
	@ManyToOne 
	private Menu menu;

	@Id
	@ManyToOne
	private Role role;
	
	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public Menu getMenu() {
		return menu;
	}

	public void setMenu(Menu menu) {
		this.menu = menu;
	}
}
