package com.robotikflow.core.interfaces.props;

public class EmailProps
{
	public final StringBuffer sender;
	public final StringBuffer subject;
	public final StringBuffer body;
	public final byte[] rawBody;

	public EmailProps()
	{
		this(null, null, null, null);
	}

	public EmailProps(
		final byte[] rawBody) 
	{
		this(null, null, null, rawBody);
	}
	
	public EmailProps(
		final String sender, 
		final String subject, 
		final String body,
		final byte[] rawBody) 
	{
		this.sender = sender != null?
			new StringBuffer(sender):
			new StringBuffer();
		this.subject = subject != null?
			new StringBuffer(subject):
			new StringBuffer();
		this.body = body != null?
			new StringBuffer(body):
			new StringBuffer();
		this.rawBody = rawBody;
	}
}
