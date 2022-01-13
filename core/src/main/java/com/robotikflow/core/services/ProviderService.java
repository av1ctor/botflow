package com.robotikflow.core.services;

import java.time.ZonedDateTime;
import java.util.List;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.ProviderSchema;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.filters.ProviderFilter;
import com.robotikflow.core.models.filters.WorkspaceFilter;
import com.robotikflow.core.models.misc.ObjFields;
import com.robotikflow.core.models.repositories.ProviderRepository;
import com.robotikflow.core.models.repositories.ProviderSchemaRepository;
import com.robotikflow.core.models.request.ProviderRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProviderService 
	extends ObjBaseService<ProviderSchema>
{
	@Autowired
	private ProviderRepository providerRepo;
    @Autowired
    private ProviderSchemaRepository providerSchemaRepo;

	private ExampleMatcher provMatcher = ExampleMatcher.matching()
		.withIgnoreNullValues()
		.withIgnoreCase();

	/**
	 * 
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<Provider> findAllByWorkspace(
		final Workspace workspace, 
		final Pageable pageable) 
	{
		return providerRepo
			.findAllByWorkspace(workspace, pageable);
	}

	/**
	 * 
	 * @param workspace
	 * @param filters
	 * @param pageable
	 * @return
	 */
	public List<Provider> findAllByWorkspace(
		final Workspace workspace, 
		final ProviderFilter filters,
		final Pageable pageable) 
	{
		filters.setWorkspace(new WorkspaceFilter(workspace.getId()));
		var example = Example.of(new Provider(filters), provMatcher);
		return providerRepo
			.findAll(example, pageable)
				.getContent();
	}

	/**
	 * 
	 * @param pubId
	 * @param workspace
	 * @return
	 */
	public Provider findByPubIdAndWorkspace(
		final String pubId, 
		final Workspace workspace) 
	{
		return providerRepo
			.findByPubIdAndWorkspace(pubId, workspace);
	}

    protected ProviderSchema findSchemaByPubId(
        final String pubId
    )
    {
        return providerSchemaRepo.findByPubId(pubId);
    }

	/**
	 * 
	 * @param pageable
	 * @return
	 */
	public List<ProviderSchema> findAllSchemas(
		final Pageable pageable) 
	{
		return providerSchemaRepo
			.findAll(pageable)
				.toList();
	}
	
	private void setCommonFields(
		final Provider cred,
		final ProviderSchema schema,
		final ProviderRequest req) 
	{
		super.setFields(cred, schema, req);
	}

	/**
	 * 
	 * @param id
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 * @throws Exception
	 */
	public Provider update(
		final String id, 
		final ProviderRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var prov = providerRepo.findByPubIdAndWorkspace(id, workspace);
		if(prov == null)
		{
			throw new ObjException("Provider not found");
		}

		var schema = findSchemaByPubId(req.getSchemaId());
		validate(schema, req.getFields());

		prov.setUpdatedBy(user);
		prov.setUpdatedAt(ZonedDateTime.now());

		setCommonFields(prov, schema, req);

		return providerRepo.save(prov);
	}

	/**
	 * 
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 * @throws Exception
	 */
	public Provider create(
		final ProviderRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var prov = new Provider();

		var schema = findSchemaByPubId(req.getSchemaId());
		validate(schema, req.getFields());

		prov.setWorkspace(workspace);
		prov.setCreatedBy(user);
		prov.setCreatedAt(ZonedDateTime.now());
		
		setCommonFields(prov, schema, req);

		return providerRepo.save(prov);
	}

	/**
	 * 
	 * @param schemaId
	 * @param fields
	 * @throws Exception
	 */
	public void validate(
		final String schemaId,
		final ObjFields fields) 
		throws Exception
	{
		var schema = findSchemaByPubId(schemaId);
		super.validate(schema, fields);
	}
}
