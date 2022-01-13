package com.robotikflow.core.models.queue;

public class DocIntCreatedMessage 
	extends Message 
{
	private Long id;
	private String caminhoCompleto;
	private boolean upload;
	
	public DocIntCreatedMessage()
	{
		super(MessageType.DOC_INT_CREATED);
	}
	
	public DocIntCreatedMessage(
		Long id, 
		String caminhoCompleto) 
	{
		this(id, caminhoCompleto, true);
	}

	public DocIntCreatedMessage(
		Long id, 
		String caminhoCompleto, 
		boolean upload) 
	{
		super(MessageType.DOC_INT_CREATED);
		this.id = id;
		this.caminhoCompleto = caminhoCompleto;
		this.upload = upload;
	}
	
	public Long getId() {
		return id;
	}

	public String getCaminhoCompleto() {
		return caminhoCompleto;
	}

	public boolean isUpload() {
		return upload;
	}
}
