package com.robotikflow.core.services;

import java.time.ZonedDateTime;
import java.util.List;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.models.entities.CredentialSchema;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.CredentialSchemaRepository;
import com.robotikflow.core.models.request.CredentialSchemaRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CredentialSchemaService 
{
    @Autowired
	private ObjSchemaService objSchemaService;
	@Autowired
    private CredentialSchemaRepository credentialSchemaRepo;

	/**
	 * 
	 * @param pageable
	 * @return
	 */
	public List<CredentialSchema> findAll(
		final Pageable pageable) 
	{
		return credentialSchemaRepo
			.findAll(pageable)
				.toList();
	}

	/**
	 * 
	 * @param pubId
	 * @return
	 */
	public CredentialSchema findByPubId(
		final String pubId) 
	{
		return credentialSchemaRepo
			.findByPubId(pubId);
	}


	private void setCommonFields(
		final CredentialSchema schema,
		final CredentialSchemaRequest req) 
	{
		objSchemaService.setFields(schema, req);
		schema.setVendor(schema.getVendor());
		schema.setMode(schema.getMode());
	}

	/**
	 * 
	 * @param id
	 * @param req
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public CredentialSchema update(
		final String id, 
		final CredentialSchemaRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var schema = credentialSchemaRepo.findByPubId(id);
		if(schema == null)
		{
			throw new ObjException("Credential schema not found");
		}

		//validate(schema, req.getFields());

		schema.setUpdatedBy(user);
		schema.setUpdatedAt(ZonedDateTime.now());

		setCommonFields(schema, req);

		return credentialSchemaRepo.save(schema);
	}

	/**
	 * 
	 * @param req
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public CredentialSchema create(
		final CredentialSchemaRequest req, 
		final User user) 
		throws Exception 
	{
		var schema = new CredentialSchema();

		//validate(schema, req.getFields());

		schema.setCreatedBy(user);
		schema.setCreatedAt(ZonedDateTime.now());
		
		setCommonFields(schema, req);

		return credentialSchemaRepo.save(schema);
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
		objSchemaService.validate(schema, CredentialSchema.class);
	}
}
