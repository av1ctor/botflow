package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users_roles")
public class Role 
{
	@Id 
	private Long id;
	
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(unique=true)
    private RoleType name;
    
    @ManyToOne(fetch = FetchType.EAGER)
    private Role aboveOf;
    
    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
	private Set<UserRoleWorkspace> userWorkspacePerfis = new HashSet<>();

	@ManyToOne(fetch = FetchType.LAZY)
	protected User deletedBy;
	
	protected ZonedDateTime deletedAt;

	public Role()
	{
	}

	public Role(RoleType name) 
	{
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@JsonIgnore
	public List<User> getUsers()
	{
		return userWorkspacePerfis.stream()
				.map(urp -> urp.getUser()).collect(Collectors.toList());
	}
	
	public RoleType getName() {
		return name;
	}

	public void setName(RoleType name) {
		this.name = name;
	}

	public Role getAboveOf() {
		return aboveOf;
	}
}
