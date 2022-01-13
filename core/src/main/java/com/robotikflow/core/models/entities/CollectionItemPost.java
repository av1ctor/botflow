package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "collections_items_posts")
public class CollectionItemPost 
	extends CollectionPost
{
	@NotNull
	private String itemId;

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
}
