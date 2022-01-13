package com.robotikflow.core.util;

import java.io.IOException;
import java.util.Arrays;
import javax.validation.Validator;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class SchemaValidator 
{
	@Autowired
	@Lazy
    private Validator validator;
    
    private ObjectMapper objectMapper = new ObjectMapper()
		.findAndRegisterModules()
        .setSerializationInclusion(Include.NON_NULL);
    
	private String removeAtFromMessage(String msg) 
	{
		var index = msg.indexOf("at [Source:");
		if (index < 0) 
		{
			return msg;
		}

		return msg.substring(0, index);
	}

	private <T> void validate(T res) throws Exception 
	{
		var violacoes = validator.validate(res);
		if (violacoes != null && violacoes.size() > 0) 
		{
			throw new Exception(String.format("Schema mal formado: %s", Arrays.toString(
					violacoes.stream().map(v -> v.getPropertyPath().toString() + ": " + v.getMessage()).toArray())));
		}
	}

	public <T> T parse(
		final String schema, 
		final Class<T> klass) throws Exception 
	{
		T res = null;
		try 
		{
			res = objectMapper.readValue(schema, klass);
		} 
		catch (JsonParseException | JsonMappingException e) 
		{
			throw new Exception(String.format("Erro de sintaxe na linha(%d) e coluna(%d): %s",
					e.getLocation().getLineNr(), e.getLocation().getColumnNr(), removeAtFromMessage(e.getMessage())),
					e);
		} 
        catch (IOException e) 
        {
			throw new Exception(String.format("Schema mal formado: %s", removeAtFromMessage(e.getMessage())), e);
		}

		validate(res);

		return res;
	}    
}