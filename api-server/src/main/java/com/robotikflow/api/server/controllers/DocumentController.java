package com.robotikflow.api.server.controllers;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

import com.robotikflow.core.exception.BadRequestException;
import com.robotikflow.core.exception.DocumentException;
import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.DocumentInt;
import com.robotikflow.core.models.entities.DocumentType;
import com.robotikflow.core.models.entities.DocumentAuthType;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.queue.DocIntUpdatedMessage;
import com.robotikflow.core.models.queue.DocIntCopiedMessage;
import com.robotikflow.core.models.response.DocumentWithPathResponse;
import com.robotikflow.core.models.response.DocumentResponse;
import com.robotikflow.core.services.DocumentService;
import com.robotikflow.core.services.factories.DocumentServiceFactory;
import com.robotikflow.core.util.DocumentUtil;
import com.robotikflow.api.server.models.request.DocumentAcaoCopiarRequest;
import com.robotikflow.api.server.models.request.DocumentAcaoCriarDiretorioRequest;
import com.robotikflow.api.server.models.request.DocumentAcaoMoverRequest;
import com.robotikflow.api.server.models.request.DocumentAcaoRenomearRequest;
import com.robotikflow.api.server.models.request.DocumentAcaoRequest;
import com.robotikflow.api.server.models.response.EmptyResponse;
import com.robotikflow.api.server.models.response.UrlResponse;

import io.swagger.annotations.ApiOperation;

@RestController
public class DocumentController extends BaseController 
{
	@Autowired
	private Logger logger;
	@Autowired
	private DocumentService documentService;
	@Autowired
	private DocumentServiceFactory documentServiceFactory;	
	private static HashSet<String> colunasValidasDeOrdenacao = new HashSet<>(Arrays.asList
	( 
		"name", "extension", "size", "owner", "createdAt", "updatedAt"
	));

	@GetMapping("/docs/{id}")
	@ApiOperation(value = "Retornar link para download do conteúdo de um arquivo")
	UrlResponse getDownloadUrl(
		@PathVariable(name = "id") String pubId, 
		@RequestParam(value = "validade", required = false, defaultValue = "60") int validadeEmSegundos)
		throws Exception
	{
		var userSession = getUserSession();

		var user = userSession.getUser();
		var workspace = userSession.getWorkspace();

		var document = documentService
			.findByPubIdAndWorkspace(pubId, workspace.getId());
		if(document == null)
		{
			throw new DocumentException("Arquivo inexistente");
		}

		String url = null;
		if(DocumentUtil.isInterno(document))
		{
			url = documentService
				.getDownloadUrl(document, user, workspace, validadeEmSegundos);
		}
		else
		{
			var doc = DocumentUtil.toExterno(document);
			var docService = documentServiceFactory.build(doc.getProvider());  
			url = docService
				.getDownloadUrl(document, user, workspace, validadeEmSegundos);
		}

		return new UrlResponse(url);
	}

	@GetMapping("/docs/{id}/preview")
	@ApiOperation(value = "Retornar link para preview de um arquivo")
	UrlResponse getPreviewUrl(
		@PathVariable(name = "id") String pubId) 
		throws Exception
	{
		var userSession = getUserSession();

		var user = userSession.getUser();
		var workspace = userSession.getWorkspace();

		var document = documentService
			.findByPubIdAndWorkspace(pubId, workspace.getId());
		if(document == null)
		{
			throw new DocumentException("Arquivo inexistente");
		}

		String url = null;
		if(DocumentUtil.isInterno(document))
		{
			url = documentService
				.getPreviewUrl(document, user, workspace);
		}
		else
		{
			var doc = DocumentUtil.toExterno(document);
			var docService = documentServiceFactory.build(doc.getProvider());  
			url = docService
				.getPreviewUrl(document, user, workspace);
		}

		return new UrlResponse(url);
	}

	private String extractPath(
		final HttpServletRequest request) 
	{
		 var urlPath = (String)request
		 	.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		 var bestMatchPattern = (String)request
		 	.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		 var path = new AntPathMatcher()
		 	.extractPathWithinPattern(bestMatchPattern, urlPath);
		 return urlPath.endsWith("/")? path + '/': path;
	}
	  
	@GetMapping("/docs/pesquisar/caminho/**")
	@ApiOperation(value = "Procurar diretórios e arquivos pelo caminho")
	List<DocumentWithPathResponse> findByCaminho(
		final HttpServletRequest request) 
		throws UnsupportedEncodingException
	{
		var userSession = getUserSession();
		
		var user = userSession.getUser();
		var workspace = userSession.getWorkspace();
		
		var caminho = extractPath(request);
		
		var documentos = documentService
			.findAllByCaminhoAndUserAndWorkspace(caminho, user, workspace);
		
		return documentos.stream()
				.map(d -> new DocumentWithPathResponse(d, documentoUtil))
					.collect(Collectors.toList());
	}
	
	@GetMapping("/docs/{id}/filhos")
	@ApiOperation(value = "Listar os diretórios e arquivos filhos a partir um diretório")
	List<DocumentResponse> lerFilhos(
		@PathVariable(name = "id") String pubId, 
		@RequestParam(value = "any", required = false) boolean any,
		@RequestParam(value = "filters", required = false) String filters,
		@RequestParam(value = "contentSearch", required = false) boolean contentSearch,
		Pageable pageable) 
		throws Exception
	{
		var userSession = getUserSession();
		
		var user = userSession.getUser();
		var workspace = userSession.getWorkspace();
		
		var diretorio = documentService
			.findByPubIdAndWorkspace(pubId, workspace.getId());
		
		documentService
			.validarAcessoAoDiretorio(diretorio, user, DocumentAuthType.READ, workspace);
		
		pageable = validateSorting(pageable, colunasValidasDeOrdenacao, "name", Direction.ASC);
		
		if(contentSearch && filters != null && filters.trim().length() > 0)
		{
			return lerFilhosFiltrandoPeloConteudo(diretorio, pageable, filters);
		}
		
		// adicionar ordenação por type (diretórios primeiro)
		pageable = PageRequest.of(
			pageable.getPageNumber(), 
			pageable.getPageSize() > 0? 
				pageable.getPageSize(): 
				10000, 
			Sort.by("type")
				.ascending()
					.and(pageable.getSort()));
		
		List<Document> documentos;
		
		if(filters != null)
		{
			documentos = any? 
					documentService.getRepo()
						.findAllByParentAndUserAndNameContainingIgnoreCase(
							diretorio, user, filters, pageable):
					documentService.getRepo()
						.findAllByParentAndUserAndTypeAndNameContainingIgnoreCase(
							diretorio, user, DocumentType.FOLDER, filters, pageable);
		}
		else
		{
			documentos = any? 
					documentService.getRepo()
						.findAllByParentAndUser(
							diretorio, user, pageable):
					documentService.getRepo()
						.findAllByParentAndUserAndType(
							diretorio, user, DocumentType.FOLDER, pageable);
		}
		
		return documentos.stream()
				.map(d -> new DocumentResponse(d, documentoUtil))
					.collect(Collectors.toList());
	}

	private List<DocumentResponse> lerFilhosFiltrandoPeloConteudo(
		final Document diretorio, 
		final Pageable pageable, 
		final String query) 
		throws Exception 
	{
		var res = new ArrayList<DocumentResponse>();
		
		var idWorkspace = diretorio.getWorkspace().getId();

		var hits = documentService.getIndex()
			.pesquisar(
				query, 
				diretorio.getPubId(), 
				diretorio.getWorkspace().getPubId(), 
				pageable);
		
		for(var hit : hits)
		{
			var doc = documentService.getRepo()
				.findByPubIdAndWorkspace(hit.getId(), idWorkspace);
			if(doc != null)
			{
				res.add(new DocumentResponse(doc, hit.getHighlightText(), documentoUtil));
			}
		}
		
		return res;
	}

	@DeleteMapping("/docs/{id}")
	@ApiOperation(value = "Remover um arquivo ou diretório")
	EmptyResponse remover(
		@PathVariable(name = "id") String pubId) 
	{
		var userSession = getUserSession();
		
		var user = userSession.getUser();
		var workspace = userSession.getWorkspace();
		
		documentService.remover(pubId, user, workspace);
		
		return new EmptyResponse();
	}

	@PatchMapping("/docs/{id}")
	@ApiOperation(value = "Executar uma ação em um arquivo ou diretório")
	DocumentResponse acao(
		@PathVariable(name = "id") String pubId, 
		@Valid @RequestBody DocumentAcaoRequest genReq) 
		throws Exception 
	{
		var userSession = getUserSession();

		var user = userSession.getUser();
		var workspace = userSession.getWorkspace();

		var document = documentService
			.findByPubIdAndWorkspace(pubId, workspace.getId());

		if(!DocumentUtil.isInterno(document))
		{
			throw new DocumentException("Somente documentos internos são suportados");
		}

		var docInt = DocumentUtil.toInterno(document);
		
		switch(genReq.getType())
		{
			case MOVER:
				return moverDocument(
					(DocumentAcaoMoverRequest)genReq, user, workspace, docInt);
			
			case COPIAR:
				return copiarDocument(
					(DocumentAcaoCopiarRequest)genReq, user, workspace, docInt);
				
			case RENOMEAR:
				return renomearDocument(
					(DocumentAcaoRenomearRequest)genReq, user, workspace, docInt);
			
			case CRIAR_DIRETORIO:
				return criarDiretorio(
					(DocumentAcaoCriarDiretorioRequest)genReq, user, workspace, docInt);
				
			default:
				return null;
		}
	}
	
	private DocumentResponse criarDiretorio(
		final DocumentAcaoCriarDiretorioRequest req, 
		final User user, 
		final Workspace workspace,
		final DocumentInt document) 
	{
		documentService
			.validarAcessoAoDocument(document, user, DocumentAuthType.MODIFY, workspace);
		
		documentService.validarNome(req.getDiretorio());
		
		var docComMesmoNome = documentService
			.findByNameAndParentAndWorkspace(req.getDiretorio(), document, workspace);
		if(docComMesmoNome != null)
		{
			throw new BadRequestException("Um diretório com o mesmo name já existe");
		}

		var dir = new DocumentInt(
			req.getDiretorio(), 
			DocumentType.FOLDER, 
			0L, 
			document, 
			user, 
			user.getGroups(workspace.getId()).get(0), 
			workspace);
		
		documentService.gravar(dir);
		
		return new DocumentResponse(dir, documentoUtil);
	}

	private DocumentResponse renomearDocument(
		final DocumentAcaoRenomearRequest req, 
		final User user, 
		final Workspace workspace,
		final DocumentInt document) 
	{
		documentService
			.validarAcessoAoDocument(document, user, DocumentAuthType.MODIFY, workspace);
		
		if(document.getParent() == null)
		{
			throw new BadRequestException("Não é possível executar essa ação no diretório raiz");
		}
		
		documentService.validarNome(req.getName());
		
		var docComMesmoNome = documentService.getRepo()
			.findByNameAndParentAndWorkspace(req.getName(), document.getParent(), workspace);
		if(docComMesmoNome != null && 
			docComMesmoNome.getId() != document.getId())
		{
			throw new BadRequestException("Um document com o mesmo name já existe no diretório");
		}
		
		document.setName(req.getName());
		documentService.gravar(document);
		return new DocumentResponse(document, documentoUtil);
	}

	private DocumentResponse copiarDocument(
		final DocumentAcaoCopiarRequest req, 
		final User user,
		final Workspace workspace, 
		final DocumentInt document) 
		throws Exception 
	{
		// tentando copiar o diretório raiz?
		if(document.getParent() == null)
		{
			throw new BadRequestException("Não é possível executar essa ação no diretório raiz");
		}

		// copiando para si mesmo?
		if(document.getPubId().equals(req.getDestino()))
		{
			throw new BadRequestException("Não é possível copiar para si mesmo");
		}
		
		var destino = documentService
			.findByPubIdAndWorkspace(req.getDestino(), workspace.getId());

		// destino não existe ou não é um diretório?
		if(destino == null || 
			destino.getType() != DocumentType.FOLDER) 
		{
			throw new BadRequestException("Destino inexistente ou não é um diretório");
		}
		
		// validar acesso ao destino
		documentService
			.validarAcessoAoDocument(destino, user, DocumentAuthType.MODIFY, workspace);

		if(!DocumentUtil.isInterno(destino) || 
			!DocumentUtil.isInterno(document))
		{
			throw new DocumentException("Somente documentos internos são suportados");
		}
		
		var copia = copiarDocument(
			DocumentUtil.toInterno(document), 
			DocumentUtil.toInterno(destino), 
			user, 
			workspace);
		
		return new DocumentResponse(copia, documentoUtil);
	}

	private DocumentResponse moverDocument(
		final DocumentAcaoMoverRequest req, 
		final User user,
		final Workspace workspace, 
		final DocumentInt document) throws Exception 
	{
		documentService
			.validarAcessoAoDocument(document, user, DocumentAuthType.MODIFY, workspace);
		
		// tentando mover o diretório raiz?
		if(document.getParent() == null)
		{
			throw new BadRequestException("Não é possível executar essa ação no diretório raiz");
		}

		// movendo para si mesmo?
		if(document.getPubId().equals(req.getDestino()))
		{
			throw new BadRequestException("Não é possível mover para si mesmo");
		}
		
		var destino = documentService.findByPubIdAndWorkspace(req.getDestino(), workspace.getId());

		// destino não existe ou não é um diretório?
		if(destino == null || destino.getType() != DocumentType.FOLDER) 
		{
			throw new BadRequestException("Destino inexistente ou não é um diretório");
		}
		
		// tentando mover um diretório para seu filho?
		if(document.getType() == DocumentType.FOLDER)
		{
			var parent = (DocumentUtil.toInterno(destino)).getParent();
			while(parent != null && 
				parent != document.getParent())
			{
				if(parent == document)
				{
					throw new BadRequestException("Não é permitido mover um diretório para um diretório filho");
				}
				parent = parent.getParent();
			}
		}
		
		// validar acesso ao destino
		documentService
			.validarAcessoAoDocument(destino, user, DocumentAuthType.MODIFY, workspace);
		
		// já existe document com o mesmo name?
		var docComMesmoNome = documentService.getRepo()
			.findByNameAndParentAndWorkspace(document.getName(), destino, workspace);
		if(docComMesmoNome != null)
		{
			throw new BadRequestException("Um document com o mesmo name já existe no diretório");
		}
		
		document.setParent(DocumentUtil.toInterno(destino));
		
		documentService.gravar(document);
		
		try
		{
			documentService.getQueue()
				.enviar(new DocIntUpdatedMessage(document.getId()));
		}
		catch(Exception ex)
		{
			logger.error(ex.getMessage());
			throw ex;
		}

		return new DocumentResponse(document, documentoUtil);
	}

	private DocumentInt copiarDocument(
		final DocumentInt document, 
		final DocumentInt destino, 
		final User user, 
		final Workspace workspace) 
		throws Exception 
	{
		documentService
			.validarAcessoAoDocument(document, user, DocumentAuthType.READ, workspace);

		// já existe document com o mesmo name?
		var name = document.getName();
		var num = 1;
		while(documentService
			.findByNameAndParentAndWorkspace(name, destino, workspace) != null)
		{
			// criar name único
			name = document.getName();
			name = document.getType() == DocumentType.FOLDER?
				name + "_cópia" + num:
				FilenameUtils.getBaseName(name) + "_cópia" + num + "." + FilenameUtils.getExtension(name);
			++num;
		}
		
		// criar cópia
		var copia = new DocumentInt(document);
		copia.setParent(destino);
		copia.setName(name);
		documentService.gravar(copia);
		
		// copiar blob
		if(document.getType() != DocumentType.FOLDER)
		{
			documentService.getQueue()
				.enviar(new DocIntCopiedMessage(copia.getId(), document.getId()));
		}
		
		// copiar todos os filhos
		if(document.getChildren() != null)
		{
			for(var child : document.getChildren())
			{
				copiarDocument((DocumentInt)child, copia, user, workspace);
			}
		}
		
		return copia;
	}
}
