package com.robotikflow.core.exception;

public class BadRequestException extends RuntimeException 
{
	private static final long serialVersionUID = -3386821036848113926L;

	public BadRequestException(String message, Throwable cause) 
	{
        super(message, cause);
    }

	public BadRequestException(String message) 
	{
        this(message, null);
    }
}
