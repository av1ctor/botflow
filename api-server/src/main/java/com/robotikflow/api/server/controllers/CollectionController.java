package com.robotikflow.api.server.controllers;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.robotikflow.api.server.models.request.CollectionAdicionarColunaRequest;
import com.robotikflow.api.server.models.request.CollectionAdicionarItensRequest;
import com.robotikflow.api.server.models.request.CollectionColumnRequest;
import com.robotikflow.api.server.models.request.CollectionDupRequest;
import com.robotikflow.api.server.models.request.CollectionItemRequest;
import com.robotikflow.api.server.models.response.CollectionAuthBaseResponse;
import com.robotikflow.api.server.models.response.CollectionAuthResponse;
import com.robotikflow.api.server.models.response.CollectionAutomationLogResponse;
import com.robotikflow.api.server.models.response.CollectionAutomationResponse;
import com.robotikflow.api.server.models.response.CollectionAutomationResponseFactory;
import com.robotikflow.api.server.models.response.CollectionComExtraResponse;
import com.robotikflow.api.server.models.response.CollectionFormResponse;
import com.robotikflow.api.server.models.response.CollectionIntegrationLogResponse;
import com.robotikflow.api.server.models.response.CollectionIntegrationResponse;
import com.robotikflow.api.server.models.response.CollectionItemLogResponse;
import com.robotikflow.api.server.models.response.CollectionItemPostResponse;
import com.robotikflow.api.server.models.response.CollectionLogResponse;
import com.robotikflow.api.server.models.response.CollectionPostResponse;
import com.robotikflow.api.server.models.response.CollectionResponse;
import com.robotikflow.api.server.models.response.CollectionVersionResponse;
import com.robotikflow.api.server.models.response.EmptyResponse;
import com.robotikflow.core.exception.CollectionException;
import com.robotikflow.core.models.UserSession;
import com.robotikflow.core.models.entities.Collection;
import com.robotikflow.core.models.entities.CollectionAuthRole;
import com.robotikflow.core.models.entities.CollectionDupOptions;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.filters.CollectionFilter;
import com.robotikflow.core.models.request.CollectionAutomationRequest;
import com.robotikflow.core.models.request.CollectionFilterRequest;
import com.robotikflow.core.models.request.CollectionIntegrationRequest;
import com.robotikflow.core.models.request.CollectionRequest;
import com.robotikflow.core.models.request.CollectionAuthRequest;
import com.robotikflow.core.models.request.CollectionSchemaDiffRequest;
import com.robotikflow.core.models.request.CollectionSchemaRequest;
import com.robotikflow.core.models.request.MoveDirections;
import com.robotikflow.core.models.request.WorkspacePostRequest;
import com.robotikflow.core.models.response.DocumentResponse;
import com.robotikflow.core.services.DocumentService;
import com.robotikflow.core.services.collections.CollectionAuthService;
import com.robotikflow.core.services.collections.CollectionAutomationService;
import com.robotikflow.core.services.collections.CollectionIntegrationService;
import com.robotikflow.core.services.collections.CollectionService;
import com.robotikflow.core.services.collections.CollectionTemplateService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;

@RestController
public class CollectionController 
	extends BaseController 
{
	@Autowired
	private CollectionService collectionService;
	@Autowired
	private CollectionIntegrationService collectionIntegrationService;
	@Autowired
	private CollectionAutomationService collectionAutomationService;
	@Autowired
	private CollectionAuthService collectionAuthService;
	@Autowired
	private CollectionTemplateService collectionTemplateService;
	@Autowired
	private DocumentService documentService;
	
	private static HashSet<String> colunasValidasDeOrdenacao = new HashSet<>(Arrays.asList
	( 
		"id", "name", "createdAt", "updatedAt", "order"
	));
	
	public CollectionController(Environment env) 
	{
		super();
	}
	
	@SuppressWarnings("unchecked")
	private <T extends Collection> T encontrarPorIdAndUser(String id, UserSession ua) 
	{
		T collection = null;
		if(userIsAdmin(ua))
		{
			collection = (T)collectionService
				.findByPubIdAndWorkspace(id, ua.getWorkspace());
		}
		else
		{
			collection = (T)collectionService
				.findByPubIdAndUserAndWorkspace(id, ua.getUser(), ua.getWorkspace());
		}

		if(collection == null)
		{
			throw new CollectionException("Coleção inexistente ou permissão negada");
		}

		return collection;
	}

	@GetMapping("/collections/{idParent}/children")
	@ApiOperation(value = "Listar as coleções do área de trabalho que estejam dentro de uma Pasta")
	List<CollectionResponse> listar(
		@PathVariable String idParent,
		@RequestParam(value = "filters", required = false) String filters,
		Pageable pageable) throws Exception
	{
		pageable = validateSorting(pageable, colunasValidasDeOrdenacao, "name", Direction.ASC);
		
		List<Collection> collections = null;

		var ua = getUserSession();

		if(filters == null || filters.length() == 0)
		{
			if(userIsAdmin(ua))
			{
				collections = collectionService.findAllByWorkspace(
					ua.getWorkspace(), idParent, pageable);
			}
			else
			{
				collections = collectionService.findAllPublishedByUserAndWorkspace(
					ua.getUser(), ua.getWorkspace(), idParent, pageable);
			}
		}
		else
		{
			if(userIsAdmin(ua))
			{
				collections = collectionService.findAllByWorkspace(
					ua.getWorkspace(), idParent, buildFilters(filters, CollectionFilter.class), pageable);
			}
			else
			{
				//TODO: implementar query
				throw new CollectionException("Não implementado");
			}
		}
		
		return collections.stream()
				.map(w -> new CollectionResponse(w)).collect(Collectors.toList());
	}

	@GetMapping("/collections/{idParent}/children/pub")
	@ApiOperation(value = "Listar as coleções publicadas do área de trabalho dentro de uma Pasta")
	List<CollectionResponse> listarPublicadas(
		@PathVariable String idParent,
		Pageable pageable) throws Exception
	{
		pageable = validateSorting(pageable, colunasValidasDeOrdenacao, "name", Direction.ASC);
		
		var ua = getUserSession();
		
		var collections = collectionService.findAllPublishedByUserAndWorkspace(
			ua.getUser(), ua.getWorkspace(), idParent, pageable);
		
		return collections.stream()
				.map(w -> new CollectionResponse(w, (String)null)).collect(Collectors.toList());
	}

	@GetMapping("/collections/{id}")
	@ApiOperation(value = "Retornar uma coleção")
	CollectionResponse getOne(
		@PathVariable String id) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.READER, collection, ua.getUser(), ua.getWorkspace());
		
		return new CollectionResponse(collection);
	}
	
	@PostMapping("/collections")
    @PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Criar uma coleção")
	CollectionResponse criar(
		@Valid @RequestBody CollectionRequest req)
	{
		var ua = getUserSession();
		
		collectionService.validarAcesso(ua.getUser(), ua.getWorkspace());
		
		var collection = collectionService.criar(
			req, ua.getUser(), ua.getWorkspace());
		
		return new CollectionResponse(collection);
	}

	@PatchMapping("/collections/{id}")
	@ApiOperation(value = "Atualizar uma coleção, atualizando o schema por diff")
	CollectionResponse atualizar(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@Valid @RequestBody CollectionRequest req)
	{
		var ua = getUserSession();
		
		Collection collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		collection = collectionService.atualizar(
			collection, req, ua.getUser(), ua.getWorkspace(), sessionId);
		
		return new CollectionResponse(collection);
	}

	@PatchMapping("/collections/{id}/schema/diff")
	@ApiOperation(value = "Atualizar o schema da coleção, passando o diff")
	CollectionResponse atualizarSchemaPorDiff(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@Valid @RequestBody CollectionSchemaDiffRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());

		var res = collectionService.atualizarSchemaDiff(
			collection, req, ua.getUser(), ua.getWorkspace(), sessionId);
		
		return new CollectionResponse(res.getKey(), req.isReturnSchema()? res.getValue(): null);
	}
	
	@PatchMapping("/collections/{id}/schema")
	@ApiOperation(value = "Atualizar o schema da coleção")
	CollectionResponse atualizarSchema(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@Valid @RequestBody CollectionSchemaRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());

		var res = collectionService.atualizarSchema(
			collection, req.getSchema(), ua.getUser(), ua.getWorkspace(), sessionId);
		
		return new CollectionResponse(res.getKey(), req.isReturnSchema()? res.getValue(): null);
	}

	private EnumSet<CollectionDupOptions> reqToOptions(CollectionDupRequest req)
	{
		var res = EnumSet.noneOf(CollectionDupOptions.class);
		
		if(req.isWithData())
		{
			res.add(CollectionDupOptions.DATA);
		}
		if(req.isWithPermissions())
		{
			res.add(CollectionDupOptions.PERMISSIONS);
		}
		if(req.isWithAutomations())
		{
			res.add(CollectionDupOptions.AUTOMATIONS);
		}
		if(req.isWithIntegrations())
		{
			res.add(CollectionDupOptions.INTEGRATIONS);
		}
		if(req.isWithAuxs())
		{
			res.add(CollectionDupOptions.AUXS);
		}

		return res;
	}
	
	@PostMapping("/collections/{id}/dup")
	@ApiOperation(value = "Duplicar uma coleção")
	CollectionResponse duplicar(
		@PathVariable String id, 
		@Valid @RequestBody CollectionDupRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());

		var collectionDup = collectionService.duplicar(
			collection, reqToOptions(req), ua.getUser(), ua.getWorkspace());
		
		return new CollectionResponse(collectionDup);
	}

	@PostMapping("/collections/{idParent}/gen/{idTemplate}")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Criar uma coleção a partir de um template")
	CollectionResponse gerar(
		@PathVariable String idParent,
		@PathVariable String idTemplate,
		@RequestParam(value = "published", required = false) boolean published)
	{
		var ua = getUserSession();
		
		var template = !idTemplate.equals("0")? 
			collectionTemplateService.findByPubId(idTemplate):
			null;
		
		var collection = template != null?
			collectionService.gerar(template, idParent, published, ua.getUser(), ua.getWorkspace()):
			collectionService.gerarPasta(idParent, published, ua.getUser(), ua.getWorkspace());
		
		return new CollectionResponse(collection);
	}

	@PatchMapping("/collections/{id}/pub")
	@ApiOperation(value = "Publicar uma coleção")
	CollectionResponse publicar(@PathVariable String id)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);

		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(collection.getPublishedBy() != null)
		{
			throw new CollectionException("Coleção já publicada");
		}
		
		if(collection.getOrder() == null  || collection.getIcon() == null || collection.getIcon().isEmpty())
		{
			throw new CollectionException("Coleção deve ter os campos ícone e order definidos");
		}

		var schema = collection.getSchemaObj();
		if(schema.getViews() == null)
		{
			throw new CollectionException("A objeto \"views\" deve ser definido no schema da coleção");
		}
		
		collection = (CollectionWithSchema)collectionService.publicar(collection, ua.getUser(), ua.getWorkspace());
		
		return new CollectionResponse(collection);
	}
	
	@PatchMapping("/collections/{id}/unpub")
	@ApiOperation(value = "Despublicar uma coleção")
	CollectionResponse despublicar(@PathVariable String id)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);		
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(collection.getPublishedBy() == null)
		{
			throw new CollectionException("Coleção não publicada");
		}
		
		collection = (CollectionWithSchema)collectionService.despublicar(collection, ua.getUser());
		
		return new CollectionResponse(collection);
	}	

	@DeleteMapping("/collections/{id}")
	@ApiOperation(value = "Apagar uma coleção")
	EmptyResponse apagar(@PathVariable String id)
	{
		var ua = getUserSession();
		
		Collection collection = encontrarPorIdAndUser(id, ua);

		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());

		collectionService.apagar(collection, ua.getUser());
		
		return new EmptyResponse();
	}

	@PatchMapping("/collections/{id}/loc/{pos}")
	@ApiOperation(value = "Mudar posição de uma coleção no menu")
	CollectionResponse mover(
		@PathVariable String id, 
		@PathVariable short pos) throws Exception
	{
		var ua = getUserSession();
		
		Collection collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		return new CollectionResponse(
			collectionService.mover(collection, pos, ua.getUser()), 
			null);
	}

	@GetMapping("/collections/{id}/auxs")
	@ApiOperation(value = "Retornar as coleções auxes de uma coleção")
	List<CollectionResponse> listarAux(
		@PathVariable String id,
		Pageable pageable)
	{
		pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Direction.ASC, "a.order"));
		
		var ua = getUserSession();

		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.EDITOR, collection, ua.getUser(), ua.getWorkspace());
			
		List<Collection> collections = null;

		if(userIsAdmin(ua))
		{
			collections = collectionService.findAllAuxsByCollection(
				collection.getId(), ua.getWorkspace(), pageable);
		}
		else
		{
			collections = collectionService.findAllAuxsByCollectionAndUser(
				collection.getId(), ua.getUser(), ua.getWorkspace(), pageable);
		}
		
		return collections.stream()
				.map(w -> new CollectionResponse(w)).collect(Collectors.toList());
		
	}

	@PostMapping("/collections/{id}/auxs/gen/{idTemplate}")
	@PreAuthorize("hasAnyRole('ADMIN_SUPERVISOR', 'USER_ADMIN')")
	@ApiOperation(value = "Criar uma coleção aux a partir de um template")
	CollectionResponse gerarAux(
		@PathVariable String id,
		@PathVariable String idTemplate,
		@RequestParam(value = "pos", required = false) Integer position,
		@RequestParam(value = "published", required = false) boolean published)
	{
		var ua = getUserSession();
		
		var template = !idTemplate.equals("0")? 
			collectionTemplateService.findByPubId(idTemplate):
			null;

		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
			
		var aux = collectionService.gerarAux(
			collection, template, position, published, ua.getUser(), ua.getWorkspace());
		
		return new CollectionResponse(aux);
	}

	@PostMapping("/collections/{id}/auxs/dup/{idAux}")
	@ApiOperation(value = "Duplicar uma coleção aux")
	CollectionResponse duplicarAux(
		@PathVariable String id, 
		@PathVariable String idAux, 
		@RequestParam(value = "pos", required = true) Integer position,
		@Valid @RequestBody CollectionDupRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema principal = encontrarPorIdAndUser(id, ua);
		CollectionWithSchema aux = encontrarPorIdAndUser(idAux, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, principal, ua.getUser(), ua.getWorkspace());
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, aux, ua.getUser(), ua.getWorkspace());

		var collectionDup = collectionService.duplicarAux(
			principal, aux, reqToOptions(req), position, ua.getUser(), ua.getWorkspace());
		
		return new CollectionResponse(collectionDup);
	}

	@PatchMapping("/collections/{id}/auxs/{idAux}/dir/{direction}")
	@ApiOperation(value = "Mudar posição de uma coleção aux nas abas")
	EmptyResponse moverAux(
		@PathVariable String id, 
		@PathVariable String idAux, 
		@PathVariable MoveDirections direction) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema principal = encontrarPorIdAndUser(id, ua);
		CollectionWithSchema aux = encontrarPorIdAndUser(idAux, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, principal, ua.getUser(), ua.getWorkspace());
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, aux, ua.getUser(), ua.getWorkspace());
		
		collectionService.moverAux(
			principal, aux, direction, ua.getUser());
		
		return new EmptyResponse();
	}

	@DeleteMapping("/collections/{id}/auxs/{idAux}")
	@ApiOperation(value = "Apagar uma coleção aux")
	EmptyResponse apagarAux(
		@PathVariable String id,
		@PathVariable String idAux)
	{
		var ua = getUserSession();
		
		Collection collection = encontrarPorIdAndUser(id, ua);
		CollectionWithSchema aux = encontrarPorIdAndUser(idAux, ua);

		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, aux, ua.getUser(), ua.getWorkspace());

		collectionService.apagarAux(collection, aux, ua.getUser());
		
		return new EmptyResponse();
	}

	@GetMapping("/collections/{id}/items/{idItem}")
	@ApiOperation(value = "Retornar um item de uma coleção")
	Object getOneItem(
		@PathVariable String id,
		@PathVariable String idItem) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);

		var isAdmin = userIsAdmin(ua);
		
		return collectionService.findItemById(collection, idItem, ua.getUser(), isAdmin);
	}

	@GetMapping("/collections/{id}/items")
	@ApiOperation(value = "Retornar itens de uma coleção", notes = "the page param is actually used as offset, not as page number")
	List<Map<String, Object>> listarItens(
		@PathVariable String id,
		@RequestParam(value = "filters", required = false) String filters,
		Pageable pageable) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);		
		
		return retornarItens(collection, filters, pageable, ua);
	}

	private List<Map<String, Object>> retornarItens(
		final CollectionWithSchema collection, 
		final String filters, 
		final Pageable pageable, 
		final UserSession ua) 
		throws IOException, JsonParseException, JsonMappingException 
	{
		var isAdmin = userIsAdmin(ua);
		
		var itens = collectionService.listarItens(
			collection, pageable, decodeFiltros(filters), ua.getUser(), isAdmin);
		 
		return itens != null? 
			itens.stream()
				.map(i -> (Map<String, Object>)i)
					.collect(Collectors.toList()): 
			null;
	}

	private List<CollectionFilterRequest> decodeFiltros(
		final String filters) 
		throws IOException, JsonParseException, JsonMappingException 
	{
		return filters == null || filters.isEmpty()? 
			null: 
			(List<CollectionFilterRequest>)objMapper.readValue(URLDecoder.decode(filters, StandardCharsets.UTF_8),
				new TypeReference<List<CollectionFilterRequest>>() {});
	}
	
	@PostMapping("/collections/{id}/items/csv")
	@ApiOperation(value = "Inserir itens a uma coleção, por meio de uma CSV")
	List<Map<String, Object>> adicionarItens(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@Valid @RequestBody CollectionAdicionarItensRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.EDITOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}
		
		return collectionService.inserirItens(
			collection, req.getCsv(), false, true, ua.getUser(), true, sessionId);
	}

	@PostMapping("/collections/{id}/items")
	@ApiOperation(value = "Adicionar um item a uma coleção")
	Map<String, Object> inserirItem(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@Valid @RequestBody CollectionItemRequest req) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);		
	
		return atualizarOuInserirItem(collection, ua, req, null, sessionId);
	}

	@PatchMapping("/collections/{id}/items/{idItem}")
	@ApiOperation(value = "Atualizar um item de uma coleção")
	Map<String, Object> atualizarItem(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@PathVariable String idItem, 
		@Valid @RequestBody CollectionItemRequest req) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);

		return atualizarOuInserirItem(collection, ua, req, idItem, sessionId);
	}

	private Map<String, Object> atualizarOuInserirItem(
		CollectionWithSchema collection, 
		UserSession ua, 
		CollectionItemRequest req, 
		String idItem,
		String sessionId) throws Exception 
	{
		collectionService.validarAcesso(
			CollectionAuthRole.EDITOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}
		
		var isAdmin = userIsAdmin(ua);
		if(!isAdmin)
		{
			collectionService.validarAcessoItem(
				collection, idItem, ua.getUser(), ua.getWorkspace());
		}
		
		var variaveis = req.getVars();
		
		var item = idItem != null?
			collectionService.atualizarItem(collection, idItem, variaveis, ua.getUser(), isAdmin, sessionId):
			collectionService.inserirItem(collection, variaveis, ua.getUser(), isAdmin, sessionId);
		
		if(item == null)
		{
			throw new CollectionException("Falha ao inserir/atualizar item da coleção");
		}
		
		return item;
	}
	
	@PatchMapping("/collections/{id}/items/{idItem}/loc/{pos}")
	@ApiOperation(value = "Mudar posição de um item de uma coleção")
	Object moverItem(
		@PathVariable String id, 
		@PathVariable String idItem, 
		@PathVariable int pos) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.EDITOR, collection, ua.getUser(), ua.getWorkspace());
		
		return collectionService.moverItem(
			collection, idItem, pos, ua.getUser());
	}

	@DeleteMapping("/collections/{id}/items/{idItem}")
	@ApiOperation(value = "Remover um item de uma coleção")
	boolean removerItem(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@PathVariable String idItem) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.EDITOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}
		
		if(!userIsAdmin(ua))
		{
			collectionService.validarAcessoItem(
				collection, idItem, ua.getUser(), ua.getWorkspace());
		}
		
		collectionService.removerItem(
			collection, idItem, ua.getUser(), sessionId);
		
		return true;
	}

	@DeleteMapping("/collections/{id}/items")
	@ApiOperation(value = "Remover itens de uma coleção por critério")
	boolean removerItens(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@RequestParam(value = "filters", required = true) String filters) throws IOException
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.EDITOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}
		
		if(filters == null || filters.isEmpty())
		{
			throw new CollectionException("Filtros devem ser definidos");
		}
		
		var filtros = (Map<String, Object>)objMapper
			.readValue("{" + filters + "}", new TypeReference<Map<String, Object>>(){});

		collectionService.removerItens(
			collection, filtros, ua.getUser(), sessionId);
		
		return true;
	}

	@DeleteMapping("/collections/{id}/items/all")
	@ApiOperation(value = "Remover todos os itens de uma coleção")
	boolean removerItensTodos(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		collectionService.removerItensTodos(
			collection, ua.getUser(), sessionId);
		
		return true;
	}
	
	@PostMapping("/collections/{id}/columns")
	@ApiOperation(value = "Adicionar coluna a uma coleção")
	CollectionResponse adicionarColuna(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@Valid @RequestBody CollectionAdicionarColunaRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}
		
		try 
		{
			collection = collectionService
				.adicionarColuna(collection, req.getType(), req.getIndex(), 
					req.getNullable(), req.getSortable(), req.getUnique(), 
						ua.getUser(), sessionId);
		} 
		catch (IOException e) 
		{
			throw new CollectionException("Erro ao adicionar coluna", e);
		}
		
		return new CollectionResponse(collection);
	}
	
	@DeleteMapping("/collections/{id}/columns/{idCol}")
	@ApiOperation(value = "Remover coluna de uma coleção")
	CollectionResponse removerColuna(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@PathVariable String idCol)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}

		try
		{
			collection = collectionService.removerColuna(
				collection, idCol, ua.getUser(), sessionId);
		} 
		catch (IOException e) 
		{
			throw new CollectionException("Erro ao remover coluna", e);
		}
		
		return new CollectionResponse(collection);
	}

	@PatchMapping("/collections/{id}/column/{idColumn}")
	@ApiOperation(value = "Atualizar coluna de uma coleção")
	CollectionComExtraResponse atualizarColuna(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@PathVariable String idColumn, 
		@Valid @RequestBody CollectionColumnRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}

		var res = collectionService.atualizarColuna(
			collection, idColumn, req.getColumn(), ua.getUser(), ua.getWorkspace(), sessionId);
		
		return new CollectionComExtraResponse(res.getValue(), res.getKey());
	}

	@PatchMapping("/collections/{id}/classes")
	@ApiOperation(value = "Atualizar classes de uma coleção")
	CollectionResponse atualizarClasses(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@Valid @RequestBody CollectionSchemaRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}

		collection = collectionService.atualizarClasses(
			collection, req.getSchema(), ua.getUser(), ua.getWorkspace(), sessionId);
		
		return new CollectionResponse(collection);
	}
	
	@PatchMapping("/collections/{id}/columns")
	@ApiOperation(value = "Atualizar colunas de uma coleção")
	CollectionResponse atualizarColunas(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@Valid @RequestBody CollectionSchemaRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}

		collection = collectionService.atualizarColunas(
			collection, req.getSchema(), ua.getUser(), ua.getWorkspace(), sessionId);
		
		return new CollectionResponse(collection);
	}
	
	@PatchMapping("/collections/{id}/indexes")
	@ApiOperation(value = "Atualizar índices de uma coleção")
	CollectionResponse atualizarIndexes(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@Valid @RequestBody CollectionSchemaRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}

		collection = collectionService.atualizarIndices(
			collection, req.getSchema(), ua.getUser(), ua.getWorkspace(),sessionId);
		
		return new CollectionResponse(collection);
	}
	
	@PatchMapping("/collections/{id}/constants")
	@ApiOperation(value = "Atualizar constantes de uma coleção")
	CollectionResponse atualizarConstantes(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@Valid @RequestBody CollectionSchemaRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}

		collection = collectionService.atualizarConstantes(
			collection, req.getSchema(), ua.getUser(), ua.getWorkspace(),sessionId);
		
		return new CollectionResponse(collection);
	}
	
	@PatchMapping("/collections/{id}/flows")
	@ApiOperation(value = "Atualizar fluxos de uma coleção")
	CollectionResponse atualizarFluxos(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@Valid @RequestBody CollectionSchemaRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!collection.isEditavel())
		{
			throw new CollectionException("Coleção não editável");
		}

		collection = collectionService.atualizarFluxos(
			collection, req.getSchema(), ua.getUser(), ua.getWorkspace(), sessionId);
		
		return new CollectionResponse(collection);
	}
	
	@GetMapping("/collections/{id}/reports/{name}")
	@ApiOperation(value = "Gerar relatório de um coleção")
	List<Object> gerarRelatorio(
			@PathVariable String id,
			@PathVariable String name,
			@RequestParam(value = "filters", required = false) String filters,
			Pageable pageable) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);

		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var filtros = filters == null || filters.isEmpty()? 
				null: 
				(Map<String, Object>)objMapper.readValue("{" + filters + "}", new TypeReference<Map<String, Object>>(){});
		
		var isAdmin = userIsAdmin(ua);
		
		var relatorio = collection.getSchemaObj().getReports().stream()
				.filter(r -> r.getName().equals(name))
					.findFirst()
						.orElseThrow(() -> new CollectionException("Relatório inexistente"));
		
		var itens = collectionService.gerarRelatorio(
			collection, relatorio, pageable, filtros, ua.getUser(), isAdmin);
		
		return itens != null? 
			itens.stream()
				.map(i -> (Object)i).collect(Collectors.toList()): 
			null;
	}

	@GetMapping("/collections/{id}/integrations")
	@ApiOperation(value = "Listar as integrações de uma coleção")
	List<CollectionIntegrationResponse> listarIntegracoes(
		@PathVariable String id, 
		Pageable pageable)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var integrations = 
			collectionIntegrationService.findAll(collection, pageable);
		
		return integrations != null? 
			integrations.stream()
				.map(i -> new CollectionIntegrationResponse(i))
					.collect(Collectors.toList()): 
			null;
	}	

	@PostMapping("/collections/{id}/integrations")
	@ApiOperation(value = "Criar integração em uma coleção")
	CollectionIntegrationResponse criarIntegracao(
		@PathVariable String id, 
		@Valid @RequestBody CollectionIntegrationRequest req) 
		throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		return new CollectionIntegrationResponse(
			collectionIntegrationService.criar(
				collection, req, ua.getUser(), ua.getWorkspace()));
	}

	@PatchMapping("/collections/{id}/integrations/{idInt}")
	@ApiOperation(value = "Atualizar integração de uma coleção")
	CollectionIntegrationResponse atualizarIntegracao(
		@PathVariable String id, 
		@PathVariable String idInt,
		@Valid @RequestBody CollectionIntegrationRequest req) 
		throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var integration = collectionIntegrationService.findByPubIdAndCollection(idInt, collection);
		if(integration == null)
		{
			throw new CollectionException("Integração de Coleção inexistente");
		}

		if(collection.getId() != integration.getCollection().getId())
		{
			throw new CollectionException("Coleção da integração diferente da Coleção da requisição");
		}
		
		return new CollectionIntegrationResponse(
			collectionIntegrationService.atualizar(
				integration, req, ua.getUser(), ua.getWorkspace()));
	}
	
	@GetMapping("/collections/{id}/integrations/{idIntegration}/logs")
	@ApiOperation(value = "Get integration logs")
	List<CollectionIntegrationLogResponse> listIntegrationLogs(
		@PathVariable String id, 
		@PathVariable String idIntegration, 
		Pageable pageable)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var integration = collectionIntegrationService.findByPubIdAndCollection(idIntegration, collection);
		if(integration == null)
		{
			throw new CollectionException("Integration not found");
		}

		if(collection.getId() != integration.getCollection().getId())
		{
			throw new CollectionException("Integration not part of the collection");
		}

		var logs = collectionIntegrationService.findAllLogs(integration, pageable);
		
		return logs.stream()
			.map(log -> 
				new CollectionIntegrationLogResponse(log, objMapper))
				.collect(Collectors.toList());
	}	

	@GetMapping("/collections/{id}/items/{idItem}/docs")
	@ApiOperation(value = "Listar os documentos de um item de uma coleção")
	List<DocumentResponse> listarDocsDoItem(
			@PathVariable String id, 
			@PathVariable String idItem,
			Pageable pageable) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.READER, collection, ua.getUser(), ua.getWorkspace());
		
		if(!userIsAdmin(ua))
		{
			collectionService.validarAcessoItem(
				collection, idItem, ua.getUser(), ua.getWorkspace());
		}

		var docs = collectionService.listarDocumentsDoItem(
			collection, idItem, ua.getWorkspace(), pageable);
		
		return docs.stream()
			.map(d -> new DocumentResponse(d, documentoUtil))
				.collect(Collectors.toList());
	}

	@PostMapping("/collections/{id}/items/{idItem}/docs/{idDoc}")
	@ApiOperation(value = "Adicionar document ao item de uma coleção")
	DocumentResponse adicionarDocAoItem(
		@PathVariable String id, 
		@PathVariable String idItem,
		@PathVariable String idDoc) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.EDITOR, collection, ua.getUser(), ua.getWorkspace());
		
		if(!userIsAdmin(ua))
		{
			collectionService.validarAcessoItem(
				collection, idItem, ua.getUser(), ua.getWorkspace());
		}

		var document = documentService
			.findByPubIdAndWorkspace(idDoc, ua.getWorkspace().getId());
		if(document == null)
		{
			throw new CollectionException("Document inexistente");
		}

		collectionService.adicionarDocumentAoItem(
			document, collection, idItem, ua.getUser(), ua.getWorkspace());
		
		return new DocumentResponse(document, documentoUtil);
	}

	@GetMapping("/collections/{id}/items/{idItem}/logs")
	@ApiOperation(value = "Listar os logs de um item de uma coleção")
	List<CollectionItemLogResponse> listarLogsDoItem(
		@PathVariable String id, 
		@PathVariable String idItem,
		Pageable pageable) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.READER, collection, ua.getUser(), ua.getWorkspace());
		
		if(!userIsAdmin(ua))
		{
			collectionService.validarAcessoItem(
				collection, idItem, ua.getUser(), ua.getWorkspace());
		}

		var logs = collectionService.listarLogsDoItem(
			collection, idItem, ua.getWorkspace(), pageable);
		
		return logs != null?
			logs.stream()
				.map(l -> new CollectionItemLogResponse(l, objMapper))
					.collect(Collectors.toList()):
			null;
	}

	@GetMapping("/collections/{id}/items/{idItem}/posts")
	@ApiOperation(value = "Listar os posts de um item de uma coleção")
	List<CollectionItemPostResponse> listarPostsDoItem(
		@PathVariable String id, 
		@PathVariable String idItem,
		@RequestParam(value = "levels", required = false, defaultValue = "0") short levels,
		Pageable pageable) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.COMMENTER, collection, ua.getUser(), ua.getWorkspace());
		
		if(!userIsAdmin(ua))
		{
			collectionService.validarAcessoItem(
				collection, idItem, ua.getUser(), ua.getWorkspace());
		}

		var posts = collectionService.listarPostsDoItem(
			collection, idItem, levels, ua.getWorkspace(), pageable);
		
		return posts != null?
			posts.stream()
				.map(l -> new CollectionItemPostResponse(l, true))
					.collect(Collectors.toList()):
			null;
	}

	@GetMapping("/collections/{id}/items/{idItem}/posts/{idPost}")
	@ApiOperation(value = "Listar os posts de um post em um item de uma coleção")
	List<CollectionItemPostResponse> listarPostsDoItemByTopicId(
		@PathVariable String id, 
		@PathVariable String idItem,
		@PathVariable String idPost,
		Pageable pageable) throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.COMMENTER, collection, ua.getUser(), ua.getWorkspace());
		
		if(!userIsAdmin(ua))
		{
			collectionService.validarAcessoItem(
				collection, idItem, ua.getUser(), ua.getWorkspace());
		}

		var posts = collectionService.listarPostsDoItem(
			collection, idItem, idPost, ua.getWorkspace(), pageable);
		
		return posts != null?
			posts.stream()
				.map(l -> new CollectionItemPostResponse(l, true))
					.collect(Collectors.toList()):
			null;
	}

	@PostMapping("/collections/{id}/items/{idItem}/posts")
	@ApiOperation(value = "Criar post em um item de uma coleção")
	CollectionItemPostResponse criarPostNoItem(
		@PathVariable String id, 
		@PathVariable String idItem,
		@Valid @RequestBody WorkspacePostRequest req) 
		throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.COMMENTER, collection, ua.getUser(), ua.getWorkspace());
		
		if(!userIsAdmin(ua))
		{
			collectionService.validarAcessoItem(
				collection, idItem, ua.getUser(), ua.getWorkspace());
		}

		var post = collectionService.criarPostNoItem(
			collection, idItem, req, ua.getUser(), ua.getWorkspace());
		
		return post != null?
			new CollectionItemPostResponse(post, true):
			null;
	}	

	@PatchMapping("/collections/{id}/items/{idItem}/posts/{idPost}")
	@ApiOperation(value = "Atualizar post em um item de uma coleção")
	CollectionItemPostResponse atualizarPostNoItem(
		@PathVariable String id, 
		@PathVariable String idItem,
		@PathVariable String idPost,
		@Valid @RequestBody WorkspacePostRequest req) 
		throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.COMMENTER, collection, ua.getUser(), ua.getWorkspace());
		
		if(!userIsAdmin(ua))
		{
			collectionService.validarAcessoItem(
				collection, idItem, ua.getUser(), ua.getWorkspace());
		}

		var post = collectionService
			.findItemPostByPubIdAndWorkspace(idPost, ua.getWorkspace());

		collectionService
			.validarAcessoDeEscritaAoPostDeItem(idItem, post, ua.getUser(), ua.getWorkspace());

		post = collectionService.atualizarPostNoItem(
			collection, idItem, post, req, ua.getUser(), ua.getWorkspace());
		
		return post != null?
			new CollectionItemPostResponse(post, true):
			null;
	}

	@DeleteMapping("/collections/{id}/items/{idItem}/posts/{idPost}")
	@ApiOperation(value = "Remover post de um item de uma coleção")
	EmptyResponse removerPostNoItem(
		@PathVariable String id, 
		@PathVariable String idItem,
		@PathVariable String idPost) 
		throws Exception
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.COMMENTER, collection, ua.getUser(), ua.getWorkspace());
		
		if(!userIsAdmin(ua))
		{
			collectionService.validarAcessoItem(
				collection, idItem, ua.getUser(), ua.getWorkspace());
		}

		var post = collectionService
			.findItemPostByPubIdAndWorkspace(idPost, ua.getWorkspace());

		collectionService
			.validarAcessoDeEscritaAoPostDeItem(idItem, post, ua.getUser(), ua.getWorkspace());

		collectionService.removerPostNoItem(
			collection, idItem, post, ua.getUser(), ua.getWorkspace());
		
		return new EmptyResponse();
	}

	@GetMapping("/collections/{id}/logs")
	@ApiOperation(value = "Listar logs de uma coleção")
	List<CollectionLogResponse> listarLogs(
		@PathVariable String id, 
		Pageable pageable)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var logs = collectionService.listarLogs(
			collection, ua.getWorkspace(), pageable);

		return logs != null?
			logs.stream()
				.map(l -> new CollectionLogResponse(l, objMapper))
					.collect(Collectors.toList()):
			null;	
	}

	@GetMapping("/collections/{id}/posts")
	@ApiOperation(value = "Listar posts de uma coleção")
	List<CollectionPostResponse> listarPosts(
		@PathVariable String id, 
		Pageable pageable)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.COMMENTER, collection, ua.getUser(), ua.getWorkspace());
		
		var logs = collectionService.listarPosts(
			collection, ua.getWorkspace(), pageable);

		return logs != null?
			logs.stream()
				.map(l -> new CollectionPostResponse(l))
					.collect(Collectors.toList()):
			null;	
	}

	@GetMapping("/collections/{id}/automations")
	@ApiOperation(value = "Listar automações de uma coleção")
	List<CollectionAutomationResponse> listarAutomacoes(
		@PathVariable String id, 
		Pageable pageable)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var automations = collectionAutomationService.findAll(collection, pageable);

		return automations != null? 
			automations.stream()
				.map(i -> CollectionAutomationResponseFactory.create(i))
					.collect(Collectors.toList()): 
			null;
	}	

	@PostMapping("/collections/{id}/automations")
	@ApiOperation(value = "Criar automação em uma coleção")
	CollectionAutomationResponse criarAutomation(
		@PathVariable String id, 
		@Valid @RequestBody CollectionAutomationRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		return CollectionAutomationResponseFactory.create(
			collectionAutomationService.criar(collection, req, ua.getUser(), ua.getWorkspace()));
	}

	@PatchMapping("/collections/{id}/automations/{idAutomation}")
	@ApiOperation(value = "Atualizar automação de uma coleção")
	CollectionAutomationResponse atualizarAutomation(
		@PathVariable String id, 
		@PathVariable String idAutomation,
		@Valid @RequestBody CollectionAutomationRequest req)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var automacao = collectionAutomationService.findByPubIdAndCollection(idAutomation, collection);
		if(automacao == null)
		{
			throw new CollectionException("Automação de Coleção inexistente");
		}

		if(collection.getId() != automacao.getCollection().getId())
		{
			throw new CollectionException("Coleção da automação diferente da Coleção da requisição");
		}
		
		return CollectionAutomationResponseFactory.create(
			collectionAutomationService.atualizar(automacao, req, ua.getUser(), ua.getWorkspace()));
	}
	
	@GetMapping("/collections/{id}/automations/{idAutomation}/logs")
	@ApiOperation(value = "Get automation logs")
	List<CollectionAutomationLogResponse> listAutomationLogs(
		@PathVariable String id, 
		@PathVariable String idAutomation, 
		Pageable pageable)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var automation = collectionAutomationService.findByPubIdAndCollection(idAutomation, collection);
		if(automation == null)
		{
			throw new CollectionException("Automation not found");
		}

		if(collection.getId() != automation.getCollection().getId())
		{
			throw new CollectionException("Automation not part of the collection");
		}

		var logs = collectionAutomationService.findAllLogs(automation, pageable);
		
		return logs.stream()
			.map(log -> 
				new CollectionAutomationLogResponse(log, objMapper))
				.collect(Collectors.toList());
	}	
	
	@GetMapping("/collections/{id}/permissions")
	@ApiOperation(value = "Listar permissões de uma coleção")
	List<CollectionAuthResponse> listarAuths(
		@PathVariable String id, 
		Pageable pageable)
	{
		var ua = getUserSession();
		
		Collection collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var auths = collectionAuthService.findAllByCollection(collection, pageable);

		return auths != null? 
			auths.stream()
				.map(i -> new CollectionAuthResponse(i, ua.getWorkspace()))
					.collect(Collectors.toList()): 
			null;
	}	

	@GetMapping("/collections/{id}/permissions/user")
	@ApiOperation(value = "List current user's permissions on a collection")
	List<CollectionAuthBaseResponse> listUserPermissions(
		@PathVariable String id)
	{
		var ua = getUserSession();
		
		Collection collection = encontrarPorIdAndUser(id, ua);
		
		var auths = collectionAuthService
			.findAllByCollectionAndUser(collection, ua.getUser());

		return auths != null? 
			auths.stream()
				.map(i -> new CollectionAuthBaseResponse(i, ua.getWorkspace()))
					.collect(Collectors.toList()): 
			null;
	}

	@PostMapping("/collections/{id}/permissions")
	@ApiOperation(value = "Criar permissão em uma coleção")
	CollectionAuthResponse criarAuth(
		@PathVariable String id, 
		@Valid @RequestBody CollectionAuthRequest req)
	{
		var ua = getUserSession();
		
		Collection collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var auth = collectionAuthService.criar(collection, req, ua.getUser(), ua.getWorkspace());
		
		return new CollectionAuthResponse(auth, ua.getWorkspace());
	}

	@PatchMapping("/collections/{id}/permissions/{idPerm}")
	@ApiOperation(value = "Atualizar permissão de uma coleção")
	CollectionAuthResponse atualizarAuth(
		@PathVariable String id, 
		@PathVariable String idPerm,
		@Valid @RequestBody CollectionAuthRequest req)
	{
		var ua = getUserSession();
		
		Collection collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(
			CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var auth = collectionAuthService.findByPubIdAndCollection(idPerm, collection);
		if(auth == null)
		{
			throw new CollectionException("Permissão inexistente na Coleção");
		}

		if(collection.getId() != auth.getCollection().getId())
		{
			throw new CollectionException("Coleção da permissão diferente da Coleção da requisição");
		}
		
		return new CollectionAuthResponse(
			collectionAuthService.atualizar(auth, req, ua.getUser(), ua.getWorkspace()),
			ua.getWorkspace());
	}

	@GetMapping("/collections/{id}/versions")
	@ApiOperation(value = "Listar versões de uma coleção")
	List<CollectionVersionResponse> listarVersoes(
		@PathVariable String id, 
		Pageable pageable)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var versoes = collectionService.findAllVersions(collection, pageable);

		return versoes != null? 
			versoes.stream()
				.map(i -> new CollectionVersionResponse(i))
					.collect(Collectors.toList()): 
			null;
	}
	
	@PatchMapping("/collections/{id}/versions/{idVersion}")
	@ApiOperation(value = "Reverter coleção para uma versão anterior")
	CollectionResponse reverterversion(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@PathVariable String idVersion)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var version = collectionService.findVersionById(collection, idVersion);

		var res = collectionService.reverterParaversion(
			collection, version, ua.getUser(), ua.getWorkspace(), sessionId);

		return new CollectionResponse(res);
	}	

	@DeleteMapping("/collections/{id}/versions/{idVersion}")
	@ApiOperation(value = "Remover uma versão anterior de uma coleção")
	List<CollectionVersionResponse> removerVersao(
		@RequestHeader("Session-Id") String sessionId,
		@PathVariable String id, 
		@PathVariable String idVersion)
	{
		var ua = getUserSession();
		
		CollectionWithSchema collection = encontrarPorIdAndUser(id, ua);
		
		collectionService.validarAcesso(CollectionAuthRole.CREATOR, collection, ua.getUser(), ua.getWorkspace());
		
		var version = collectionService.findVersionById(collection, idVersion);

		var versoes = collectionService.removerVersao(
			collection, version, ua.getUser(), ua.getWorkspace(), sessionId);

		return versoes != null? 
			versoes.stream()
				.map(i -> new CollectionVersionResponse(i))
					.collect(Collectors.toList()): 
			null;
	}	

	@GetMapping(path = {"/collections/{id}/forms/{idForm}", "public/collections/{id}/forms/{idForm}"})
	@ApiOperation(value = "Return a collection's form")
	CollectionFormResponse listForm(
		@PathVariable String id,
		@PathVariable String idForm) 
		throws Exception
	{
		CollectionWithSchema collection = (CollectionWithSchema)collectionService
			.findByPubId(id);
		if(collection == null)
		{
			throw new CollectionException("Collection not found");
		}

		var schema = collection.getSchemaObj();
		if(schema.getForms() == null)
		{
			throw new CollectionException("Form not found");
		}

		var form = schema.getForms().stream()
			.filter(f -> f.getId().equals(idForm))
			.findFirst()
			.orElseThrow(() -> new CollectionException("Form not found"));

		if(!form.isActive())
		{
			throw new CollectionException("Form disabled");
		}

		if(!form.isPublic())
		{
			var ua = getUserSession();

			collectionService.validarAcessoAoForm(
				collection, form, ua.getUser(), ua.getWorkspace());
		}
		
		return new CollectionFormResponse(collection, form);
	}
}
