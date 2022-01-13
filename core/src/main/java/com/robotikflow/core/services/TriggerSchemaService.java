package com.robotikflow.core.services;

import java.time.ZonedDateTime;
import java.util.List;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.models.entities.TriggerSchema;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.TriggerSchemaRepository;
import com.robotikflow.core.models.request.TriggerSchemaRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TriggerSchemaService 
{
    @Autowired
	private ObjSchemaService objSchemaService;
	@Autowired
    private TriggerSchemaRepository triggerSchemaRepo;

	/**
	 * 
	 * @param pageable
	 * @return
	 */
	public List<TriggerSchema> findAll(
		final Pageable pageable) 
	{
		return triggerSchemaRepo
			.findAll(pageable)
				.toList();
	}

	/**
	 * 
	 * @param pubId
	 * @return
	 */
	public TriggerSchema findByPubId(
		final String pubId) 
	{
		return triggerSchemaRepo
			.findByPubId(pubId);
	}


	private void setCommonFields(
		final TriggerSchema schema,
		final TriggerSchemaRequest req) 
	{
		objSchemaService.setFields(schema, req);
		schema.setOptions(req.getOptions());
	}

	/**
	 * 
	 * @param id
	 * @param req
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public TriggerSchema update(
		final String id, 
		final TriggerSchemaRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var schema = triggerSchemaRepo.findByPubId(id);
		if(schema == null)
		{
			throw new ObjException("Trigger schema not found");
		}

		//validate(schema, req.getFields());

		schema.setUpdatedBy(user);
		schema.setUpdatedAt(ZonedDateTime.now());

		setCommonFields(schema, req);

		return triggerSchemaRepo.save(schema);
	}

	/**
	 * 
	 * @param req
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public TriggerSchema create(
		final TriggerSchemaRequest req, 
		final User user) 
		throws Exception 
	{
		var schema = new TriggerSchema();

		//validate(schema, req.getFields());

		schema.setCreatedBy(user);
		schema.setCreatedAt(ZonedDateTime.now());
		
		setCommonFields(schema, req);

		return triggerSchemaRepo.save(schema);
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
		objSchemaService.validate(schema, TriggerSchema.class);
	}
}
