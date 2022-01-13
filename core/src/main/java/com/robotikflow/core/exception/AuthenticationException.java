package com.robotikflow.core.exception;

public class AuthenticationException extends RuntimeException 
{
	private static final long serialVersionUID = 4579896551440794457L;

	public AuthenticationException(String message) 
	{
        super(message);
    }

	public AuthenticationException(String message, Throwable cause) 
	{
        super(message, cause);
    }
}
