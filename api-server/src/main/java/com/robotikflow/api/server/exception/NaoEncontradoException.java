package com.robotikflow.api.server.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NaoEncontradoException extends RuntimeException
{
	private static final long serialVersionUID = -5187527002113805339L;

	public NaoEncontradoException()
	{
		super();
	}
	
	public NaoEncontradoException(String message)
	{
		super(message);
	}
}
