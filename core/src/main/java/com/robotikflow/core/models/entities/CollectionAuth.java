package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.util.IdUtil;

@Entity
@Table(name = "collections_auths")
public class CollectionAuth 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull
	private String pubId;
	
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private CollectionAuthRole role;
	
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private Collection collection;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private User user;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Group group;

	@NotNull
	private boolean reverse;
	
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	protected User createdBy;
	
	@NotNull
	protected ZonedDateTime createdAt;
	
	@ManyToOne(fetch = FetchType.EAGER)
	protected User updatedBy;
	
	protected ZonedDateTime updatedAt;
	
	public CollectionAuth()
	{
		this.pubId = IdUtil.genId();
	}

	public CollectionAuth(
		Collection collection, 
		User user, 
		CollectionAuthRole role)
	{
		this();
		this.collection = collection;
		this.user = user;
		this.role = role;
		this.reverse = false;
	}

	public CollectionAuth(
		Collection collection, 
		Group group, 
		CollectionAuthRole role)
	{
		this();
		this.collection = collection;
		this.group = group;
		this.role = role;
		this.reverse = false;
	}

	public CollectionAuth(
		CollectionAuth cup, 
		Collection collection) 
	{
		this();
		this.collection = collection;
		this.user = cup.user;
		this.group = cup.group;
		this.role = cup.role;
		this.reverse = cup.reverse;
		this.createdBy = cup.createdBy;
		this.createdAt = cup.createdAt;
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

	public CollectionAuthRole getRole() {
		return role;
	}

	public void setRole(CollectionAuthRole role) {
		this.role = role;
	}

	public Collection getCollection() {
		return collection;
	}

	public void setCollection(Collection collection) {
		this.collection = collection;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public boolean isReverse() {
		return reverse;
	}

	public void setReverse(boolean reversa) {
		this.reverse = reversa;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public ZonedDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(ZonedDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public User getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(User updatedBy) {
		this.updatedBy = updatedBy;
	}

	public ZonedDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(ZonedDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
