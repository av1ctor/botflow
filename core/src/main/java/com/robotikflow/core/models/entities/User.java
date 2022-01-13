package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.UserFilter;
import com.robotikflow.core.util.IdUtil;

@Entity
@Table(name = "users")
public class User 
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	private String pubId;

	@NotNull
	private String email;

	@NotNull
	private String nick;

	private String name;

	private String password;

	@NotNull
	private boolean active;

	@NotNull
	private String icon;

	@NotNull
	private String timezone;

	private String theme;

	@NotNull
	private String lang;

	@NotNull
	private ZonedDateTime createdAt;

	private ZonedDateTime updatedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	protected User updatedBy;

	private ZonedDateTime confirmedAt;

	protected ZonedDateTime deletedAt;

	@ManyToOne(fetch = FetchType.LAZY)
	protected User deletedBy;

	@OneToMany(mappedBy = "user", 
		fetch = FetchType.EAGER, 
		cascade = {CascadeType.MERGE, CascadeType.PERSIST}, 
		orphanRemoval = true)
	private Set<UserPropsWorkspace> props = new HashSet<>();

	@OneToMany(mappedBy = "user", 
		fetch = FetchType.EAGER, 
		cascade = {CascadeType.MERGE, CascadeType.PERSIST}, 
		orphanRemoval = true)
	private List<UserGroupWorkspace> groups = new ArrayList<>();

	@OneToMany(mappedBy = "user", 
		fetch = FetchType.EAGER, 
		cascade = {CascadeType.MERGE, CascadeType.PERSIST}, 
		orphanRemoval = true)
	private Set<UserRoleWorkspace> roles = new HashSet<>();

	public User() 
	{
		this.pubId = IdUtil.genId();
	}

	public User(
		final String email, 
		final String password, 
		final String nick,
		final String icon,
		final String timezone,
		final String lang,
		final boolean active, 
		final ZonedDateTime createdAt) 
	{
		this();
		this.email = email;
		this.password = password;
		this.nick = nick;
		this.icon = icon;
		this.timezone = timezone;
		this.lang = lang;
		this.active = active;
		this.createdAt = createdAt;
	}

	public User(UserFilter filtros)
	{
		this.pubId = filtros.getPubId();
		this.email = filtros.getEmail();
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

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Set<UserPropsWorkspace> getProps()
	{
		return props;
	}

	public UserPropsWorkspace getProps(
		final Long idWorkspace)
	{
		return props.stream()
			.filter(p -> p.getWorkspace().getId() == idWorkspace)
				.findFirst()
					.orElse(null);
	}

	public Set<UserRoleWorkspace> getRoles() 
	{
		return roles;
	}

	public Set<Role> getRoles(
		final Long idWorkspace) 
	{
		if(idWorkspace == null)
		{
			return getRoles().stream()
				.map(p -> p.getRole())
					.collect(Collectors.toSet());
		}
		
		return roles.stream()
			.filter(urp -> urp.getWorkspace().getId() == idWorkspace)
				.map(urp -> urp.getRole())
					.collect(Collectors.toSet());
	}

	public List<Role> getRolesSorted(
		final Long idWorkspace) 
	{
		Stream<Role> res;
		
		if(idWorkspace == null)
		{
			res = getRoles().stream()
				.map(p -> p.getRole());
		}
		else
		{
			res = roles.stream()
				.filter(urp -> urp.getWorkspace().getId() == idWorkspace)
					.map(urp -> urp.getRole());
		}	

		return res
			.sorted((a, b) -> Long.compare(
					a.getAboveOf() != null? a.getAboveOf().getId(): 0, 
					b.getAboveOf() != null? b.getAboveOf().getId(): 0))
				.collect(Collectors.toList());
	}	

	public Role getRole(
		final Long idWorkspace) 
	{
		Role res = null;

		var roles = getRolesSorted(idWorkspace);
		for(var role : roles)
		{
			if(res == null)
			{
				res = role;
			}
			else if(role.getAboveOf() != null && role.getAboveOf().getId() == res.getId())
			{
				res = role;
			}
		}

		return res;
	}
	
	public boolean addRoleNoWorkspace(
		final UserRoleWorkspace urp) 
	{
		return roles.add(urp);
	}

	public void delRoleNoWorkspace(
		final RoleType roleNome, 
		final Long idWorkspace) 
	{
		roles = roles.stream()
			.filter(urp -> !(
				urp.getRole().getName().equals(roleNome) && 
				urp.getWorkspace().getId() == idWorkspace))
				.collect(Collectors.toSet());
	}
	
	public Set<Workspace> getWorkspaces() 
	{
		return roles.stream()
			.map(urp -> urp.getWorkspace())
				.collect(Collectors.toSet());
	}

	public Workspace getWorkspace(final String name) 
	{
		return roles.stream()
			.filter(urp -> urp.getWorkspace().getName().toLowerCase().equals(name.toLowerCase()))
				.map(urp -> urp.getWorkspace())
					.findFirst()
						.orElse(null);
	}
	
	public List<UserGroupWorkspace> getGroups() 
	{
		return groups;
	}

	public List<Group> getGroups(final Long idWorkspace) 
	{
		return groups.stream()
			.filter(g -> g.getWorkspace().getId() == idWorkspace)
				.map(g -> g.getGroup())
					.collect(Collectors.toList());
	}

	public void addGroupNoWorkspace(UserGroupWorkspace urg) 
	{
		groups.add(urg);
	}
	
	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public ZonedDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(
		final ZonedDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public User getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(
		final User updatedBy) {
		this.updatedBy = updatedBy;
	}

	public ZonedDateTime getConfirmedAt() {
		return confirmedAt;
	}

	public void setConfirmedAt(ZonedDateTime confirmedAt) {
		this.confirmedAt = confirmedAt;
	}

	public ZonedDateTime getDeletedAt() {
		return deletedAt;
	}

	public void setDeletedAt(
		final ZonedDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	public User getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(
		final User deletedBy) {
		this.deletedBy = deletedBy;
	}

	public boolean isAdmin(
		final Long idWorkspace) 
	{
		return getRoles(idWorkspace).stream()
			.anyMatch(p -> 
				p.getName().equals(RoleType.ROLE_USER_ADMIN));
	}

	public boolean isSuperAdmin() 
	{
		return getRoles().stream()
			.anyMatch(urp -> 
				urp.getRole().getName().equals(RoleType.ROLE_ADMIN_SUPERVISOR));
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getTheme() {
		return theme;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}
}
