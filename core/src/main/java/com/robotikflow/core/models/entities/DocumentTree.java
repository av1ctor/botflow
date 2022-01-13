package com.robotikflow.core.models.entities;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Embeddable
@Table(name = "documents_tree")
public class DocumentTree 
{
	@NotNull
	@ManyToOne
	private DocumentInt parent;
	
	@NotNull
	@ManyToOne
	private DocumentInt child;
	
	@NotNull
	private Short depth;
}
