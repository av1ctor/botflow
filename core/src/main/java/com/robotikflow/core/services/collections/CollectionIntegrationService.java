package com.robotikflow.core.services.collections;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Validator;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.exception.CollectionException;
import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.CollectionIntegration;
import com.robotikflow.core.models.entities.CollectionIntegrationActivity;
import com.robotikflow.core.models.entities.CollectionIntegrationLog;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.Trigger;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.repositories.CollectionIntegrationLogRepository;
import com.robotikflow.core.models.repositories.CollectionIntegrationRepository;
import com.robotikflow.core.models.repositories.ObjStateRepository;
import com.robotikflow.core.models.request.ActivityRequest;
import com.robotikflow.core.models.request.CollectionIntegrationActivityRequest;
import com.robotikflow.core.models.request.CollectionIntegrationRequest;
import com.robotikflow.core.models.request.TriggerRequest;
import com.robotikflow.core.services.ActivityService;
import com.robotikflow.core.services.TriggerService;
import com.robotikflow.core.util.Dates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Lazy
public class CollectionIntegrationService 
{
	@Autowired
	private CollectionIntegrationRepository collectionIntegrationRepo;
	@Autowired
	private CollectionIntegrationLogRepository collectionIntegrationLogRepo;
	@Autowired
	private ActivityService activityService;
	@Autowired
	private TriggerService triggerService;
	@Autowired
	private ObjStateRepository objStateRepo;
	@Autowired
	@Lazy
    private Validator validator;	
	
	private ObjectMapper objectMapper = new ObjectMapper()
		.findAndRegisterModules()
		.setSerializationInclusion(Include.NON_NULL);
    
	public CollectionIntegrationService(Environment env)
	{
	}	
    
    /**
	 * 
	 * @return
	 */
	public ObjectMapper getObjectMapper()
	{
		return objectMapper;
	}

	/**
	 * 
	 * @param collection
	 * @param pageable
	 * @return
	 */
	public List<CollectionIntegration> findAll(
		final CollectionWithSchema collection, 
		final Pageable pageable) 
	{
		return collectionIntegrationRepo.findAllByCollection(
			collection, pageable);
	}

	/**
	 * 
	 * @param id
	 * @param collection
	 * @return
	 */
	public CollectionIntegration findByPubIdAndCollection(
		final String id, 
		final CollectionWithSchema collection) 
	{
		return collectionIntegrationRepo.findByPubIdAndCollection(id, collection);
    }
    
	private String removeAtFromMessage(
		final String msg)
	{
		var index = msg.indexOf("at [Source:");
		if(index < 0)
		{
			return msg;
		}
		
		return msg.substring(0, index);
	}

    public <T> T validarSchema(
		final String schema, 
		final Class<T> klass)
    {
        T res;
        
        try 
		{
			res = objectMapper.readValue(schema, klass);
		} 
		catch(JsonParseException|JsonMappingException e)
		{
			throw new CollectionException(
				String.format("Erro de sintaxe na linha(%d) e coluna(%d): %s", 
						e.getLocation().getLineNr(), 
						e.getLocation().getColumnNr(), 
						removeAtFromMessage(e.getMessage())), 
				e);
		}
		catch (IOException e) 
		{
			throw new CollectionException(
					String.format("Schema mal formado: %s", removeAtFromMessage(e.getMessage())), e);
		}
		
		var violacoes = validator.validate(res);
		if(violacoes != null && violacoes.size() > 0)
		{
			throw new CollectionException(
					String.format("Schema mal formado: %s", 
						Arrays.toString(violacoes.stream().map(v -> v.getPropertyPath().toString() + ": " + v.getMessage()).toArray())));
        }
        
        return res;
    }

    /**
     * 
     * @param collection
     * @param req
     * @param user
     * @param workspace
     * @return
     */
	@Transactional
	public CollectionIntegration criar(
		final CollectionWithSchema collection, 
		final CollectionIntegrationRequest req, 
		final User user, 
		final Workspace workspace) 
		throws Exception
	{
        var integration = new CollectionIntegration();
		
        integration.setCollection(collection);
        integration.setCreatedAt(ZonedDateTime.now());
		integration.setCreatedBy(user);
        setCommonFields(integration, req, user, workspace);

		return collectionIntegrationRepo.save(integration);
	}

	/**
	 * 
	 * @param integration
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	@Transactional
	public CollectionIntegration atualizar(
		final CollectionIntegration integration, 
		final CollectionIntegrationRequest req, 
		final User user, 
		final Workspace workspace)
		throws Exception 
	{
		integration.setUpdatedAt(ZonedDateTime.now());
		integration.setUpdatedBy(user);
		setCommonFields(integration, req, user, workspace);

		return collectionIntegrationRepo.save(integration);
	}

	private Trigger buildTrigger(
		final CollectionIntegration integration, 
		final User user,
		final Workspace workspace,
		final TriggerRequest req) 
		throws Exception 
	{
		Trigger trigger = null;
		if(req.getId() == null)
		{
			trigger = triggerService.create(req, user, workspace);
		}
		else
		{
			trigger = triggerService.update(req.getId(), req, user, workspace);
		}

		return trigger;
	}

	private Activity buildActivity(
		final User user,
		final Workspace workspace,
		final ActivityRequest req) 
		throws Exception 
	{
		if(req.getId() == null)
		{
			return activityService.create(req, user, workspace);
		}
		else
		{
			return activityService.update(req.getId(), req, user, workspace);
		}
	}

	private CollectionIntegrationActivity buildActivity(
		final CollectionIntegration integration, 
		final User user,
		final Workspace workspace,
		final CollectionIntegrationActivityRequest req) 
		throws Exception 
	{
		var activity = buildActivity(user, workspace, req.getActivity());
		
		if(req.getId() == null)
		{
			return new CollectionIntegrationActivity(
				activity, integration);
		}
		else
		{
			var act = integration.getActivities().stream()
				.filter(a -> a.getPubId().equals(req.getId()))
				.findFirst()
				.orElseThrow(() -> new CollectionException("Activity not found"));
			act.setActivity(activity);

			return act;
		}
	}

	private void setCommonFields(
		final CollectionIntegration integration, 
		final CollectionIntegrationRequest req,
		final User user, 
		final Workspace workspace)
		throws Exception 
    {
		integration.setDesc(req.getDesc());
		
		var start = Dates.parse(req.getStart());
		integration.setStart(start);
		integration.setStarted(start == null || start.compareTo(ZonedDateTime.now()) >= 0);
		integration.setFreq(req.getFreq());
		integration.setMinOfDay(req.getMinOfDay());

		integration.setActive(req.isActive());
		integration.setRerunAt(null);
		integration.setReruns(0);

		var trigger = buildTrigger(integration, user, workspace, req.getTrigger());
		if(trigger == null)
		{
			throw new CollectionException("Invalid trigger");
		}
		integration.setTrigger(trigger);
		
		if(integration.getId() == null)
		{
			integration.setTriggerState(triggerService.createState());
		}
		else if(!req.getTrigger().getId().equals(trigger.getPubId()))
		{
			integration.getTriggerState().setState(new HashMap<String, Object>());
		}

		var errors = new ArrayList<String>();
		var activities = req.getActivities().stream()
			.map((a) -> 
			{
				try 
				{
					return buildActivity(integration, user, workspace, a);
				} 
				catch (Exception ex) 
				{
					errors.add(ex.getMessage());
					return null;
				}
			})
			.collect(Collectors.toList());
		
		if(errors.size() > 0)
		{
			throw new CollectionException(String.join(", ", errors));
		}

		var statesToSave = integration.mergeActivities(activities);
    
		for(var state : statesToSave)
		{
			objStateRepo.save(state);
		}
	}

	/**
	 * 
	 * @param integration
	 * @param pageable
	 * @return
	 */
	public List<CollectionIntegrationLog> findAllLogs(
		CollectionIntegration integration, 
		Pageable pageable) 
	{
		return collectionIntegrationLogRepo
			.findAllByIntegration(integration, pageable);
	}
}