package com.robotikflow.core.services;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import com.robotikflow.core.exception.ObjException;
import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.ActivitySchema;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.ActivityRepository;
import com.robotikflow.core.models.repositories.ActivitySchemaRepository;
import com.robotikflow.core.models.request.ActivityRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ActivityService 
	extends ObjBaseService<ActivitySchema>
{
	@Autowired
	private ActivityRepository activityRepo;
    @Autowired
    private ActivitySchemaRepository activitySchemaRepo;

	/**
	 * 
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<Activity> findAllByWorkspace(
		final Workspace workspace, 
		final Pageable pageable) 
	{
		return activityRepo
			.findAllByWorkspace(workspace, pageable);
	}

	public Activity findByPubIdAndWorkspace(
		final String pubId, 
		final Workspace workspace) 
	{
		return activityRepo
			.findByPubIdAndWorkspace(pubId, workspace);
	}

    protected ActivitySchema findSchemaByPubId(
        final String pubId
    )
    {
        return activitySchemaRepo.findByPubId(pubId);
    }
	
	private void setCommonFields(
		final Activity cred,
		final ActivitySchema schema,
		final ActivityRequest req) 
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
	public Activity update(
		final String id, 
		final ActivityRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var act = activityRepo.findByPubIdAndWorkspace(id, workspace);
		if(act == null)
		{
			throw new ObjException("Activity not found");
		}

		var schema = act.getSchema();

		act.setUpdatedBy(user);
		act.setUpdatedAt(ZonedDateTime.now());

		setCommonFields(act, schema, req);

		return activityRepo.save(act);
	}

	/**
	 * 
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 * @throws Exception
	 */
	public Activity create(
		final ActivityRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var act = new Activity();

		var schema = findSchemaByPubId(req.getSchemaId());
		validate(schema, req.getFields());

		act.setWorkspace(workspace);
		act.setCreatedBy(user);
		act.setCreatedAt(ZonedDateTime.now());
		
		setCommonFields(act, schema, req);

		return activityRepo.save(act);
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
