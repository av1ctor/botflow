package com.robotikflow.core.models.entities;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.util.IdUtil;

@Entity
@Table(name = "collections_automations_activities")
public class CollectionAutomationActivity
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull
    private String pubId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private CollectionAutomation automation;
	
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER,
		optional = false, 
		cascade = CascadeType.REMOVE)
	private Activity activity;
    
	@ManyToOne(fetch = FetchType.EAGER,
		cascade = CascadeType.REMOVE)
	private ObjState state;
    
    public CollectionAutomationActivity()
    {
		this.pubId = IdUtil.genId();
    }
    
    public CollectionAutomationActivity(
		Activity activity,
		CollectionAutomation automation) 
    {
		this();
		this.activity = activity;
		this.automation = automation;
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

	public CollectionAutomation getAutomation() {
		return automation;
	}

	public void setAutomation(CollectionAutomation automation) {
		this.automation = automation;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public ObjState getState() {
		return state;
	}

	public void setState(ObjState state) {
		this.state = state;
	}
}