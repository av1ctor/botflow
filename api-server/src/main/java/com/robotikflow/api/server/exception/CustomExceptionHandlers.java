package com.robotikflow.api.server.exception;

import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.robotikflow.core.exception.StorageException;
import com.robotikflow.core.exception.UserException;
import com.robotikflow.core.exception.WorkspaceException;
import com.robotikflow.core.exception.WorkflowException;
import com.robotikflow.api.server.models.response.ExceptionResponse;
import com.robotikflow.core.exception.AuthenticationException;
import com.robotikflow.core.exception.BadRequestException;
import com.robotikflow.core.exception.CollectionException;
import com.robotikflow.core.exception.CollectionTemplateException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

@ControllerAdvice
@RestController
public class CustomExceptionHandlers extends ResponseEntityExceptionHandler 
{
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

	@Override
	protected ResponseEntity<Object> handleServletRequestBindingException
		(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request)  
	{
		return new ResponseEntity<Object>(new ExceptionResponse(ex.getMessage()), status);
  	}
	
	@Override
	protected ResponseEntity<Object> handleConversionNotSupported
		(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request)  
	{
		return new ResponseEntity<Object>(new ExceptionResponse(ex.getMessage()), status);
  	}
	
	@Override
	protected ResponseEntity<Object> handleTypeMismatch
		(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request)  
	{
		return new ResponseEntity<Object>(new ExceptionResponse(ex.getMessage()), status);
  	}
	
	@Override
	protected ResponseEntity<Object> handleHttpMessageNotReadable
		(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request)  
	{
		return new ResponseEntity<Object>(new ExceptionResponse(ex.getMessage()), status);
  	}
	
	@Override
	protected ResponseEntity<Object> handleMissingServletRequestPart
		(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request)  
	{
		return new ResponseEntity<Object>(new ExceptionResponse(ex.getMessage()), status);
  	}

	@Override
	protected ResponseEntity<Object> handleBindException
		(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request)  
	{
		return new ResponseEntity<Object>(new ExceptionResponse(ex.getMessage()), status);
  	}
	  
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) 
	{
		var headers = new ExceptionResponse(ex.getMessage());

		return new ResponseEntity<Object>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	  
	@ExceptionHandler(NaoEncontradoException.class)
	ResponseEntity<Object> naoEncontradoHandler(NaoEncontradoException ex) 
	{
    	var headers = new ExceptionResponse(ex.getMessage());
    	
    	return new ResponseEntity<Object>(headers, HttpStatus.NOT_FOUND);
	}
	
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleAuthenticationException(AuthenticationException ex) 
	{
    	var headers = new ExceptionResponse(ex.getMessage());
    	
    	return new ResponseEntity<Object>(headers, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Object> handleBadRequestException(BadRequestException ex) 
	{
    	var headers = new ExceptionResponse(ex.getMessage());
    	
    	return new ResponseEntity<Object>(headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<Object> handleStorageException(StorageException ex) 
	{
    	var headers = new ExceptionResponse(ex.getMessage());
    	
    	return new ResponseEntity<Object>(headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WorkspaceException.class)
    public ResponseEntity<Object> handleWorkspaceException(WorkspaceException ex) 
	{
    	var headers = new ExceptionResponse(ex.getMessage());
    	
    	return new ResponseEntity<Object>(headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<Object> handleUserException(UserException ex) 
	{
    	var headers = new ExceptionResponse(ex.getMessage());
    	
    	return new ResponseEntity<Object>(headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WorkflowException.class)
    public ResponseEntity<Object> handleWorkflowException(WorkflowException ex) 
	{
    	var headers = new ExceptionResponse(ex.getMessage());
    	
    	return new ResponseEntity<Object>(headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CollectionException.class)
    public ResponseEntity<Object> handleCollectionException(CollectionException ex) 
	{
    	var headers = new ExceptionResponse(ex.getMessage());
    	
    	return new ResponseEntity<Object>(headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CollectionTemplateException.class)
    public ResponseEntity<Object> handleCollectionTemplateException(CollectionTemplateException ex) 
	{
    	var headers = new ExceptionResponse(ex.getMessage());
    	
    	return new ResponseEntity<Object>(headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ErroInternoException.class)
    public ResponseEntity<Object> handleErroInternoException(ErroInternoException ex) 
	{
    	var headers = new ExceptionResponse(ex.getMessage());
    	
    	return new ResponseEntity<Object>(headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
