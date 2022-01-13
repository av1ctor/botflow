package com.robotikflow.core.models.queue;

public class DocIntUpdatedMessage 
	extends Message 
{
	private Long id;
	
	public DocIntUpdatedMessage()
	{
		super(MessageType.DOC_INT_UPDATED);
	}
	
	public DocIntUpdatedMessage(Long id) 
	{
		super(MessageType.DOC_INT_UPDATED);
		this.id = id;
	}
	
	public Long getId() {
		return id;
	}
}
