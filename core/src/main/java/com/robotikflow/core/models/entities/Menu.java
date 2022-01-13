package com.robotikflow.core.models.entities;

import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "menus")
public class Menu 
{
	@Id 
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
    @NotNull
    @Column(unique=true)
    private String label;

    private String alias;
	
    @NotNull
    private Short order;
    
    @NotNull
    private String icon;

    private String command;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Menu parent;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private Workspace workspace;

    @OneToMany(mappedBy = "parent", fetch = FetchType.EAGER)
    @OrderBy("order ASC")
	private List<Menu> items;
    
    @OneToMany(mappedBy = "menu", fetch = FetchType.EAGER)
    private Set<MenuGroup> groups;
	
    @OneToMany(mappedBy = "menu", fetch = FetchType.EAGER)
    private Set<MenuRole> roles;
	
	public Menu()
	{
	}

	public Menu(Workspace workspace)
	{
		this.workspace = workspace;
	}

	public Menu(String label, Short order, String icon, String command, Menu parent, Workspace workspace, List<Menu> items, Set<MenuGroup> groups)
	{
		this.label = label;
		this.order = order;
		this.icon = icon;
		this.command = command;
		this.parent = parent;
		this.workspace = workspace;
		this.items = items;
		this.groups = groups;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public Menu getParent() {
		return parent;
	}

	public void setParent(Menu parent) {
		this.parent = parent;
	}

	public List<Menu> getItems()
	{
		return items;
	}
	
	public void setItems(List<Menu> items) {
		this.items = items;
	}

	public Short getOrder() {
		return order;
	}

	public void setOrder(Short order) {
		this.order = order;
	}

	public Set<MenuGroup> getGroups() {
		return groups;
	}

	public void setGroups(Set<MenuGroup> groups) {
		this.groups = groups;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public Set<MenuRole> getRoles() {
		return roles;
	}

	public void setRoles(Set<MenuRole> roles) {
		this.roles = roles;
	}
}
