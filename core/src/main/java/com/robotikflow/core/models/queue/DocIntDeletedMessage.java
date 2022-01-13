package com.robotikflow.core.models.queue;

public class DocIntDeletedMessage 
	extends Message 
{
	private Long id;

	public DocIntDeletedMessage()
	{
		super(MessageType.DOC_INT_DELETED);
	}
	
	public DocIntDeletedMessage(Long id) 
	{
		super(MessageType.DOC_INT_DELETED);
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
