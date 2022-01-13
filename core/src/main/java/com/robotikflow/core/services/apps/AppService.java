package com.robotikflow.core.services.apps;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import javax.validation.Validator;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.exception.WorkspaceException;
import com.robotikflow.core.models.entities.App;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.filters.AppFilter;
import com.robotikflow.core.models.filters.WorkspaceFilter;
import com.robotikflow.core.models.repositories.AppAuthRepository;
import com.robotikflow.core.models.repositories.AppRepository;
import com.robotikflow.core.models.request.AccessType;
import com.robotikflow.core.models.request.AppRequest;
import com.robotikflow.core.models.schemas.app.AppSchema;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AppService 
{
	@Autowired
	private AppRepository appRepo;
	@Autowired
	private AppAuthRepository appAuthRepo;
	@Autowired
	@Lazy
	private Validator validator;
	
	private ObjectMapper objectMapper = new ObjectMapper()
		.findAndRegisterModules()
		.setSerializationInclusion(Include.NON_NULL);

	private ExampleMatcher appMatcher = ExampleMatcher.matching()
		.withIgnoreNullValues()
		.withIgnoreCase();

	/**
	 * 
	 * @param workspace
	 * @param appable
	 * @return
	 */
	public List<App> findAllByWorkspace(
		final Workspace workspace, 
		final Pageable appable) 
	{
		return appRepo
			.findAllByWorkspace(workspace, appable);
	}

	/**
	 * 
	 * @param filters
	 * @param workspace
	 * @param appable
	 * @return
	 */
	public List<App> findAllByWorkspace(
		final Workspace workspace, 
		final AppFilter filters,
		final Pageable appable) 
	{
		filters.setWorkspace(new WorkspaceFilter(workspace.getId()));
		var example = Example.of(new App(filters), appMatcher);
		return appRepo
			.findAll(example, appable)
				.getContent();
	}

	public App findByPubIdAndWorkspace(
		final String pubId, 
		final Workspace workspace) 
	{
		return appRepo
			.findByPubIdAndWorkspace(pubId, workspace.getId());
	}

	private void setCommonFields(
		final App app,
		final AppRequest req) 
	{
		app.setTitle(req.getTitle());
		app.setDesc(req.getDesc());
		app.setIcon(req.getIcon());
		app.setActive(req.getActive());
		app.setOptions(req.getOptions());
		app.setSchema(req.getSchema());
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
	public App update(
		final App app, 
		final AppRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		validate(req.getSchema());

		app.setUpdatedBy(user);
		app.setUpdatedAt(ZonedDateTime.now());

		setCommonFields(app, req);

		return appRepo.save(app);
	}

	/**
	 * 
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 * @throws Exception
	 */
	public App create(
		final AppRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		var app = new App();

		validate(req.getSchema());

		app.setWorkspace(workspace);
		app.setCreatedBy(user);
		app.setCreatedAt(ZonedDateTime.now());
		
		setCommonFields(app, req);

		return appRepo.save(app);
	}

	/**
	 * 
	 * @param app
	 * @param user
	 */
	public void delete(
		final App app, 
		final User user) 
	{
		appRepo.delete(app, user, ZonedDateTime.now());
	}

	private AppSchema stringToSchema(
		final String text) 
	{
		AppSchema schema = null;
		try 
		{
			schema = objectMapper.readValue(text, AppSchema.class);
		} 
		catch (JsonParseException | JsonMappingException e) 
		{
			throw new WorkspaceException(String.format("Syntax error at line(%d):column(%d): %s",
				e.getLocation().getLineNr(), e.getLocation().getColumnNr(), removeAtFromMessage(e.getMessage())),
				e);
		} 
		catch (IOException e) 
		{
			throw new WorkspaceException(String.format("Invalid schema: %s", removeAtFromMessage(e.getMessage())), e);
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

    protected AppSchema validate(
        final String text) 
        throws Exception
    {
		var schema = stringToSchema(text);
		
        var violations = validator.validate(schema);
		if (violations != null && violations.size() > 0) 
		{
			throw new WorkspaceException(
                String.format("Invalid schema: %s", Arrays.toString(
					violations.stream()
                        .map(v -> v.getPropertyPath().toString() + ": " + v.getMessage())
                            .toArray())));
		}

		return schema;
    }
	
	/**
	 * 
	 * @param user
	 * @param workspace
	 */
	public void authorize(
		final AccessType type,
		final User user, 
		final Workspace workspace) 
	{
		if(!user.isAdmin(workspace.getId()))
		{
			throw new WorkspaceException("Access denied");
		}
	}

	/**
	 * 
	 * @param app
	 * @param user
	 * @param workspace
	 */
	public void authorize(
		final AccessType type,	
		final App app, 
		final User user, 
		final Workspace workspace) 
	{
		if(!user.isSuperAdmin())
		{
			if(app.getWorkspace().getId() != workspace.getId())
			{
				throw new WorkspaceException("Access denied");
			}
		
			if(!user.isAdmin(workspace.getId()))
			{
				var perms = appAuthRepo
					.findAllByPageAndUserAndWorkspace(
						app, user, workspace);
				if(perms == null)
				{
					throw new WorkspaceException("Access denied");
				}
			}
		}
	}
}
