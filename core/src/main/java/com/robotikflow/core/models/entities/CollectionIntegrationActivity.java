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
@Table(name = "collections_integrations_activities")
public class CollectionIntegrationActivity
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull
    private String pubId;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    private CollectionIntegration integration;

	@NotNull
	@ManyToOne(fetch = FetchType.EAGER, 
		optional = false, 
		cascade = CascadeType.REMOVE)
	private Activity activity;

	@ManyToOne(fetch = FetchType.EAGER,
		cascade = CascadeType.REMOVE)
	private ObjState state;
	
    public CollectionIntegrationActivity()
    {
		this.pubId = IdUtil.genId();
	}
    
    public CollectionIntegrationActivity(
		final Activity activity, 
		final CollectionIntegration integration) 
    {
		this();
		this.activity = activity;
        this.integration = integration;
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

	public CollectionIntegration getIntegration() {
		return integration;
	}

	public void setIntegration(CollectionIntegration integration) {
		this.integration = integration;
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