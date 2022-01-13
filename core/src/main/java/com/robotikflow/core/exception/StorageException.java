package com.robotikflow.core.exception;

public class StorageException extends RuntimeException 
{
	private static final long serialVersionUID = -1707584678091138812L;

	public StorageException(String message, Throwable cause) 
	{
        super(message, cause);
    }

	public StorageException(String message) 
	{
        this(message, null);
    }
}
