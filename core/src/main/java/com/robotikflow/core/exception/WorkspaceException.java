package com.robotikflow.core.exception;

public class WorkspaceException extends RuntimeException 
{
	private static final long serialVersionUID = -1707584678091138812L;

	public WorkspaceException(String message, Throwable cause) 
	{
        super(message, cause);
    }

	public WorkspaceException(String message) 
	{
        this(message, null);
    }
}
