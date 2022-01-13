package com.robotikflow.core.services.collections;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Validator;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.robotikflow.core.exception.CollectionException;
import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.CollectionAutomation;
import com.robotikflow.core.models.entities.CollectionAutomationActivity;
import com.robotikflow.core.models.entities.CollectionAutomationDate;
import com.robotikflow.core.models.entities.CollectionAutomationDateRepeat;
import com.robotikflow.core.models.entities.CollectionAutomationField;
import com.robotikflow.core.models.entities.CollectionAutomationItem;
import com.robotikflow.core.models.entities.CollectionAutomationTrigger;
import com.robotikflow.core.models.entities.CollectionAutomationType;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.CollectionAutomationLog;
import com.robotikflow.core.models.repositories.CollectionAutomationRepository;
import com.robotikflow.core.models.repositories.CollectionAutomationLogRepository;
import com.robotikflow.core.models.repositories.ObjStateRepository;
import com.robotikflow.core.models.request.ActivityRequest;
import com.robotikflow.core.models.request.CollectionAutomationActivityRequest;
import com.robotikflow.core.models.request.CollectionAutomationDateRequest;
import com.robotikflow.core.models.request.CollectionAutomationFieldRequest;
import com.robotikflow.core.models.request.CollectionAutomationRequest;
import com.robotikflow.core.models.schemas.collection.automation.AutomationSchema;
import com.robotikflow.core.models.schemas.collection.automation.Condition;
import com.robotikflow.core.models.schemas.expr.LogicalExpr;
import com.robotikflow.core.services.ActivityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Lazy
public class CollectionAutomationService 
{
	@Autowired
	private CollectionAutomationRepository collectionAutomationRepo;
	@Autowired
	private CollectionAutomationLogRepository collectionAutomationLogRepo;
	@Autowired
	private ActivityService activityService;
	@Autowired
	private ObjStateRepository objStateRepo;
	@Autowired
	@Lazy
	private Validator validator;

	private ObjectMapper objectMapper = new ObjectMapper()
		.findAndRegisterModules()
		.setSerializationInclusion(Include.NON_NULL);

	public CollectionAutomationService(Environment env) 
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
	public List<CollectionAutomation> findAll(
		CollectionWithSchema collection, 
		Pageable pageable) 
	{
		return collectionAutomationRepo.findAllByCollection(collection, pageable);
	}

	/**
	 * 
	 * @param collection
	 * @param type
	 * @param pageable
	 * @return
	 */
	public List<CollectionAutomation> findAllByTipo(
		CollectionWithSchema collection, 
		String type, 
		Pageable pageable) 
	{
		return collectionAutomationRepo
			.findAllByCollectionAndTypo(
				collection, 
				CollectionAutomationType.valueOf(type.toUpperCase()),
				pageable);
	}

	/**
	 * 
	 * @param collection
	 * @param tipos
	 * @param pageable
	 * @return
	 */
	public List<CollectionAutomation> findAllByTipoIn(
		CollectionWithSchema collection, 
		List<String> tipos, 
		Pageable pageable) 
	{
		var list = tipos.stream()
			.map(t -> CollectionAutomationType.valueOf(t.toUpperCase()))
				.collect(Collectors.toList());

		return collectionAutomationRepo.findAllByCollectionAndTypeIn(collection, list, pageable);
	}

	/**
	 * 
	 * @param id
	 * @param collection
	 * @return
	 */
	public CollectionAutomation findByPubIdAndCollection(
		String id, 
		CollectionWithSchema collection) 
	{
		return collectionAutomationRepo.findByPubIdAndCollection(id, collection);
	}

	private String removeAtFromMessage(
		String msg) 
	{
		var index = msg.indexOf("at [Source:");
		if (index < 0) 
		{
			return msg;
		}

		return msg.substring(0, index);
	}

	public <T> T validarSchema(
		String schema, 
		Class<T> klass) 
	{
		T res;

		try 
		{
			res = objectMapper.readValue(schema, klass);
		} 
		catch (JsonParseException | JsonMappingException e) 
		{
			throw new CollectionException(String.format("Erro de sintaxe na linha(%d) e coluna(%d): %s",
					e.getLocation().getLineNr(), e.getLocation().getColumnNr(), removeAtFromMessage(e.getMessage())),
					e);
		} 
		catch (IOException e) 
		{
			throw new CollectionException(String.format("Schema mal formado: %s", removeAtFromMessage(e.getMessage())), e);
		}

		var violacoes = validator.validate(res);
		if (violacoes != null && violacoes.size() > 0) 
		{
			throw new CollectionException(String.format("Schema mal formado: %s", Arrays.toString(
					violacoes.stream().map(v -> v.getPropertyPath().toString() + ": " + v.getMessage()).toArray())));
		}

		return res;
	}

	private void validarCondicao(
		LogicalExpr<Condition> expr
	)
	{
		if(expr.getCond() != null)
		{
			var cond = expr.getCond();
			switch(cond.getType())
			{
				case FIELD:
					var column = cond.getField();	
					if(column == null)
					{
						throw new CollectionException("Campo deve ser informado");
					}
					break;

				default:
					throw new CollectionException("Tipo de condição inexistente");
			}
		}
		else
		{
			var list = expr.getAnd() != null?
				expr.getAnd():
				expr.getOr();

			for(var node : list)
			{
				validarCondicao(node);
			}
		}
	}

	private AutomationSchema validarAutomationSchema(
		final String schema,
		final CollectionAutomationTrigger trigger)
	{
		var res = validarSchema(schema, AutomationSchema.class);

		if(res.getCondition() != null)
		{
			validarCondicao(res.getCondition());
		}
		else
		{
			if(trigger == CollectionAutomationTrigger.FIELD_UPDATED_TO)
			{
				throw new CollectionException("Condição deve ser informada");
			}
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
	public CollectionAutomation criar(
		CollectionWithSchema collection, 
		CollectionAutomationRequest req, 
		User user,
		Workspace workspace) 
	{
		CollectionAutomation automation;

		switch (req.getType()) 
		{
		case FIELD:
			automation = new CollectionAutomationField();
			break;
		case ITEM:
			automation = new CollectionAutomationItem();
			break;
		case DATE:
			automation = new CollectionAutomationDate();
			break;
		default:
			throw new CollectionException("Tipo de automação não implementado");
		}

		automation.setCollection(collection);
		automation.setCreatedAt(ZonedDateTime.now());
		automation.setCreatedBy(user);
		setCommonFields(automation, req, user, workspace);

		return collectionAutomationRepo.save(automation);
	}

	/**
	 * 
	 * @param automation
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	@Transactional
	public CollectionAutomation atualizar(
		CollectionAutomation automation, 
		CollectionAutomationRequest req, 
		User user,
		Workspace workspace) 
	{
		if (!automation.getType().equals(req.getType())) 
		{
			throw new CollectionException("Tipos diferentes de integração");
		}

		automation.setUpdatedAt(ZonedDateTime.now());
		automation.setUpdatedBy(user);
		setCommonFields(automation, req, user, workspace);

		return collectionAutomationRepo.save(automation);
	}

	private void setCommonFields(
		CollectionAutomation automation, 
		CollectionAutomationRequest req,
		User user, 
		Workspace workspace) 
	{
		automation.setDesc(req.getDesc());
		automation.setTrigger(req.getTrigger());
		
		var errors = new ArrayList<String>();
		var activities = req.getActivities().stream()
			.map((a) -> 
			{
				try
				{
					return buildActivity(automation, user, workspace, a);
				}
				catch(Exception ex)
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

		var statesToSave = automation.mergeActivities(activities);

		for(var state : statesToSave)
		{
			objStateRepo.save(state);
		}

		switch (automation.getType()) 
		{
		case FIELD: 
			{
				var ca = (CollectionAutomationField) automation;
				var careq = (CollectionAutomationFieldRequest) req;

				validarAutomationSchema(careq.getSchema(), careq.getTrigger());
				ca.setSchema(careq.getSchema());
			}
			break;

		case ITEM: 
			break;

		case DATE: 
			{
				var da = (CollectionAutomationDate) automation;
				var dareq = (CollectionAutomationDateRequest) req;

				da.setRepeat(dareq.getRepeat());
				var start = ZonedDateTime.parse(dareq.getStart());
				da.setStart(start);
				da.setNext(calcNext(start, dareq.getRepeat()));
			}
			break;

		default:
			throw new CollectionException("Tipo de automação não implementado");
		}
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

	private CollectionAutomationActivity buildActivity(
		final CollectionAutomation automation, 
		final User user,
		final Workspace workspace,
		final CollectionAutomationActivityRequest req) 
		throws Exception 
	{
		var activity = buildActivity(user, workspace, req.getActivity());

		if(req.getId() == null)
		{
			return new CollectionAutomationActivity(
				activity, automation);
		}
		else
		{
			var act = automation.getActivities().stream()
				.filter(a -> a.getPubId().equals(req.getId()))
				.findFirst()
				.orElseThrow(() -> new CollectionException("Activity not found"));
			
			act.setActivity(activity);

			return act;
		}
	}

	private ZonedDateTime calcNext(
		ZonedDateTime start, 
		CollectionAutomationDateRepeat repeat) 
	{
		switch(repeat)
		{
		case HOURLY:
			return start.plusHours(1);
		case DAILY:
			return start.plusDays(1);
		case WEEKLY:
			return start.plusWeeks(1);
		case BIWEEKLY:
			return start.plusWeeks(2);
		case MONTHLY:
			return start.plusMonths(1);
		case BIMONTHLY:
			return start.plusMonths(2);
		case QUARTERLY:
			return start.plusMonths(3);
		case SEMIANNUAL:
			return start.plusMonths(6);
		case YEARLY:
			return start.plusYears(1);
		default:
			return null;
		}
	}

	/**
	 * 
	 * @param automation
	 * @param pageable
	 * @return
	 */
	public List<CollectionAutomationLog> findAllLogs(
		CollectionAutomation automation, 
		Pageable pageable) 
	{
		return collectionAutomationLogRepo
			.findAllByAutomation(automation, pageable);
	}
}