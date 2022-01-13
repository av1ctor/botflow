package com.robotikflow.upload.server.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.robotikflow.core.exception.BadRequestException;
import com.robotikflow.upload.server.models.response.ExceptionResponse;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@ControllerAdvice
@RestController
public class CustomExceptionHandlers extends ResponseEntityExceptionHandler 
{
	  @ExceptionHandler(Exception.class)
	  public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) 
	  {
		  var headers = new ExceptionResponse(ex.getMessage(), request.getDescription(false));

		  return new ResponseEntity<Object>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
	  }
	  
	  @Override
	  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
	      HttpHeaders httpHeaders, HttpStatus status, WebRequest request) 
	  {
		  Multimap<String, String> fields = ArrayListMultimap.create();
		  for(var err : ex.getBindingResult().getFieldErrors())
		  {
			  fields.put(err.getField(), err.getDefaultMessage());
		  }
		  
		  var headers = new ExceptionResponse("Validação falhou", fields.asMap());
		  
		  return new ResponseEntity<Object>(headers, HttpStatus.BAD_REQUEST);
	  }
	  
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) 
	{
    	var headers = new ExceptionResponse(ex.getMessage());
    	
    	return new ResponseEntity<Object>(headers, HttpStatus.BAD_REQUEST);
    }
}
