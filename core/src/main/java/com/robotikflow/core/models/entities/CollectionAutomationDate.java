package com.robotikflow.core.models.entities;

import java.time.ZonedDateTime;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "collections_automations_dates")
public class CollectionAutomationDate 
	extends CollectionAutomation
{
	@NotNull
	private ZonedDateTime start;

	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private CollectionAutomationDateRepeat repeat;
	
	@NotNull
	private ZonedDateTime next;

	public CollectionAutomationDate()
	{
		super(CollectionAutomationType.DATE);
	}

	public CollectionAutomationDate(
		CollectionAutomationDate cad, 
		CollectionWithSchema collection)
	{
		super(cad, collection);
		this.start = cad.start;
		this.repeat = cad.repeat;
		this.next = cad.next;
	}
	
	public ZonedDateTime getStart() {
		return start;
	}

	public void setStart(ZonedDateTime start) {
		this.start = start;
	}

	public CollectionAutomationDateRepeat getRepeat() {
		return repeat;
	}

	public void setRepeat(CollectionAutomationDateRepeat repeat) {
		this.repeat = repeat;
	}

	public ZonedDateTime getNext() {
		return next;
	}

	public void setNext(ZonedDateTime next) {
		this.next = next;
	}
}
