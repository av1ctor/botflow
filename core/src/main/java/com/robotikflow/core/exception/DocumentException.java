package com.robotikflow.core.exception;

public class DocumentException extends RuntimeException 
{
	private static final long serialVersionUID = -1707584678091138812L;

	public DocumentException(String message, Throwable cause) 
	{
        super(message, cause);
    }

	public DocumentException(String message) 
	{
        this(message, null);
    }
}
