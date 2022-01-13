package com.robotikflow.core.models.entities;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.robotikflow.core.models.filters.CollectionFilter;
import com.robotikflow.core.models.schemas.collection.CollectionSchema;
import com.robotikflow.core.util.converters.CollectionSchemaToJsonConverter;

@Entity
@Table(name = "collections_schemas")
public class CollectionWithSchema 
	extends Collection
{
	@Column(name = "schema", insertable = false, updatable = false)
	@Convert(converter = CollectionSchemaToJsonConverter.class)
	private CollectionSchema schemaObj;

	@NotNull
	private String schema;

	@NotNull
	private long options;

	private String autoGenId;
	
	private String positionalId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	private Provider provider;

	@OneToMany(mappedBy = "collection", fetch = FetchType.LAZY)
	private Set<CollectionIntegration> integrations;

	@OneToMany(mappedBy = "collection", fetch = FetchType.LAZY)
	private Set<CollectionAutomation> automations;

	@OneToMany(mappedBy = "principal", fetch = FetchType.LAZY)
	private Set<CollectionAux> auxs;

	public CollectionWithSchema()
	{
		super(CollectionType.SCHEMA);
	}

	public CollectionWithSchema(CollectionFilter filtros, String idParent) 
	{
		super(filtros, idParent);
	}

	public CollectionWithSchema(
		CollectionWithSchema collection, 
		User user, 
		EnumSet<CollectionDupOptions> with)
	{
		super(collection, user, with);
		this.schema = collection.schema;
		this.schemaObj = collection.schemaObj;
		this.autoGenId = collection.autoGenId;
		this.positionalId = collection.positionalId;
		this.options = collection.options;
		this.provider = collection.provider;

		if(with.contains(CollectionDupOptions.INTEGRATIONS))
		{
			if(collection.integrations != null)
			{
				this.integrations = new HashSet<>();
				
				for(var integration : collection.integrations)
				{
					var integracaoDup = new CollectionIntegration(integration, this);
					this.integrations.add(integracaoDup);
				}
			}
		}

		if(with.contains(CollectionDupOptions.AUTOMATIONS))
		{
			if(collection.automations != null)
			{
				this.automations = new HashSet<>();
				
				for(var automacao : collection.automations)
				{
					CollectionAutomation automacaoDup = null;
					
					if(automacao instanceof CollectionAutomationField)
					{
						automacaoDup = new CollectionAutomationField((CollectionAutomationField)automacao, this);
					}
					else if(automacao instanceof CollectionAutomationItem)
					{
						automacaoDup = new CollectionAutomationItem((CollectionAutomationItem)automacao, this);
					}
					else if(automacao instanceof CollectionAutomationDate)
					{
						automacaoDup = new CollectionAutomationDate((CollectionAutomationDate)automacao, this);
					}
					else
					{
						automacaoDup = new CollectionAutomation(automacao, this);
					}

					this.automations.add(automacaoDup);
				}
			}
		}

		if(with.contains(CollectionDupOptions.AUXS))
		{
			if(collection.auxs != null)
			{
				this.auxs = new HashSet<>();
				for(var aux : collection.auxs)
				{
					var auxDup = new CollectionAux(aux, this);
					this.auxs.add(auxDup);
				}
			}
		}
	}

	public CollectionSchema getSchemaObj() {
		return schemaObj;
	}

	public void setSchemaObj(CollectionSchema schemaObj) {
		this.schemaObj = schemaObj;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getAutoGenId() {
		return autoGenId;
	}

	public void setAutoGenId(String autoGenId) {
		this.autoGenId = autoGenId;
	}
	
	public long getOptions() {
		return options;
	}

	public boolean isEditavel() {
		return (options & CollectionOptions.NO_EDIT.asLong()) == 0;
	}

	public void setOptions(long options) {
		this.options = options;
	}

	public void setOptions(EnumSet<CollectionOptions> options) {
		for(var opcao : options)
		{
			this.options |= opcao.asLong();
		}
	}

	public String getPositionalId() {
		return positionalId;
	}

	public void setPositionalId(String positionalId) {
		this.positionalId = positionalId;
	}

	public Provider getProvider() {
		return provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public Set<CollectionIntegration> getIntegrations() {
		return integrations;
	}

	public void setIntegrations(Set<CollectionIntegration> integrations) {
		this.integrations = integrations;
	}

	public Set<CollectionAutomation> getAutomations() {
		return automations;
	}

	public void setAutomations(Set<CollectionAutomation> automations) {
		this.automations = automations;
	}

	public Set<CollectionAux> getAuxs() {
		return auxs;
	}

	public void setAuxs(Set<CollectionAux> auxs) {
		this.auxs = auxs;
	}
}
