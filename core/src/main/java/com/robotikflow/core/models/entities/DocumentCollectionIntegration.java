package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "documents_collections_integrations")
public class DocumentCollectionIntegration 
{
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@NotNull
	@ManyToOne
	private DocumentInt document;

	@NotNull
	@ManyToOne
	private CollectionIntegration integration;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public DocumentInt getDocument() {
		return document;
	}

	public void setDocument(DocumentInt document) {
		this.document = document;
	}

	public CollectionIntegration getIntegration() {
		return integration;
	}

	public void setIntegration(CollectionIntegration integration) {
		this.integration = integration;
	}

}
