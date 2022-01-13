package com.robotikflow.core.exception;

public class WorkflowException extends RuntimeException 
{
	private static final long serialVersionUID = -9021875776774017129L;

	public WorkflowException(String message, Throwable cause) 
	{
        super(message, cause);
    }

	public WorkflowException(String message) 
	{
        this(message, null);
    }
}
