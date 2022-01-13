package com.robotikflow.core.exception;

public class UserException extends RuntimeException 
{
	private static final long serialVersionUID = 3588398751507850990L;

	public UserException(String message, Throwable cause) 
	{
        super(message, cause);
    }

	public UserException(String message) 
	{
        this(message, null);
    }
}
