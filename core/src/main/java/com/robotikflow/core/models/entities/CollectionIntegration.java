package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.CascadeType;
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

import com.robotikflow.core.util.IdUtil;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
@Table(name = "collections_integrations")
public class CollectionIntegration 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull
	private String pubId;
	
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	private CollectionWithSchema collection;

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER,
		optional = false, 
		cascade = CascadeType.REMOVE)
	private Trigger trigger;

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER,
		optional = false, 
		cascade = CascadeType.REMOVE)
	private ObjState triggerState;

	@Column(name = "\"desc\"")
	private String desc;
	
	@NotNull
	private short priority;

	private Integer freq;

	private Integer minOfDay;

	@NotNull
	private boolean active;

	@NotNull
	private boolean started;

	private ZonedDateTime start;
	
	private ZonedDateTime rerunAt;

	private Integer reruns;

	@OneToMany(mappedBy = "integration", 
		fetch = FetchType.EAGER, 
		cascade = {CascadeType.MERGE, CascadeType.PERSIST}, 
		orphanRemoval = true)
	@Fetch(value = FetchMode.SUBSELECT)
	@OrderBy("id asc")
	private List<CollectionIntegrationActivity> activities = new ArrayList<>();

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	protected User createdBy;
	
	@NotNull
	protected ZonedDateTime createdAt;
	
	@ManyToOne(fetch = FetchType.EAGER)
	protected User updatedBy;
	
	protected ZonedDateTime updatedAt;
	
	public CollectionIntegration()
	{
		this.pubId = IdUtil.genId();
	}

	public CollectionIntegration(
		final CollectionIntegration ci, 
		final CollectionWithSchema collection)
	{
		this();
		this.collection = collection;
		this.desc = ci.desc;
		this.priority = ci.priority;
		this.freq = ci.freq;
		this.minOfDay = ci.minOfDay;
		this.active = ci.active;
		this.started = ci.started;
		this.start = ci.start;
		this.trigger = ci.trigger;
		this.triggerState = new ObjState();
		this.createdBy = collection.getCreatedBy();
		this.createdAt = collection.getCreatedAt();
		
		for(var activity : ci.getActivities())
		{
			var act = new Activity(activity.getActivity());
			var dup = new CollectionIntegrationActivity(act, this);
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

	public CollectionWithSchema getCollection() {
		return collection;
	}

	public void setCollection(CollectionWithSchema collection) {
		this.collection = collection;
	}

	public Trigger getTrigger() {
		return trigger;
	}

	public void setTrigger(Trigger trigger) {
		this.trigger = trigger;
	}

	public ObjState getTriggerState() {
		return triggerState;
	}

	public void setTriggerState(ObjState triggerState) {
		this.triggerState = triggerState;
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

	public Integer getFreq() {
		return freq;
	}

	public void setFreq(Integer freq) {
		this.freq = freq;
	}

	public Integer getMinOfDay() {
		return minOfDay;
	}

	public void setMinOfDay(Integer minOfDay) {
		this.minOfDay = minOfDay;
	}

	public short getPriority() {
		return priority;
	}

	public void setPriority(short priority) {
		this.priority = priority;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isStarted() {
		return started;
	}

	public void setStarted(boolean started) {
		this.started = started;
	}

	public ZonedDateTime getStart() {
		return start;
	}

	public void setStart(ZonedDateTime start) {
		this.start = start;
	}
	
	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public ZonedDateTime getRerunAt() {
		return rerunAt;
	}

	public void setRerunAt(ZonedDateTime rerunAt) {
		this.rerunAt = rerunAt != null?
			rerunAt.withSecond(0).withNano(0):
			null;
	}

	public int getReruns() {
		return reruns != null? reruns.intValue(): 0;
	}

	public void setReruns(Integer reruns) {
		this.reruns = reruns;
	}

	public List<CollectionIntegrationActivity> getActivities() {
		return activities;
	}

	public void setActivities(List<CollectionIntegrationActivity> activities) {
		this.activities = activities;
	}

	public void addActivity(CollectionIntegrationActivity activity) {
		this.activities.add(activity);
	}

	public void delActivity(CollectionIntegrationActivity activity) {
		this.activities.remove(activity);
	}

	public ArrayList<ObjState> mergeActivities(List<CollectionIntegrationActivity> others) 
	{
		var statesToSave = new ArrayList<ObjState>();
		
		// remover as que não constam na nova lista e atualizar as que existem
		var toRemove = new ArrayList<CollectionIntegrationActivity>();
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
