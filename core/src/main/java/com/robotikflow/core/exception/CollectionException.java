package com.robotikflow.core.exception;

public class CollectionException extends RuntimeException 
{
	private static final long serialVersionUID = 3859818014355783495L;

	public CollectionException(String message, Throwable cause) 
	{
        super(message, cause);
    }

	public CollectionException(String message) 
	{
        this(message, null);
    }
}
