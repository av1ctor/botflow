package com.robotikflow.api.server.models.response;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ExceptionResponse 
{
	  private String message;
	  private Map<String, Collection<String>> errors;

	  public ExceptionResponse(String message, Map<String, Collection<String>> errors) 
	  {
	    this.message = message;
	    this.errors = errors;
	  }

	  public ExceptionResponse(String message, String error) 
	  {
	    this.message = message;
	    this.errors = new HashMap<String, Collection<String>>();
	    this.errors.put(error, null);
	  }
	  
	  public ExceptionResponse(String message) 
	  {
	    this.message = message;
	    this.errors = null;
	  }

	  public String getMessage() 
	  {
		  return message;
	  }

	  public Map<String, Collection<String>> getErrors() 
	  {
		  return errors;
	  }
}
