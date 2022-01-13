package com.robotikflow.api.server.exception;

public class ErroInternoException extends RuntimeException 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8199505978674661426L;

	public ErroInternoException(String message, Throwable cause) 
	{
        super(message, cause);
    }

	public ErroInternoException(String message) 
	{
        this(message, null);
    }
}
