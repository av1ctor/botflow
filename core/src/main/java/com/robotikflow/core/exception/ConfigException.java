package com.robotikflow.core.exception;

public class ConfigException extends RuntimeException 
{
	private static final long serialVersionUID = 6626706836135172113L;

	public ConfigException(String message, Throwable cause) 
	{
        super(message, cause);
    }

	public ConfigException(String message) 
	{
        this(message, null);
    }
}
