package com.robotikflow.core.exception;

public class CollectionTemplateException extends RuntimeException 
{
	private static final long serialVersionUID = 6817028569322687248L;

	public CollectionTemplateException(String message, Throwable cause) 
	{
		super(message, cause);
	}

	public CollectionTemplateException(String message) 
	{
        this(message, null);
    }
}
