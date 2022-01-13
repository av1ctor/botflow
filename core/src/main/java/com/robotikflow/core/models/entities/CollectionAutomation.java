package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.util.IdUtil;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "collections_automations")
@Inheritance(strategy = InheritanceType.JOINED)
public class CollectionAutomation 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull
	private String pubId;
	
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private CollectionAutomationType type;
	
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private CollectionAutomationTrigger trigger;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private CollectionWithSchema collection;
	
	@Column(name = "\"desc\"")
	private String desc;
	
	@NotNull
	private short priority;

	@OneToMany(mappedBy = "automation", 
		fetch = FetchType.EAGER, 
		cascade = {CascadeType.MERGE, CascadeType.PERSIST}, 
		orphanRemoval = true)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("id asc")
	private List<CollectionAutomationActivity> activities = new ArrayList<>();
	
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	protected User createdBy;
	
	@NotNull
	protected ZonedDateTime createdAt;
	
	@ManyToOne(fetch = FetchType.EAGER)
	protected User updatedBy;
	
	protected ZonedDateTime updatedAt;
	
	public CollectionAutomation()
	{
		this.pubId = IdUtil.genId();
	}

	public CollectionAutomation(
		CollectionAutomationType type)
	{
		this();
		this.type = type;
	}
	
	public CollectionAutomation(
		CollectionAutomation ca, 
		CollectionWithSchema collection)
	{
		this();
		this.type = ca.type;
		this.trigger = ca.trigger;
		this.collection = collection;
		this.desc = ca.desc;
		this.priority = ca.priority;
		this.createdBy = collection.getCreatedBy();
		this.createdAt = collection.getCreatedAt();

		for(var activity : ca.getActivities())
		{
			var act = new Activity(activity.getActivity());
			var dup = new CollectionAutomationActivity(act, this);
			if(activity.getState() != null)
			{
				dup.setState(new ObjState());
			}
			this.addActivity(dup);
		}
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

	public CollectionAutomationType getType() {
		return type;
	}

	public void setType(CollectionAutomationType type) {
		this.type = type;
	}

	public CollectionWithSchema getCollection() {
		return collection;
	}

	public void setCollection(CollectionWithSchema collection) {
		this.collection = collection;
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

	public short getPriority() {
		return priority;
	}

	public void setPrioridade(short priority) {
		this.priority = priority;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public CollectionAutomationTrigger getTrigger() {
		return trigger;
	}

	public void setTrigger(CollectionAutomationTrigger trigger) {
		this.trigger = trigger;
	}

	public List<CollectionAutomationActivity> getActivities() {
		return activities;
	}

	public void setActivities(List<CollectionAutomationActivity> activities) {
		this.activities = activities;
	}

	public void addActivity(CollectionAutomationActivity activity) {
		this.activities.add(activity);
	}

	public void delActivity(CollectionAutomationActivity activity) {
		this.activities.remove(activity);
	}

	public ArrayList<ObjState> mergeActivities(List<CollectionAutomationActivity> others) 
	{
		var statesToSave = new ArrayList<ObjState>();
		
		// remover as que não constam na nova lista e atualizar as que existem
		var toRemove = new ArrayList<CollectionAutomationActivity>();
		for(var act: activities)
		{
			var other = others.stream()
				.filter(a -> a.getId() == act.getId())
					.findFirst();
			if(!other.isPresent())
			{
				toRemove.add(act); 
			}
			else
			{
				var oAct = other.get().getActivity();
				var cAct = act.getActivity();
				act.setActivity(oAct);
				
				if(cAct == null || cAct.getId() != oAct.getId())
				{
					var state = act.getState();
					if(state != null)
					{
						state.setState(new HashMap<String, Object>());
						statesToSave.add(state);
					}
				}
			}
		}
		
		activities.removeAll(toRemove);
		
		// adicionar as novas
		for(var other : others)
		{
			if(!activities.stream()
				.anyMatch(current -> current.getId() == other.getId()))
			{
				var state = new ObjState();
				other.setState(state);
				statesToSave.add(state);
				
				activities.add(other);
			}
		}

		return statesToSave;
	}
}
