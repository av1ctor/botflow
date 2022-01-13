package com.robotikflow.core.services.collections;

import java.io.IOException;
import java.io.StringReader;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;
import javax.validation.Validator;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReaderHeaderAwareBuilder;
import com.opencsv.enums.CSVReaderNullFieldIndicator;
import com.robotikflow.core.exception.CollectionException;
import com.robotikflow.core.models.entities.Collection;
import com.robotikflow.core.models.entities.CollectionAuthRole;
import com.robotikflow.core.models.entities.CollectionAutomation;
import com.robotikflow.core.models.entities.CollectionAutomationActivity;
import com.robotikflow.core.models.entities.CollectionAutomationField;
import com.robotikflow.core.models.entities.CollectionAutomationTrigger;
import com.robotikflow.core.models.entities.CollectionAutomationType;
import com.robotikflow.core.models.entities.CollectionDupOptions;
import com.robotikflow.core.models.entities.CollectionIntegration;
import com.robotikflow.core.models.entities.CollectionIntegrationActivity;
import com.robotikflow.core.models.entities.CollectionItemDocument;
import com.robotikflow.core.models.entities.CollectionItemLog;
import com.robotikflow.core.models.entities.CollectionItemPost;
import com.robotikflow.core.models.entities.CollectionLog;
import com.robotikflow.core.models.entities.CollectionOptions;
import com.robotikflow.core.models.entities.CollectionPost;
import com.robotikflow.core.models.entities.CollectionTemplate;
import com.robotikflow.core.models.entities.CollectionType;
import com.robotikflow.core.models.entities.CollectionVersion;
import com.robotikflow.core.models.entities.CollectionVersionChangeId;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.CollectionAuth;
import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.WorkspacePostType;
import com.robotikflow.core.models.filters.CollectionFilter;
import com.robotikflow.core.models.filters.WorkspaceFilter;
import com.robotikflow.core.models.nosql.Filter;
import com.robotikflow.core.models.nosql.FilterOperator;
import com.robotikflow.core.models.nosql.Reference;
import com.robotikflow.core.models.nosql.ReferenceType;
import com.robotikflow.core.models.queue.ActivityTriggeredMessage;
import com.robotikflow.core.models.queue.CollectionUpdatedEvent;
import com.robotikflow.core.models.queue.CollectionUpdatedMessage;
import com.robotikflow.core.models.repositories.CollectionAuthRepository;
import com.robotikflow.core.models.repositories.CollectionAutomationRepository;
import com.robotikflow.core.models.repositories.CollectionIntegrationRepository;
import com.robotikflow.core.models.repositories.CollectionItemDocumentRepository;
import com.robotikflow.core.models.repositories.CollectionItemLogRepository;
import com.robotikflow.core.models.repositories.CollectionItemPostRepository;
import com.robotikflow.core.models.repositories.CollectionLogRepository;
import com.robotikflow.core.models.repositories.CollectionPostRepository;
import com.robotikflow.core.models.repositories.CollectionRepository;
import com.robotikflow.core.models.repositories.CollectionVersionChangeRepository;
import com.robotikflow.core.models.repositories.ProviderRepository;
import com.robotikflow.core.models.repositories.UserRepository;
import com.robotikflow.core.models.repositories.WorkspacePostRepository;
import com.robotikflow.core.models.request.CollectionFilterRequest;
import com.robotikflow.core.models.request.CollectionRequest;
import com.robotikflow.core.models.request.CollectionSchemaDiffRequest;
import com.robotikflow.core.models.request.MoveDirections;
import com.robotikflow.core.models.request.WorkspacePostRequest;
import com.robotikflow.core.models.schemas.collection.Auth;
import com.robotikflow.core.models.schemas.collection.AuthAction;
import com.robotikflow.core.models.schemas.collection.AuthActionWhen;
import com.robotikflow.core.models.schemas.collection.AuthColumn;
import com.robotikflow.core.models.schemas.collection.AuthUser;
import com.robotikflow.core.models.schemas.collection.CollectionSchema;
import com.robotikflow.core.models.schemas.collection.Const;
import com.robotikflow.core.models.schemas.collection.Field;
import com.robotikflow.core.models.schemas.collection.FieldComponent;
import com.robotikflow.core.models.schemas.collection.FieldDependency;
import com.robotikflow.core.models.schemas.collection.FieldHidden;
import com.robotikflow.core.models.schemas.collection.FieldHiddenType;
import com.robotikflow.core.models.schemas.collection.FieldIndexDir;
import com.robotikflow.core.models.schemas.collection.FieldType;
import com.robotikflow.core.models.schemas.collection.Flow;
import com.robotikflow.core.models.schemas.collection.FlowCondition;
import com.robotikflow.core.models.schemas.collection.FlowItemMeta;
import com.robotikflow.core.models.schemas.collection.FlowItemMetaStatus;
import com.robotikflow.core.models.schemas.collection.FlowOut;
import com.robotikflow.core.models.schemas.collection.FlowType;
import com.robotikflow.core.models.schemas.collection.Form;
import com.robotikflow.core.models.schemas.collection.Index;
import com.robotikflow.core.models.schemas.collection.ItemMeta;
import com.robotikflow.core.models.schemas.collection.Klass;
import com.robotikflow.core.models.schemas.collection.Method;
import com.robotikflow.core.models.schemas.collection.Ref;
import com.robotikflow.core.models.schemas.collection.Report;
import com.robotikflow.core.models.schemas.collection.ReportColumn;
import com.robotikflow.core.models.schemas.collection.View;
import com.robotikflow.core.models.schemas.collection.ViewField;
import com.robotikflow.core.models.schemas.collection.automation.Condition;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrColumnOrValue;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrFieldOrValue;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrFunctionOrValue;
import com.robotikflow.core.models.schemas.collection.valuetype.ScriptOrValue;
import com.robotikflow.core.models.schemas.expr.LogicalExpr;
import com.robotikflow.core.services.collections.models.result.SchemaValidationResult;
import com.robotikflow.core.services.collections.services.CalendarScriptService;
import com.robotikflow.core.services.collections.services.DateScriptService;
import com.robotikflow.core.services.collections.services.DbScriptService;
import com.robotikflow.core.services.collections.services.MathScriptService;
import com.robotikflow.core.services.collections.services.NetScriptService;
import com.robotikflow.core.services.collections.services.UserScriptService;
import com.robotikflow.core.services.collections.services.UtilScriptService;
import com.robotikflow.core.services.formula.eval.EvalContext;
import com.robotikflow.core.services.formula.parser.CachedParser;
import com.robotikflow.core.services.log.CollectionAutomationLogger;
import com.robotikflow.core.services.log.CollectionItemLogger;
import com.robotikflow.core.services.log.CollectionLogger;
import com.robotikflow.core.services.log.WorkspaceLogger;
import com.robotikflow.core.services.nosql.AggregateOperator;
import com.robotikflow.core.services.nosql.NoSqlService;
import com.robotikflow.core.services.queue.QueueService;
import com.robotikflow.core.services.queue.RabbitQueueService;
import com.robotikflow.core.util.Collections;
import com.robotikflow.core.util.ProviderUtil;
import com.robotikflow.core.util.diff_match_patch;
import com.robotikflow.core.util.expr.Evaluator;

import org.apache.commons.text.CaseUtils;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javafx.util.Pair;

@Service
@Lazy
public class CollectionService 
{
	private Logger logger = LoggerFactory.getLogger(CollectionService.class);
	@Autowired
	private CollectionRepository collectionRepo;
	@Autowired
	private CollectionIntegrationRepository collectionIntegrationRepo;
	@Autowired
	private CollectionAutomationRepository collectionAutomationRepo;
	@Autowired
	private CollectionItemDocumentRepository collectionItemDocumentRepo;
	@Autowired
	private CollectionLogRepository collectionLogRepo;
	@Autowired
	private CollectionPostRepository collectionPostRepo;
	@Autowired
	private CollectionItemLogRepository collectionItemLogRepo;
	@Autowired
	private CollectionItemPostRepository collectionItemPostRepo;
	@Autowired
	private CollectionAuthRepository collectionAuthRepo;
	@Autowired
	private CollectionVersionChangeRepository collectionVersionChangeRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private WorkspacePostRepository workspacePostRepo;
	@Autowired
	private NoSqlService noSqlService;
	@Autowired
	private WorkspaceLogger workspaceLogger;
	@Autowired
	private CollectionLogger collectionLogger;
	@Autowired
	private CollectionAutomationLogger collectionAutomationLogger;
	@Autowired
	private CollectionItemLogger collectionItemLogger;
	@Autowired
	private ProviderRepository providerRepo;
	@Autowired
	@Lazy
	@Qualifier("stompQueueService")
	private QueueService stompQueueService;
	@Autowired
	@Lazy
	private QueueService messengerQueueService;
	@Autowired
	@Lazy
	private QueueService activitiesQueueService;
	@Autowired
	@Lazy
	private Validator validator;
	@Autowired
	private CachedParser cachedParser;

	private ObjectMapper objectMapper = new ObjectMapper()
		.findAndRegisterModules()
		.setSerializationInclusion(Include.NON_NULL);

	private ExampleMatcher matcher = ExampleMatcher.matching()
		.withIgnoreNullValues()
		.withIgnoreCase()
		.withIgnorePaths("editavel")
		.withMatcher("name", m -> m.startsWith());

	private diff_match_patch dmp = new diff_match_patch();

	private static final String posicionalSeqSuffix = "-pos";

	private static final String ID_NAME = "_id";

	private static final Set<String> reservedNames = 
		new HashSet<>(Arrays.asList(ID_NAME, "_meta"));

	public CollectionService(Environment env) 
	{
	}

	/**
	 * 
	 * @param workspace
	 */
	public void iniciar(
		final String idWorkspace) 
	{
		noSqlService.initialize(idWorkspace);
	}

	public String getIdName() 
	{
		return ID_NAME;
	}

	/**
	 * 
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<Collection> findAllByWorkspace(
		final Workspace workspace, 
		final String idParent,
		final Pageable pageable) 
	{
		return idParent == null ? 
			collectionRepo.findAllByWorkspace(workspace, pageable): 
			collectionRepo.findAllByWorkspace(workspace, idParent, pageable);
	}

	/**
	 * 
	 * @param workspace
	 * @param pageable
	 * @param filtros
	 * @return
	 */
	public List<Collection> findAllByWorkspace(
		final Workspace workspace, 
		final String idParent,
		final CollectionFilter filtros, 
		final Pageable pageable) 
	{
		filtros.setWorkspace(new WorkspaceFilter() 
		{
			{
				setId(workspace.getId());
			}
		});
		
		var example = Example.of(new Collection(filtros, idParent), matcher);
		return collectionRepo.findAll(example, pageable).getContent();
	}

	/**
	 * 
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<Collection> findAllPublishedByWorkspace(
		final Workspace workspace, 
		final String idParent,
		final Pageable pageable) 
	{
		return collectionRepo
			.findAllPublishedByWorkspace(workspace, idParent, pageable);
	}

	/**
	 * 
	 * @param user
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<Collection> findAllPublishedByUserAndWorkspace(
		final User user, 
		final Workspace workspace,
		final String parent, 
		final Pageable pageable) 
	{
		return collectionRepo
			.findAllPublishedByUserAndWorkspace(user, workspace, parent, pageable);
	}
	/**
	 * 
	 * @param id
	 * @return
	 */
	public Collection findById(
		final Long id) 
	{
		return collectionRepo
			.findById(id)
				.orElse(null);
	}

	/**
	 * 
	 * @param pubId
	 * @return
	 */
	public Collection findByPubId(
		final String pubId) 
	{
		return collectionRepo.findByPubId(pubId);
	}

	/**
	 * 
	 * @param pubId
	 * @param workspace
	 * @return
	 */
	public Collection findByPubIdAndWorkspace(
		final String pubId, 
		final Workspace workspace) 
	{
		return collectionRepo.findByPubIdAndWorkspace(pubId, workspace.getId());
	}

	/**
	 * 
	 * @param pubId
	 * @param workspace
	 * @return
	 */
	public Collection findByPubIdAndWorkspace(
		final String pubId, 
		final Long workspace) 
	{
		return collectionRepo.findByPubIdAndWorkspace(pubId, workspace);
	}

	/**
	 * 
	 * @param pubId
	 * @param workspace
	 * @return
	 */
	public Collection findByPubIdAndUserAndWorkspace(
		final String pubId, 
		final User user,
		final Workspace workspace) 
	{
		return collectionRepo.findByPubIdAndUserAndWorkspace(pubId, user, workspace);
	}

	/**
	 * 
	 * @param id
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<Collection> findAllAuxsByCollection(
		final Long id, 
		final Workspace workspace, 
		final Pageable pageable) 
	{
		return collectionRepo.findAllAuxsByCollection(id, workspace, pageable);
	}

	/**
	 * 
	 * @param id
	 * @param user
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<Collection> findAllAuxsByCollectionAndUser(
		final Long id, 
		final User user,
		final Workspace workspace, 
		final Pageable pageable) 
	{
		return collectionRepo.findAllAuxsByCollectionAndUser(id, user, workspace, pageable);
	}

	/**
	 * 
	 * @param collection
	 * @param pageable
	 * @return
	 */
	public List<CollectionVersion> findAllVersions(
		final CollectionWithSchema collection, 
		final Pageable pageable) 
	{
		return collectionRepo.findAllVersions(collection, pageable);
	}

	/**
	 * 
	 * @param collection
	 * @param pubId
	 * @return
	 */
	public CollectionVersion findVersionById(
		final CollectionWithSchema collection, 
		final String pubId) 
	{
		return collectionRepo.findVersionById(collection, pubId);
	}	
	/**
	 * 
	 * @param collection
	 * @return
	 */
	public Collection save(Collection collection) 
	{
		return collectionRepo.save(collection);
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

	private boolean hasIndex(
		final CollectionSchema schema, 
		final String name) 
	{
		if (schema.getIndexes() != null) 
		{
			return schema.getIndexes().stream()
				.anyMatch(i -> i.getColumns().stream().anyMatch(c -> c.equals(name)));
		} 
		else 
		{
			return false;
		}
	}

	private <T> void validarViolacoesNoSchema(T res) 
	{
		var violacoes = validator.validate(res);
		if (violacoes != null && violacoes.size() > 0) 
		{
			throw new CollectionException(String.format("Schema mal formado: %s", Arrays.toString(
					violacoes.stream().map(v -> v.getPropertyPath().toString() + ": " + v.getMessage()).toArray())));
		}
	}

	private <T> T stringToSchema(
		final String schema, 
		final Class<T> klass) 
	{
		T res = null;
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

		validarViolacoesNoSchema(res);

		return res;
	}

	private SchemaValidationResult validarSchema(
		final String schema, 
		final boolean verificarSchema, 
		final Workspace workspace) 
	{
		var res = new SchemaValidationResult();

		//
		res.schema = stringToSchema(schema, CollectionSchema.class);
		var colunas = res.schema.getColumns();

		// procurar coluna "auto": true
		var autoIds = getAutoIds(colunas);
		if (autoIds != null && autoIds.size() > 0) {
			res.autoId = autoIds.get(0);
		}

		// procurar coluna "positional": true
		var posIds = getPosIds(colunas);
		if (posIds != null && posIds.size() > 0) {
			res.posId = posIds.get(0);
		}

		if (!verificarSchema) {
			return res;
		}

		//
		var referencias = res.schema.getRefs();
		var classes = res.schema.getClasses();

		// validação das classes
		validarClassesNoSchema(classes);

		// constantes
		validarConstantesNoSchema(res.schema.getConstants());

		// colunas
		validarColunasNoSchema(res.schema, colunas, classes, referencias, autoIds, posIds);

		// índices
		validarIndicesNoSchema(res.schema, res.schema.getIndexes(), colunas, referencias, classes);

		// verificar se as colunas referenciadas existem na tabela
		validarRefsNoSchema(workspace, referencias);

		// vies
		validarViewsNoSchema(res.schema, res.schema.getViews(), colunas, referencias, classes);

		// relatórios
		validarRelatoriosNoSchema(res.schema, res.schema.getReports(), colunas, referencias, classes);

		// autorização
		validarAuthNoSchema(res.schema, res.schema.getAuth());

		// flows
		validarFluxosNoSchema(res.schema, colunas, classes, referencias, res.schema.getFlows());

		return res;
	}

	private void validarFluxosNoSchema(
		final CollectionSchema schema, 
		final Map<String, Field> colunas,
		final Map<String, Klass> classes, 
		final Map<String, Ref> referencias,
		final Map<String, Flow> flows) 
	{
		if(flows == null)
		{
			return;
		}

		Flow startNode = null;
		Flow endNode = null;

		for(var entry: flows.entrySet())
		{
			var flowName = entry.getKey();
			var flow = entry.getValue();
			switch(flow.getType())
			{
			case START:
				if(flow.getIn() != null && flow.getIn().size() > 0)
				{
					throw new CollectionException(String.format(
						"Fluxo '%s' é inicial e não pode possuir entradas", flowName));
				}
				if(flow.getOut() == null || flow.getOut().size() != 1)
				{
					throw new CollectionException(String.format(
						"Fluxo '%s' é inicial e deve possuir uma única saída", flowName));
				}

				var condition = flow.getOut().values().stream().findFirst().get().getCondition();
				if(condition != null)
				{
					throw new CollectionException(String.format(
						"Fluxo '%s' é inicial e sua saída não deve possuir condições", flowName));
				}

				if(startNode != null)
				{
					throw new CollectionException(String.format(
						"Só pode existir um nó inicial. Duplicação em '%'", flowName));
				}

				startNode = flow;
				break;
			
			case END:
				if(flow.getIn() == null || flow.getIn().size() == 0)
				{
					throw new CollectionException(String.format(
						"Fluxo '%s' é final e deve possuir ao menos uma entrada", flowName));
				}
				if(flow.getOut() != null && flow.getOut().size() > 0)
				{
					throw new CollectionException(String.format(
						"Fluxo '%s' é final e não pode possuir saídas", flowName));
				}

				if(endNode != null)
				{
					throw new CollectionException(String.format(
						"Só pode existir um nó final. Duplicação em '%'", flowName));
				}
		
				endNode = flow;
				break;
			
			case XOR:
			case OR:
			case PARALLEL:
				if(flow.getIn() == null || flow.getIn().size() == 0)
				{
					throw new CollectionException(String.format(
						"Fluxo '%s' deve possuir ao menos uma entrada", flowName));
				}
				if(flow.getOut() == null || flow.getOut().size() == 0)
				{
					throw new CollectionException(String.format(
						"Fluxo '%s' deve possuir ao menos uma saída", flowName));
				}
				break;
			
			case SERVICE:
			case USER:
				if(flow.getIn() == null || flow.getIn().size() == 0)
				{
					throw new CollectionException(String.format(
						"Fluxo '%s' deve possuir ao menos uma entrada", flowName));
				}
				if(flow.getOut() == null || flow.getOut().size() != 1)
				{
					throw new CollectionException(String.format(
						"Fluxo '%s' deve possuir uma única saída", flowName));
				}
				break;

			case PART:
				if(flow.getIn() != null && flow.getIn().size() > 0)
				{
					throw new CollectionException(String.format(
						"Fluxo '%s' não deve possuir entradas", flowName));
				}
				if(flow.getOut() != null && flow.getOut().size() > 0)
				{
					throw new CollectionException(String.format(
						"Fluxo '%s' não deve possuir saídas", flowName));
				}
				break;
		
			default:
				break;
			}

			if(flow.getIn() != null)
			{
				for(var inName : flow.getIn())
				{
					if(!flows.containsKey(inName))
					{
						throw new CollectionException(String.format(
							"Fluxo de entrada '%s', do fluxo '%s', inexistente", inName, flowName));
					}

					var inFlow = flows.get(inName);
					if(inFlow.getOut() == null || !inFlow.getOut().keySet().contains(flowName))
					{
						throw new CollectionException(String.format(
							"Fluxo de entrada '%s', do fluxo '%s', não possui fluxo de saída correspondente", inName, flowName));
					}
				}
			}

			if(flow.getOut() != null)
			{
				for(var oentry : flow.getOut().entrySet())
				{
					var outName = oentry.getKey();
					if(!flows.containsKey(outName))
					{
						throw new CollectionException(String.format(
							"Fluxo de saída '%s', do fluxo' %s', inexistente", outName, flowName));
					}
					
					var outFlow = flows.get(outName);
					if(outFlow.getIn() == null || !outFlow.getIn().contains(flowName))
					{
						throw new CollectionException(String.format(
							"Fluxo de saída '%s', do fluxo '%s', não possui fluxo de entrada correspondente", outName, flowName));
					}

					var out = oentry.getValue();
					if(out != null && out.getCondition() != null)
					{
						validarCondicoesDoFluxo(
							out.getCondition(), colunas, classes, referencias, outName, flowName);
					}
				}
			}
		}

		if(startNode == null)
		{
			throw new CollectionException("Nó inicial é obrigatório");
		}

		if(endNode == null)
		{
			throw new CollectionException("Nó final é obrigatório");
		}

		// percorrer todos os caminhos a partir do nó inicial e verificar se ocorre looping
		if(startNode.getOut() != null)
		{
			var visited = new HashSet<String>();
			var path = new StringBuilder();
			path.append("/");
			path.append(startNode.getLabel().getValue());
			visitOutNodes(flows, startNode.getOut(), visited, path);
		}

	}

	private void validarCondicoesDoFluxo(
		final LogicalExpr<FlowCondition> cond,
		final Map<String, Field> colunas,
		final Map<String, Klass> classes, 
		final Map<String, Ref> referencias,
		final String outName, 
		final String flowName) 
	{
		if(cond.getCond() != null)
		{
			var val = cond.getCond();

			if(val.getColumn() != null)
			{
				var col = val.getColumn();
				var key = col.getName();
				if(!colunaExisteNoSchema(colunas, referencias, classes, key))
				{
					throw new CollectionException(String.format(
						"Coluna '%s' da saída '%s', do fluxo '%s', inexistente", key, outName, flowName));
				}

				if(col.getOp() == null)
				{
					throw new CollectionException(String.format(
						"Operador na saída '%s', do fluxo '%s', obrigatório", outName, flowName));
				}

				if(col.getOp() == FilterOperator.notnull || col.getOp() == FilterOperator.isnull)
				{
					if(col.getValue() != null)
					{
						throw new CollectionException(String.format(
							"Valor na saída '%s', do fluxo '%s', não deve ser informado", outName, flowName));
					}
				}
				else
				{
					if(col.getValue() == null)
					{
						throw new CollectionException(String.format(
							"Valor na saída '%s', do fluxo '%s', obrigatório", outName, flowName));
					}
				}								
			}
			else if(val.getAutomation() != null)
			{

			}
		}
		else
		{
			var list = cond.getAnd() != null? 
				cond.getAnd():
				cond.getOr();

			if(list == null || list.size() == 0)
			{
				throw new CollectionException(String.format(
					"Condições na saída '%s', do fluxo '%s', devem ser informadas", outName, flowName));
			}

			for(var item : list)
			{
				validarCondicoesDoFluxo(
					item, colunas, classes, referencias, outName, flowName);
			}
		}
	}

	private void visitOutNodes(
		final Map<String, Flow> flows,
		final Map<String, FlowOut> outs, 
		final HashSet<String> visited,
		final StringBuilder path) 
	{
		for(var entry: outs.entrySet())
		{
			var out = entry.getValue();
			if(out.getCondition() == null)
			{
				var name = entry.getKey();
				var flow = flows.get(name);
				
				var outPath = new StringBuilder(path);
				outPath.append("/");
				outPath.append(flow.getLabel().getValue());

				if(visited.contains(name))
				{
					throw new CollectionException(String.format("Loop detectado: '%s'. Corrija o desenho, inserindo condições", outPath));
				}
				
				var outVisited = new HashSet<String>(visited);
				outVisited.add(name);
				
				if(flow.getOut() != null)
				{
					visitOutNodes(flows, flow.getOut(), outVisited, outPath);
				}
			}
		}
	}

	private void validarViewsNoSchema(
		final CollectionSchema schema, 
		final List<View> views, 
		final Map<String, Field> colunas, 
		final Map<String, Ref> referencias, 
		final Map<String, Klass> classes) 
	{
		if(views == null)
		{
			return;
		}

		for(var view : views)
		{
			if(view.getGroupBy() != null)
			{
				var groupBy = view.getGroupBy();
				var name = groupBy.getColumn();

				// verificar se o campo em 'groupBy' existe na tabela, referências ou classes
				if(!colunaExisteNoSchema(colunas, referencias, classes, name))
				{
					throw new CollectionException(String.format(
						"Coluna %s em 'groupBy' da view %s não encontrada", name, view.getName()));
				}
				
				if(!hasIndex(schema, name))
				{
					throw new CollectionException(String.format(
						"Coluna %s em 'groupBy' da view %s deve ter seu próprio índice ou ser parte da chave", name, view.getName()));
				}

				if(groupBy.getAggregates() != null)
				{
					for(var coluna: groupBy.getAggregates().keySet())
					{
						if(!colunaExisteNoSchema(colunas, referencias, classes, coluna))
						{
							throw new CollectionException(String.format(
								"Coluna %s em 'groupBy.aggregates' da view %s não encontrada", coluna, view.getName()));
						}
					}
				}
			}
			
			if(view.getFilters() != null)
			{
				if(schema.getIndexes() == null)
				{
					throw new CollectionException(String.format(
						"Para definir o objeto 'filters' na view %s, é necessário criar índices na tabela", view.getName()));
				}
				
				for(var entry: view.getFilters().entrySet())
				{
					var name = entry.getKey();
					if(!colunaExisteNoSchema(colunas, referencias, classes, name))
					{
						throw new CollectionException(String.format(
							"Coluna %s no objeto 'filters' da view %s não encontrada", name, view.getName()));
					}

					// verificar o campo em 'filter' possui índice
					if(!hasIndex(schema, name))
					{
						throw new CollectionException(String.format(
							"Coluna %s no objeto 'filters' da view %s deve ter seu próprio índice ou ser parte da chave", name, view.getName()));
					}
				}
			}

			if(view.getSort() != null)
			{
				if(schema.getIndexes() == null)
				{
					throw new CollectionException(String.format(
						"Para definir o objeto 'sort' da view %s, é necessário criar índices na tabela", view.getName()));
				}
				
				for(var entry: view.getSort().entrySet())
				{
					var name = entry.getKey();

					// verificar se o campo em 'sort' existe na tabela, referências ou classes
					if(!colunaExisteNoSchema(colunas, referencias, classes, name))
					{
						throw new CollectionException(String.format(
							"Coluna %s no objeto 'sort' da view %s não encontrada", name, view.getName()));
					}
					
					// verificar o campo em 'sort' possui índice
					if(!hasIndex(schema, name))
					{
						throw new CollectionException(String.format(
							"Coluna %s no objeto 'sort' da view %s deve ter seu próprio índice ou ser parte da chave", name, view.getName()));
					}
				}
			}
			
			for(var entry : view.getFields().entrySet())
			{
				var name = entry.getKey();
				if(!colunaExisteNoSchema(colunas, referencias, classes, name))
				{
					throw new CollectionException(String.format(
						"Coluna %s no objeto 'fields' da view %s não encontrada", name, view.getName()));
				}

				var field = entry.getValue();
				if(field.getFields() != null)
				{
					var coluna = colunas.get(name);  
					if(coluna.getType() != FieldType.object || coluna.getClass_() == null)
					{
						throw new CollectionException(String.format(
							"Coluna %s no objeto 'fields' da view %s não é do type objeto", name, view.getName()));
					}

					for(var nomeProp: field.getFields().keySet())
					{
						if(!propExisteNaClasse(coluna, nomeProp, classes))
						{
							throw new CollectionException(String.format(
								"Coluna %s.%s no objeto 'fields' da view %s não encontrada", name, nomeProp, view.getName()));
						}
					}
				}
			}
		}
	}

	private void validarRefsNoSchema(
		final Workspace workspace, 
		final Map<String, Ref> referencias) 
	{
		if(referencias == null)
		{
			return;
		}

		for(var entry : referencias.entrySet())
		{
			var name = entry.getKey();
			var referencia = entry.getValue();

			var ref = (CollectionWithSchema)collectionRepo
				.findByPubIdAndWorkspace(referencia.getCollection(), workspace.getId());
			if(ref == null)
			{
				throw new CollectionException(String.format(
					"Coleção %s referenciada em refs não encontrada no área de trabalho", name));
			}
			
			var colunasRef = ref.getSchemaObj().getColumns();

			if(referencia.getFilters() != null)
			{
				for(var filtro : referencia.getFilters())
				{
					if(!colunaExisteNoSchema(colunasRef, null, null, filtro.getColumn()))
					{
						throw new CollectionException(String.format(
							"Coluna %s em refs[%s].filters[] não encontrada na tabela referenciada", filtro.getColumn(), name));
					}
				}
			}
			
			if(referencia.getPreview() != null)
			{
				for(var coluna : referencia.getPreview().getColumns())
				{
					if(!colunaExisteNoSchema(colunasRef, null, null, coluna))
					{
						throw new CollectionException(String.format(
							"Coluna %s em refs[%s].preview.columns[] não encontrada na tabela referenciada", coluna, name));
					}
				}
			}
		}
	}

	private void validarAuthNoSchema(
		final CollectionSchema schema,
		final Auth auth) 
	{
		if(auth == null)
		{
			return;
		}

		if(auth.getRead() != null)
		{
			verificarAcaoAcesso(schema, auth.getRead(), "read");
		}
		
		if(auth.getCreate() != null)
		{
			verificarAcaoAcesso(schema, auth.getCreate(), "create");
		}
		
		if(auth.getEdit() != null)
		{
			verificarAcaoAcesso(schema, auth.getEdit(), "edit");
		}
	}

	private void validarRelatoriosNoSchema(
		final CollectionSchema schema, 
		final List<Report> reports,
		final Map<String, Field> colunas, 
		final Map<String, Ref> referencias, 
		final Map<String, Klass> classes) 
	{
		if(reports == null)
		{
			return;
		}

		for(var relatorio : reports)
		{
			if(reports.stream()
				.anyMatch(r -> r != relatorio && r.getId().equals(relatorio.getId())))
			{
				throw new CollectionException(String.format(
					"Há mais de um relatório com o mesmo id: %s", relatorio.getId()));
			}
			
			if(reports.stream()
				.anyMatch(r -> r != relatorio && r.getName().equals(relatorio.getName())))
			{
				throw new CollectionException(String.format(
					"Há mais de um relatório com o mesmo name: %s", relatorio.getName()));
			}
			
			var filter = relatorio.getFilter();
			if(filter != null)
			{
				var form = filter.getForm();
				if(form != null)
				{
					for(var entry: form.getFields().entrySet())
					{
						var coluna = entry.getKey();
						var filtro = entry.getValue();
						if(!filtro.isTemplate())
						{
							if(!colunaExisteNoSchema(colunas, referencias, classes, coluna))
							{
								throw new CollectionException(String.format(
									"Campo %s em filter.fields do relatório %s não encontrado na tabela", coluna, relatorio.getName()));
							}
						}
					}
				}
				
				var fields = filter.getFields();
				if(fields != null)
				{
					var constantes = relatorio.getConsts();
					for(var coluna: fields.keySet())
					{
						if(!colunaExisteNoSchema(colunas, referencias, classes, coluna))
						{
							if(constantes == null || !constantes.containsKey(coluna))
							{
								throw new CollectionException(String.format(
									"Campo %s em filter.columns do relatório %s não encontrado na tabela ou nas constantes", coluna, relatorio.getName()));
							}
						}
					}
				}
			}
		}
	}

	private void validarIndicesNoSchema(
		final CollectionSchema schema, 
		final List<Index> indexes, 
		final Map<String, Field> colunas,
		final Map<String, Ref> referencias,
		final Map<String, Klass> classes) 
	{
		if(indexes == null)
		{
			return;
		}

		for(var indice : indexes)
		{
			for(var coluna: indice.getColumns())
			{
				if(!colunaExisteNoSchema(colunas, referencias, classes, coluna))
				{
					throw new CollectionException(String.format(
						"Coluna %s em indexes não encontrada na tabela", coluna));
				}
			}
		}

		validarIndicesObrigatoriosNoSchema(null, indexes, colunas, classes);
	}

	private void validarIndicesObrigatoriosNoSchema(
		final String parentKey,
		final List<Index> indexes, 
		final Map<String, Field> colunas,
		final Map<String, Klass> classes) 
	{
		for(var entry : colunas.entrySet())
		{
			var key = parentKey != null? 
				parentKey + '.' + entry.getKey():
				entry.getKey();
			var col = entry.getValue();
			
			if(col.isPositional() || col.isSortable() || col.isUnique())
			{
				if(!indexes.stream().anyMatch(i -> i.getColumns().contains(key)))
				{
					throw new CollectionException(String.format(
						"Coluna %s é do type posicional, indexável e/ou única, mas não há índice que a contenha", key));
				}
			}

			if(col.getClass_() != null)
			{
				var klass = classes.get(col.getClass_());
				validarIndicesObrigatoriosNoSchema(key, indexes, klass.getProps(), classes);
			}
		}
	}

	private List<String> getAutoIds(
		final Map<String, Field> columns) 
	{
		return columns
			.entrySet()
				.stream()
					.filter(c -> c.getValue().isAuto())
						.map(Map.Entry::getKey)
							.collect(Collectors.toList());
	}

	private List<String> getPosIds(
		final Map<String, Field> columns)
	{
		return columns
			.entrySet()
				.stream()
					.filter(c -> c.getValue().isPositional())
						.map(Map.Entry::getKey)
							.collect(Collectors.toList());		
	}

	private void validarColunasNoSchema(
		final CollectionSchema schema,
		final Map<String, Field> colunas, 
		final Map<String, Klass> classes, 
		final Map<String, Ref> referencias,
		final List<String> autoIds,
		final List<String> posIds)
	{
		// verificar se colunas autogen são chave, do type numérico, etc
		String autoId = null;
		if(autoIds != null && autoIds.size() > 0)
		{
			if(autoIds.size() > 1)
			{
				throw new CollectionException("Somente uma coluna pode ser do type auto");
			}

			autoId = autoIds.get(0);
						
			var coluna = colunas.get(autoId);
			if(coluna.getType() != FieldType.number)
			{
				throw new CollectionException("Coluna auto deve ser to type numérico");
			}
		}

		// verificar colunas posicional
		String posId = null;
		if(posIds != null && posIds.size() > 0)
		{
			if(posIds.size() > 1)
			{
				throw new CollectionException("Somente uma coluna pode ser do type posicional");
			}

			posId = posIds.get(0);

			var coluna = colunas.get(posId);
			if(coluna.getType() != FieldType.number)
			{
				throw new CollectionException("Coluna posicional deve ser to type numérico");
			}
			
		}
		
		if(autoId != null && posId != null && autoId.equals(posId))
		{
			throw new CollectionException("Coluna auto não pode ser ao mesmo tempo posicional");
		}

		// validar colunas da tabela
		for(var entry: colunas.entrySet())
		{
			var key = entry.getKey();
			var col = entry.getValue();

			validateColumnKey(key);

			if(col.getSubtype() != null)
			{
				if(col.getType() != FieldType.array)
				{
					throw new CollectionException(String.format(
						"Coluna %s não pode conter subtipo", key));
				}
			}

			// verificar para coluna do type objeto se a classe existe 
			if(col.getType() == FieldType.object || col.getSubtype() == FieldType.object)
			{
				var klass = col.getClass_();
				if(klass == null)
				{
					throw new CollectionException(String.format(
						"Coluna %s do type objeto/objeto[] deve referenciar uma classe", key));
				}
				
				if(classes == null || !classes.containsKey(klass))
				{
					throw new CollectionException(String.format(
						"Coluna %s referencia a classe inexistente %s", key, klass));
				}
			}
			else
			{
				if(col.getClass_() != null)
				{
					throw new CollectionException(String.format(
						"Coluna %s define uma classe, mas não é do type objeto/objeto[]", key));
				}
			}

			if(col.getOptions() != null && (col.getType() == null || col.getType() != FieldType.enumeration))
			{
				throw new CollectionException(String.format(
					"Coluna %s não deve possuir opções", key));
			}

			if(col.isNullable())
			{
				if(col.getDefault() != null)
				{
					throw new CollectionException(String.format(
						"Coluna %s pode ser vazia, portanto não deve possuir valor padrão", key));
				}
			}
			
			var dep = col.getDepends();
			if(dep != null)
			{
				validarDependsOnNoSchema(dep, key, colunas, classes, schema.getFlows());
			}

			if(col.getRef() != null)
			{
				var ref = col.getRef();
				if(referencias == null || !referencias.containsKey(ref.getName()))
				{
					throw new CollectionException(String.format(
						"Coluna %s possui referência inexistente %s", key, ref.getName()));
				}

				if(ref.getDisplay() != null)
				{
					var refObj = referencias.get(ref.getName());
					var colunaRef = ref.getDisplay();
					if(!refObj.getFilters().stream().anyMatch(filter -> filter.getColumn().equals(colunaRef)))
					{
						throw new CollectionException(String.format(
							"Coluna %s não encontrada na tabela referenciada na definição de %s", colunaRef, key));
					}
				}
			}

			if(col.getMethods() != null)
			{
				validarMetodosNoSchema(col.getMethods(), key);
			}
		}		
	}

	private void validarDependsOnNoSchema(
		final LogicalExpr<FieldDependency> cond,
		final String key,
		final Map<String, Field> colunas,
		final Map<String, Klass> classes,
		final Map<String, Flow> flows) 
	{
		if(cond.getCond() != null)
		{
			var dep = cond.getCond();
			var column = dep.getColumn();
			if(column != null)
			{
				if(!colunaExisteNoSchema(colunas, null, classes, column.getName()))
				{
					throw new CollectionException(String.format(
						"Coluna '%s' referencia dependência inexistente %s", key, column.getName()));
				}
			}
			else
			{
				var flow = dep.getFlow();
				if(flow == null)
				{
					throw new CollectionException(String.format(
						"Coluna '%s' não possui dependência definida", key));
				}

				if(flows == null || !flows.containsKey(flow.getName()))
				{
					throw new CollectionException(String.format(
						"Coluna '%s' referencia dependência inexistente %s", key, flow.getName()));
				}
			}
		}
		else
		{
			var list = cond.getAnd() != null? 
				cond.getAnd():
				cond.getOr();

			if(list == null || list.size() == 0)
			{
				throw new CollectionException(String.format(
					"Coluna '%s' possui dependência sem condições definidas", key));
			}

			for(var item : list)
			{
				validarDependsOnNoSchema(
					item,
					key,
					colunas,
					classes,
					flows);
			}
		}
	}

	private void validarClassesNoSchema(
		final Map<String, Klass> classes) 
	{
		if(classes == null)
		{
			return;
		}
		
		for(var kentry : classes.entrySet())
		{
			var klass = kentry.getValue();
			var nomeClasse = kentry.getKey();

			if(klass.getProps() == null || klass.getProps().size() == 0)
			{
				throw new CollectionException(String.format(
					"Classe %s deve possuir ao menos uma propriedade", nomeClasse));
			}

			for(var entry: klass.getProps().entrySet())
			{
				var key = entry.getKey();
				var prop = entry.getValue();
				var label = prop.getLabel();

				validateColumnKey(key);

				// propriedades de classes não podem ser objetos, porque recursividade não é suportada
				if(prop.getType() == FieldType.object)
				{
					throw new CollectionException(String.format(
						"Campo %s da classe %s não pode ser do type objeto", label, nomeClasse));
				}

				// verificar opções inválidas para props de uma classe
				if(prop.getAuto() != null)
				{
					throw new CollectionException(String.format(
						"Campo %s da classe %s não deve ser auto", label, nomeClasse));
				}

				if(prop.getPositional() != null)
				{
					throw new CollectionException(String.format(
						"Campo %s da classe %s não deve ser posicional", label, nomeClasse));
				}

				if(prop.getClass_() != null)
				{
					throw new CollectionException(String.format(
						"Campo %s da classe %s não deve possuir uma classe", label, nomeClasse));
				}

				if(prop.getOptions() != null && (prop.getType() == null || prop.getType() != FieldType.enumeration))
				{
					throw new CollectionException(String.format(
						"Campo %s da classe %s não deve possuir opções", label, nomeClasse));
				}

				if(prop.isNullable())
				{
					if(prop.getDefault() != null)
					{
						throw new CollectionException(String.format(
							"Campo %s da classe %s pode ser vazio, portanto não deve possuir valor padrão", label, nomeClasse));
					}
				}

				if(prop.getDepends() != null)
				{
					throw new CollectionException(String.format(
						"Campo %s da classe %s não deve possuir dependência", label, nomeClasse));
				}

				if(prop.getRef() != null)
				{
					throw new CollectionException(String.format(
						"Campo %s da classe %s não deve possuir referência", label, nomeClasse));
				}

				if(prop.getMethods() != null)
				{
					validarMetodosNoSchema(prop.getMethods(), label);
				}
			}

			if(klass.getMethods() != null)
			{
				validarMetodosNoSchema(klass.getMethods(), nomeClasse);
			}
		}
	}

	private void validarMetodosNoSchema(
		final Map<String, Method> methods, 
		final String parentName)  
	{
		var nomesMeths = new HashSet<String>();

		for(var entry: methods.entrySet())
		{
			// verificar se há name duplicado
			var name = entry.getKey();
			if(nomesMeths.contains(name))
			{
				throw new CollectionException(String.format(
					"Método %s definido em %s duplicado", name, parentName));
			}
			nomesMeths.add(name);

			var method = entry.getValue();
			if(method.getScript() == null)
			{
				throw new CollectionException(String.format(
					"Método %s definido em %s deve conter um script", name, parentName));
			}
		}
	}
	
	private Collection criarOuAtualizar(
		final Collection collection, 
		final CollectionRequest req, 
		final User user,
		final Workspace workspace, 
		final boolean isDiff) 
	{
		Provider provider = null;
		
		if(req.getProvider() != null && req.getProvider().getId() != null)
		{
			provider = providerRepo
				.findByPubIdAndWorkspace(req.getProvider().getId(), workspace);
			if(provider == null)
			{
				throw new CollectionException("Provider not found");
			}
		}

		if(collection.getType() == CollectionType.SCHEMA)
		{
			var tbCollection = (CollectionWithSchema)collection;
			var schemaAtual = tbCollection.getSchema();
			
			var schema = isDiff && req.getSchema() != null? 
				applyPatch(schemaAtual, req.getSchema()):
				req.getSchema();
			
			var db = workspace.getPubId();
			
			if(schema != null)
			{
				// validar schema se o JSON foi alterado ou se novo
				var verificarSchema = schemaAtual != null && !schemaAtual.equals(schema);
				
				var val = validarSchema(schema, verificarSchema, workspace);

				// verificar se é necessário recriar índices
				var recriarIndices = true;
				if(verificarSchema)
				{
					if(collection.getId() != null)
					{
						recriarIndices = verificarIndices(tbCollection.getSchemaObj(), val.schema);
					}
				}

				// criar coleção?
				if (collection.getId() == null) 
				{
					noSqlService.createCollection(db, collection.getPubId());
				}

				if (verificarSchema && recriarIndices) 
				{
					criarIndices(db, collection.getPubId(), val.schema, collection.getId() != null);
				}

				if(verificarSchema)
				{
					updateSchema(tbCollection, schema, CollectionVersionChangeId.GENERIC, false);
				}
				else
				{
					tbCollection.setSchema(schema);
				}
				
				tbCollection.setSchemaObj(val.schema);

				//
				criarOuAtualizarAuto(db, tbCollection, val.autoId);
				tbCollection.setAutoGenId(val.autoId);
				criarOuAtualizarPosicional(db, tbCollection, val.posId);
				tbCollection.setPositionalId(val.posId);
			}

			tbCollection.setOptions(req.getOptions());
			tbCollection.setProvider(provider);
		}

		//
		collection.setName(req.getName());
		collection.setDesc(req.getDesc());
		collection.setIcon(req.getIcon());
		collection.setOrder(req.getOrder());
		if(req.isPublished())
		{
			if(collection.getPublishedAt() == null)
			{
				if(req.getOrder() == null || req.getIcon() == null || req.getIcon().isEmpty())
				{
					throw new CollectionException("Os campos ícone e order devem estar definidos para publicar uma Coleção");
				}
					
				collection.setPublishedAt(ZonedDateTime.now());
				collection.setPublishedBy(user);
			}
		}
		else
		{
			if(collection.getPublishedAt() != null)
			{
				collection.setPublishedAt(null);
				collection.setPublishedBy(null);
			}
		}

		//
		return collectionRepo.save(collection);
	}

	private void criarIndices(
		final String db, 
		final String name, 
		final CollectionSchema schema, 
		final boolean removerExistentes) 
	{
		if(removerExistentes) 
		{
			noSqlService.dropIndexes(db, name);
		}

		if (schema.getIndexes() != null) 
		{
			for (var indice : schema.getIndexes()) 
			{
				criarIndiceNoDB(db, name, indice);
			}
		}
	}

	private void criarIndiceNoDB(
		final String db, 
		final String name, 
		final Index index) 
	{
		if(index.getColumns().size() > 0)
		{
			noSqlService.createIndex(db, name, index.getColumns(),
					index.getDir() == FieldIndexDir.asc, index.isUnique());
		}
	}

	private void removerIndiceNoDB(
		final String db, 
		final String name, 
		final Index index)
	{
		if(index.getColumns().size() > 0)
		{
			noSqlService.dropIndex(
				db, 
				name, 
				index.getColumns(), 
				index.getDir() == FieldIndexDir.asc);
			}
	}

	private boolean verificarIndices(
		final CollectionSchema atualSchema, 
		final CollectionSchema novoSchema) 
	{
		var indicesAtuais = novoSchema.getIndexes();
		var indicesNovos = atualSchema.getIndexes();

		if (indicesAtuais == null) 
		{
			return indicesNovos != null;
		} 
		
		if (indicesNovos == null) 
		{
			return indicesAtuais != null;
		} 
		
		if (indicesAtuais.size() != indicesNovos.size()) 
		{
			return true;
		} 
		
		var igualCnt = 0;
		for (var atual : indicesAtuais) 
		{
			for (var novo : indicesNovos) 
			{
				if (atual.equals(novo)) 
				{
					++igualCnt;
					break;
				}
			}
		}

		return igualCnt != indicesAtuais.size();
	}

	private void criarAuto(
		final String db, 
		final Collection collection, 
		final String autoGenId)
	{
		// criar novo sequence
		if (autoGenId != null) 
		{
			noSqlService.createSequence(db, collection.getPubId());
		}
	}

	private void criarOuAtualizarAuto(
		final String db, 
		final CollectionWithSchema collection, 
		final String autoId)
	{
		var oldAutoId = collection.getAutoGenId();

		// remover sequence antigo se a chave mudou
		var createSeq = autoId != null;
		if (oldAutoId != null) 
		{
			if (autoId == null || !oldAutoId.equals(autoId)) 
			{
				noSqlService.deleteSequence(db, collection.getPubId());
			} 
			else 
			{
				createSeq = false;
			}
		}

		// criar novo sequence
		if (createSeq) 
		{
			noSqlService.createSequence(db, collection.getPubId());
		}
	}

	private void criarPosicional(
		final String db, 
		final Collection collection, 
		final String posId)
	{
		// criar novo sequence para a coluna posicional (nota: não podemos usar
		// max(posId), pois não há como dar lock na collection no MongoDB)
		if (posId != null) 
		{
			noSqlService.createSequence(db, collection.getPubId() + posicionalSeqSuffix);
		}
	}

	private void criarOuAtualizarPosicional(
		final String db, 
		final CollectionWithSchema collection, 
		final String posId)
	{
		var oldPosId = collection.getPositionalId();

		// remover sequence antigo se a coluna posicional mudou
		var createPos = posId != null;
		if (oldPosId != null) 
		{
			if (posId == null || !oldPosId.equals(posId)) 
			{
				noSqlService.deleteSequence(db, collection.getPubId() + posicionalSeqSuffix);
			} 
			else 
			{
				createPos = false;
			}
		}

		// criar novo sequence para a coluna posicional (nota: não podemos usar
		// max(posId), pois não há como dar lock na collection no MongoDB)
		if (createPos) 
		{
			noSqlService.createSequence(db, collection.getPubId() + posicionalSeqSuffix);
		}		
	}
	
	public static boolean colunaExisteNoSchema(
		final Map<String, Field> colunas,
		final Map<String, Ref> referencias,
		final Map<String, Klass> classes, 
		final String name) 
	{
		var p = name.indexOf('.');
		if(p < 0)
		{
			if(colunas.containsKey(name))
			{
				return true;
			}
			
			return referencias != null && referencias.containsKey(name);
		}
		
		var nomeCol = name.substring(0, p);
		if(!colunas.containsKey(nomeCol))
		{
			return referencias != null && referencias.containsKey(nomeCol);
		}
		
		var coluna = colunas.get(nomeCol);  
		if(coluna.getType() != FieldType.object && 
			!(coluna.getType() == FieldType.array && 
				coluna.getSubtype() == FieldType.object))
		{
			return false;
		}

		if(coluna.getClass_() == null)
		{
			return false;
		}

		var nomeProp = name.substring(p+1);
		return propExisteNaClasse(coluna, nomeProp, classes);
	}

	public static boolean propExisteNaClasse( 
		final Field coluna,
		final String name,
		final Map<String, Klass> classes)
	{
		var nomeClass = coluna.getClass_();
		if(classes == null || !classes.containsKey(nomeClass))
		{
			return false;
		}
		
		var klass = classes.get(nomeClass);
		
		return klass.getProps() != null && klass.getProps().containsKey(name);
	}

	private void verificarAcaoAcesso(
		final CollectionSchema schema, 
		final LogicalExpr<AuthAction> cond, 
		final String sessao) 
	{
		if(cond.getCond() != null)
		{
			var acao = cond.getCond();
			switch(acao.getWhen())
			{
			case USER_SAME:
				var userAcesso = acao.getUser();
				if(userAcesso != null)
				{
					verificarColunasUserAcesso(schema, userAcesso, sessao);
				}
				break;

			case COLUMN_VALUE:
				if(sessao.equals("create"))
				{
					throw new CollectionException("Condição não suportada na autorização de criação");
				}
				
				var coluna = acao.getColumn().getName();
				if(!colunaExisteNoSchema(
					schema.getColumns(), schema.getRefs(), schema.getClasses(), coluna))
				{
					throw new CollectionException(String.format(
						"Coluna %s em auth.%s não encontrada na tabela", coluna, sessao));
				}
				break;

			default:
				throw new CollectionException("Condição não suportada na autorização");
			}
		}
		else
		{
			var list = cond.getAnd() != null?
				cond.getAnd():
				cond.getOr();

			for(var node : list)
			{
				verificarAcaoAcesso(schema, node, sessao);
			}
		}
	}

	private void verificarColunasUserAcesso(
		final CollectionSchema schema, 
		final AuthUser authUser, 
		final String sessao) 
	{
		var colunas = schema.getColumns();
		var referencias = schema.getRefs();
		var classes = schema.getClasses();
		
		for(var coluna : authUser.getColumns())
		{
			if(!colunaExisteNoSchema(colunas, referencias, classes, coluna))
			{
				throw new CollectionException(String.format(
					"Coluna %s em auth.%s.columns não encontrada na tabela", coluna, sessao));
			}
		}
	}
	
	/**
	 * 
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	public Collection criar(
		final @Valid CollectionRequest req, 
		final User user, 
		final Workspace workspace) 
	{
		var collection = new CollectionWithSchema();
		
		collection.setWorspace(workspace);
		collection.setCreatedAt(ZonedDateTime.now());
		collection.setCreatedBy(user);
		var res = criarOuAtualizar(collection, req, user, workspace, false);

		var cup = criarAuthInicial(user, res);
		res.setAuthorizations(Set.of(cup));

		collectionLogger.info(collection, user, "Criou coleção");

		return res;
	}

	private CollectionAuth criarAuthInicial(
		final User user, 
		final Collection collection) 
	{
		var cup = new CollectionAuth(collection, user, CollectionAuthRole.CREATOR);
		cup.setCreatedAt(collection.getCreatedAt());
		cup.setCreatedBy(collection.getCreatedBy());
		return collectionAuthRepo.save(cup);
	}

	/**
	 * 
	 * @param collection
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	public Collection atualizar(
		final Collection collection, 
		final @Valid CollectionRequest req, 
		final User user, Workspace workspace, 
		final String sessionId) 
	{
		if(collection.getPublishedAt() != null)
		{
			if(req.getOrder() == null || 
				(collection.getType() == CollectionType.SCHEMA && (req.getIcon() == null || req.getIcon().isEmpty())))
			{
				throw new CollectionException("Coleção publicada deve ter os campos ícone e order definidos");
			}
		}
		
		collection.setUpdatedAt(ZonedDateTime.now());
		collection.setUpdatedBy(user);

		var recarregarMenu = 
			(!req.getName().equals(collection.getName())) ||
			((req.getIcon() != null && collection.getIcon() == null) || 
				(req.getIcon() == null && collection.getIcon() != null) || 
				(req.getIcon() != null && !req.getIcon().equals(collection.getIcon()))) ||
			((req.getOrder() != null && collection.getOrder() == null) || 
				(req.getOrder() == null && collection.getOrder() != null) || 
				(req.getOrder() != null && req.getOrder() != collection.getOrder())) ||
			((req.isPublished() && collection.getPublishedAt() == null) || 
				(!req.isPublished() && collection.getPublishedAt() != null));

		var res = criarOuAtualizar(collection, req, user, workspace, true);

		var schemaMudou = req.getSchema() != null;

		broadcast(
			collection, 
			schemaMudou? 
				CollectionUpdatedEvent.SCHEMA_UPDATED: 
				CollectionUpdatedEvent.PROPS_UPDATED, 
			sessionId, 
			schemaMudou? ((CollectionWithSchema)res).getSchema(): null, 
			recarregarMenu);

		collectionLogger.info(collection, user, "Atualizou coleção");
			
		return res;
	}

	private String schemaToString(
		final CollectionSchema schema) 
	{
		try 
		{
			return objectMapper.writeValueAsString(schema);
		} 
		catch (JsonProcessingException e) 
		{
			return null;
		}
	}

	/**
	 * 
	 * @param collection
	 * @param withData
	 * @param user
	 * @param workspace
	 * @return
	 */
	@Transactional
	public CollectionWithSchema duplicar(
		final CollectionWithSchema collection, 
		final EnumSet<CollectionDupOptions> with,
		final User user, 
		final Workspace workspace) 
	{
		var db = workspace.getPubId();

		var collectionDup = new CollectionWithSchema(collection, user, with);

		collectionDup.setName(gerarNomeUnico(collectionDup, collection.getName(), "Cópia", workspace));

		noSqlService.createCollection(db, collectionDup.getPubId());

		criarIndices(db, collectionDup.getPubId(), collectionDup.getSchemaObj(), false);
		criarAuto(db, collectionDup, collectionDup.getAutoGenId());
		criarPosicional(db, collectionDup, collectionDup.getPositionalId());

		collectionDup = collectionRepo.save(collectionDup);

		if(collectionDup.getAuthorizations() != null)
		{
			for(var cup : collectionDup.getAuthorizations())
			{
				cup.setCollection(collectionDup);
				collectionAuthRepo.save(cup);
			}
		}
		
		if(collectionDup.getIntegrations() != null)
		{
			for(var integration : collectionDup.getIntegrations())
			{
				integration.setCollection(collectionDup);
				collectionIntegrationRepo.save(integration);
			}
		}

		if(collectionDup.getAutomations() != null)
		{
			for(var automation : collectionDup.getAutomations())
			{
				automation.setCollection(collectionDup);
				collectionAutomationRepo.save(automation);
			}
		}

		if(collectionDup.getAuxs() != null)
		{
			for(var aux : collectionDup.getAuxs())
			{
				collectionRepo.insertAuxAt(collectionDup.getId(), aux.getAux().getId(), aux.getOrder());
			}
		}

		if(with.contains(CollectionDupOptions.DATA))
		{
			noSqlService.dupData(db, collection.getPubId(), collectionDup.getPubId());
		}

		//FIXME: auto e posicional devem usar os valores max() que estão no DB

		collectionLogger.info(
			collectionDup, 
			user, 
			"Duplicou coleção",  
			Map.of("id", collection.getPubId()));

		return collectionDup;
	}

	/**
	 * 
	 * @param principal
	 * @param aux
	 * @param with
	 * @param user
	 * @param workspace
	 * @return
	 */
	@Transactional
	public CollectionWithSchema duplicarAux(
		final CollectionWithSchema principal, 
		final CollectionWithSchema aux,
		final EnumSet<CollectionDupOptions> with,
		final Integer position,
		final User user, 
		final Workspace workspace) 
	{
		var dup = duplicar(aux, with, user, workspace);

		collectionRepo.updateAuxsOrder(principal.getId(), position, 1);
		collectionRepo.insertAuxAt(principal.getId(), dup.getId(), position);

		return dup;
	}

	/**
	 * 
	 * @param template
	 * @param user
	 * @param workspace
	 * @return
	 */
	public CollectionWithSchema gerar(
		final CollectionTemplate template, 
		final String idParent, 
		final boolean published, 
		final User user, 
		final Workspace workspace) 
	{
		var db = workspace.getPubId();

		var collection = new CollectionWithSchema();

		var res = validarSchema(template.getSchema(), true, workspace);

		collection.setParent(idParent != null? 
			collectionRepo.findByPubIdAndWorkspace(idParent, workspace.getId()): 
			null);
		collection.setWorspace(workspace);
		collection.setName(gerarNomeUnico(collection, template.getName(), "Novo", workspace));
		collection.setAutoGenId(res.autoId);
		collection.setPositionalId(res.posId);
		collection.setIcon(template.getIcon());
		collection.setOrder((short)99);
		collection.setSchema(template.getSchema());
		collection.setSchemaObj(res.schema);
		collection.setOptions(EnumSet.of(CollectionOptions.NONE));
		collection.setCreatedBy(user);
		collection.setCreatedAt(ZonedDateTime.now());
		if(published)
		{
			collection.setPublishedBy(user);
			collection.setPublishedAt(ZonedDateTime.now());
		}
		
		var internalStorageProvider = providerRepo
			.findByNameAndWorkspace(ProviderUtil.getInternalStorageProviderName(), workspace);
		if(internalStorageProvider == null)
		{
			throw new CollectionException("Internal storage provider not found");
		}

		collection.setProvider(internalStorageProvider);
		
		noSqlService.createCollection(db, collection.getPubId());

		criarIndices(db, collection.getPubId(), collection.getSchemaObj(), false);
		criarAuto(db, collection, collection.getAutoGenId());
		criarPosicional(db, collection, collection.getPositionalId());
		
		collection = collectionRepo.save(collection);

		var cup = criarAuthInicial(user, collection);
		collection.setAuthorizations(Set.of(cup));

		collectionLogger.info(
			collection, 
			user, 
			"Criou coleção",
			Map.of("templateId", template.getPubId()));

		return collection;
	}

	/**
	 * 
	 * @param idParent
	 * @param published
	 * @param user
	 * @param workspace
	 * @return
	 */
	public Collection gerarPasta(
		final String idParent, 
		final boolean published, 
		final User user, 
		final Workspace workspace) 
	{
		var collection = new Collection(CollectionType.FOLDER);

		collection.setParent(idParent != null? 
			collectionRepo.findByPubIdAndWorkspace(idParent, workspace.getId()): 
			null);
		collection.setWorspace(workspace);
		collection.setName(gerarNomeUnico(collection, "Pasta", "Nova", workspace));
		collection.setIcon("folder");
		collection.setOrder((short)0);
		collection.setCreatedBy(user);
		collection.setCreatedAt(ZonedDateTime.now());
		if(published)
		{
			collection.setPublishedBy(user);
			collection.setPublishedAt(ZonedDateTime.now());
		}
		
		collection = collectionRepo.save(collection);

		var cup = criarAuthInicial(user, collection);
		collection.setAuthorizations(Set.of(cup));

		collectionLogger.info(
			collection, user, "Criou pasta");

		return collection;
	}

	/**
	 * 
	 * @param template
	 * @param idParent
	 * @param published
	 * @param user
	 * @param workspace
	 * @return
	 */
	public Collection gerarAux(
		final CollectionWithSchema collection,
		final CollectionTemplate template, 
		final Integer position,
		final boolean published, 
		final User user, 
		final Workspace workspace) 
	{
		var idParent = collection.getParent() != null? 
			collection.getParent().getPubId(): 
			null;

		var aux = gerar(template, idParent, published, user, workspace);

		if(position != null)
		{
			collectionRepo.updateAuxsOrder(collection.getId(), position, 1);
			collectionRepo.insertAuxAt(collection.getId(), aux.getId(), position);
		}
		else
		{
			collectionRepo.insertAux(collection.getId(), aux.getId());
		}

		collectionLogger.info(
			collection, 
			user, 
			"Criou coleção aux", 
			Map.of("id", aux.getPubId()));

		return aux;
	}

	private String gerarNomeUnico(
		final Collection collection, 
		final String name, 
		final String sufixo, 
		final Workspace workspace) 
	{
		var parent = collection.getParent();

		var cnt = 1;
		while(true)
		{
			var nomeCompleto = String.format("%s-%s-%03d", name, sufixo, cnt++);
			if(collectionRepo.findByNameAndWorkspace(nomeCompleto, workspace.getId(), parent.getId()) == null)
			{
				return nomeCompleto;
			}
		}
	}

	/**
	 * 
	 * @param collection
	 * @param user
	 */
	public void apagar(
		final Collection collection, 
		final User user)
	{
		if(collection.getType() == CollectionType.SCHEMA)
		{
			var db = collection.getWorkspace().getPubId();
			noSqlService.dropCollection(db, collection.getPubId());
		}
		else
		{
			var children = collection.getChildren();
			for(var child : children)
			{
				apagar(child, user);
			}
		}

		workspaceLogger.info(
			collection.getWorkspace(), 
			user, 
			"Removeu coleção", 
			Map.of("id", collection.getPubId()));

		collectionRepo.delete(collection);
	}

	/**
	 * 
	 * @param principal
	 * @param collection
	 * @param user
	 */
	public void apagarAux(
		final Collection principal, 
		final Collection collection, 
		final User user)
	{
		apagar(collection, user);

		//NOTA: não é necessário remover da tabela relacional, pois as FK's são ON DELETE CASCADE
	}

	/**
	 * 
	 * @param collection
	 * @param posNova
	 * @return
	 */
	public Collection mover(
		final Collection collection, 
		final short posNova,
		final User user)
	{
		var posAtual = (short)(collection.getOrder() == null? 0: collection.getOrder());
		
		if(posAtual == posNova)
		{
			return collection;
		}
		
		var first = posNova;
		var qtd = (short)(posAtual > posNova? +1: -1);
		var last = (short)(posAtual + (posAtual > posNova? -1: +1));
		if(first > last)
		{
			var temp = first;
			first = last;
			last = temp;
		}

		collectionRepo.updatePosicao(first, last, qtd, collection.getWorkspace());

		collectionLogger.info(
			collection, user, "Moveu coleção");

		collection.setOrder(posNova);
		return collectionRepo.save(collection);
	}

	/**
	 * 
	 * @param principal
	 * @param aux
	 * @param pos
	 * @return
	 */
	public void moverAux(
		final CollectionWithSchema principal, 
		final CollectionWithSchema aux, 
		final MoveDirections dir,
		final User user) 
	{
		var order = collectionRepo.findAllAuxsOrdersByCollection(principal.getId());

		var from = order.stream()
			.filter(o -> o.getId() == aux.getId())
				.findFirst().get();
		var curPos = from.getOrder();
		
		var inc = (dir == MoveDirections.LEFT || dir == MoveDirections.FIRST? -1: 1);
		var to = order.get(dir == MoveDirections.FIRST? 
			0:
			dir == MoveDirections.LAST?
				order.size()-1:
				IntStream.range(0, order.size())
					.filter(i -> order.get(i).getId() == aux.getId())
						.findFirst()
							.getAsInt() + inc);
		var newPos = to.getOrder() + inc;

		if(curPos == newPos)
		{
			return;
		}
		
		var first = newPos;
		var qtd = (curPos > newPos? +1: -1);
		var last = (curPos + (curPos > newPos? -1: +1));
		if(first > last)
		{
			var temp = first;
			first = last;
			last = temp;
		}

		collectionRepo.updateAuxsOrder(principal.getId(), aux.getId(), first, last, qtd);
		collectionRepo.updateAuxOrder(principal.getId(), aux.getId(), newPos);

		collectionLogger.info(
			principal, 
			user, 
			"Moveu coleção aux", 
			Map.of("id", aux.getPubId()));
	}

	/**
	 * 
	 * @param collection
	 * @param req
	 * @param workspace
	 * @return
	 */
	public Pair<CollectionWithSchema, String> atualizarSchemaDiff(
		final CollectionWithSchema collection, 
		final CollectionSchemaDiffRequest req, 
		final User user,
		final Workspace workspace,
		final String sessionId) 
	{
		var curSchema = collection.getSchema();
		var newSchema = applyPatch(curSchema, req.getDiff());
		
		return atualizarSchema(collection, newSchema, user, workspace, sessionId);
	}

	/**
	 * 
	 * @param collection
	 * @param workspace
	 * @param sessionId
	 * @param schema
	 * @return
	 */
	public Pair<CollectionWithSchema, String> atualizarSchema(
		final CollectionWithSchema collection, 
		final String schema,
		final User user,
		final Workspace workspace, 
		final String sessionId) 
	{
		return atualizarSchema(collection, schema, user, workspace, sessionId, false);
	}

	private Pair<CollectionWithSchema, String> atualizarSchema(
		final CollectionWithSchema collection, 
		final String schema,
		final User user,
		final Workspace workspace, 
		final String sessionId,
		final boolean reversing) 
	{
		var db = workspace.getPubId();

		var val = validarSchema(schema, true, workspace);

		// verificar se é necessário recriar índices
		var recriarIndices = verificarIndices(collection.getSchemaObj(), val.schema);
		if (recriarIndices) 
		{
			criarIndices(db, collection.getPubId(), val.schema, true);
		}

		updateSchema(collection, schema, CollectionVersionChangeId.GENERIC, reversing);
		collection.setSchemaObj(val.schema);

		//
		criarOuAtualizarAuto(db, collection, val.autoId);
		collection.setAutoGenId(val.autoId);
		criarOuAtualizarPosicional(db, collection, val.posId);
		collection.setPositionalId(val.posId);
		
		//
		var res = collectionRepo.save(collection);

		broadcast(res, CollectionUpdatedEvent.SCHEMA_UPDATED, sessionId, schema, null);

		collectionLogger.info(
			collection, user, "Atualizou schema");

		return new Pair<>(res, schema);
	}

	/**
	 * 
	 * @param collection
	 * @param user
	 * @return
	 */
	public Collection publicar(
		final Collection collection, 
		final User user, 
		final Workspace workspace) 
	{
		collection.setPublishedAt(ZonedDateTime.now());
		collection.setPublishedBy(user);

		collectionLogger.info(
			collection, user, "Publicou coleção");
		
		return collectionRepo.save(collection);
	}
	
	/**
	 * 
	 * @param collection
	 * @param user
	 * @return
	 */
	public Collection despublicar(
		final Collection collection, 
		final User user) 
	{
		collection.setPublishedAt(null);
		collection.setPublishedBy(null);

		collectionLogger.info(
			collection, user, "Despublicou coleção");
		
		var res = collectionRepo.save(collection);

		return res;
	}
	
	private Map<String, Object> transformValuesForInsert(
		final CollectionWithSchema collection,
		final Map<String, Object> vars, 
		final Map<String, Field> columns, 
		final EvalContext scriptContext,
		final boolean checkDisabled,
		final User user,
		final boolean asAdmin)
	{
		var db = collection.getWorkspace().getPubId();
		var classes = collection.getSchemaObj().getClasses();
		var values = new HashMap<String, Object>();
		
		for(var entry : columns.entrySet())
		{
			var name = entry.getKey();
			var field = entry.getValue();
			Object value = null; 
			if(vars.containsKey(name))
			{
				var include = true;
				if(checkDisabled)
				{
					include = !isDisabledForInsert(field, name, user, asAdmin);
				}
				
				if(include)
				{
					value = vars.get(name);
					
					if(field.getType() == FieldType.object)
					{
						var klass = classes.get(field.getClass_());
						
						if(!(value instanceof Map<?,?>))
						{
							throw new CollectionException(String.format("Coluna %s não é do type objeto", name));
						}
						
						@SuppressWarnings("unchecked")
						var valorMap = ((Map<String, Object>)value);
						values.put(name, 
								transformValuesForInsert(
									collection, 
									valorMap, 
									klass.getProps(), 
									scriptContext,
									checkDisabled, 
									user, 
									asAdmin));
					}
				}
			}
			
			if(value == null)
			{
				if(field.isAuto())
				{
					value = noSqlService.autoGen(db, collection.getPubId());
				}
				else if(field.isPositional())
				{
					value = noSqlService.autoGen(db, collection.getPubId() + posicionalSeqSuffix);
				}
				else if(field.getDefault() != null)
				{
					value = evalValueOrScriptOrFunction(field.getDefault(), scriptContext, null);
				}			
			}
			
			if(value != null)
			{
				if(field.getRef() == null)
				{
					value = converterParaTipo(value, field.getType());
				}
				else
				{
					value = noSqlService.toObjectId(value);
				}
			}

			values.put(name, value);
		}
		
		return values;
	}

	/**
	 * 
	 * @param collection
	 * @param view
	 * @param scriptContext
	 * @param values
	 * @param verificarDesabilitados
	 * @param validarColunas
	 * @param user
	 * @param asAdmin
	 * @return
	 */
	public Map<String, Object> inserirItem(
		final CollectionWithSchema collection, 
		final EvalContext scriptContext, 
		final Map<String, Object> values, 
		final boolean verificarDesabilitados, 
		final boolean validarColunas,
		final User user,
		final boolean asAdmin,
		final String sessionId,
		final boolean isActivity)
	{
		var db = collection.getWorkspace().getPubId();
		var schema = collection.getSchemaObj();
		var fields = schema.getColumns();
		
		var prepared = transformValuesForInsert(
			collection, values, fields, scriptContext, verificarDesabilitados, user, asAdmin);

		var meta = getMeta(null);

		// executar automations
		if(!isActivity)
		{
			var automations = getAutomations(
				collection, 
				CollectionAutomationType.ITEM, 
				EnumSet.of(CollectionAutomationTrigger.ITEM_INSERTED));
			if(automations != null)
			{
				try
				{
					execAutomations(
						collection, fields, null, null, scriptContext, automations, prepared, meta);
				}
				catch (Exception e) 
				{
					throw new CollectionException(String.format(
						"Erro ao executar automações: %s", e.getMessage()));
				}
			}
		}

		// validar valores
		if(validarColunas)
		{
			for(var entry : prepared.entrySet())
			{
				var name = entry.getKey();
				validarColuna(collection, name, fields.get(name), entry.getValue(), null);
			}
		}
		
		//
		if(!isActivity)
		{
			execFlows(
				collection, user, null, prepared, scriptContext, meta);
		}
		
		//
		setMeta(prepared, meta);
		var id = noSqlService.insert(db, collection.getPubId(), prepared);
		prepared.putIfAbsent(ID_NAME, id);

		//
		if(!isActivity)
		{
			collectionItemLogger.info(
				collection, id, user, "Created item");
		}

		//
		broadcast(collection, CollectionUpdatedEvent.ITEM_CREATED, sessionId, null, id);

		return prepared;
	}

	private void broadcast(
		final Collection collection, 
		final CollectionUpdatedEvent type, 
		final String origem, 
		final String schema, 
		final Object extra)
	{
		var mensagem = new CollectionUpdatedMessage(type, origem, schema, extra);
		try 
		{
			stompQueueService.enviar(
				RabbitQueueService.AMQP_TOPIC_EXCHANGE, 
				String.format("collections.%s", collection.getPubId()), 
				mensagem);
		} 
		catch (Exception e) 
		{
			logger.error("Falha ao enviar atualização da Coleção", e);
		}
	}

	private Map<String, Object> transformValuesForUpdate(
		final CollectionWithSchema collection,
		final CollectionSchema schema, 
		final Map<String, Object> values, 
		final Map<String, Object> item, 
		final Map<String, Field> fields, 
		final EvalContext scriptContext,
		final User user,
		final boolean asAdmin)
	{
		var classes = schema.getClasses();
		var res = new HashMap<String, Object>();
		
		for(var entry : values.entrySet())
		{
			var name = entry.getKey();
			if(fields.containsKey(name))
			{
				var field = fields.get(name);
				var value = entry.getValue();
				
				var include = !isDisabledForUpdate(
					collection, field, item, name, value, user, asAdmin);
				
				if(include && field.getDepends() != null)
				{
					include = isDependencyResolvedForUpdate(collection, schema, field, item);
				}
				
				if(include)
				{
					if(value == null)
					{
						if(field.getDefault() != null)
						{
							value = evalValueOrScriptOrFunction(
								field.getDefault(), scriptContext, null);
						}
					}

					if(field.getType() == FieldType.object)
					{
						var klass = classes.get(field.getClass_());
						
						if(!(value instanceof Map<?,?>))
						{
							throw new CollectionException(String.format("Coluna %s não é do type objeto", name));
						}
						
						@SuppressWarnings("unchecked")
						var valorMap = ((Map<String, Object>)value);
						res.put(
							name, 
							transformValuesForUpdate(
								collection,
								schema, 
								valorMap, 
								item, 
								klass.getProps(), 
								scriptContext,
								user, 
								asAdmin));
					}
					else
					{
						if(value != null)
						{
							if(field.getRef() == null)
							{
								value = converterParaTipo(value, field.getType());
							}
							else
							{
								value = noSqlService.toObjectId(value);
							}
						}

						res.put(name, value);
					}
				}
			}
		}

		return res;
	}

	public Map<String, Object> atualizarItem(
		final CollectionWithSchema collection,
		final String id,
		final Map<String, Object> item, 
		final EvalContext scriptContext, 
		final Map<String, Object> values,
		final User user,
		final boolean asAdmin,
		final String sessionId,
		final boolean isActivity)
	{
		var db = collection.getWorkspace().getPubId();
		var schema = collection.getSchemaObj();
		var fields = schema.getColumns();
		
		var meta = getMeta(item);

		var prepared = transformValuesForUpdate(
			collection, schema, values, item, fields, scriptContext, user, asAdmin);
		
		if(!isActivity)
		{
			// executar automações
			var automations = getAutomations(
				collection, 
				CollectionAutomationType.FIELD, 
				EnumSet.of(CollectionAutomationTrigger.FIELD_UPDATED_TO));
			if(automations != null)
			{
				try
				{
					execAutomations(
						collection, fields, id, item, scriptContext, automations, prepared, meta);
				}
				catch (Exception e) 
				{
					throw new CollectionException(String.format("Erro ao executar automações: %s", e.getMessage()));
				}
			}
		}

		// validar valores
		for(var entry : prepared.entrySet())
		{
			var name = entry.getKey();
			validarColuna(collection, name, fields.get(name), entry.getValue(), prepared);
		}

		//
		if(!isActivity)
		{
			execFlows(
				collection, user, item, prepared, scriptContext, meta);
		}
		
		//
		setMeta(prepared, meta);
		noSqlService.update(db, collection.getPubId(), id, prepared);

		if(!isActivity)
		{
			// tabela possui referências? se houve mudança em alguma chave local, é necessário carregar todas as referências novamente
			var recarregarItem = false;
			if(schema.getRefs() != null)
			{
				for(var entry: prepared.entrySet())
				{
					var key = entry.getKey();
					var column = schema.getColumns().get(key);
					if(column != null && column.getRef() != null)
					{
						var atual = (ObjectId)item.get(key);
						var novo = (ObjectId)prepared.get(key);
						if(novo != null)
						{
							if(atual == null || !atual.equals(novo))
							{
								recarregarItem = true;
								break;
							}
						}
					}
				}
				
				if(recarregarItem)
				{
					prepared = findItemById(collection, id, null, true);
				}
			}

			if(!recarregarItem)
			{
				prepared.putIfAbsent(ID_NAME, id);
			}

			//
			collectionItemLogger.info(
				collection, 
				id, 
				user, 
				"Updated item",
				values);
		}

		//
		broadcast(collection, CollectionUpdatedEvent.ITEM_UPDATED, sessionId, null, id);
		
		return prepared;
	}

	@SuppressWarnings("unchecked")
	private ItemMeta getMeta(
		final Map<String, Object> item)
	{
		var meta = new ItemMeta();
		if(item != null)
		{
			var metaMap = (Map<String, Object>)item.get("_meta");
			if(metaMap != null)
			{
				var flowsMap = (Map<String, Object>)metaMap.get("flows");
				if(flowsMap != null)
				{
					var flows = new HashMap<String, FlowItemMeta>();
					meta.setFlows(flows);

					for(var entry: flowsMap.entrySet())
					{
						var key = entry.getKey();
						var dataMap = (Map<String, Object>)entry.getValue();
						
						var data = new FlowItemMeta();
						data.setStatus(FlowItemMetaStatus.valueOf((String)dataMap.get("status")));
						data.setPending((List<String>)dataMap.get("pending"));
						data.setCount((int)dataMap.get("count"));
						
						flows.put(key, data);
					}
				}

				var automations = (List<String>)metaMap.get("automations");
				if(automations != null)
				{
					meta.setAutomations(
						automations.stream().collect(Collectors.toSet()));
				}
			}
		}

		return meta;
	}

	private void setMeta(
		final Map<String, Object> item,
		final ItemMeta meta)
	{
		var metaMap = meta != null? 
			new HashMap<String, Object>():
			null;

		if(meta != null)
		{
			var flows = meta.getFlows();	
			var flowsMap = flows != null?
				new HashMap<String, Object>():
				null;
			metaMap.put("flows", flowsMap);
				
			if(flows != null)
			{
				for(var entry: flows.entrySet())
				{
					var key = entry.getKey();
					var flow = entry.getValue();
					var dataMap = flow != null?
						new HashMap<String, Object>():
						null;
					flowsMap.put(key, dataMap);
					if(flow != null)
					{
						dataMap.put("status", flow.getStatus().toString());
						dataMap.put("pending", flow.getPending());
						dataMap.put("count", flow.getCount());
					}
				}
			}

			metaMap.put("automations", meta.getAutomations());
		}

		item.put("_meta", metaMap);
	}
	
	private void execFlows(
		final CollectionWithSchema collection, 
		final User user,
		final Map<String, Object> item,
		final Map<String, Object> values,
		final EvalContext scriptContext,
		final ItemMeta meta) 
	{
		var schema = collection.getSchemaObj();
		var flows = schema.getFlows();
		if(flows == null)
		{
			return;
		}

		String id = null;
		Map<String, Object> mixed = null;
		if(item != null) 
		{
			id = item.get("_id") != null?
				item.get("_id").toString():
				null;
			mixed = new HashMap<>(item);
			deepCopy(values, mixed);
		}
		else 
		{
			mixed = new HashMap<>(values);
		}

		scriptContext.put("cols", mixed);
		if(item != null) 
		{
			scriptContext.put("prevCols", item);
		}

		var activeFlows = meta.getFlows();
		if(activeFlows == null)
		{
			activeFlows = flows.entrySet().stream()
				.filter(e -> e.getValue().getType() == FlowType.START)
					.map(e -> new Pair<String, FlowItemMeta>(
							e.getKey(), 
							new FlowItemMeta(FlowItemMetaStatus.IDLE)))
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
		}

		var idleActiveFlows = new HashSet<String>();
		
		while(activeFlows != null)
		{
			var flowsToActivate = new HashMap<String, FlowItemMeta>();

			for(var activeEntry : activeFlows.entrySet())
			{
				var flowName = activeEntry.getKey();
				var flowData = activeEntry.getValue(); 
				var activeFlow = flows.get(flowName);
				if(activeFlow == null)
				{
					flowData.setStatus(FlowItemMetaStatus.FINISHED);
					continue;
				}

				if(flowData.getStatus() == FlowItemMetaStatus.IDLE)
				{
					if(idleActiveFlows.contains(flowName))
					{
						var err = String.format(
							"Fluxo de trabalho em looping em '%s'. Corrija o desenho, inserindo condições", 
							activeFlow.getLabel().getValue());
						if(item != null)
						{
							collectionItemLogger.error(collection, item.get(ID_NAME), user, err);
						}
						throw new CollectionException(err);
					}

					idleActiveFlows.add(flowName);
				}
				
				var type = activeFlow.getType();

				var status = flowData.getStatus();
				var count = flowData.getCount();
			
				var ins = activeFlow.getIn();
				if(type == FlowType.PARALLEL && 
					ins != null && ins.size() > 0)
				{
					status = count >= ins.size()?
						FlowItemMetaStatus.RUNNING:
						FlowItemMetaStatus.STARTING;
				}
				else
				{
					if(status == FlowItemMetaStatus.IDLE || 
						status == FlowItemMetaStatus.STARTING)
					{
						status = FlowItemMetaStatus.RUNNING;
					}
				}

				// out
				var outs = activeFlow.getOut();
				List<String> pending = null;
				if(status == FlowItemMetaStatus.RUNNING)
				{
					if(activeFlow.getType() == FlowType.SERVICE)
					{
						if(activeFlow.getActivities() != null)	
						{
							execFlowActivities(
								collection,
								activeFlow,
								id,
								mixed,
								scriptContext
							);
						}
					}

					pending = flowData.getPending();
					if(pending == null)
					{
						if(outs == null || outs.size() == 0)
						{
							status = FlowItemMetaStatus.FINISHED;
						}
						else
						{
							pending = new ArrayList<String>();
							for(var out : outs.keySet())
							{
								pending.add(out);
							}
						}
					}
				}

				if(pending != null && pending.size() > 0)
				{
					var toRemove = new HashSet<String>();
					String defaultFlow = null;
					var matches = 0;
					for(var outFlow : pending)
					{
						var out = outs.get(outFlow);
						if(out == null || out.getCondition() == null)
						{
							if(type != FlowType.PARALLEL)
							{
								defaultFlow = outFlow;
							}
							else
							{
								toRemove.add(outFlow);
								flowsToActivate.put(
									outFlow, 
									new FlowItemMeta(FlowItemMetaStatus.IDLE, 1));
							}
						}
						else
						{
							if(evalLogicalConditions(
								collection, schema, out.getCondition(), mixed, meta, scriptContext))
							{
								flowsToActivate.put(
									outFlow, 
									new FlowItemMeta(FlowItemMetaStatus.IDLE, 1));
								++matches;

								if(type != FlowType.PARALLEL)
								{
									toRemove.addAll(pending);
									break;
								}
								else
								{
									toRemove.add(outFlow);
								}
							}
						}
					}

					if(defaultFlow != null && matches == 0)
					{
						toRemove.addAll(pending);
						flowsToActivate.put(
							defaultFlow, 
							new FlowItemMeta(FlowItemMetaStatus.IDLE, 1));
					}

					pending.removeAll(toRemove);
					if(pending.size() == 0)
					{
						flowData.setPending(null);
						status = FlowItemMetaStatus.FINISHED;
					}
					else
					{
						flowData.setPending(pending);
						status = FlowItemMetaStatus.RUNNING;
					}
				}
				else
				{
					if(status == FlowItemMetaStatus.RUNNING)
					{
						status = FlowItemMetaStatus.FINISHED;
					}
				}

				flowData.setStatus(status);
			}

			//
			activeFlows = activeFlows.entrySet().stream()
				.filter(e -> e.getValue().getStatus() != FlowItemMetaStatus.FINISHED)
					.collect(Collectors
						.toMap(Map.Entry::getKey, Map.Entry::getValue));

			if(flowsToActivate.size() == 0)
			{
				break;
			}

			//
			for(var entry: flowsToActivate.entrySet())
			{
				var flowName = entry.getKey();
				var data = entry.getValue();
				if(!activeFlows.containsKey(flowName))
				{
					activeFlows.put(flowName, data);
				}
				else
				{
					var flow = activeFlows.get(flowName);
					flow.setCount(flow.getCount() + data.getCount());
				}
			}
		}

		//
		meta.setFlows(activeFlows);
	}

	private void execFlowActivities(
		final CollectionWithSchema collection, 
		final Flow flow,
		final String id,
		final Map<String, Object> values,
		final EvalContext scriptContext)
	{
		try
		{			
			var transf = id != null?
				new HashMap<>(values):
				values;
			if(id != null)
			{
				transf.put("_id", id);
			}

			for(var act : flow.getActivities())
			{
				var params = new HashMap<String, Object>();
				params.put("schemaId", act.getSchemaId());
				params.put("fields", act.getFields());
				params.put("values", transf);

				var msg = new ActivityTriggeredMessage(
					collection.getId(),
					null,
					params);

				activitiesQueueService.enviar(msg);
			}
		}
		catch(Exception e)
		{
			collectionLogger.error(
				collection, 
				collection.getCreatedBy(), 
				"Failed to queue flow activities", 
				e.getMessage());
		}
	}

	private Boolean evalFlowCondition(
		final CollectionWithSchema collection, 
		final CollectionSchema schema,
		final FlowCondition cond,
		final Map<String, Object> item,
		final ItemMeta meta,
		final EvalContext scriptCtx) 
	{
		if(cond.getColumn() != null)
		{
			var col = cond.getColumn();
			var colName = col.getName();
			var column = getColunaDaTabela(collection, schema.getColumns(), colName);
			
			if(column != null)
			{
				var left = getFieldValue(item, colName);
				var op = col.getOp();
				var right = evalValueOrScriptOrColumnOrFunction(col.getValue(), item, scriptCtx);

				return op == FilterOperator.isnull || op == FilterOperator.notnull? 
					execUop(op, left):
					execBop(op, left, right, column.getType());
			}
			return false;
		}
		else
		{
			var id = cond.getAutomation().getName();
			return meta.getAutomations().contains(id);
		}
	}

	private boolean evalLogicalConditions(
		final CollectionWithSchema collection, 
		final CollectionSchema schema,
		final LogicalExpr<FlowCondition> conditions,
		final Map<String, Object> item,
		final ItemMeta meta,
		final EvalContext scriptCtx) 
	{
		return Evaluator.logicalEval(
			conditions, 
			(cond) -> 
				evalFlowCondition(
					collection, schema, cond, item, meta, scriptCtx
				));
	}

	@SuppressWarnings("unchecked")
	private void deepCopy(
		final Map<String, Object> fonte, 
		final Map<String, Object> destino) 
	{
		for(var entry: fonte.entrySet())
		{
			var key = entry.getKey();
			var value = entry.getValue();
			if(value != null && value instanceof Map<?,?> && destino.containsKey(key))
			{
				// preservar campos que existam só no destino, em razão do update parcial de um objeto
				var campos = (Map<String, Object>)destino.get(key);
				if(campos == null)
				{
					destino.put(key, value);
				}
				else
				{
					campos.putAll((Map<String, Object>)value);
				}
			}
			else
			{
				destino.put(key, value);
			}
		}
	}
	
	private void execAutomations(
		final CollectionWithSchema collection, 
		final Map<String, Field> colunas, 
		final String id,
		final Map<String, Object> item, 
		final EvalContext scriptContext,
		final Set<CollectionAutomation> automations, 
		final Map<String, Object> values,
		final ItemMeta meta) 
		throws Exception 
	{
		Map<String, Object> mixed = null;
		if (item != null) 
		{
			mixed = new HashMap<>(item);
			deepCopy(values, mixed);
			mixed.put("_id", id);
		} 
		else 
		{
			mixed = new HashMap<>(values);
		}

		scriptContext.put("cols", mixed);
		if (item != null) 
		{
			scriptContext.put("prevCols", item);
		}

		for (var auto : automations) 
		{
			try
			{
				switch(auto.getType())
				{
				case FIELD:
					var automation = (CollectionAutomationField)auto;

					switch(auto.getTrigger())
					{
					case FIELD_UPDATED_TO:
						if(!evalAutomationCondition(
							collection,
							colunas,
							automation.getSchemaObj().getCondition(),
							values,
							mixed,
							scriptContext))
						{
							continue;
						}
						break;

					default:
						throw new CollectionException("Automation trigger not supported");
					}
					break;

				default:
					throw new CollectionException("Automation type not supported");
				}

				if(auto.getActivities().size() > 0)
				{
					var activities = auto.getActivities().stream()
						.map(a -> a.getActivity().getPubId())
							.collect(Collectors.toList());
				
					var msg = new ActivityTriggeredMessage(
						collection.getId(),
						activities,
						mixed);

					activitiesQueueService.enviar(msg);

					meta.getAutomations().add(auto.getPubId());
				}
			}
			catch(Exception e)
			{
				collectionAutomationLogger.error(auto, "Falha ao executar automação", e);
				throw e;
			}
		}
	}

	private boolean evalAutomationCondition(
		final CollectionWithSchema collection,
		final Map<String, Field> fields,
		final LogicalExpr<Condition> expr,
		final Map<String, Object> set,
		final Map<String, Object> mixed,
		final EvalContext scriptContext) 
	{
		return Evaluator.logicalEval(expr, 
		(cond) ->
		{
			var field = cond.getField();
			var name = field.getName();

			switch(field.getOp())
			{
			case exists:
				return hasField(set, name);
			case notexists:
				return !hasField(set, name);
			default:
				break;
			}

			var value = getFieldValue(mixed, name);

			switch(field.getOp())
			{
			case isnull:
				return value == null;
			case notnull:
				return value != null;
			default:
				break;
			}
			
			var type = getColunaDaTabela(collection, fields, name).getType();

			switch(field.getType())
			{
			case VALUE:
				return execBop(field.getOp(), value, field.getValue(), type);

			case REGEX:
				var	res = Pattern.matches((String)field.getValue(), (String)value);
				// NOTE: only eq and ne are supported
				return field.getOp() == FilterOperator.eq? res: !res;

			case SCRIPT:
				return execBop(field.getOp(), value, execScript((String)field.getValue(), scriptContext), type);

			default:
				return false;
			}
		});
	}

	public Object execScript(
		final String script, 
		final EvalContext context) 
	{
		try 
		{
			var ast = cachedParser.parse(script);
			return ast.eval(context);
		} 
		catch (Exception e) 
		{
			throw new CollectionException(e.getMessage());
		}
	}
	
	private String valueOuScriptToString(
		final ScriptOrValue expr, 
		final EvalContext scriptContext) 
	{
		var res = evalValueOrScript(expr, scriptContext);
		
		return res == null? "": res.toString();
	}
	
	public Object evalValueOrScript(
		final ScriptOrValue expr,
		final EvalContext scriptContext) 
	{
		if(expr.getScript() != null)
		{
			return execScript(expr.getScript(), scriptContext);
		}
		
		return expr.getValue();
	}

	public Object evalValueOrScriptOrFunction(
		final ScriptOrFunctionOrValue expr,
		final EvalContext scriptContext,
		final String extra) 
	{
		if(expr == null)
		{
			return null;
		}
		
		if(expr.getFunction() != null)
		{
			var script = mapFunctionToScript(expr, extra);
			
			return script != null? execScript(script, scriptContext): null;
		}

		return evalValueOrScript(expr, scriptContext);
	}

	public Object evalValueOrScriptOrFunction(
		final ScriptOrFunctionOrValue expr,
		final EvalContext scriptContext) 
	{
		return evalValueOrScriptOrFunction(expr, scriptContext, null);
	}

	public Object evalValueOrScriptOrColumnOrFunction(
		final ScriptOrFunctionOrColumnOrValue expr,
		final EvalContext scriptContext) 
	{
		if(expr == null)
		{
			return null;
		}
		
		if(expr.getColumn() != null)
		{
			return String.format("$%s", expr.getColumn());
		}
		
		return evalValueOrScriptOrFunction(expr, scriptContext);
	}

	public Object evalValueOrScriptOrColumnOrFunction(
		final ScriptOrFunctionOrColumnOrValue expr,
		final Map<String, Object> source,
		final EvalContext scriptContext) 
	{
		if(expr == null)
		{
			return null;
		}
		
		if(expr.getColumn() != null)
		{
			return getFieldValue(source, expr.getColumn());
		}
		
		return evalValueOrScriptOrFunction(expr, scriptContext);
	}

	public Object evalValueOrScriptOrFieldOrFunction(
		final ScriptOrFunctionOrFieldOrValue expr,
		final Map<String, Object> src, 
		final EvalContext scriptContext) 
	{
		if(expr == null)
		{
			return null;
		}
		
		if(expr.getField() != null)
		{
			return src.get(expr.getField().toString());
		}
		
		return evalValueOrScriptOrFunction(expr, scriptContext);
	}

	private String mapFunctionToScript(
		final ScriptOrFunctionOrValue expr, 
		final String extra) 
	{
		switch(expr.getFunction())
		{
		case nowAsDate:
			return "calendar.now().toDate()";
		case nowAsIso:
			return "calendar.now().toIso()";
		case hourOfDay:
			return String.format("db.hour('%s', user.getTimeZone())", extra);
		case dayOfMonth:
			return String.format("db.day('%s', user.getTimeZone())", extra);
		case monthOfYear:
			return String.format("db.month('%s', user.getTimeZone())", extra);
		case currentUser:
			//FIXME: utilizar pubId após implementar coluna do type 'user'
			return "user.getCurrent().getEmail()";
		default:
			return null;
		}
	}
	

	private long objectNumberToLong(Object value)
	{
		if(value instanceof Long)
			return (long)value;
		else if(value instanceof Integer)
			return (long)(int)value;
		else if(value instanceof Short)
			return (long)(short)value;
		else if(value instanceof Float)
			return (long)(float)value;
		else if(value instanceof Double)
			return (long)(double)value;
		else if(value instanceof String)
			return Long.parseLong((String)value);
		
		return 0;
	}
	
	private double objectDecimalToDouble(Object value)
	{
		if(value instanceof Float)
			return (double)(float)value;
		else if(value instanceof Double)
			return (double)value;
		else if(value instanceof Long)
			return (double)(long)value;
		else if(value instanceof Integer)
			return (double)(int)value;
		else if(value instanceof Short)
			return (double)(short)value;
		else if(value instanceof String)
			return Double.parseDouble((String)value);
	
		return 0.0;
	}

	private boolean execUop(
		final FilterOperator op,
		final Object left) 
	{
		if(left == null)
		{
			return op == FilterOperator.isnull;
		}
		else
		{
			return op == FilterOperator.notnull;
		}
	}

	private boolean execBop(
		final FilterOperator op, 
		final Object left, 
		final Object right, 
		final FieldType type) 
	{
		if(left == null)
		{
			return op == FilterOperator.eq? 
				right == null:
				right != null;
		}
		
		if(right == null)
		{
			if(op == FilterOperator.isnull)
			{
				return true;
			}

			return op == FilterOperator.ne? 
				true:
				false;
		}
		else
		{
			if(op == FilterOperator.notnull)
			{
				return true;
			}
		}
		
		switch(type)
		{
		case date:
			return compararDate(left, right, op);
		
		case number:
			return compararNumber(objectNumberToLong(left), right, op);

		case decimal:
			return compararDecimal(objectDecimalToDouble(left), right, op);

		case string:
			return compararString((String)left, right, op);

		case bool:
			return compararBool((Boolean)left, (Boolean)right, op);

		case enumeration:
			if(left instanceof String)
			{
				return compararString((String)left, right, op);
			}
			else if(left instanceof Long || left instanceof Integer || left instanceof Short)
			{
				return compararNumber(objectNumberToLong(left), right, op);
			}
			else if(left instanceof Double || left instanceof Float)
			{
				return compararDecimal(objectDecimalToDouble(left), right, op);
			}
		
		case array:
			//FIXME: fazer comparação de cada item da array 
			return false;
		
		default:
			break;
		}
		
		return false;
	}

	@SuppressWarnings("unchecked")
	private boolean compararDecimal(
		final double left, 
		final Object right, 
		final FilterOperator op) 
	{
		switch(op)
		{
		case eq:
			return left == objectDecimalToDouble(right);
		case ne:
			return left != objectDecimalToDouble(right);
		case lt:
			return left < objectDecimalToDouble(right);
		case lte:
			return left <= objectDecimalToDouble(right);
		case gt:
			return left > objectDecimalToDouble(right);
		case gte:
			return left >= objectDecimalToDouble(right);
		case in:
			return ((ArrayList<Object>)right).stream().anyMatch(o -> left == objectDecimalToDouble(o));
		case notin:
			return !((ArrayList<Object>)right).stream().anyMatch(o -> left == objectDecimalToDouble(o));
		case between:
			return left >= objectDecimalToDouble(((ArrayList<Object>)right).get(0)) &&
				left <= objectDecimalToDouble(((ArrayList<Object>)right).get(1));
		default:
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean compararNumber(
		final long left, 
		final Object right, 
		final FilterOperator op) 
	{
		switch(op)
		{
		case eq:
			return left == objectNumberToLong(right);
		case ne:
			return left != objectNumberToLong(right);
		case lt:
			return left < objectNumberToLong(right);
		case lte:
			return left <= objectNumberToLong(right);
		case gt:
			return left > objectNumberToLong(right);
		case gte:
			return left >= objectNumberToLong(right);
		case in:
			return ((ArrayList<Object>)right).stream().anyMatch(o -> left == objectNumberToLong(o));
		case notin:
			return !((ArrayList<Object>)right).stream().anyMatch(o -> left == objectNumberToLong(o));
		case between:
			return left >= objectNumberToLong(((ArrayList<Object>)right).get(0)) &&
				left <= objectNumberToLong(((ArrayList<Object>)right).get(1));
		default:
			return false;
		}
	}

	private Instant objectToInstant(final Object obj)
	{
		if(obj instanceof String)
		{
			return ZonedDateTime.parse((String)obj).toInstant();
		}
		else if(obj instanceof Date)
		{
			return ((Date)obj).toInstant();
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	private boolean compararDate(
		final Object left, 
		final Object right, 
		final FilterOperator op) 
	{
		var dleft = objectToInstant(left);
		
		switch(op)
		{
		case eq:
			return dleft.equals(objectToInstant(right));
		case ne:
			return !dleft.equals(objectToInstant(right));
		case lt:
			return dleft.isBefore(objectToInstant(right));
		case lte:
			return dleft.equals(objectToInstant(right)) || dleft.isBefore(objectToInstant(right));
		case gt:
			return dleft.isAfter(objectToInstant(right));
		case gte:
			return dleft.equals(objectToInstant(right)) || dleft.isAfter(objectToInstant(right));
		case in:
			return ((ArrayList<Object>)right).stream().anyMatch(o -> dleft.equals(objectToInstant(o)));
		case notin:
			return !((ArrayList<Object>)right).stream().anyMatch(o -> dleft.equals(objectToInstant(o)));
		case between:
		{
			var first = objectToInstant(((ArrayList<Object>)right).get(0));
			var last = objectToInstant(((ArrayList<Object>)right).get(1));
			return ((dleft.equals(first)) || dleft.isAfter(first)) &&
				((dleft.equals(last)) || dleft.isBefore(last));
		}
		default:
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean compararString(
		final String left, 
		final Object right, 
		final FilterOperator op) 
	{
		switch(op)
		{
		case eq:
			return left.equals((String)right);
		case ne:
			return !left.equals((String)right);
		case like:
			return Pattern.matches(String.format("(?i:%s.*)", right), left);
		case in:
			return ((ArrayList<String>)right).contains(left);
		case notin:
			return !((ArrayList<String>)right).contains(left);
		default:
			return false;
		}
	}
	
	private boolean compararBool(
		final Boolean left, 
		final Boolean right, 
		final FilterOperator op) 
	{
		switch(op)
		{
		case eq:
			return left.equals(right);
		case ne:
			return !left.equals(right);
		default:
			return false;
		}
	}
	
	private boolean isDisabledForInsert(
		final Field column,
		final String key,
		final User user,
		final boolean asAdmin) 
	{
		var disabled = column.getDisabled();
		if(disabled == null)
		{
			return false;
		}
		
		return Evaluator.logicalEval(
			disabled.getInsert(),
			(dis) ->
			{
				switch(dis.getWhen())
				{
				case ALWAYS:
					return true;
				case USER:
					var user_ = dis.getUser();
					return isDisabledForUser(user_.getEmail(), user_.getOp(), user, asAdmin);
				default:
					return false;
				}
			});
	}

	private boolean isDisabledForUser(
		final String email,
		final FilterOperator op,
		final User user,
		final boolean asAdmin
	)
	{
		var userEmail = user.getEmail();
		var pattern = email;
		if(email.indexOf('{') == 0)
		{
			switch(email)
			{
			case "{admin}":
				if(asAdmin)
				{
					pattern = userEmail;
				}
				break;
			}
		}
		
		switch(op)
		{
		case eq:
			return pattern.equals(userEmail);
		case ne:
			return !pattern.equals(userEmail);
		case in:
		case notin:
			var arr = Arrays.asList(pattern.split("|"));
			return arr.contains(userEmail)? 
				(op == FilterOperator.in): 
				(op == FilterOperator.notin);
		default:
			return false;
		}

	}
	
	private boolean isDisabledForUpdate(
		final CollectionWithSchema collection, 
		final Field column,
		final Map<String, Object> item, 
		final String key, 
		final Object value,
		final User user,
		final boolean asAdmin) 
	{
		var disabled = column.getDisabled();
		if(disabled == null)
		{
			return false;
		}
		
		return Evaluator.logicalEval(
			disabled.getUpdate(),
			(dis) ->
			{
				switch(dis.getWhen())
				{
				case ALWAYS:
					return true;
				case USER:
					var user_ = dis.getUser();
					return isDisabledForUser(user_.getEmail(), user_.getOp(), user, asAdmin);
				case VALUE:
					var	val = dis.getValue();
					return execBop(val.getOp(), value, val.getValue(), column.getType());
				default:
					return false;
				}
			});
	}

	private boolean isDependencyResolvedForUpdate(
		final CollectionWithSchema collection, 
		final CollectionSchema schema, 
		final Field column,
		final Map<String, Object> item) 
	{
		return Evaluator.logicalEval(
			column.getDepends(),
			(dep) ->
			{
				if(dep.getColumn() != null)
				{
					var col = dep.getColumn();
					var nomeColuna = col.getName();
					var left = getFieldValue(item, nomeColuna);
					var op = col.getOp() != null? col.getOp(): FilterOperator.eq;

					if(op == FilterOperator.isnull || op == FilterOperator.notnull)
					{
						return execUop(op, left);
					}
					else
					{
						var right = col.getValue();
						return execBop(
							op, 
							left, 
							right, 
							getColunaDaTabela(collection, schema.getColumns(), nomeColuna).getType());
					}
				}
				else
				{
					var flow = dep.getFlow().getName();
					var meta = getMeta(item);
					return meta.getFlows() != null?
						meta.getFlows().containsKey(flow):
						false;
				}
			}
		);
	}
	
	private Field getColunaDaTabela(
		final CollectionWithSchema collection, 
		final Map<String, Field> colunas, 
		final String coluna) 
	{
		var p = coluna.indexOf('.');
		if(p < 0) 
		{
			return colunas.get(coluna);
		}
		
		var col = colunas.get(coluna.substring(0, p));
		if(col.getType() != FieldType.object)
		{
			throw new CollectionException(String.format("Coluna %s não é do type objeto", coluna));
		}
		
		var klass = col.getClass_();
		return collection.getSchemaObj().getClasses().get(klass)
			.getProps().get(coluna.substring(p+1));
	}

	private Object getFieldValue(
		final Map<String, Object> map, 
		final String field) 
	{
		var p = field.indexOf('.');
		if(p < 0) 
		{
			return map.get(field);
		}
		
		var value = map.get(field.substring(0, p));
		if(value == null)
		{
			return null;
		}
		if(!(value instanceof Map<?, ?>))
		{
			throw new CollectionException(String.format("Coluna %s não é do type objeto", field));
		}
		
		@SuppressWarnings("unchecked")
		var sub = (Map<String, Object>)value;
		
		return sub.get(field.substring(p+1));
	}

	private boolean hasField(
		final Map<String, Object> map, 
		final String field) 
	{
		var p = field.indexOf('.');
		if(p < 0) 
		{
			return map.containsKey(field);
		}
		
		var value = map.get(field.substring(0, p));
		if(value == null)
		{
			return false;
		}
		if(!(value instanceof Map<?, ?>))
		{
			throw new CollectionException(String.format("Coluna %s não é do type objeto", field));
		}
		
		@SuppressWarnings("unchecked")
		var sub = (Map<String, Object>)value;
		
		return sub.containsKey(field.substring(p+1));
	}

	private String getOptionValue(
		final String option)
	{
		var p = option.indexOf('|');
		if(p == -1)
		{
			return option;
		}

		return option.substring(0, p);
	}

	@SuppressWarnings("unchecked")
	private void validarColuna(
		final CollectionWithSchema collection, 
		final String name, 
		final Field field, 
		final Object value, 
		final Map<String, Object> item) 
	{
		
		if(value == null)
		{
			if(!field.isNullable())
			{
				if(item == null || !item.containsKey(name))
				{
					throw new CollectionException(String.format("Coluna %s não pode ser nula", name));
				}
			}
		}

		switch(field.getType())
		{
		case enumeration:
			var options = field.getOptions();	
			if(options != null && 
				 !options.stream().anyMatch(option -> getOptionValue(option).equals(value)))
			{
				throw new CollectionException(String.format("Valor inválido passado para a coluna %s", name));
			}
			break;
			
		case object:
			var klass = collection.getSchemaObj().getClasses().get(field.getClass_());
			
			var map = (Map<String, Object>)value;
			
			for(var entry : klass.getProps().entrySet()) 
			{
				var key = entry.getKey();
				validarColuna(
					collection, 
					key, 
					entry.getValue(), 
					map.get(key), 
					item != null? 
						(Map<String, Object>)item.get(name): 
						null);
			}
			break;
			
		default:
			break;
		}
	}
	
	private CalendarScriptService calendarioScriptServiceInstance = new CalendarScriptService();
	private DateScriptService dateScriptServiceInstance = new DateScriptService();
	private UtilScriptService utilScriptServiceInstance = new UtilScriptService();
	private MathScriptService mathScriptServiceInstance = new MathScriptService();
	private DbScriptService dbScriptServiceInstance = new DbScriptService();
	private NetScriptService netScriptServiceInstance = new NetScriptService();
	
	public EvalContext configScriptContext(
		final CollectionWithSchema collection, 
		final User user)
	{
		var ctx = new EvalContext();
		ctx.put(
			"user", 
			user != null? 
				new UserScriptService(userRepo, collection.getWorkspace(), user): 
				null);
		ctx.put("calendar", calendarioScriptServiceInstance);
		ctx.put("date", dateScriptServiceInstance);
		ctx.put("util", utilScriptServiceInstance);
		ctx.put("math", mathScriptServiceInstance);
		ctx.put("db", dbScriptServiceInstance);
		ctx.put("net", netScriptServiceInstance);
		ctx.put(
			"consts", 
			collection != null? 
				evalConstants(collection.getSchemaObj().getConstants(), ctx): 
				null);
		return ctx;
	}

	public EvalContext reconfigScriptContext(
		final EvalContext ctx,
		final CollectionWithSchema collection, 
		final User user)
	{
		ctx.put(
			"user", 
			user != null? 
				new UserScriptService(userRepo, collection.getWorkspace(), user): 
				null);
		ctx.put(
			"consts", 
			collection != null? 
				evalConstants(collection.getSchemaObj().getConstants(), ctx): 
				null);
		return ctx;
	}

	private Map<String, Object> evalConstants(
		final Map<String, Const> consts,
		final EvalContext context) 
	{
		var res = new HashMap<String, Object>();

		if(consts != null)
		{
			for(var entry: consts.entrySet())
			{
				var value = evalValueOrScriptOrFunction(entry.getValue().getValue(), context);
				res.put(entry.getKey(), value);
			}
		}

		return res;
	}

	/**
	 * 
	 * @param collection
	 * @param id
	 * @param values
	 * @param user
	 * @param asAdmin
	 * @param sessionId
	 * @return
	 */
	public Map<String, Object> atualizarItem(
		final CollectionWithSchema collection, 
		final String id, 
		final Map<String, Object> values,
		final User user,
		final boolean asAdmin,
		final String sessionId)
	{
		var item = findItemById(collection, id);
		if(item == null)
		{
			throw new CollectionException("Item não encontrado na coleção");
		}

		return atualizarItem(
			collection, 
			id, 
			item, 
			configScriptContext(collection, user), 
			values, 
			user, 
			asAdmin, 
			sessionId,
			false);
	}
	
	/**
	 * 
	 * @param collection
	 * @param variaveis
	 * @param user
	 * @param asAdmin
	 * @param sessionId
	 * @return
	 */
	public Map<String, Object> inserirItem(
		final CollectionWithSchema collection, 
		final Map<String, Object> variaveis,
		final User user,
		final boolean asAdmin,
		final String sessionId)
	{
		return inserirItem(
			collection, 
			configScriptContext(collection, user), 
			variaveis, 
			true, 
			true, 
			user, 
			asAdmin, 
			sessionId, 
			false);
	}

	/**
	 * 
	 * @param collection
	 * @param variaveis
	 * @param verificarDesabilitados
	 * @param validarColunas
	 * @param user
	 * @param asAdmin
	 * @return
	 */
	public Map<String, Object> inserirItem(
		final CollectionWithSchema collection, 
		final Map<String, Object> variaveis, 
		final boolean verificarDesabilitados, 
		final boolean validarColunas,
		final User user,
		final boolean asAdmin,
		final String sessionId)
	{
		return inserirItem(
			collection, 
			configScriptContext(collection, user), 
			variaveis, 
			verificarDesabilitados, 
			validarColunas, 
			user, 
			asAdmin, 
			sessionId,
			false);
	}

	/**
	 * 
	 * @param collection
	 * @param variaveis
	 * @param verificarDesabilitados
	 * @param user
	 * @param asAdmin
	 * @return
	 */
	public Map<String, Object> inserirItem(
		final CollectionWithSchema collection, 
		final Map<String, Object> variaveis, 
		final boolean verificarDesabilitados,
		final User user,
		final boolean asAdmin,
		final String sessionId)
	{
		return inserirItem(
			collection, 
			variaveis, 
			verificarDesabilitados, 
			true, 
			user, 
			asAdmin, 
			sessionId);
	}

	/**
	 * 
	 * @param collection
	 * @param variaveisLista
	 * @param verificarDesabilitados
	 * @param validarColunas
	 * @param user
	 * @param asAdmin
	 * @return
	 */
	public List<Map<String, Object>> inserirItens(
		final CollectionWithSchema collection, 
		final List<Map<String, Object>> variaveisLista, 
		final boolean verificarDesabilitados, 
		final boolean validarColunas,
		final User user,
		final boolean asAdmin,
		final String sessionId)
	{
		var res = new ArrayList<Map<String, Object>>();
		
		var ctx = configScriptContext(collection, user);
		
		for(var variaveis : variaveisLista)
		{
			res.add(inserirItem(
				collection, 
				ctx, 
				variaveis, 
				verificarDesabilitados, 
				validarColunas, 
				user, 
				asAdmin, 
				sessionId,
				false));
		}
		
		return res;
	}

	/**
	 * 
	 * @param collection
	 * @param csv
	 * @param verificarDesabilitados
	 * @param validarColunas
	 * @param user
	 * @param asAdmin
	 * @param sessionId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> inserirItens(
		final CollectionWithSchema collection, 
		final String csv, 
		final boolean verificarDesabilitados, 
		final boolean validarColunas,
		final User user,
		final boolean asAdmin,
		final String sessionId)
	{
		var classes = collection.getSchemaObj().getClasses();

		var variaveisLista = new ArrayList<Map<String, Object>>();

		var linha = 0;
		try(var csvReader = ((CSVReaderHeaderAwareBuilder)new CSVReaderHeaderAwareBuilder(new StringReader(csv))
				.withFieldAsNull(CSVReaderNullFieldIndicator.EMPTY_SEPARATORS)
				).build())
		{
			var colunas = collection.getSchemaObj().getColumns();
			
			var variaveis = csvReader.readMap();
			while(variaveis != null)
			{
				++linha;
				var variaveisFiltradas = new HashMap<String, Object>();
				var cnt = 0;
				for(var entry : variaveis.entrySet())
				{
					var key = entry.getKey();
					var p = key.indexOf('.');
					if(p < 0)
					{
						var campoColuna = lookupColunaPeloNome(colunas, key);
						var campo = campoColuna.getKey();
						var coluna = campoColuna.getValue();

						if(coluna.getType() == FieldType.object)
						{
							throw new CollectionException(String.format(
								"Coluna %s é do type objeto. Cada propriedade da classe deve ser atribuída individualmente", key));
						}
						
						variaveisFiltradas.put(
							campo, 
							CollectionService
								.converterParaTipo(entry.getValue(), coluna.getType()));
						++cnt;
					}
					else
					{
						var campo = key.substring(p+1);
						var classeColuna = lookupColunaPeloNome(colunas, key.substring(0, p));
						var classe = classeColuna.getKey();
						var coluna = classeColuna.getValue();
								
						if(coluna.getType() != FieldType.object)
						{
							throw new CollectionException(String.format("Coluna %s não é do type objeto", classe));
						}
						
						var klass = classes.get(coluna.getClass_());
						
						if(!klass.getProps().containsKey(campo))
						{
							throw new CollectionException(String.format("Propriedade %s inexistente na classe %s", campo, coluna.getClass_()));
						}
						
						var prop = klass.getProps().get(campo);
						
						if(!variaveisFiltradas.containsKey(classe)) 
						{
							variaveisFiltradas.put(classe, new HashMap<String, Object>());
						}
					
						((Map<String, Object>)variaveisFiltradas.get(classe))
							.put(campo, converterParaTipo(entry.getValue(), prop.getType()));
						++cnt;
					}
				}
				
				if(cnt != variaveis.size())
				{
					throw new CollectionException(String.format(
						"Encontradas colunas em quantidade diferente ou com nomes que não correspondem ao schema da coleção na linha %d", linha));
				}
				
				variaveisLista.add(variaveisFiltradas);
				
				variaveis = csvReader.readMap();
			};
		}
		catch(CollectionException e)
		{
			throw e;
		}
		catch(IOException e)
		{
			throw new CollectionException(e.getMessage(), e);
		}
		catch (Exception e) 
		{
			throw new CollectionException(String.format(
				"Importação de itens da coleção falhou na linha %d", linha), e);
		}
		
		return inserirItens(
			collection, 
			variaveisLista, 
			verificarDesabilitados, 
			validarColunas, 
			user, 
			asAdmin, 
			sessionId);
	}

	private Set<CollectionAutomation> getAutomations(
		final CollectionWithSchema collection, 
		final CollectionAutomationType type, 
		final EnumSet<CollectionAutomationTrigger> gatilhos) 
	{
		if(collection.getAutomations() == null)
		{
			return null;
		}
		
		return collection.getAutomations()
			.stream()
				.filter(a -> a.getType() == type && gatilhos.contains(a.getTrigger()))
					.collect(Collectors.toSet());		
	}

	/**
	 * 
	 * @param collection
	 * @param pageable
	 * @param filtros
	 * @param user
	 * @param asAdmin
	 * @return
	 */
	public List<org.bson.Document> listarItens(
		final CollectionWithSchema collection, 
		final Pageable pageable, 
		final List<CollectionFilterRequest> filtros, 
		final User user, 
		final boolean asAdmin)
	{
		// projeção (select)
		var projection = buildProjection(collection, user, asAdmin);
		
		// referências (joins)
		List<Reference> refs = buildReferencias(collection);
		
		// filtros (where)
		Filter docFilters = null;
		Filter refFilters = null;
		if(filtros != null)
		{
			var res = buildFiltersWhere(collection, filtros);
			if(res != null)
			{
				docFilters = res.getKey();
				refFilters = res.getValue();
			}
		}
		
		// autorizacao
		if(!asAdmin)
		{
			if(collection.getCreatedBy().getId() != user.getId() && 
				collection.getSchemaObj().getAuth() != null)
			{
				var acesso = collection.getSchemaObj().getAuth();
				if(acesso.getRead() != null)
				{
					var res = buildFiltrosDeAcesso(
						collection, 
						refs, 
						user, 
						acesso.getRead(),
						null, 
						null,
						FilterOperator.and);
					if(res != null)
					{
						docFilters = Filter.by(FilterOperator.and, docFilters, res.getKey());
						refFilters = Filter.by(FilterOperator.and, refFilters, res.getValue());
					}
				}
			}
		}
		
		// order by
		var sort = pageable == null?
				Sort.by("_id"):
				pageable.getSortOr(Sort.by("_id"));
		
		//
		return noSqlService.findAll(
				collection.getWorkspace().getPubId(), 
				collection.getPubId(),
				projection,
				refs,
				docFilters,
				refFilters,
				sort.stream()
					.map(s -> new com.robotikflow.core.models.nosql.Sort(s.getProperty(), s.isAscending()))
						.collect(Collectors.toList()),
				pageable != null? (int)pageable.getPageNumber(): 0, //NOTE: using page as offset, not as page number
				pageable != null? pageable.getPageSize(): 0);
	}

	/**
	 * 
	 * @param collection
	 * @param filtros
	 * @param user
	 * @param asAdmin
	 * @return
	 */
	public List<org.bson.Document> findAllItems(
		final CollectionWithSchema collection, 
		final List<CollectionFilterRequest> filtros, 
		final User user, 
		final boolean asAdmin)
	{
		return listarItens(collection, null, filtros, user, asAdmin);
	}
	/**
	 * 
	 * @param collection
	 * @param filtros
	 * @param user
	 * @param asAdmin
	 * @return
	 */
	public org.bson.Document findItem(
		final CollectionWithSchema collection, 
		final List<CollectionFilterRequest> filtros, 
		final User user, 
		final boolean asAdmin)
	{
		// projeção (select)
		var projection = buildProjection(collection, user, asAdmin);
		
		// referências (joins)
		List<Reference> refs = buildReferencias(collection);
		
		// filtros (where)
		Filter docFilters = null;
		Filter refFilters = null;
		if(filtros != null)
		{
			var res = buildFiltersWhere(collection, filtros);
			if(res != null)
			{
				docFilters = res.getKey();
				refFilters = res.getValue();
			}
		}
		
		// autorizacao
		if(!asAdmin)
		{
			if(collection.getCreatedBy().getId() != user.getId() && 
				collection.getSchemaObj().getAuth() != null)
			{
				var acesso = collection.getSchemaObj().getAuth();
				if(acesso.getRead() != null)
				{
					var res = buildFiltrosDeAcesso(
						collection, 
						refs, 
						user, 
						acesso.getRead(),
						null, 
						null,
						FilterOperator.and);
					if(res != null)
					{
						docFilters = Filter.by(FilterOperator.and, docFilters, res.getKey());
						refFilters = Filter.by(FilterOperator.and, refFilters, res.getValue());
					}
				}
			}
		}
		
		//
		return noSqlService.findOne(
				collection.getWorkspace().getPubId(), 
				collection.getPubId(), 
				projection,
				refs,
				docFilters,
				refFilters);
	}

	public static Date convertToDate(
		final Object value)
	{
		try
		{
			if(value instanceof String)
			{
				var s = (String)value;
				if(s.chars().allMatch(Character::isDigit))
				{
					return new Date(Long.parseLong(s));
				}
				else
				{
					return Date.from(ZonedDateTime.parse(s).toInstant());
				}
			}
			else if(value instanceof Date)
			{
				return (Date)value;
			}
			else if(value instanceof Long)
			{
				return new Date((Long)value);
			}
			else
			{
				return null;
			}
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	/**
	 * 
	 * @param valor
	 * @param type
	 * @return
	 */
	public static Object converterParaTipo(
		final Object valor, 
		final FieldType type)
	{
		if(valor == null)
		{
			return null;
		}
		
		switch(type)
		{
		case number:
			if(valor instanceof String)
			{
				return Long.parseLong((String)valor);
			}
			else if(valor instanceof Long)
				return (long)valor;
			else if(valor instanceof Integer)
				return (long)(int)valor;
			else if(valor instanceof Short)
				return (long)(short)valor;
			else if(valor instanceof Boolean)
				return (Boolean)valor? 1: 0;
			else if(valor instanceof Double)
				return Math.round((Double)valor);
			else if(valor instanceof Float)
				return Math.round((Float)valor);
			else
				return null;
			
		case decimal:
				if(valor instanceof String)
				{
					return Double.parseDouble((String)valor);
				}
				else if(valor instanceof Double)
					return (double)valor;
				else if(valor instanceof Float)
					return (double)(float)valor;
				else if(valor instanceof Long)
					return (double)(long)valor;
				else if(valor instanceof Integer)
					return (double)(int)valor;
				else if(valor instanceof Short)
					return (double)(short)valor;
				else if(valor instanceof Boolean)
					return (double)((Boolean)valor? 1.0: 0.0);
				else
					return null;
				
		case string:
			if(valor instanceof String)
				return valor;
			else if(valor instanceof Long)
				return ((Long)valor).toString();
			else if(valor instanceof Integer)
				return ((Integer)valor).toString();
			else if(valor instanceof Short)
				return ((Short)valor).toString();
			else if(valor instanceof Boolean)
				return (Boolean)valor? "true": "false";
			else if(valor instanceof Double)
				return ((Double)valor).toString();
			else if(valor instanceof Float)
				return ((Float)valor).toString();
			else
				return null;

		case date:
			return convertToDate(valor);
					
		default:
			//FIXME: implementar outras conversões
			return valor;
		}
	}

	/**
	 * 
	 * @param valor
	 * @param type
	 * @return
	 */
	public static Object converterParaTipo(
		final Object valor, 
		final Field coluna)
	{
		return coluna != null? 
			converterParaTipo(valor, coluna.getType()):
			valor;
	}

	/**
	 * 
	 * @param valor
	 * @param type
	 * @return
	 */
	public static Object converterParaTipo(
		final String valor, 
		final FieldType type)
	{
		switch(type)
		{
		case number:
			return Long.parseLong(valor);
			
		case decimal:
			return Double.parseDouble(valor);

		case date:
			return convertToDate(valor);
		
		default:
			return valor;
		}
	}

	/**
	 * 
	 * @param collection
	 * @param id
	 * @return
	 */
	public org.bson.Document findItemById(
		final CollectionWithSchema collection, 
		final String id,
		final User user,
		final boolean isAdmin) 
	{
		var idsFilter = new ArrayList<CollectionFilterRequest>();
		idsFilter.add(new CollectionFilterRequest(ID_NAME, noSqlService.toObjectId(id)));
		
		return findItem(collection, idsFilter, user, isAdmin);
	}

		/**
	 * 
	 * @param collection
	 * @param view
	 * @param id
	 * @return
	 */
	public org.bson.Document findItemById(
		final CollectionWithSchema collection, 
		final String id) 
	{
		return findItemById(collection, id, null, true);
	}

	private org.bson.Document buildProjection(
		final CollectionWithSchema collection, 
		final User user, 
		final boolean asAdmin) 
	{
		var projection = new org.bson.Document();
		var schema = collection.getSchemaObj();
		var hiddenCnt = 0;

		if(!asAdmin)
		{
			for(var entry: schema.getColumns().entrySet())
			{
				var key = entry.getKey();
				var column = entry.getValue();
				if(isHidden(column.getHidden(), user, asAdmin))
				{
					projection.append(key, 0);
					++hiddenCnt;
				}

				if(column.getClass_() != null)
				{
					var klass = schema.getClasses().get(column.getClass_());
					var hiddenProps = klass.getProps().entrySet().stream()
							.filter(e -> e.getValue().getHidden() != null)
								.collect(Collectors.toList());
							
					for(var subEntry: hiddenProps)
					{
						var prop = subEntry.getValue();
						if(isHidden(prop.getHidden(), user, asAdmin))
						{
							projection.append(String.format("%s.%s", key, subEntry.getKey()), 0);
							++hiddenCnt;
						}
					}
				}
			}
		}

		if(hiddenCnt == 0)
		{
			projection.append("_id", 1);
			projection.append("_meta", 1);
			for(var campo: schema.getColumns().keySet())
			{
				projection.append(campo, 1);
			}
		}
		
		return projection;
	}

	private boolean isHidden(
		final FieldHidden hidden, 
		final User user, 
		final boolean asAdmin) 
	{
		if(hidden == null)
		{
			return false;
		}
		
		var type = hidden.getType();
		if(type == FieldHiddenType.ALWAYS)
		{
			return true;
		}
		
		var user_ = hidden.getUser();
		
		var email = user_.getEmail();
		var userEmail = user.getEmail();
		if(email.indexOf('{') == 0)
		{
			switch(email)
			{
			case "{admin}":
				if(asAdmin)
				{
					email = userEmail;
				}
				break;
			}
		}
		
		switch(user_.getOp())
		{
		case eq:
			return email.equals(userEmail);
		case ne:
			return !email.equals(userEmail);
		case in:
			var arr = Arrays.asList(email.split("|"));
			return arr.contains(userEmail);
		default:
			return false;
		}
	}

	private List<Reference> buildReferencias(
		final CollectionWithSchema collection) 
	{
		if(collection.getSchemaObj().getRefs() == null)
		{
			return null;
		}

		var res = new ArrayList<Reference>();

		for(var entry : collection.getSchemaObj().getColumns().entrySet())
		{
			var column = entry.getValue();
			if(column.getRef() != null)
			{
				var name = column.getRef().getName();
				var ref = collection.getSchemaObj().getRefs().get(name);
				
				res.add(new Reference(
					ref.getType() != null? ref.getType(): ReferenceType.single, 
					ref.getCollection(), 
					entry.getKey(), 
					ID_NAME, 
					String.format("%s-%s", name, entry.getKey())));
			}
		}

		return res;
	}

	private Filter buildFilter(
		final CollectionFilterRequest filter, 
		final Field field)
	{
		var name = filter.getName();
		var op = filter.getOp();
		var value = filter.getValue();

		if(value == null)
		{
			if(name.equals(ID_NAME))
			{
				return null;
			}

			return new Filter(op, name, null);
		}

		if(name.equals(ID_NAME))
		{
			if((value instanceof String))
			{
				if(((String)value).length() != 24)
				{
					return null;
				}
			}
			
			value = noSqlService.toObjectId(value);
		}

		switch(op)
		{
		case between:
			if(!(value instanceof ArrayList<?>))
			{
				throw new CollectionException(
					"Valor do filtro do type 'between' deve ser uma array");
			}
	
			@SuppressWarnings("unchecked")
			var array = (ArrayList<Object>)value;
			if(array.size()!= 2)
			{
				throw new CollectionException(
					"Array do filtro do type 'between' deve conter exatamente 2 elementos");
			}
		
			return new Filter(
				op, 
				name, 
				converterParaTipo(array.get(0), field), 
				converterParaTipo(array.get(1), field));

		case in:
		case notin:
			if(!(value instanceof ArrayList<?>))
			{
				throw new CollectionException(
					"Valor do filtro do type 'in' ou 'notin' deve ser uma array");
			}
	
			return new Filter(
				op, 
				name, 
				((ArrayList<?>)value).stream()
					.map(v -> converterParaTipo(v, field))
						.collect(Collectors.toList()));

		default:
			return new Filter(
				op, 
				name, 
				converterParaTipo(value, field));
		} 
			
	}
	
	private Pair<Filter, Filter> buildFiltersWhere(
		final CollectionWithSchema collection, 
		final List<CollectionFilterRequest> reqFiltros)
	{
		Filter docFilters = null;
		Filter refFilters = null;
		
		var schema = collection.getSchemaObj();
		var fields = schema.getColumns();
		var indexes = schema.getIndexes();
		var references = schema.getRefs();
		
		for(var filtro: reqFiltros)
		{
			var name = filtro.getName();
			Filter filter = null;
			if(indexes != null)
			{
				var field = getColunaDaTabela(collection, fields, name);
				if(field == null || field.getRef() == null)
				{
					if(name.equals(ID_NAME))
					{
						filter = buildFilter(filtro, null);
					}
					else if(indexes.stream().anyMatch(i -> i.getColumns().contains(name)? true: false))
					{
						filter = buildFilter(filtro, field);
					}

					docFilters = Filter.by(FilterOperator.and, docFilters, filter);
				}
				else
				{
					var ref = field.getRef();

					var refName = String.format("%s-%s.%s" ,ref.getName(), name, ref.getDisplay());
					filter = buildFilter(
						new CollectionFilterRequest(refName, filtro.getOp(), filtro.getValue()),
						null);
					
					refFilters = Filter.by(FilterOperator.and, refFilters, filter);
				}
			}
			
			if(filter == null && references != null)
			{
				var p = name.indexOf('.');
				if(p > 0)
				{
					var refName = name.substring(0, p);
					var ref = references.get(refName);
					if(ref != null)
					{
						var fieldName = name.substring(p+1);
						if(ref.getFilters().stream().anyMatch(f -> f.getColumn().equals(fieldName)))
						{
							filter = buildFilter(filtro, null);
							refFilters = Filter.by(FilterOperator.and, refFilters, filter);
						}
					}
				}
			}
		}
		
		if(docFilters == null && refFilters == null)
		{
			return null;
		}
		
		return new Pair<>(docFilters, refFilters);
	}

	private Filter buildFiltros(
		final Collection collection, 
		final Map<String, Object> reqFiltros)
	{
		Filter docFilters = null;
		
		for(var filtro: reqFiltros.entrySet())
		{
			var name = filtro.getKey();
			var filter = new Filter(FilterOperator.eq, name, filtro.getValue());
			docFilters = Filter.by(FilterOperator.and, docFilters, filter);
		}
		
		return docFilters;
	}

	private Pair<Filter, Filter> buildFiltrosDeAcessoDeUser(
		final CollectionWithSchema collection,
		final AuthActionWhen when,
		final AuthUser userAcesso, 
		List<Reference> referencias, 
		final User user, 
		final Workspace workspace)
	{
		
		var colunas = collection.getSchemaObj().getColumns();

		Filter accessDocFilters = null;
		Filter accessRefFilters = null;
		
		switch(when)
		{
		case USER_SAME:
			var valor = user.getPubId();
			
			for(var campo : userAcesso.getColumns())
			{
				var filter = new Filter(FilterOperator.eq, campo, valor);
				if(campo.indexOf('.') < 0 || 
					colunas.get(campo).getType() == FieldType.object)
				{
					accessDocFilters = Filter.by(
						FilterOperator.or, accessDocFilters, filter);
				}
				else
				{
					accessRefFilters = Filter.by(
						FilterOperator.or, accessRefFilters, filter);
				}
			}
			break;

		default:
			throw new CollectionException("Condição não suportada no block Auth");
		}

		if(accessDocFilters == null && accessRefFilters == null)
		{
			return null;
		}
		
		return new Pair<>(accessDocFilters, accessRefFilters);
	}

	private Pair<Filter, Filter> buildFiltrosDeAcessoDeColuna(
		final CollectionWithSchema collection, 
		final AuthColumn coluna) 
	{
		var colunas = collection.getSchemaObj().getColumns();

		var name = coluna.getName();
		var filter = new Filter(
			coluna.getOp() != null? 
				coluna.getOp(): 
				FilterOperator.eq, 
			name, 
			coluna.getValue()
		);
		
		if(name.indexOf('.') < 0 || 
			colunas.get(name).getType() == FieldType.object)
		{
			return new Pair<>(filter, null);
		}
		else
		{
			return new Pair<>(null, filter);
		}
	}

	private Pair<Filter, Filter> buildFiltrosDeAcesso(
		final CollectionWithSchema collection, 
		final List<Reference> references, 
		final User user, 
		final LogicalExpr<AuthAction> expr,
		Filter docFilters,
		Filter refFilters,
		final FilterOperator logOp
	)
	{
		if(expr.getCond() != null)
		{
			var acao = expr.getCond();
			switch(acao.getWhen())
			{
			case USER_SAME:
			{
				var user_ = acao.getUser();
				var res = buildFiltrosDeAcessoDeUser(
					collection, 
					acao.getWhen(),
					user_, 
					references, 
					user, 
					collection.getWorkspace());
				if(res != null)
				{
					docFilters = Filter.by(logOp, docFilters, res.getKey());
					refFilters = Filter.by(logOp, refFilters, res.getValue());
				}
				break;
			}

			case COLUMN_VALUE:
			{
				var res = buildFiltrosDeAcessoDeColuna(
					collection, acao.getColumn());
				if(res != null)
				{
					docFilters = Filter.by(logOp, docFilters, res.getKey());
					refFilters = Filter.by(logOp, refFilters, res.getValue());
				}
				break;
			}
			
			}
		}
		else
		{
			var list = expr.getAnd() != null?
				expr.getAnd():
				expr.getOr();

			var op = expr.getAnd() != null?
				FilterOperator.and:
				FilterOperator.or;
			
			for(var node : list)
			{
				var res = buildFiltrosDeAcesso(
					collection, 
					references, 
					user, 
					node, 
					docFilters, 
					refFilters, 
					op);

				if(res != null)
				{
					docFilters = res.getKey();
					refFilters = res.getValue();
				}
			}
		}		

		if(docFilters == null && refFilters == null)
		{
			return null;
		}

		return new Pair<>(docFilters, refFilters);		
	}

	/**
	 * 
	 * @param collection
	 * @param nomeRelatorio
	 * @param pageable
	 * @param filtros
	 * @param user
	 * @param asAdmin
	 * @return
	 */
	public List<org.bson.Document> gerarRelatorio(
		final CollectionWithSchema collection, 
		final Report relatorio, 
		final Pageable pageable, 
		final Map<String, Object> reqFiltros, 
		final User user, 
		final boolean asAdmin)
	{
		var scriptContext = configScriptContext(collection, user);
		
		// projeção inicial (select)
		var preProjection = buildProjection(collection, user, asAdmin);
		if(relatorio.getConsts() != null)
		{
			for(var entry: relatorio.getConsts().entrySet())
			{
				preProjection.append(
					entry.getKey(), 
					evalValueOrScriptOrFunction(entry.getValue(), scriptContext));
			}
		}
		
		// referências (joins)
		List<Reference> refs = buildReferencias(collection);
		
		// filtros (where)
		Filter docFilters = null;
		Filter refFilters = null;
		
		var filterFormFields = relatorio.getFilter() != null && 
				relatorio.getFilter().getForm() != null? 
			relatorio.getFilter().getForm().getFields(): 
			null;
		var filterFields = relatorio.getFilter() != null? 
			relatorio.getFilter().getFields():
			null;
		
		var filtros = new HashMap<String, Object>();
		if(filterFormFields != null)
		{
			for(var entry: filterFormFields.entrySet())
			{
				var name = entry.getKey();
				var filtro = entry.getValue();
				var value = reqFiltros != null && reqFiltros.containsKey(name)? 
					reqFiltros.get(name): 
					null;
				if(value == null && filtro.getDefault() != null)
				{
					value = filtro.getDefault();
				}
				
				filtros.put(name, value);
				
				if(value != null && !filtro.isTemplate())
				{
					var filter = new Filter(filtro.getOp() != null? 
						filtro.getOp(): 
						FilterOperator.eq, name, value);
					
					if(name.indexOf('.') < 0)
					{
						docFilters = Filter.by(FilterOperator.and, docFilters, filter);
					}
					else
					{
						refFilters = Filter.by(FilterOperator.and, refFilters, filter);
					}
				}
			}
		}
		
		if(filtros.size() > 0)
		{
			scriptContext.put("filters", filtros);
		}
		
		if(filterFields != null)
		{
			for(var entry: filterFields.entrySet())
			{
				var name = entry.getKey();
				var filtro = entry.getValue();
				var op = filtro.getOp() != null? filtro.getOp(): FilterOperator.eq;
				
				Filter filter = null;
				if(op != FilterOperator.between)
				{
					filter = new Filter(op, name, 
						evalValueOrScriptOrFunction(filtro.getValue(), scriptContext));
				}
				else
				{
					filter = new Filter(op, name, 
						evalValueOrScriptOrFunction(filtro.getfrom(), scriptContext), 
						evalValueOrScriptOrFunction(filtro.getTo(), scriptContext));
				}
				
				if(name.indexOf('.') < 0)
				{
					docFilters = Filter.by(FilterOperator.and, docFilters, filter);
				}
				else
				{
					refFilters = Filter.by(FilterOperator.and, refFilters, filter);
				}
			}
		}
		
		// autorizacao
		if(!asAdmin)
		{
			if(collection.getCreatedBy().getId() != user.getId() && 
				collection.getSchemaObj().getAuth() != null)
			{
				var acesso = collection.getSchemaObj().getAuth();
				if(acesso.getRead() != null)
				{
					var res = buildFiltrosDeAcesso(
						collection, 
						refs, 
						user, 
						acesso.getRead(),
						null, 
						null,
						FilterOperator.and);
					if(res != null)
					{
						docFilters = Filter.by(FilterOperator.and, docFilters, res.getKey());
						refFilters = Filter.by(FilterOperator.and, refFilters, res.getValue());
					}
				}
			}
		}

		var columns = relatorio.getColumns();
		var ids = columns.entrySet().stream()
			.filter(e -> e.getValue().getOp() == null)
				.collect(Collectors.toList());
		var aggs = columns.entrySet().stream()
			.filter(e -> e.getValue().getOp() != null)
				.collect(Collectors.toList());
		
		// ids e aggregates
		var aggregates = new ArrayList<Bson>();
		
		for(var entry : ids)
		{
			var id = entry.getValue();
			if(getColunaDaTabela(collection, collection.getSchemaObj().getColumns(), id.getColumn())
				.getType() == FieldType.array)
			{
				aggregates.add(noSqlService.createUnwindAggregate(id.getColumn()));
			}
		}
		
		var idsMap = new HashMap<String, Object>();
		for(var entry : ids)
		{
			var key = entry.getKey();
			var value = entry.getValue().getApply();
			var column = entry.getValue().getColumn();
			idsMap.put(key, value == null? 
				String.format("$%s", column): 
				evalValueOrScriptOrFunction(value, scriptContext, column));
		}
		
		var ops = new HashMap<String, Pair<AggregateOperator, Object>>();
		for(var entry : aggs)
		{
			var agg = entry.getValue();
			var value = evalValueOrScriptOrColumnOrFunction(agg.getApply(), scriptContext);
			var pair = new Pair<AggregateOperator, Object>(agg.getOp(), value);
			ops.put(entry.getKey(), pair);
		}
		
		aggregates.add(noSqlService.createGroupByAggregate(idsMap, ops));
		
		// projeção final
		var proj = new org.bson.Document("_id", 0);
		for(var entry: ids)
		{
			var name = entry.getKey();
			proj.append(name, "$_id." + name);
		}

		for(var entry: aggs)
		{
			var name = entry.getKey();
			proj.append(name, 1);
		}
		
		aggregates.add(noSqlService.createProjectAggregate(proj));
		
		// order by
		org.bson.Document sort = null; 
				
		if(pageable != null && 
			pageable.getSort().isSorted())
		{
			var order = pageable.getSort().get().findFirst().get();
			sort = new org.bson.Document(
				order.getProperty(), 
				order.getDirection() == Direction.ASC? 1: -1);
		}
		else if(relatorio.getOrder() != null)
		{
			for(var entry : relatorio.getOrder().entrySet())
			{
				sort = new org.bson.Document(
					entry.getKey(), 
					entry.getValue() == FieldIndexDir.asc? 1: -1);
			}
		}
		
		if(sort != null)
		{
			aggregates.add(noSqlService.createSortAggregate(sort));
		}
		
		//
		return noSqlService.findAll(
			collection.getWorkspace().getPubId(), 
			collection.getPubId(),
			preProjection,
			refs,
			docFilters,
			refFilters,
			null,
			pageable != null? (int)pageable.getOffset(): 0, 
			pageable != null? pageable.getPageSize(): 0,
			aggregates);
	}
	
	/**
	 * 
	 * @param collection
	 * @param id
	 * @param posNova
	 * @return
	 */
	public Map<String, Object> moverItem(
		final CollectionWithSchema collection, 
		final String id, 
		final long posNova,
		final User user) 
	{
		var db = collection.getWorkspace().getPubId();
		
		var item = findItemById(collection, id);
		if(item == null)
		{
			throw new CollectionException("Item inexistente na coleção");
		}
		
		var posId = collection.getPositionalId();
		if(posId == null)
		{
			throw new CollectionException(
				"Coleção não possui coluna posicional, impossível mover item");
		}
		
		var posAtual = (long)item.get(posId);
		
		if(posAtual == posNova)
		{
			return item;
		}
		
		var first = posNova;
		var qtd = posAtual > posNova? +1: -1;
		var last = posAtual + (posAtual > posNova? -1: +1);
		if(first > last)
		{
			var temp = first;
			first = last;
			last = temp;
		}
		
		// atualizar todos itens entre o item fonte e o destino
		{
			var filters = new Filter(FilterOperator.and, 
				new Filter(FilterOperator.gte, posId, first),
				new Filter(FilterOperator.lte, posId, last));
			
			var incs = new HashMap<String, Integer>();
			incs.put(posId, qtd);
			noSqlService.update(db, collection.getPubId(), filters, null, incs);
		}
		
		// atualizar item fonte
		{
			var vars = new HashMap<String, Object>();
			vars.put(posId, posNova);
			noSqlService.update(db, collection.getPubId(), id, vars);
			item.put(posId, posNova);
		}

		collectionItemLogger.info(
			collection, id, user, "Moveu item");
		
		return item;
	}

	/**
	 * 
	 * @param collection
	 * @param idName
	 * @param id
	 */
	public void removerItem(
		final CollectionWithSchema collection, 
		final String id, 
		final User user,
		final String sessionId) 
	{
		noSqlService.deleteOne(collection.getWorkspace().getPubId(), collection.getPubId(), id);
		
		broadcast(collection, CollectionUpdatedEvent.ITEM_DELETED, sessionId, null, id);

		collectionLogger.info(
			collection, 
			user, 
			"Removeu item", 
			Map.of("id", id));
	}
	
	/**
	 * 
	 * @param collection
	 */
	public void removerItens(
		final Collection collection, 
		final Map<String, Object> filtros, 
		final User user,
		final String sessionId) 
	{
		noSqlService.deleteMany(
			collection.getWorkspace().getPubId(), 
			collection.getPubId(), 
			buildFiltros(collection, filtros));

		broadcast(collection, CollectionUpdatedEvent.ITEMS_DELETED, sessionId, null, null);

		collectionLogger.info(
			collection, 
			user, 
			"Removeu itens", 
			filtros);
	}

	/**
	 * 
	 * @param collection
	 */
	public void removerItensTodos(
		final Collection collection,
		final User user, 
		final String sessionId) 
	{
		noSqlService.deleteAll(
			collection.getWorkspace().getPubId(), 
			collection.getPubId());

		broadcast(collection, CollectionUpdatedEvent.ITEMS_DELETED, sessionId, null, null);

		collectionLogger.info(
			collection, 
			user, 
			"Removeu todos os itens");
	}
	
	private String createColumnName(
		final Map<String, Field> columns)
	{
		var i = 0;
		var name = "";
		do
		{
			name = String.format("col%03d", i++);
		} while(columns.containsKey(name));

		return name;
	}
	
	private FieldType cellTypeToColumnType(
		final String type)
	{
		switch(type)
		{
		case "number":
			return FieldType.number;
		case "array":
			return FieldType.array;
		case "date":
			return FieldType.date;
		case "grid":
			return FieldType.enumeration;
		case "list":
			return FieldType.enumeration;
		case "rating":
			return FieldType.decimal;
		default:
			return FieldType.string;
		}
	}
	
	private FieldComponent cellTypeToColumnComponent(
		final String type)
	{
		switch(type)
		{
		case "array":
			return FieldComponent.array;
		case "date":
			return FieldComponent.date;
		case "grid":
			return FieldComponent.grid;
		case "list":
			return FieldComponent.select;
		case "rating":
			return FieldComponent.rating;
		case "textarea":
			return FieldComponent.textarea;
		case "bool":
			return FieldComponent.bool;
		case "email":
			return FieldComponent.email;
		case "reference":
			return FieldComponent.reference;
		}
		
		return FieldComponent.text;
	}
	
	private FieldIndexDir getIndexDir(
		final FieldType type)
	{
		return type == FieldType.date? 
			FieldIndexDir.desc: 
			FieldIndexDir.asc;
	}
	
	/**
	 * 
	 * @param collection
	 * @param type
	 * @throws IOException 
	 */
	public CollectionWithSchema adicionarColuna(
		final CollectionWithSchema collection, 
		final String type, 
		final int index, 
		final Boolean nullable, 
		final Boolean sortable, 
		final Boolean unique,
		final User user,
		final String sessionId) throws IOException 
	{
		var schema = collection.getSchemaObj();
		
		// tabela.colunas
		var name = createColumnName(schema.getColumns());
		var label = String.format("Coluna-%s", name.substring(name.length()-3));
		var fieldType = cellTypeToColumnType(type);
		var component = cellTypeToColumnComponent(type);
		
		var col = new Field(label, fieldType, component, nullable, sortable, unique);  
		schema.getColumns().put(name, col);
		
		verificarIndicesAndDefaultDaColuna(
			collection, user, schema, collection.getWorkspace().getPubId(), name, col, null);

		if(schema.getViews() != null)
		{
			for(var view: schema.getViews())
			{
				for(var field: view.getFields().values())
				{
					if(field.getIndex() >= index)
					{
						field.setIndex(field.getIndex() + 1);
					}
				}
		
				// view.fields
				var field = new ViewField(index);
				view.getFields().put(name, field);
			}
		}

		//ALERTA: o cliente é obrigado a atualizar o schema todo, já que a order de certos campos pode mudar
		var newSchema = schemaToString(collection.getSchemaObj());
		updateSchema(collection, newSchema, CollectionVersionChangeId.COLUMNS, false);
		
		var res = collectionRepo.save(collection);
		
		broadcast(res, CollectionUpdatedEvent.COLUMN_CREATED, sessionId, res.getSchema(), name);

		collectionLogger.info(
			collection, user, "Adicionou campo");
		
		return res;
	}
	
	private void removerColunaDoSchema(
		final CollectionWithSchema collection, 
		final Field column,
		final String key)
	{
		var schema = collection.getSchemaObj();

		// se a coluna for do type objeto, é necessário procurar pela base do name
		var isObject = column != null? 
			column.getClass_() != null:
			false;
		var partialKey = isObject? 
			key + '.': 
			key;

		String parentKey = null;
		String fieldKey = null;
		var p = key.indexOf(".");
		if(p > 0)
		{
			parentKey = key.substring(0, p);
			fieldKey = key.substring(p+1);
		}

		//FIXME: check columns on .depends

		var indexes = schema.getIndexes();
		if(indexes != null)
		{
			var toRemove = new ArrayList<Index>();
			for(var index: indexes)
			{
				var columns = index.getColumns();
				if(!isObject)
				{
					Collections.removeEntriesWithKey(columns, key);
				}
				else
				{
					Collections.removeEntriesWithPartialKey(columns, partialKey);
				}

				if(columns.size() == 0)
				{
					toRemove.add(index);
				}
			}
			
			indexes.removeAll(toRemove);
			if(indexes.size() == 0)
			{
				schema.setIndexes(null);
			}
		}

		if(column != null && column.getRef() != null)
		{
			var refName = column.getRef().getName();
			var references = schema.getRefs();			
			if(references != null && references.containsKey(refName))
			{
				var cnt = 0;
				for(var col: schema.getColumns().values()) 
				{
					if(col != column && col.getRef() != null && col.getRef().getName().equals(refName))
					{
						++cnt;
					}
				}

				if(cnt == 0)
				{
					references.remove(refName);
					if(references.size() == 0)
					{
						schema.setRefs(null);
					}
				}
			}
		}

		var automations = collection.getAutomations();
		if(automations != null)
		{
			var toRemove = new ArrayList<CollectionAutomation>();
			for(var automation : automations)
			{
				switch(automation.getType())
				{
				case FIELD:
				{
					var auto = (CollectionAutomationField)automation;

					//FIXME: check the logical tree
					break;
				}

				default:
					continue;
				}
			}

			for(var automation : toRemove)
			{
				collectionAutomationRepo.delete(automation);
				automations.remove(automation);
			}
		}

		var integrations = collection.getIntegrations();
		if(integrations != null)
		{
			var toRemove = new ArrayList<CollectionIntegration>();
			for(var integration : integrations)
			{
				var changed = removerColunaIntegration(
					isObject, partialKey, key, integration.getActivities());

				if(integration.getActivities().size() == 0)
				{
					toRemove.add(integration);
				}
				else if(changed > 0)
				{
					collectionIntegrationRepo.save(integration);
				}	
			}

			for(var integration : toRemove)
			{
				collectionIntegrationRepo.delete(integration);
				integrations.remove(integration);
			}
		}

		if(schema.getViews() != null)
		{
			for(var view : schema.getViews())
			{
				var fields = view.getFields();
				Collections.removeEntriesWithKey(fields, key);
				if(isObject)
				{
					Collections.removeEntriesWithPartialKey(fields, partialKey);
				}

				if(fields.size() == 0)
				{
					view.setFields(null);
					fields = null;
				}

				if(parentKey != null && fields != null && fields.containsKey(parentKey))
				{
					var parent = fields.get(parentKey);
					var children = parent.getFields();
					if(children != null)
					{
						Collections.removeEntriesWithKey(children, fieldKey);
						if(children.size() == 0)
						{
							parent.setFields(null);
						}
					}
				}
				
				var filters = view.getFilters();
				if(filters != null)
				{
					if(!isObject)
					{
						Collections.removeEntriesWithKey(filters, key);
					}
					else
					{
						Collections.removeEntriesWithPartialKey(filters, partialKey);
					}

					if(filters.size() == 0)
					{
						view.setFilters(null);
					}
				}
	
				var sort = view.getSort();
				if(sort != null)
				{
					if(!isObject)
					{
						Collections.removeEntriesWithKey(sort, key);
					}
					else
					{
						Collections.removeEntriesWithPartialKey(sort, partialKey);
					}
					
					if(sort.size() == 0)
					{
						view.setSort(null);
					}
				}
			}
		}

		var auth = schema.getAuth();
		if(auth != null)
		{
			if(removerColunaDaAutorizacao(auth.getRead(), key, partialKey, isObject))
			{
				auth.setRead(null);
			}
			if(removerColunaDaAutorizacao(auth.getCreate(), key, partialKey, isObject))
			{
				auth.setCreate(null);
			}
			if(removerColunaDaAutorizacao(auth.getEdit(), key, partialKey, isObject))
			{
				auth.setEdit(null);
			}

			if(auth.getRead() == null &&
				auth.getCreate() == null &&
					auth.getEdit() == null)
			{
				schema.setAuth(null);
			}
		}

		// reports
		var reports = schema.getReports();
		if(reports != null)
		{
			for(var report : reports)
			{
				var columns = report.getColumns();
				if(columns != null)
				{
					var toRemove = new ArrayList<ReportColumn>();
					for(var col : columns.values())
					{
						var colRef = col.getColumn();
						if(colRef != null)
						{
							if(!isObject)
							{
								if(colRef.equals(key))
								{
									toRemove.add(col);
								}
							}
							else
							{
								if(colRef.indexOf(partialKey) == 0)
								{
									toRemove.add(col);
								}
							}
						}
						else
						{
							var apply = col.getApply();
							if(apply != null)
							{
								var colApplyRef = apply.getColumn();
								if(colApplyRef != null)
								{
									if(!isObject)
									{
										if(colApplyRef.equals(key))
										{
											apply.setColumn(null);
										}
									}
									else
									{
										if(colApplyRef.indexOf(partialKey) == 0)
										{
											apply.setColumn(null);
										}
									}
								}
							}
						}
					}
				}

				var filter = report.getFilter();
				if(filter != null)
				{
					{
						var fields = filter.getFields();
						if(fields != null)
						{
							if(!isObject)
							{
								Collections.removeEntriesWithKey(fields, key);
							}
							else
							{
								Collections.removeEntriesWithPartialKey(fields, partialKey);
							}
						}
					}

					var form = filter.getForm();
					if(form != null)
					{
						var fields = form.getFields();
						if(fields != null)
						{
							if(!isObject)
							{
								Collections.removeEntriesWithKey(fields, key, f -> !f.isTemplate());
							}
							else
							{
								Collections.removeEntriesWithKey(fields, key, f -> !f.isTemplate());
							}
						}
					}
				}
			}
		}

		// remover dos flows
		var flows = schema.getFlows();
		if(flows != null)
		{
			for(var flow : flows.values())
			{
				var outs = flow.getOut();
				if(outs != null)
				{
					for(var out : outs.values())
					{
						var cond = out.getCondition();
						if(cond != null)
						{
							if(removerColunaDaCondicaoDoFlow(key, partialKey, isObject, cond))
							{
								out.setCondition(null);
							}
						}
					}
				}
			}
		}

		if(column != null)
		{
			schema.getColumns().remove(key);
		}
	}

	private boolean removerColunaDaAutorizacao(
		final LogicalExpr<AuthAction> action,
		final String key,
		final String partialKey,
		final boolean isObject) 
	{
		if(action == null)
		{
			return false;
		}

		if(action.getCond() != null)
		{
			var cond = action.getCond();
			switch(cond.getWhen())
			{
			case USER_SAME:
				var columns = cond.getUser().getColumns();
				Collections.removeEntriesWithKey(columns, key);
				if(isObject)
				{
					Collections.removeEntriesWithPartialKey(columns, partialKey);
				}

				if(columns.size() == 0)
				{
					return true;
				}
				break;

			case COLUMN_VALUE:
				var col = cond.getColumn().getName();	
				if(col.equals(key))
				{
					col = null;
				}
				if(isObject)
				{
					if(col != null && col.indexOf(partialKey) == 0)
					{
						col = null;
					}
				}

				if(col == null)
				{
					return true;
				}

				break;
			}
		}
		else
		{
			var list = action.getAnd() != null?
				action.getAnd():
				action.getOr();

			var toRemove = new ArrayList<LogicalExpr<AuthAction>>();

			for(var node: list)
			{
				var res = removerColunaDaAutorizacao(
					node, 
					key,
					partialKey,
					isObject
				);

				if(!res)
				{
					toRemove.add(node);
				}
			}

			list.removeAll(toRemove);

			if(list.size() == 0)
			{
				return true;
			}
		}

		return false;
	}

	private boolean removerColunaDaCondicaoDoFlow(
		final String key, 
		final String partialKey,
		final boolean isObject,
		final LogicalExpr<FlowCondition> cond) 
	{
		if(cond.getCond() != null)
		{
			var val = cond.getCond();
			if(val.getColumn() != null)
			{
				var col = val.getColumn();
				var colRef = col.getName();
				if(colRef.equals(key))
				{
					cond.setCond(null);
					return true;
				}
				else if(isObject)
				{
					if(colRef.indexOf(partialKey) == 0)
					{
						cond.setCond(null);
						return true;
					}
				}
			}
		}
		else
		{
			var list = cond.getAnd() != null? 
				cond.getAnd():
				cond.getOr();
			
			var toRemove = new ArrayList<LogicalExpr<FlowCondition>>();
			for(var item : list)
			{
				if(removerColunaDaCondicaoDoFlow(
					key, partialKey, isObject, item))
				{
					toRemove.add(item);
				}
			}

			list.removeAll(toRemove);
			if(list.size() == 0)
			{
				if(cond.getAnd() != null)
				{
					cond.setAnd(null);
				}
				else
				{
					cond.setOr(null);
				}

				return true;
			}
		}

		return false;
	}
	
	private int removerColunaIntegration(
		final boolean isObject,
		final String partialKey,	
		final String key, 
		final List<CollectionIntegrationActivity> activities) 
	{
		var changed = 0;

		var toRemove = new ArrayList<CollectionIntegrationActivity>();

		for(var act : activities)
		{
			//TODO: remove column from insertItemActivity or updateItemActivity
		}

		activities.removeAll(toRemove);

		return changed;
	}

	/**
	 * 
	 * @param collection
	 * @param colunas
	 * @throws IOException
	 */
	public CollectionWithSchema removerColuna(
		final CollectionWithSchema collection, 
		final String key, 
		final User user,
		final String sessionId) throws IOException 
	{
		var schema = collection.getSchemaObj();
		if(!schema.getColumns().containsKey(key))
		{
			throw new CollectionException(String.format("Coluna %s inexistente na coleção", key));
		}
		
		// remover índices que contenham a(s) coluna(s) que será/ão removida(s)
		// NOTA: precisa vir primeiro, porque o próxmo método vai remover os índices do schema
		var column = schema.getColumns().get(key);
		removerColunaDoDB(collection, column, key);

		// remover colunas do schema
		removerColunaDoSchema(collection, column, key);
		
		//ALERTA: o cliente é obrigado a atualizar o schema todo, já que a order de certos campos pode mudar
		var newSchema = schemaToString(collection.getSchemaObj());
		updateSchema(collection, newSchema, CollectionVersionChangeId.COLUMNS, false);

		// atualizar schema no DB
		var res = collectionRepo.save(collection);

		broadcast(res, CollectionUpdatedEvent.COLUMN_DELETED, sessionId, res.getSchema(), key);

		collectionLogger.info(
			collection, 
			user, 
			"Removeu campo", 
			Map.of("id", key));
		
		return res;
	}

	private void removerColunaDoDB(
		final CollectionWithSchema collection, 
		final Field column,
		final String key) 
	{
		var db = collection.getWorkspace().getPubId();
		var schema = collection.getSchemaObj();
		
		// removendo a chave?
		if(reservedNames.contains(key))
		{
			throw new CollectionException("Não é possível remover uma coluna reservada");
		}

		removerIndicesDaColuna(collection, key);

		// objeto? é necessário remover os índices de cada property...
		if(column != null && column.getClass_() != null)
		{
			var klass = schema.getClasses().get(column.getClass_());
			for(var e : klass.getProps().entrySet())
			{
				var propKey = e.getKey();
				var prop = e.getValue();
				var fullKey = String.format("%s.%s", key, propKey);
				if(prop.isSortable() || prop.isUnique() || prop.isPositional())
				{
					removerIndicesDaColuna(collection, fullKey);
				}
			}
		}
		
		// remover colunas no DB NoSql
		noSqlService.deleteFields(db, collection.getPubId(), Arrays.asList(key));
	}

	private void renomearColunaAutorizacao(
		final AuthUser user, 
		final boolean isObject,
		final String partialKey,
		final String curKey, 
		final String newKey) 
	{
		Collections.renameEntriesWithKey(user.getColumns(), curKey, newKey);
		if(isObject)
		{
			Collections.renameEntriesWithPartialKey(user.getColumns(), partialKey, curKey, newKey);
		}
	}

	private void renomearColunaNoSchema(
		final CollectionWithSchema collection, 
		final Field column,
		final String curKey, 
		final String newKey,
		final String newLabel)
	{
		var schema = collection.getSchemaObj();

		// se a coluna for do type objeto, é necessário procurar pela base do name
		var isObject = column != null? 
			column.getClass_() != null:
			false;
		var partialKey = isObject? 
			curKey + '.': 
			curKey;

		String parentKey = null;
		String curFieldKey = null;
		String newFieldKey = null;
		var p = curKey.indexOf(".");
		if(p > 0)
		{
			parentKey = curKey.substring(0, p);
			curFieldKey = curKey.substring(p+1);
			p = newKey.indexOf(".");
			newFieldKey = newKey.substring(p+1);
		}

		//FIXME: check columns on .depends

		var indexes = schema.getIndexes();
		if(indexes != null)
		{
			if(!isObject)
			{
				for(var index: indexes)
				{
					Collections.renameEntriesWithKey(index.getColumns(), curKey, newKey);
				}
			}
			else
			{
				for(var index: indexes)
				{
					Collections.renameEntriesWithPartialKey(index.getColumns(), partialKey, curKey, newKey);
				}
			}
		}

		var automations = collection.getAutomations();
		if(automations != null)
		{
			for(var automation : automations)
			{
				var changed = renomearColunaAutomation(
					isObject, partialKey, curKey, newKey, automation.getActivities());

				switch(automation.getType())
				{
				case FIELD:
				{
					var auto = (CollectionAutomationField)automation;

					//FIXME: check the logical tree

					break;
				}

				default:
					break;
				}

				if(changed > 0)
				{
					collectionAutomationRepo.save(automation);
				}
			}
		}

		var integrations = collection.getIntegrations();
		if(integrations != null)
		{
			for(var integration : integrations)
			{
				var changed = renomearColunaIntegration(
					isObject, partialKey, curKey, newKey, integration.getActivities());

				if(changed > 0)
				{
					collectionIntegrationRepo.save(integration);
				}	
			}
		}

		if(schema.getViews() != null)
		{
			for(var view : schema.getViews())
			{
				var fields = view.getFields();

				Collections.renameEntriesWithKey(fields, curKey, newKey);
				if(isObject)
				{
					Collections.renameEntriesWithPartialKey(fields, partialKey, curKey, newKey);
				}

				if(parentKey != null && fields.containsKey(parentKey))
				{
					var parent = fields.get(parentKey);
					var children = parent.getFields();
					if(children != null && children.containsKey(curFieldKey))
					{
						children.put(newFieldKey, children.get(curFieldKey));
						children.remove(curFieldKey);
					}
				}

				var filters = view.getFilters();
				if(filters != null)
				{
					if(!isObject)
					{
						Collections.renameEntriesWithKey(filters, curKey, newKey);
					}
					else
					{
						Collections.renameEntriesWithPartialKey(filters, partialKey, curKey, newKey);
					}
				}
	
				var sort = view.getSort();
				if(sort != null)
				{
					if(!isObject)
					{
						Collections.renameEntriesWithKey(sort, curKey, newKey);
					}
					else
					{
						Collections.renameEntriesWithPartialKey(sort, partialKey, curKey, newKey);
					}
				}
			}
		}

		var auth = schema.getAuth();
		if(auth != null)
		{
			renomearColunaNoAuth(auth.getRead(), curKey, partialKey, newKey, isObject);
			renomearColunaNoAuth(auth.getCreate(), curKey, partialKey, newKey, isObject);
			renomearColunaNoAuth(auth.getEdit(), curKey, partialKey, newKey, isObject);
		}

		// reports
		var reports = schema.getReports();
		if(reports != null)
		{
			for(var report : reports)
			{
				var columns = report.getColumns();
				if(columns != null)
				{
					for(var col : columns.values())
					{
						var colRef = col.getColumn();
						if(colRef != null)
						{
							if(!isObject)
							{
								if(colRef.equals(curKey))
								{
									col.setColumn(newKey);
								}
							}
							else
							{
								if(colRef.indexOf(partialKey) == 0)
								{
									col.setColumn(colRef.replace(curKey, newKey));
								}
							}
						}
						else
						{
							var apply = col.getApply();
							if(apply != null)
							{
								var colApplyRef = apply.getColumn();
								if(colApplyRef != null)
								{
									if(!isObject)
									{
										if(colApplyRef.equals(curKey))
										{
											apply.setColumn(newKey);
										}
									}
									else
									{
										if(colApplyRef.indexOf(partialKey) == 0)
										{
											apply.setColumn(colApplyRef.replace(curKey, newKey));
										}
									}
								}
							}
						}
					}
				}

				var filter = report.getFilter();
				if(filter != null)
				{
					{
						var fields = filter.getFields();
						if(fields != null)
						{
							if(!isObject)
							{
								Collections.renameEntriesWithKey(
									fields, curKey, newKey);
							}
							else
							{
								Collections.renameEntriesWithPartialKey(
									fields, partialKey, curKey, newKey);
							}
						}
					}

					var form = filter.getForm();
					if(form != null)
					{
						var fields = form.getFields();
						if(fields != null)
						{
							if(!isObject)
							{
								Collections.renameEntriesWithKey(
									fields, curKey, newKey, field -> !field.isTemplate());
							}
							else
							{
								Collections.renameEntriesWithPartialKey(
									fields, partialKey, curKey, newKey, field -> !field.isTemplate());
							}
						}
					}
				}
			}
		}

		// renomear nos flows
		var flows = schema.getFlows();
		if(flows != null)
		{
			for(var flow : flows.values())
			{
				var outs = flow.getOut();
				if(outs != null)
				{
					for(var out : outs.values())
					{
						var conds = out.getCondition();
						renomearColunaNasCondicoesDoFlow(curKey, newKey, partialKey, isObject, conds);
					}
				}
			}
		}

		if(parentKey == null && newLabel != null)
		{
			var columns = schema.getColumns();
			var col = columns.get(curKey);
			col.setLabel(newLabel);
			columns.put(newKey, col);
			columns.remove(curKey);
		}
	}

	private void renomearColunaNoAuth(
		final LogicalExpr<AuthAction> action,
		final String curKey, 
		final String partialKey,
		final String newKey, 
		final boolean isObject) 
	{
		if(action == null)
		{
			return;
		}

		var cond = action.getCond();
		if(cond != null)
		{
			switch(cond.getWhen())
			{
			case USER_SAME:
				renomearColunaAutorizacao(
					cond.getUser(), isObject, partialKey, curKey, newKey);
				break;

			case COLUMN_VALUE:
				var col = cond.getColumn().getName();
				if(col.equals(curKey))
				{
					cond.getColumn().setName(newKey);
				}
				if(isObject)
				{
					if(col.indexOf(partialKey) == 0)
					{
						cond.getColumn().setName(newKey);
					}
				}
				break;
			}
		}
		else
		{
			var list = action.getAnd() != null?
				action.getAnd():
				action.getOr();

			for(var node : list)
			{
				renomearColunaNoAuth(
					node, curKey, partialKey, newKey, isObject);
			}
		}
	}

	private void renomearColunaNasCondicoesDoFlow(
		final String curKey, 
		final String newKey, 
		final String partialKey,
		final boolean isObject, 
		final LogicalExpr<FlowCondition> cond) 
	{
		if(cond != null)
		{
			if(cond.getCond() != null)
			{
				var val = cond.getCond();
				if(val.getColumn() != null)
				{
					var col = val.getColumn();
					var colRef = col.getName();
					if(colRef.equals(curKey))
					{
						col.setName(newKey);
					}
					else if(isObject)
					{
						if(colRef.indexOf(partialKey) == 0)
						{
							col.setName(colRef.replace(curKey, newKey));
						}
					}
				}
			}
			else
			{
				var list = cond.getAnd() != null? 
					cond.getAnd():
					cond.getOr();
				for(var item : list)
				{
					renomearColunaNasCondicoesDoFlow(
						curKey, newKey, partialKey, isObject, item);
				}
			}
		}
	}

	private int renomearColunaAutomation(
		final boolean isObject,
		final String partialKey, 	
		final String curKey, 
		final String newKey, 
		final List<CollectionAutomationActivity> activities) 
	{
		var changed = 0;

		for(var act : activities)
		{
			//FIXME: remove column from insertItemActivity or updateItemActivity
		}

		return changed;	
	}

	private int renomearColunaIntegration(
		final boolean isObject,
		final String partialKey,
		final String curKey, 
		final String newKey, 
		final List<CollectionIntegrationActivity> activities) 
	{
		var changed = 0;

		for(var act : activities)
		{
			//FIXME: remove column from insertItemActivity or updateItemActivity
		}

		return changed;
	}

	private void renomearColunaNoDB(
		final CollectionWithSchema collection, 
		final Field column,
		final String curKey, 
		final String newKey)
	{
		var db = collection.getWorkspace().getPubId();
		var indicesDropados = new ArrayList<Index>();
		var schema = collection.getSchemaObj();

		// se a coluna for do type objeto, é necessário procurar pela base do name
		var isObject = column != null? 
			column.getClass_() != null:
			false;
		var partialKey = isObject? 
			curKey + '.': 
			curKey;

		// renomeando a chave?
		if(reservedNames.contains(curKey))
		{
			throw new CollectionException("Não é possível renomear uma coluna reservada");
		}

		// remover índices que contenham a coluna que será renomeada
		var indices = schema.getIndexes();
		if(indices != null)
		{
			Set<Index> matches = null;
			
			if(!isObject)
			{
				matches = indices.stream()
					.filter(i -> i.getColumns().contains(curKey))
						.collect(Collectors.toSet());
			}
			else
			{
				matches = indices.stream()
					.filter(i -> i.getColumns().stream()
							.anyMatch(c -> c.indexOf(partialKey) == 0))
						.collect(Collectors.toSet());
			}
					
			for(var indice: matches) 
			{
				var colunasIndice = indice.getColumns();
				
				if(!indicesDropados.contains(indice)) 
				{
					indicesDropados.add(indice);
					try 
					{
						noSqlService.dropIndex(
								db, 
								collection.getPubId(), 
								colunasIndice, 
								indice.getDir() == FieldIndexDir.asc);
					}
					catch(Exception e)
					{
					}
				}
				
				colunasIndice.add(newKey);
				if(!isObject)
				{
					colunasIndice.remove(curKey);
				}
				else
				{
					indice.setColumns(
						colunasIndice.stream()
							.filter(c -> c.indexOf(partialKey) != 0)
								.collect(Collectors.toList()));
				}
			}
		}	
		
		// renomear coluna no DB NoSql
		noSqlService.renameField(db, collection.getPubId(), curKey, newKey);
		
		// recriar os índices que foram dropados
		for(var indice: indicesDropados)
		{
			if(indice.getColumns().size() > 0)
			{
				noSqlService.createIndex(
						db, 
						collection.getPubId(), 
						indice.getColumns(), 
						indice.getDir() == FieldIndexDir.asc, 
						indice.isUnique());
			}
		}
	}
	
	private void validateColumnKey(final String key) 
	{
		if(key.length() < 2 || key.length() > 32)
		{
			throw new CollectionException(String.format(
				"Coluna %s deve possuir name entre 2 e 32 caracteres", key));
		}
		
		var ch = key.charAt(0);
		if(!Character.isJavaIdentifierStart(ch))
		{
			throw new CollectionException(String.format(
				"Coluna %s deve possuir name iniciado com caractere válido", key));
		}

		if(reservedNames.contains(key))
		{
			throw new CollectionException(String.format(
				"Coluna %s não pode ter os nomes reservados", key));
		}
	}

	private static char[] camelCaseDelimiters = new char[] { '_' };

	private String labelToKey(final String label) 
	{
		return CaseUtils.toCamelCase(label.trim().toLowerCase()
			.replaceAll("[\\\\|/|:|\\*|!|@|\"|<|>|\\(|\\)|\\[|\\]|{|}|^|~|´|`|\\.|,|\\+|\\-|\\||'|$|#|%|&|?| |\t]", "_")
			.replace("ç", "c")
			.replaceAll("[á|à|â|ã|ä]", "a")
			.replaceAll("[é|è|ê|ë]", "e")
			.replaceAll("[í|ì|î|ï]", "i")
			.replaceAll("[ó|ò|ô|õ|ö]", "i")
			.replaceAll("[ú|ù|û|ü]", "u"), false, camelCaseDelimiters);
	}

	/**
	 * 
	 * @param colunas
	 * @param name
	 * @return
	 */
	public Pair<String, Field> lookupColunaPeloNome(
		final Map<String, Field> colunas, 
		final String name) 
	{
		var key = name.toLowerCase();
		
		if(colunas.containsKey(key))
		{
			return new Pair<>(key, colunas.get(key));
		}
		else
		{
			var col = colunas.entrySet().stream()
				.filter(e -> e.getValue().getLabel() != null && 
					e.getValue().getLabel().toLowerCase().equals(key))
					.findFirst();
			
			if(!col.isPresent())
			{
				throw new CollectionException(String.format("Coluna %s inexistente", name));
			}

			var entry = col.get();

			return new Pair<>(entry.getKey(), entry.getValue());
		}
	}

	/**
	 * 
	 * @param user
	 * @param workspace
	 */
	public void validarAcesso(
		final User user, 
		final Workspace workspace) 
	{
		if(!user.isAdmin(workspace.getId()))
		{
			throw new CollectionException("Acesso negado");
		}
	}

	private boolean verificarSeUserTemAcesso(
		final Collection collection, 
		final User user, 
		final CollectionAuthRole role,
		final Workspace workspace)
	{
		var perms = collectionAuthRepo
			.findAllByCollectionAndUserAndWorkspace(
				collection, user, workspace);
		if(perms == null)
		{
			return false;
		}
		
		if(perms.stream()
			.anyMatch(p -> 
				role.getValue() >= p.getRole().getValue()))
		{
			return true;
		}

		return false;
	}

	/**
	 * 
	 * @param role
	 * @param collection
	 * @param user
	 * @param workspace
	 */
	public void validarAcesso(
		final CollectionAuthRole role, 
		final Collection collection, 
		final User user, 
		final Workspace workspace) 
	{
		if(!user.isSuperAdmin())
		{
			if(collection.getWorkspace().getId() != workspace.getId())
			{
				throw new CollectionException("Access denied");
			}
		
			if(!user.isAdmin(workspace.getId()))
			{
				if(!verificarSeUserTemAcesso(collection, user, role, workspace))
				{
					throw new CollectionException("Access denied");
				}
			}
		}
	}
	
	/**
	 * 
	 * @param collection
	 * @param form
	 * @param user
	 * @param workspace
	 */
	public void validarAcessoAoForm(
		final Collection collection, 
		final Form form,
		final User user, 
		final Workspace workspace) 
	{
		if(!user.isSuperAdmin())
		{
			if(collection.getWorkspace().getId() != workspace.getId())
			{
				throw new CollectionException("Access denied");
			}
		
			if(!user.isAdmin(workspace.getId()))
			{
				var role = CollectionAuthRole.READER;
				switch(form.getUse())
				{
				case create:
					role = CollectionAuthRole.CREATOR;
					break;
				case edit:
					role = CollectionAuthRole.EDITOR;
					break;
				default:
					break;
				}
				
				if(!verificarSeUserTemAcesso(collection, user, role, workspace))
				{
					throw new CollectionException("Access denied");
				}
			}
		}
	}

	/**
	 * 
	 * @param collection
	 * @param idItem
	 * @param user
	 * @param workspace
	 */
	public void validarAcessoItem(
		final CollectionWithSchema collection, 
		final String idItem, 
		final User user, 
		final Workspace workspace) throws Exception
	{
		Callable<Void> throwError = () -> {
			throw new CollectionException(String.format(
				"Acesso negado para %s de item da coleção", idItem != null? "modificação": "criação"));
		};

		if(collection.getCreatedBy().getId() == user.getId())
		{
			return;
		}
		
		var acesso = collection.getSchemaObj().getAuth();
		if(acesso == null)
		{
			throwError.call();
		}
		
		var acessoAcao = idItem != null? 
			acesso.getEdit(): 
			acesso.getCreate();
		if(acessoAcao == null)
		{
			throwError.call();
		}
		
		var references = buildReferencias(collection);
		
		var filters = buildFiltrosDeAcesso(
			collection, 
			references, 
			user, 
			acessoAcao,
			null, 
			null,
			FilterOperator.and);
		if(filters == null)
		{
			return;
		}

		var accessDocFilters = filters.getKey(); 
		var accessRefFilters = filters.getValue();
		
		var docFilters = new Filter(FilterOperator.eq, ID_NAME, idItem);
		docFilters = Filter.by(FilterOperator.and, docFilters, accessDocFilters);

		if(noSqlService.findOne(
			workspace.getPubId(), 
			collection.getPubId(),
			buildProjection(collection, user, false),
			accessRefFilters != null? references: null, 
			docFilters, 
			accessRefFilters) == null)
		{
			throwError.call();
		}
	}

	/**
	 * 
	 * @param doc
	 * @return
	 */
	public Map<String, Object> documentToMap(
		final org.bson.Document doc)
	{
		var res = new HashMap<String, Object>();
		
		for(var entry: doc.entrySet())
		{
			var key = entry.getKey();
			if(!key.equals("_id"))
			{
				res.put(key, valueToObject(entry.getValue()));
			}
		}
		
		return res;
	}

	/**
	 * 
	 * @param list
	 * @return
	 */
	public List<Map<String, Object>> documentListToMap(
		final List<org.bson.Document> list)
	{
		var res = new ArrayList<Map<String, Object>>();
		
		for(var entry: list)
		{
			res.add(documentToMap(entry));
		}
		
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private Object valueToObject(
		final Object value)
	{
		if(value instanceof org.bson.Document)
		{
			return documentToMap((org.bson.Document)value);
		}
		else if(value instanceof ArrayList)
		{
			return arrayToObject((ArrayList<Object>)value);
		}
		else
		{
			return value;
		}
	}
	
	private Object arrayToObject(
		final ArrayList<Object> array) 
	{
		var res = new ArrayList<Object>();
		for(var i = 0; i < array.size(); i++)
		{
			res.add(valueToObject(array.get(i)));
		}
		return res;
	}

	/**
	 * 
	 * @param document
	 * @param collection
	 * @param itemId
	 * @param user
	 * @param workspace
	 * @return
	 */
	public CollectionItemDocument adicionarDocumentAoItem(
		final Document document, 
		final Collection collection, 
		final String itemId, 
		final User user, 
		final Workspace workspace)
	{
		var idoc = new CollectionItemDocument();
		idoc.setWorkspace(workspace);
		idoc.setCollection(collection);
		idoc.setItemId(itemId);
		idoc.setDocument(document);
		idoc.setCreatedBy(user);
		idoc.setCreatedAt(ZonedDateTime.now());

		var res = collectionItemDocumentRepo.save(idoc);

		collectionItemLogger.info(
			collection, 
			itemId, 
			user, 
			"Adicionou document",
			Map.of("id", document.getPubId()));
		
		return res;
	}
	
	/**
	 * 
	 * @param collection
	 * @param itemId
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<Document> listarDocumentsDoItem(
		final CollectionWithSchema collection, 
		final String itemId, 
		final Workspace workspace, 
		final Pageable pageable)
	{
		var docs = collectionItemDocumentRepo
			.findAllByCollectionAndItemIdAndWorkspace(collection, itemId, workspace, pageable);

		return docs.stream()
			.map(d -> d.getDocument())
				.collect(Collectors.toList());
	}

	/**
	 * 
	 * @param collection
	 * @param itemId
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<CollectionItemLog> listarLogsDoItem(
		final CollectionWithSchema collection, 
		final String itemId, 
		final Workspace workspace, 
		final Pageable pageable)
	{
		var logs = collectionItemLogRepo
			.findAllByCollectionAndItemIdAndWorkspace(collection, itemId, workspace, pageable);

		return logs;
	}

	/**
	 * 
	 * @param itemId
	 * @param postId
	 * @param workspace
	 * @return
	 */
	public CollectionItemPost findItemPostByPubIdAndWorkspace(
		final String postId, 
		final Workspace workspace
	)
	{
		var res = collectionItemPostRepo
			.findByPubIdAndWorkspace(postId, workspace);

		return res;
	}

	/**
	 * 
	 * @param collection
	 * @param itemId
	 * @param levels
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<CollectionItemPost> listarPostsDoItem(
		final CollectionWithSchema collection, 
		final String itemId, 
		final short levels,
		final Workspace workspace, 
		final Pageable pageable)
	{
		if(levels == 0)
		{
			return collectionItemPostRepo
				.findAllByItemIdAndType(
					collection, itemId, workspace, WorkspacePostType.TOPIC, pageable);
		}
		else
		{
			return collectionItemPostRepo
				.findAllByAndItemIdAndLevels(
					collection, itemId, workspace, levels, pageable);
		}
	}

	/**
	 * 
	 * @param collection
	 * @param itemId
	 * @param postId
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<CollectionItemPost> listarPostsDoItem(
		final CollectionWithSchema collection, 
		final String itemId, 
		final String postId,
		final Workspace workspace, 
		final Pageable pageable)
	{
		var post = collectionItemPostRepo
			.findByPubIdAndWorkspace(postId, workspace);
		if(post == null)
		{
			throw new CollectionException("Comentário inexistente");
		}

		return collectionItemPostRepo
			.findTreeByPostId(
				collection, itemId, post.getId(), workspace, pageable);
	}	

	/**
	 * 
	 * @param collection
	 * @param itemId
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	@Transactional
	public CollectionItemPost criarPostNoItem(
		final CollectionWithSchema collection, 
		final String itemId, 
		final WorkspacePostRequest req,
		final User user,
		final Workspace workspace)
	{
		var parent = req.getParentId() != null?
			workspacePostRepo
				.findByPubIdAndWorkspace(req.getParentId(), workspace):
			null;

		var type = parent == null || parent.getType() == WorkspacePostType.FORUM? 
			WorkspacePostType.TOPIC: 
			WorkspacePostType.POST;
		
		switch(type)
		{
		case FORUM:
		case TOPIC:
			if(req.getTitle() == null || req.getTitle().trim().length() == 0)
			{
				throw new CollectionException("Campo 'Título' deve ser definido");
			}
			break;

		case POST:
			if(req.getTitle() != null)
			{
				throw new CollectionException("Campo 'Título' deve ser nulo");
			}
			break;
		}

		var post = new CollectionItemPost();

		post.setWorkspace(workspace);
		post.setCollection(collection);
		post.setItemId(itemId);
		post.setParent(parent);
		post.setType(type);
		post.setLevel(parent == null? 
			0: 
			(short)(parent.getLevel() + 1));
		post.setOptions(0);
		post.setOrder((short)0);
		post.setPosts(0);
		post.setTitle(req.getTitle() != null? 
			req.getTitle().trim(): 
			null);
		post.setMessage(req.getMessage() != null? 
			req.getMessage().trim(): 
			null);
		post.setCreatedBy(user);
		post.setCreatedAt(ZonedDateTime.now());

		post = collectionItemPostRepo.save(post);

		if(parent != null)
		{
			parent.setPosts(parent.getPosts() + 1);
			workspacePostRepo.save(parent);
		}

		collectionItemLogger.info(
			collection,
			itemId,
			user, 
			String.format(
				"Postou %s", 
				type == WorkspacePostType.TOPIC? 
					"tópico": 
					"resposta"),
			Map.of("id", post.getPubId()));

		return post;
	}

	/**
	 * 
	 * @param itemId
	 * @param post
	 * @param user
	 * @param workspace
	 */
	public void validarAcessoDeEscritaAoPostDeItem(
		final String itemId,
		final CollectionItemPost post,
		final User user,
		final Workspace workspace
	)
	{
		if(post == null)
		{
			throw new CollectionException("Post inexistente");
		}

		if(!post.getItemId().equals(itemId))
		{
			throw new CollectionException("Post não pertence ao item da Coleção");
		}

		if(post.getCreatedBy().getId() != user.getId())
		{
			if(!user.isAdmin(workspace.getId()))
			{
				throw new CollectionException("Acesso negado");
			}
		}
	}

	/**
	 * 
	 * @param collection
	 * @param itemId
	 * @param idPost
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	public CollectionItemPost atualizarPostNoItem(
		final CollectionWithSchema collection, 
		final String itemId, 
		final CollectionItemPost post,
		final WorkspacePostRequest req,
		final User user,
		final Workspace workspace)
	{
		post.setTitle(req.getTitle());
		post.setMessage(req.getMessage());
		post.setUpdatedBy(user);
		post.setUpdatedAt(ZonedDateTime.now());

		var res = collectionItemPostRepo.save(post);

		collectionItemLogger.info(
			collection,
			itemId, 
			user, 
			String.format(
				"Alterou %s", 
				post.getType() == WorkspacePostType.TOPIC? 
					"tópico": 
					"resposta"),
			Map.of("id", post.getPubId()));

		return res;
	}	

	/**
	 * 
	 * @param collection
	 * @param itemId
	 * @param idPost
	 * @param user
	 * @param workspace
	 * @return
	 */
	@Transactional
	public void removerPostNoItem(
		final CollectionWithSchema collection, 
		final String itemId, 
		final CollectionItemPost post,
		final User user,
		final Workspace workspace)
	{
		var type = post.getType();
		var pubId = post.getPubId();

		var parent = post.getParent()!= null?
			workspacePostRepo
				.findByPubIdAndWorkspace(post.getParent().getPubId(), workspace):
			null;

		collectionItemPostRepo.delete(post);

		if(parent != null)
		{
			parent.setPosts(parent.getPosts() - 1);
			workspacePostRepo.save(parent);
		}

		collectionItemLogger.info(
			collection,
			itemId, 
			user, 
			String.format(
				"Removeu %s", 
				type == WorkspacePostType.TOPIC? 
					"tópico": 
					"resposta"),
			Map.of("id", pubId));

	}

	/**
	 * 
	 * @param collection
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<CollectionLog> listarLogs(
		final CollectionWithSchema collection, 
		final Workspace workspace, 
		final Pageable pageable)
	{
		var logs = collectionLogRepo
			.findAllByCollectionAndWorkspace(collection, workspace, pageable);

		return logs;
	}	

	/**
	 * 
	 * @param collection
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<CollectionPost> listarPosts(
		final CollectionWithSchema collection, 
		final Workspace workspace, 
		final Pageable pageable)
	{
		var posts = collectionPostRepo
			.findAllByCollectionAndWorkspaceAndType(
				collection, workspace, WorkspacePostType.TOPIC, pageable);

		return posts;
	}

	/**
	 * 
	 * @param collection
	 * @param name
	 * @return
	 */
	public View findViewByName(
		final CollectionWithSchema collection, 
		final String name) 
	{
		return collection.getSchemaObj().getViews().stream()
				.filter(v -> v.getName().equals(name))
				.findFirst()
					.orElseThrow(() -> new CollectionException("View inexistente na coleção") );

	}

	private Map<String, Klass> classSchemaToMap(
		final String schema)
	{
		Map<String, Klass> res = null;
		try 
		{
			res = objectMapper.readValue(schema, new TypeReference<Map<String, Klass>>() {});
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

		validarViolacoesNoSchema(res);

		return res;
	}

	private Map<String, Field> columnsSchemaToMap(
		final String schema)
	{
		Map<String, Field> res = null;
		try 
		{
			res = objectMapper.readValue(schema, new TypeReference<Map<String, Field>>() {});
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

		validarViolacoesNoSchema(res);

		return res;
	}

	private Map<String, Const> constantsSchemaToMap(String schema)
	{
		Map<String, Const> res = null;
		try 
		{
			res = objectMapper.readValue(schema, new TypeReference<Map<String, Const>>() {});
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

		validarViolacoesNoSchema(res);

		return res;
	}

	private Map<String, Flow> flowsSchemaToMap(String schema)
	{
		Map<String, Flow> res = null;
		try 
		{
			res = objectMapper.readValue(schema, new TypeReference<Map<String, Flow>>() {});
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

		validarViolacoesNoSchema(res);

		return res;
	}

	private List<Index> indexesSchemaToList(String schema)
	{
		List<Index> res = null;
		try 
		{
			res = objectMapper.readValue(schema, new TypeReference<List<Index>>() {});
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

		validarViolacoesNoSchema(res);

		return res;
	}

	private void removerCampo(
		final CollectionWithSchema collection, 
		final Klass classe, 
		final String nomeClasse, 
		final String name, 
		final boolean updateSelf)
	{
		var schema = collection.getSchemaObj();

		for(var entry : schema.getColumns().entrySet())
		{
			var key = entry.getKey();
			var column = entry.getValue();
			if(column.getClass_() != null && column.getClass_().equals(nomeClasse))
			{
				var fullKey = key + '.' + name;
				// NOTA: precisa vir primeiro, porque o próxmo método vai remover os índices do schema
				removerColunaDoDB(collection, null, fullKey);
				removerColunaDoSchema(collection, null, fullKey);
			}
		}

		if(updateSelf)
		{
			classe.getProps().remove(name);
			if(classe.getProps().size() == 0)
			{
				collection.getSchemaObj().getClasses().remove(nomeClasse);
			}
		}
	}

	private String renomearCampo(
		final CollectionWithSchema collection, 
		final Klass klass, 
		final String className, 
		final String fromKey, 
		final String toLabel, 
		final boolean updateClass)
	{
		var toKey = labelToKey(toLabel);
		validateColumnKey(toKey);

		var schema = collection.getSchemaObj();
		for(var entry : schema.getColumns().entrySet())
		{
			var key = entry.getKey();
			var column = entry.getValue();
			if(column.getClass_() != null && column.getClass_().equals(className))
			{
				var fullCurKey = key + '.' + fromKey;
				var fullNewKey = key + '.' + toKey;
				// NOTA: precisa vir primeiro, porque o próximo método vai remover os índices do schema
				renomearColunaNoDB(collection, null, fullCurKey, fullNewKey);
				renomearColunaNoSchema(collection, null, fullCurKey, fullNewKey, null);
			}
		}

		if(updateClass)
		{
			var props = schema.getClasses().get(className).getProps();
			var campo = props.get(fromKey);
			campo.setLabel(toLabel);
			props.put(toKey, campo);
			props.remove(fromKey);
		}

		return toKey;
	}	

	/**
	 * 
	 * @param collection
	 * @param schema
	 * @param sessionId
	 * @return
	 */
	public CollectionWithSchema atualizarClasses(
		final CollectionWithSchema collection, 
		final String schema, 
		final User user,
		final Workspace workspace,
		final String sessionId) 
	{
		return atualizarClasses(
			collection, schema, user, workspace, sessionId, false);
	}

	private CollectionWithSchema atualizarClasses(
		final CollectionWithSchema collection, 
		final String schema, 
		final User user,
		final Workspace workspace,
		final String sessionId,
		final boolean reversing) 
	{
		var toClasses = classSchemaToMap(schema);

		validarClassesNoSchema(toClasses);

		var db = workspace.getPubId();

		var fromSchema = collection.getSchemaObj();
		var fromClasses = fromSchema.getClasses();

		if(fromClasses != null)
		{
			for(var entry : toClasses.entrySet())
			{
				var classKey = entry.getKey();
				var toClass = entry.getValue();
				if(fromClasses.containsKey(classKey)) 
				{
					var colsOfThisClass = fromSchema.getColumns().entrySet().stream()
						.filter(e -> e.getValue().getClass_() != null && e.getValue().getClass_().equals(classKey))
							.map(e -> e.getKey())
								.collect(Collectors.toList());
		
					var fromClass = fromClasses.get(classKey);
					
					for(var pentry : fromClass.getProps().entrySet())
					{
						var propKey = pentry.getKey();
						var fromProp = pentry.getValue();
						var toProps = toClass.getProps();
						// verificar campos removidos...
						if(!toProps.containsKey(propKey))
						{
							removerCampo(collection, fromClass, classKey, propKey, false);
						}
						else
						{
							var toProp = toProps.get(propKey);
							// verificar campos renomeados...
							if(!fromProp.getLabel().equals(toProp.getLabel()))
							{
								var toPropKey = renomearCampo(collection, fromClass, classKey, propKey, toProp.getLabel(), false);
								toProps.put(toPropKey, toProp);
								toProps.remove(propKey);
								propKey = toPropKey;
							}

							// verificar se é necessário criar ou remover índice...
							if(toProp.isSortable() || toProp.isUnique() || toProp.isPositional())
							{
								if(!(fromProp.isSortable() || fromProp.isUnique()))
								{
									for(var colKey : colsOfThisClass)
									{
										var fullKey = String.format("%s.%s", colKey, propKey);
										var index = criarIndiceNoSchema(fromSchema, fullKey, toProp.getUnique(), getIndexDir(toProp.getType()));
										if(index != null)
										{
											criarIndiceNoDB(db, fullKey, index);
										}
									}
								}
							}
							else
							{
								if(fromProp.isSortable() || fromProp.isUnique() || fromProp.isPositional())
								{
									for(var colKey : colsOfThisClass)
									{
										var fullKey = String.format("%s.%s", colKey, propKey);
										removerIndicesDaColuna(collection, fullKey);
									}
								}
							}

							// se default foi definido, é necessário varrer TODOS os itens e setar o valor se estiver null
							if(toProp.getDefault() != null)
							{
								if(fromProp.getDefault() == null)
								{
									// só permitir se houver índice (abrir excessão para objetos...)
									if(toProp.isSortable() || toProp.isUnique() || toProp.getClass_() != null)
									{
										for(var colKey : colsOfThisClass)
										{
											var fullKey = String.format("%s.%s", colKey, propKey);
											atribuirValoresDefault(db, collection, user, fullKey, toProp);
										}
									}
								}
							}

						}
					}
				}
			}

			// verificar classes removidas...
			for(var entry : fromClasses.entrySet())
			{
				var classKey = entry.getKey();
				if(!toClasses.containsKey(classKey)) 
				{
					var colsOfThisClass = fromSchema.getColumns().entrySet().stream()
						.filter(e -> e.getValue().getClass_() != null && e.getValue().getClass_().equals(classKey))
							.map(e -> e.getKey())
								.collect(Collectors.toList());
					
					for(var colKey : colsOfThisClass)
					{
						// NOTA: precisa vir primeiro, porque o próximo método vai remover os índices do schema
						var col = fromSchema.getColumns().get(colKey);
						removerColunaDoDB(collection, col, colKey);
						removerColunaDoSchema(collection, col, colKey);
					}
				}
			}
		}

		fromSchema.setClasses(toClasses);

		//ALERTA: o cliente é obrigado a atualizar o schema todo, já que a order de certos campos pode mudar
		var newSchema = schemaToString(fromSchema);
		updateSchema(collection, newSchema, CollectionVersionChangeId.CLASSES, reversing);

		var res = collectionRepo.save(collection);

		broadcast(res, CollectionUpdatedEvent.SCHEMA_UPDATED, sessionId, res.getSchema(), null);

		collectionLogger.info(
			collection, user, "Atualizou classes");
		
		return res;
	}

	/**
	 * 
	 * @param collection
	 * @param fromKey
	 * @param to
	 * @param user
	 * @param workspace
	 * @param sessionId
	 * @return
	 */
	public Pair<String, CollectionWithSchema> atualizarColuna(
		final CollectionWithSchema collection, 
		final String fromKey, 
		final Field to,
		final User user, 
		final Workspace workspace, 
		final String sessionId) 
	{
		return atualizarColuna(
			collection, fromKey, to, user, workspace, sessionId, false);
	}

	private	Pair<String, CollectionWithSchema> atualizarColuna(
		final CollectionWithSchema collection, 
		final String fromKey, 
		final Field to,
		final User user, 
		final Workspace workspace, 
		final String sessionId, 
		final boolean reversing) 
	{
		var db = workspace.getPubId();

		var fromSchema = collection.getSchemaObj();
		var fromColumns = fromSchema.getColumns();

		var from = fromColumns.get(fromKey);
		var fromLabel = from.getLabel() != null? 
			from.getLabel(): 
			fromKey;
		var key = fromKey;
		if(!fromLabel.equals(to.getLabel()))
		{
			var label = to.getLabel();
			var toKey = labelToKey(label);
			validateColumnKey(toKey);
			
			// NOTA: precisa vir primeiro, porque o próxmo método vai remover os índices do schema
			if(!fromKey.equals(toKey))
			{
				renomearColunaNoDB(collection, from, fromKey, toKey);
				renomearColunaNoSchema(collection, from, fromKey, toKey, null);
				key = toKey;

				fromColumns.remove(fromKey);
			}
		}

		fromColumns.put(key, to);

		// verificar alterações que necessitem criação ou remoção de índice
		verificarIndicesAndDefaultDaColuna(
			collection, user, fromSchema, db, key, to, from);

		//
		if(to.isAuto())
		{
			criarOuAtualizarAuto(db, collection, key);
			collection.setAutoGenId(key);
		}

		if(to.isPositional())
		{
			criarOuAtualizarPosicional(db, collection, key);
			collection.setPositionalId(key);
		}

		//ALERTA: o cliente é obrigado a atualizar o schema todo, já que a order de certos campos pode mudar
		var newSchema = schemaToString(fromSchema);
		updateSchema(collection, newSchema, CollectionVersionChangeId.COLUMNS, reversing);

		var res = collectionRepo.save(collection);

		broadcast(res, CollectionUpdatedEvent.SCHEMA_UPDATED, sessionId, res.getSchema(), null);

		collectionLogger.info(
			collection, user, "Atualizou campo", Map.of("id", fromKey));

		return new Pair<>(key, collection);
	}
	
	/**
	 * 
	 * @param collection
	 * @param schema
	 * @param user
	 * @param workspace
	 * @param sessionId
	 * @return
	 */
	public CollectionWithSchema atualizarColunas(
		final CollectionWithSchema collection, 
		final String schema, 
		final User user,
		final Workspace workspace,
		final String sessionId) 
	{
		return atualizarColunas(collection, schema, user, workspace, sessionId, false);
	}

	private CollectionWithSchema atualizarColunas(
		final CollectionWithSchema collection, 
		final String schema, 
		final User user,
		final Workspace workspace,
		final String sessionId,
		final boolean reversing) 
	{
		var toColumns = columnsSchemaToMap(schema);

		var fromSchema = collection.getSchemaObj();
		var fromColumns = fromSchema.getColumns();

		var db = workspace.getPubId();

		// procurar coluna "auto": true
		var autoIds = getAutoIds(toColumns);
		String autoId = null;
		if(autoIds != null && autoIds.size() > 0)
		{
			autoId = autoIds.get(0);
		}
		
		// procurar coluna "positional": true
		var posIds = getPosIds(toColumns);
		String posId = null;
		if(posIds != null && posIds.size() > 0)
		{
			posId = posIds.get(0);
		}		

		validarColunasNoSchema(
			fromSchema, toColumns, fromSchema.getClasses(), fromSchema.getRefs(), autoIds, posIds);

		var renamedKeys = new HashMap<String, String>();
		
		// verificar colunas renomeadas...
		for(var entry : toColumns.entrySet())
		{
			var colKey = entry.getKey();
			var toCol = entry.getValue();
			if(fromColumns.containsKey(colKey)) 
			{
				var fromCol = fromColumns.get(colKey);
				var fromLabel = fromCol.getLabel() != null? fromCol.getLabel(): colKey;
				if(!fromLabel.equals(toCol.getLabel()))
				{
					var label = toCol.getLabel();
					var toKey = labelToKey(label);
					validateColumnKey(toKey);
					
					// NOTA: precisa vir primeiro, porque o próxmo método vai remover os índices do schema
					if(!colKey.equals(toKey))
					{
						renomearColunaNoDB(collection, fromCol, colKey, toKey);
						renomearColunaNoSchema(collection, fromCol, colKey, toKey, null);

						renamedKeys.put(colKey, toKey);
						colKey = toKey;
					}
				}

				// verificar alterações que necessitem criação ou remoção de índice
				verificarIndicesAndDefaultDaColuna(
					collection, user, fromSchema, db, colKey, toCol, fromCol);
			}
			else
			{
				verificarIndicesAndDefaultDaColuna(
					collection, user, fromSchema, db, colKey, toCol, null);
			}
		}

		// verificar colunas removidas...
		for(var entry : fromColumns.entrySet())
		{
			var colKey = entry.getKey();
			var fromCol = entry.getValue();
			if(!toColumns.containsKey(colKey)) 
			{
				// NOTA: precisa vir primeiro, porque o próximo método vai remover os índices do schema
				removerColunaDoDB(collection, fromCol, colKey);
				removerColunaDoSchema(collection, fromCol, colKey);
			}
		}

		// atualizar chaves, que podem ter sido renomeadas
		for(var entry: renamedKeys.entrySet())
		{
			var from = entry.getKey();
			var to = entry.getValue();
			toColumns.put(to, toColumns.get(from));
			toColumns.remove(from);
		}

		fromSchema.setColumns(toColumns);
		
		//
		criarOuAtualizarAuto(db, collection, autoId);
		collection.setAutoGenId(autoId);
		criarOuAtualizarPosicional(db, collection, posId);
		collection.setPositionalId(posId);

		//ALERTA: o cliente é obrigado a atualizar o schema todo, já que a order de certos campos pode mudar
		var newSchema = schemaToString(fromSchema);
		updateSchema(collection, newSchema, CollectionVersionChangeId.COLUMNS, reversing);

		var res = collectionRepo.save(collection);

		broadcast(res, CollectionUpdatedEvent.SCHEMA_UPDATED, sessionId, res.getSchema(), null);

		collectionLogger.info(collection, user, "Atualizou campos");
		
		return res;
	}

	private void verificarIndicesAndDefaultDaColuna(
		final CollectionWithSchema collection, 
		final User user,
		final CollectionSchema fromSchema, 
		final String db, 
		final String key,
		final Field to, 
		final Field from) 
	{
		if(to.isSortable() || to.isUnique() || to.isPositional())
		{
			if(from == null || !(from.isSortable() || from.isUnique()))
			{
				var index = criarIndiceNoSchema(
					fromSchema, key, to.getUnique(), getIndexDir(to.getType()));
				if(index != null)
				{
					criarIndiceNoDB(db, key, index);
				}
			}
		}
		else
		{
			if(from != null && (from.isSortable() || from.isUnique() || from.isPositional()))
			{
				removerIndicesDaColuna(collection, key);
			}
		}

		verificarDefaultDaColuna(collection, user, fromSchema, db, key, to, from);

		// nova do type objeto? verificar cada prop..
		if(to.getClass_() != null)
		{
			if(from == null || (from.getClass_() == null || !from.getClass_().equals(to.getClass_())))
			{
				var klass = fromSchema.getClasses().get(to.getClass_());
				for(var e : klass.getProps().entrySet())
				{
					var propKey = e.getKey();
					var prop = e.getValue();
					var fullKey = String.format("%s.%s", key, propKey);
					verificarIndicesAndDefaultDaColuna(collection, user, fromSchema, db, fullKey, prop, null);
					verificarDefaultDaColuna(collection, user, fromSchema, db, fullKey, prop, null);
				}
			}
		}
		// antiga do type objeto? remover os índices..
		else if(from != null && from.getClass_() != null)
		{
			var klass = fromSchema.getClasses().get(from.getClass_());
			for(var e : klass.getProps().entrySet())
			{
				var propKey = e.getKey();
				var prop = e.getValue();
				var fullKey = String.format("%s.%s", key, propKey);
				if(prop.isSortable() || prop.isUnique() || prop.isPositional())
				{
					removerIndicesDaColuna(collection, fullKey);
				}
			}
		}
	}

	private void verificarDefaultDaColuna(
		final CollectionWithSchema collection, 
		final User user,
		final CollectionSchema fromSchema, 
		final String db, 
		final String key,
		final Field to, 
		final Field from) 
	{
		// se default foi definido, é necessário varrer TODOS os itens e setar o valor se estiver null
		if(to.getDefault() != null)
		{
			if(from == null || from.getDefault() == null)
			{
				// só permitir se houver índice (abrir excessão para objetos...)
				if(to.isSortable() || to.isUnique() || to.getClass_() != null)
				{
					atribuirValoresDefault(db, collection, user, key, to);
				}
			}
		}
	}

	private void atribuirValoresDefault(
		final String db, 
		final CollectionWithSchema collection, 
		final User user,
		final String key, 
		final Field column) 
	{
		var ctx = configScriptContext(collection, user);

		var filters = new Filter(FilterOperator.isnull, key);
		var vars = new HashMap<String, Object>();
		vars.put(key, evalValueOrScriptOrFunction(column.getDefault(), ctx));

		noSqlService.update(db, collection.getPubId(), filters, vars);
	}

	private void removerIndicesDaColuna(
		final CollectionWithSchema collection, 
		final String key) 
	{
		var db = collection.getWorkspace().getPubId();
		
		var indicesDropados = new ArrayList<Index>();

		var schema = collection.getSchemaObj();

		// verificar se coluna faz parte de algum índice
		var indices = schema.getIndexes();
		if(indices != null)
		{
			var matches = indices.stream()
				.filter(i -> i.getColumns().contains(key))
					.collect(Collectors.toSet());
			for(var indice: matches) 
			{
				var colunasIndice = indice.getColumns();
				
				if(!indicesDropados.contains(indice)) 
				{
					indicesDropados.add(indice);
					try 
					{
						noSqlService.dropIndex(
							db, 
							collection.getPubId(), 
							colunasIndice, 
							indice.getDir() == FieldIndexDir.asc);
					}
					catch(Exception e)
					{
						logger.error("Erro ao remover índice", e);
					}
				}
				
				colunasIndice.remove(key);
				if(colunasIndice.size() == 0)
				{
					indicesDropados.remove(indice);
					indices.remove(indice);
				}
			}
			
			if(indices.size() == 0)
			{
				schema.setIndexes(null);
			}
		}
		
		// recriar os índices que foram dropados, utilizando as colunas que sobraram
		for(var indice: indicesDropados)
		{
			if(indice.getColumns().size() > 0)
			{
				noSqlService.createIndex(
						db, 
						collection.getPubId(), 
						indice.getColumns(), 
						indice.getDir() == FieldIndexDir.asc, 
						indice.isUnique());
			}
		}
	}


	private Index criarIndiceNoSchema(
		final CollectionSchema schema, 
		final String key, 
		final Boolean unique, 
		final FieldIndexDir dir) 
	{
		var indexes = schema.getIndexes();
		if(indexes == null)
		{
			schema.setIndexes(new ArrayList<>());
			indexes = schema.getIndexes();
		}

		var index = new Index(Arrays.asList(key), dir, unique);

		// índice já existe? não criar...
		if(indexes.stream().anyMatch(i -> i.equals(index)))
		{
			return null;
		}

		indexes.add(index);

		return index;
	}

	/**
	 * 
	 * @param collection
	 * @param schema
	 * @param sessionId
	 * @return
	 */
	public CollectionWithSchema atualizarIndices(
		final CollectionWithSchema collection, 
		final String schema, 
		final User user,
		final Workspace workspace,
		final String sessionId) 
	{
		return atualizarIndices(
			collection, schema, user, workspace, sessionId, false);
	}

	private CollectionWithSchema atualizarIndices(
		final CollectionWithSchema collection, 
		final String schema, 
		final User user,
		final Workspace workspace,
		final String sessionId,
		final boolean reversing) 
	{
		var newIndexes = indexesSchemaToList(schema);

		var curSchema = collection.getSchemaObj();
		var curIndexes = curSchema.getIndexes();

		validarIndicesNoSchema(
			curSchema, newIndexes, curSchema.getColumns(), 
			curSchema.getRefs(), curSchema.getClasses());

		var db = workspace.getPubId();

		// verificar índices removidos...
		var droppedIndexes = new ArrayList<Index>();
		if(curIndexes != null)
		{
			for(var index : curIndexes)
			{
				if(!newIndexes.stream().anyMatch(ind -> index.equals(ind)))
				{
					droppedIndexes.add(index);
					try
					{
						removerIndiceNoDB(db, collection.getPubId(), index);
					}
					catch(Exception e)
					{
						logger.error("Erro ao remover índice", e);
					}
				}
			}
		}

		curIndexes.removeAll(droppedIndexes);

		// verificar novos índices...
		if(newIndexes != null)
		{
			for(var index : newIndexes)
			{
				if(!curIndexes.stream().anyMatch(ind -> index.equals(ind)))
				{
					criarIndiceNoDB(db, collection.getPubId(), index);
				}
			}
		}
		
		//NOTA: qualquer alteração no índice (unique etc) vai causar um drop seguido de create

		curSchema.setIndexes(newIndexes);

		//ALERTA: o cliente é obrigado a atualizar o schema todo, já que a order de certos campos pode mudar
		var newSchema = schemaToString(curSchema);
		updateSchema(collection, newSchema, CollectionVersionChangeId.INDEXES, reversing);

		var res = collectionRepo.save(collection);

		broadcast(res, CollectionUpdatedEvent.SCHEMA_UPDATED, sessionId, res.getSchema(), null);

		collectionLogger.info(collection, user, "Atualizou índices");
		
		return res;
	}	

	/**
	 * 
	 * @param collection
	 * @param schema
	 * @param sessionId
	 * @return
	 */
	public CollectionWithSchema atualizarConstantes(
		final CollectionWithSchema collection, 
		final String schema, 
		final User user,
		final Workspace workspace,
		final String sessionId) 
	{
		return atualizarConstantes(
			collection, schema, user, workspace, sessionId, false);
	}

	private CollectionWithSchema atualizarConstantes(
		final CollectionWithSchema collection, 
		final String schema, 
		final User user,
		final Workspace workspace,
		final String sessionId,
		final boolean reversing) 
	{
		var newConsts = constantsSchemaToMap(schema);

		var curSchema = collection.getSchemaObj();

		validarConstantesNoSchema(newConsts);

		curSchema.setConstants(newConsts);

		//ALERTA: o cliente é obrigado a atualizar o schema todo, já que a order de certos campos pode mudar
		var newSchema = schemaToString(curSchema);
		updateSchema(collection, newSchema, CollectionVersionChangeId.CONSTS, reversing);

		var res = collectionRepo.save(collection);

		broadcast(res, CollectionUpdatedEvent.SCHEMA_UPDATED, sessionId, res.getSchema(), null);

		collectionLogger.info(collection, user, "Atualizou constantes");
		
		return res;
	}

	private void validarConstantesNoSchema(
		final Map<String, Const> consts) 
	{
		if(consts == null)
		{
			return;
		}

		var toRename = new HashMap<String, String>();

		for(var entry: consts.entrySet())
		{
			var constant = entry.getValue();
			var key = labelToKey(constant.getLabel());
			validateColumnKey(key);
			if(!key.equals(entry.getKey()))
			{
				toRename.put(entry.getKey(), key);
			}
		}

		for(var entry: toRename.entrySet())
		{
			var from = entry.getValue();
			var to = entry.getValue();
			consts.put(to, consts.get(from));
			consts.remove(from);
		}
	}

	private void removerFlowDoDepends(
		final LogicalExpr<FieldDependency> cond,
		final String key,
		final Field column
	)
	{
		if(cond.getCond() != null)
		{
			var dep = cond.getCond();
			var flow = dep.getFlow();
			if(flow != null)
			{
				if(flow.getName().equals(key))
				{
					column.setDepends(null);
				}
			}
		}
		else
		{
			var list = cond.getAnd() != null?
				cond.getAnd():
				cond.getOr();

			for(var item : list)
			{
				removerFlowDoDepends(item, key, column);
			}
		}
	}

	/**
	 * 
	 * @param collection
	 * @param schema
	 * @param sessionId
	 * @return
	 */
	public CollectionWithSchema atualizarFluxos(
		final CollectionWithSchema collection, 
		final String schema, 
		final User user, 
		final Workspace workspace,
		final String sessionId) 
	{
		return atualizarFluxos(
			collection, schema, user, workspace, sessionId, false);
	}

	private CollectionWithSchema atualizarFluxos(
		final CollectionWithSchema collection, 
		final String schema, 
		final User user, 
		final Workspace workspace,
		final String sessionId,
		final boolean reversing) 
	{
		var curSchema = collection.getSchemaObj();
		var columns = curSchema.getColumns();

		var newFlows = schema != null? 
			flowsSchemaToMap(schema):
			null;

		if(newFlows != null)
		{
			validarFluxosNoSchema(
				curSchema, 
				columns, 
				curSchema.getClasses(), 
				curSchema.getRefs(), 
				newFlows);
		}

		//FIXME: se o flow foi renomeado, o key antigo deve ser mantido pelo client, e todas as menções ao flow antigo devem ser trocadas

		if(curSchema.getFlows() != null)
		{
			for(var curFlow : curSchema.getFlows().keySet())
			{
				if(!newFlows.containsKey(curFlow))
				{
					// retirar dos .depends das colunas os flows removidos
					for(var entry: columns.entrySet())
					{
						var column = entry.getValue();
						var dep = column.getDepends();
						if(dep != null)
						{
							removerFlowDoDepends(dep, curFlow, column);
						}
					}
				}
			}
		}

		curSchema.setFlows(newFlows);

		//ALERTA: o cliente é obrigado a atualizar o schema todo, já que a order de certos campos pode mudar
		var newSchema = schemaToString(curSchema);
		updateSchema(collection, newSchema, CollectionVersionChangeId.FLOWS, reversing);

		var res = collectionRepo.save(collection);

		broadcast(res, CollectionUpdatedEvent.SCHEMA_UPDATED, sessionId, res.getSchema(), null);

		collectionLogger.info(collection, user, "Atualizou fluxos");
		
		return res;
	}

	private void updateSchema(
		final CollectionWithSchema collection, 
		final String newSchema,
		final CollectionVersionChangeId alteracaoId,
		final boolean reversing) 
	{
		var diff = dmp.patch_toText(dmp.patch_make(newSchema, collection.getSchema()));

		if(!reversing)
		{
			var alteracao = collectionVersionChangeRepo.findById(alteracaoId).get();
		
			var version = new CollectionVersion(collection, diff);
			version.setChange(alteracao);
			version.setCreatedAt(ZonedDateTime.now());

			collection.getVersions().add(version);
		}
		
		collection.setSchema(newSchema);
	}

	private String applyPatch(
		final String text, 
		final String diff)
	{
		return (String)dmp.patch_apply(new LinkedList<>(dmp.patch_fromText(diff)), text)[0];
	}

	private CollectionWithSchema reverterSchema(
		final CollectionWithSchema collection, 
		final CollectionVersion version,
		final User user,
		final Workspace workspace,
		final String sessionId)
			throws Exception
	{
		var schema = applyPatch(collection.getSchema(), version.getDiff());

		return atualizarSchema(collection, schema, user, workspace, sessionId, true).getKey();
	}


	/**
	 * 
	 * @param collection
	 * @param version
	 */
	public Collection reverterParaversion(
		final CollectionWithSchema collection, 
		final CollectionVersion version,
		final User user,
		final Workspace workspace,
		final String sessionId) 
	{
		var toRemove = new ArrayList<CollectionVersion>();
		toRemove.add(version);
		
		var res = collection;

		var versoesApos = collectionRepo.findAllVersionsAfter(collection, version.getCreatedAt());
		for(var apos : versoesApos)
		{
			try 
			{
				res = reverterSchema(res, apos, user, workspace, sessionId);
			} 
			catch (Exception e) 
			{
				throw new CollectionException("Erro ao tentar reverter versão", e);
			}

			toRemove.add(apos);
		}

		try 
		{
			collection.getVersions().removeAll(toRemove);
			res = reverterSchema(res, version, user, workspace, sessionId);
		} 
		catch (Exception e) 
		{
			throw new CollectionException("Erro ao tentar reverter versão", e);
		}

		collectionLogger.info(
			collection, 
			user, 
			"Reverteu para versão anterior", 
			Map.of("id", version.getPubId()));

		return res;
	}

	/**
	 * 
	 * @param collection
	 * @param version
	 */
	public List<CollectionVersion> removerVersao(
		final CollectionWithSchema collection, 
		final CollectionVersion version,
		final User user,
		final Workspace workspace,
		final String sessionId) 
	{
		collection.getVersions().remove(version);
		
		var col = collectionRepo.save(collection);

		collectionLogger.info(
			collection, 
			user, 
			"Removeu versão", 
			Map.of("id", version.getId()));
		
		return col.getVersions();
	}

}	

