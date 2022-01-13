package com.robotikflow.core.models.queue;

public class DocExtCreatedMessage 	
    extends Message 
{
	private Long id;
	private String path;
	private boolean upload;
	
	public DocExtCreatedMessage()
	{
		super(MessageType.DOC_EXT_CREATED);
	}
	
	public DocExtCreatedMessage(
		Long id, 
		String path) 
	{
		this(id, path, true);
	}

	public DocExtCreatedMessage(
		Long id, 
		String path, 
		boolean upload) 
	{
		super(MessageType.DOC_EXT_CREATED);
		this.id = id;
		this.path = path;
		this.upload = upload;
	}
	
	public Long getId() {
		return id;
	}

	public String getPath() {
		return path;
	}

	public boolean isUpload() {
		return upload;
	}
}

