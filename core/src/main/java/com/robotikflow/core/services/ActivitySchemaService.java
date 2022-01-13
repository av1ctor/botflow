package com.robotikflow.core.services;

import java.time.ZonedDateTime;
import java.util.List;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.models.entities.ActivitySchema;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.ActivitySchemaRepository;
import com.robotikflow.core.models.request.ActivitySchemaRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ActivitySchemaService 
{
    @Autowired
	private ObjSchemaService objSchemaService;
	@Autowired
    private ActivitySchemaRepository activitySchemaRepo;

	/**
	 * 
	 * @param pageable
	 * @return
	 */
	public List<ActivitySchema> findAll(
		final Pageable pageable) 
	{
		return activitySchemaRepo
			.findAll(pageable)
				.toList();
	}

	/**
	 * 
	 * @param pubId
	 * @return
	 */
	public ActivitySchema findByPubId(
		final String pubId) 
	{
		return activitySchemaRepo
			.findByPubId(pubId);
	}


	private void setCommonFields(
		final ActivitySchema schema,
		final ActivitySchemaRequest req) 
	{
		objSchemaService.setFields(schema, req);
		schema.setDir(req.getDir());
	}

	/**
	 * 
	 * @param id
	 * @param req
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public ActivitySchema update(
		final String id, 
		final ActivitySchemaRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var schema = activitySchemaRepo.findByPubId(id);
		if(schema == null)
		{
			throw new ObjException("Activity schema not found");
		}

		//validate(schema, req.getFields());

		schema.setUpdatedBy(user);
		schema.setUpdatedAt(ZonedDateTime.now());

		setCommonFields(schema, req);

		return activitySchemaRepo.save(schema);
	}

	/**
	 * 
	 * @param req
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public ActivitySchema create(
		final ActivitySchemaRequest req, 
		final User user) 
		throws Exception 
	{
		var schema = new ActivitySchema();

		//validate(schema, req.getFields());

		schema.setCreatedBy(user);
		schema.setCreatedAt(ZonedDateTime.now());
		
		setCommonFields(schema, req);

		return activitySchemaRepo.save(schema);
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
		objSchemaService.validate(schema, ActivitySchema.class);
	}
}
