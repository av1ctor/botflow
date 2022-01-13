package com.robotikflow.core.models.queue;

public class IntegrationTriggeredMessage 
	extends Message 
{
	private Long id;
	
	public IntegrationTriggeredMessage()
	{
		super(MessageType.INTEGRATION_TRIGGERED);
	}
	
	public IntegrationTriggeredMessage(Long id) 
	{
		super(MessageType.INTEGRATION_TRIGGERED);
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
