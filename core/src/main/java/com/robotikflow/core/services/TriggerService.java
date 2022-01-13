package com.robotikflow.core.services;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.models.entities.Trigger;
import com.robotikflow.core.models.entities.TriggerSchema;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.TriggerRepository;
import com.robotikflow.core.models.repositories.TriggerSchemaRepository;
import com.robotikflow.core.models.request.TriggerRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TriggerService 
	extends ObjBaseService<TriggerSchema>
{
	@Autowired
	private TriggerRepository triggerRepo;
    @Autowired
    private TriggerSchemaRepository triggerSchemaRepo;

	/**
	 * 
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<Trigger> findAllByWorkspace(
		final Workspace workspace, 
		final Pageable pageable) 
	{
		return triggerRepo
			.findAllByWorkspace(workspace, pageable);
	}

	public Trigger findByPubIdAndWorkspace(
		final String pubId, 
		final Workspace workspace) 
	{
		return triggerRepo
			.findByPubIdAndWorkspace(pubId, workspace);
	}

    protected TriggerSchema findSchemaByPubId(
        final String pubId
    )
    {
        return triggerSchemaRepo.findByPubId(pubId);
    }

	private void setCommonFields(
		final Trigger trig,
		final TriggerSchema schema,
		final TriggerRequest req) 
	{
		super.setFields(trig, schema, req);
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
	public Trigger update(
		final String id, 
		final TriggerRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var trigger = triggerRepo.findByPubIdAndWorkspace(id, workspace);
		if(trigger == null)
		{
			throw new ObjException("Trigger not found");
		}

		var schema = trigger.getSchema();

		trigger.setUpdatedBy(user);
		trigger.setUpdatedAt(ZonedDateTime.now());

		setCommonFields(trigger, schema, req);

		return triggerRepo.save(trigger);
	}

	/**
	 * 
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 * @throws Exception
	 */
	public Trigger create(
		final TriggerRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var trigger = new Trigger();

		var schema = findSchemaByPubId(req.getSchemaId());
		validate(schema, req.getFields());

		trigger.setWorkspace(workspace);
		trigger.setCreatedBy(user);
		trigger.setCreatedAt(ZonedDateTime.now());
		
		setCommonFields(trigger, schema, req);

		return triggerRepo.save(trigger);
	}

	/**
	 * 
	 * @param schemaId
	 * @param fields
	 * @throws Exception
	 */
	public void validateSchema(
		final String schemaId,
		final Map<String, Object> fields
	) throws Exception
	{
		var schema = findSchemaByPubId(schemaId);
		super.validate(schema, fields);
	}
}
