package com.robotikflow.core.models.queue;

public class DocIntCopiedMessage 
	extends Message 
{
	private Long id;
	private Long fonteId;
	
	public DocIntCopiedMessage()
	{
		super(MessageType.DOC_INT_COPIED);
	}
	
	public DocIntCopiedMessage(Long id, Long fonteId) 
	{
		super(MessageType.DOC_INT_COPIED);
		this.id = id;
		this.fonteId = fonteId;
	}
	
	public Long getId() {
		return id;
	}

	public Long getFonteId() {
		return fonteId;
	}
}
