package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "documents_auths")
public class DocumentAuth 
{
	@NotNull
	@Id
	private Long id;
	
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private DocumentAuthType type;
	
	@ManyToOne(fetch = FetchType.EAGER)
	private User user;

	@ManyToOne(fetch = FetchType.EAGER)
	private Group group;
	
	@NotNull
	@ManyToOne(fetch = FetchType.EAGER)
	private Document document;
	
	@NotNull
	private boolean reverse;

	public DocumentAuthType getType() {
		return type;
	}

	public void setType(DocumentAuthType type) {
		this.type = type;
	}
}
