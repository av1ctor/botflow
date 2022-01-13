package com.robotikflow.core.models.queue;

public class EmailMessengerMessage extends Message 
{
	private Long providerId;
	private String to;
	private String subject;
	private String body;
	
	public EmailMessengerMessage()
	{
		super(MessageType.MESSENGER_EMAIL);
	}
	
	public EmailMessengerMessage(Long providerId, String to, String subject, String body) 
	{
		super(MessageType.MESSENGER_EMAIL);
		this.providerId = providerId;
		this.to = to;
		this.subject = subject;
		this.body = body;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Long getProviderId() {
		return providerId;
	}

	public void setProviderId(Long providerId) {
		this.providerId = providerId;
	}
}
