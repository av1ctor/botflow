package com.robotikflow.core.services;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import javax.validation.Validator;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.models.entities.ObjSchema;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.ObjSchemaRepository;
import com.robotikflow.core.models.request.ObjSchemaRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ObjSchemaService 
{
    @Autowired
    private ObjSchemaRepository objSchemaRepo;
	@Autowired
	@Lazy
	private Validator validator;
    
	private ObjectMapper objectMapper = new ObjectMapper()
		.findAndRegisterModules()
		.setSerializationInclusion(Include.NON_NULL);

	/**
	 * 
	 * @param pageable
	 * @return
	 */
	public List<ObjSchema> findAll(
		final Pageable pageable) 
	{
		return objSchemaRepo
			.findAll(pageable)
				.toList();
	}

	/**
	 * 
	 * @param pubId
	 * @return
	 */
	public ObjSchema findByPubId(
		final String pubId) 
	{
		return objSchemaRepo
			.findByPubId(pubId);
	}


	private void setCommonFields(
		final ObjSchema schema,
		final ObjSchemaRequest req) 
	{
		setFields(schema, req);
	}

	/**
	 * 
	 * @param id
	 * @param req
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public ObjSchema update(
		final String id, 
		final ObjSchemaRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var schema = objSchemaRepo.findByPubId(id);
		if(schema == null)
		{
			throw new ObjException("Obj schema not found");
		}

		//validate(schema, req.getFields());

		schema.setUpdatedBy(user);
		schema.setUpdatedAt(ZonedDateTime.now());

		setCommonFields(schema, req);

		return objSchemaRepo.save(schema);
	}

	/**
	 * 
	 * @param req
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public ObjSchema create(
		final ObjSchemaRequest req, 
		final User user) 
		throws Exception 
	{
		var schema = new ObjSchema(req.getType());

		//validate(schema, req.getFields());

		schema.setCreatedBy(user);
		schema.setCreatedAt(ZonedDateTime.now());
		
		setCommonFields(schema, req);

		return objSchemaRepo.save(schema);
	}
	
	/**
	 * 
	 * @param schema
	 * @throws Exception
	 */
	public void validate(
		final String schema) 
		throws Exception
	{
		validate(schema, ObjSchema.class);
	}

	protected void setFields(
        final ObjSchema obj,
        final ObjSchemaRequest req) 
    {
        obj.setVersion(req.getVersion());
        obj.setType(req.getType());
        obj.setName(req.getName());
        obj.setTitle(req.getTitle());
        obj.setDesc(req.getDesc());
        obj.setIcon(req.getIcon());
        obj.setCategory(req.getCategory());
		obj.setSchema(req.getSchema());
	}

	private <T> T stringToSchema(
		final String text, 
		final Class<T> klass) 
	{
		T schema = null;
		try 
		{
			schema = objectMapper.readValue(text, klass);
		} 
		catch (JsonParseException | JsonMappingException e) 
		{
			throw new ObjException(String.format("Syntax error at line(%d):column(%d): %s",
					e.getLocation().getLineNr(), e.getLocation().getColumnNr(), removeAtFromMessage(e.getMessage())),
					e);
		} 
		catch (IOException e) 
		{
			throw new ObjException(String.format("Invalid schema: %s", removeAtFromMessage(e.getMessage())), e);
		}

		return schema;
	}

	private String removeAtFromMessage(String msg) 
	{
		var index = msg.indexOf("at [Source:");
		if (index < 0) 
		{
			return msg;
		}

		return msg.substring(0, index);
	}

    protected <T extends ObjSchema> void validate(
        final String text, 
        final Class<T> clazz) 
        throws Exception
    {
		var schema = stringToSchema(text, clazz);
		
        var violations = validator.validate(schema);
		if (violations != null && violations.size() > 0) 
		{
			throw new ObjException(
                String.format("Invalid schema: %s", Arrays.toString(
					violations.stream()
                        .map(v -> v.getPropertyPath().toString() + ": " + v.getMessage())
                            .toArray())));
		}
    }
}
