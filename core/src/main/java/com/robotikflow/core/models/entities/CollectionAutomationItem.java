package com.robotikflow.core.models.entities;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "collections_automations_rows")
public class CollectionAutomationItem 
	extends CollectionAutomation
{
	public CollectionAutomationItem()
	{
	}

	public CollectionAutomationItem(
		CollectionAutomationItem cal, 
		CollectionWithSchema collection)
	{
		super(cal, collection);
	}
}
