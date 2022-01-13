package com.robotikflow.core.exception;

public class ObjException 
	extends RuntimeException 
{
	private static final long serialVersionUID = 6626703459875172113L;

	public ObjException(String message, Throwable cause) 
	{
        super(message, cause);
    }

	public ObjException(String message) 
	{
        this(message, null);
    }

	public ObjException(Exception e) 
	{
        super(e);
    }
}
