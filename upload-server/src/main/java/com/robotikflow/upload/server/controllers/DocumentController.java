package com.robotikflow.upload.server.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.robotikflow.core.exception.BadRequestException;
import com.robotikflow.core.exception.DocumentException;
import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.DocumentInt;
import com.robotikflow.core.models.entities.DocumentType;
import com.robotikflow.core.models.entities.DocumentAuthType;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.queue.DocExtCreatedMessage;
import com.robotikflow.core.models.queue.DocIntCreatedMessage;
import com.robotikflow.core.models.repositories.ProviderRepository;
import com.robotikflow.core.models.response.DocumentResponse;
import com.robotikflow.core.services.DocumentService;
import com.robotikflow.core.services.factories.DocumentServiceFactory;
import com.robotikflow.core.util.DocumentUtil;
import com.robotikflow.core.util.ProviderUtil;
import com.robotikflow.core.web.services.UserService;
import com.robotikflow.upload.server.models.request.DocumentCriarRequest;

import me.desair.tus.server.TusFileUploadService;

@RestController
public class DocumentController 
{
    @Autowired
    private TusFileUploadService tusFileUploadService;
	@Autowired
	private UserService userService;
	@Autowired
	private DocumentServiceFactory documentServiceFactory;
	@Autowired
	private DocumentUtil documentoUtil;
	@Autowired
	private ProviderRepository providerRepo;
	@Autowired
	private Logger logger;
    @Value("${tus.server.data.directory}")
    private String tusDataPath;
	
	private void validarAcesso(
		final DocumentService documentService,
		final Document diretorio, 
		final User user, 
		final Workspace workspace, 
		final String name)
	{
		documentService.validarAcessoAoDiretorio(
			diretorio, user, DocumentAuthType.MODIFY, workspace);
		
		var document = (DocumentInt)documentService
			.findByNameAndParentAndWorkspace(
				name, DocumentUtil.toInterno(diretorio), workspace);
        
        if(document != null)
        {
            //se o arquivo já existir e estiver bloqueado, só o usuário que o bloqueou pode alterá-lo
			if(document.isLocked() && 
				document.getLockedBy().getId() != user.getId())
        	{
        		throw new BadRequestException(String.format(
					"Document \"%s\" já existe e está bloqueado. Somente quem o bloqueou pode alterá-lo", name));
        	}
            
            //se o arquivo já existir e estiver checkado, só o usuário que fez o check in pode alterá-lo
        	if(document.getChecked())
        	{
        		if(document.getCheckedBy().getId() != user.getId())
        		{
        			throw new BadRequestException(String.format(
						"Document \"%s\" já existe e está com check in. Somente quem fez o check in pode alterá-lo", name));
        		}
        		
        		//TODO: tornar versão sendo criada como atual e remover check in
        	}
        	else
        	{
        		//TODO: arquivo já existe, então é necessário criar uma nova versão
        		throw new BadRequestException(String.format(
					"Document \"%s\" já existe", name));
        	}
        }
	}
	
	private Map<String, String> metadadosToParams(
		final String metadados)
	{
		var res = new HashMap<String, String>();
		
		var paresDeNomeValor = metadados.split(",");
		for(var nomeEspacoValor : paresDeNomeValor)
		{
			var nomeValor = nomeEspacoValor.split(" ");
			var name = nomeValor[0];
			var valor = nomeValor.length > 1? nomeValor[1]: "";
			res.put(
				name, 
				valor != null? 
					new String(Base64.getDecoder().decode(valor), StandardCharsets.UTF_8): 
					"");
		}
		
		return res;
	}

    @PostMapping("/docs/upload")
    public void iniciarUpload(
		final HttpServletRequest req, 
		final HttpServletResponse res) 
		throws Exception 
    {
		var meta = metadadosToParams(req.getHeader("Upload-Metadata"));
		
		var userSession = userService.getUserSession();

		var user = userSession.getUser();
		var workspace = userSession.getWorkspace();
		
		var provider = meta.get("providerId") != null?
			providerRepo.findByPubIdAndWorkspace(
				meta.get("providerId"), workspace):
			null;

		var documentService = documentServiceFactory.build(provider);

		var isInternal = provider == null || 
			ProviderUtil.isInternalStorage(provider);
		
		var name = meta.get("name");
		
		documentService.validarNome(name);

		var diretorio = criarDestino(
			documentService,
			provider,
			meta.get("parentId"), 
			meta.get("parentPath"),
			user,
			workspace);
        
		if(isInternal)
		{
			validarAcesso(
				documentService, diretorio, user, workspace, name);
		}
		
		res.setHeader("Parent-Id", diretorio.getPubId());
    	tusFileUploadService.process(req, res);
    }

    @PatchMapping("/docs/upload/**")
    public void processarUpload(
		final HttpServletRequest req, 
		final HttpServletResponse res) 
		throws IOException 
    {
    	tusFileUploadService.process(req, res);
    }
    
    @RequestMapping(value = "/docs/upload/**", method = RequestMethod.HEAD)
    public void continuarUpload(
		final HttpServletRequest req, 
		final HttpServletResponse res) 
		throws IOException 
    {
    	tusFileUploadService.process(req, res);
    }
    
	@PostMapping("/docs")
	DocumentResponse criar(
		final @Valid @RequestBody DocumentCriarRequest req) 
		throws Exception
	{
		var userSession = userService.getUserSession();

		var user = userSession.getUser();
		var workspace = userSession.getWorkspace();
		
		var uploadInfo = tusFileUploadService
			.getUploadInfo(req.getUploadUrl());
		
		if(uploadInfo.isUploadInProgress()) 
		{
			throw new BadRequestException(
				"Upload do conteúdo do document não foi finalizado");
		}
        
		var provider = req.getProviderId() != null?
			providerRepo.findByPubIdAndWorkspace(
				req.getProviderId(), workspace):
			null;

		var documentService = documentServiceFactory.build(provider);

		var isInternal = provider == null || 
			ProviderUtil.isInternalStorage(provider);

		if(req.getParentId() == null)
		{
			throw new DocumentException("Campo parentId não pode ser nulo");
		}

		var diretorio = documentService.findByPubIdAndWorkspace(
			req.getParentId(), workspace.getId());
		
		if(isInternal)
		{
			validarAcesso(documentService, diretorio, user, workspace, req.getName());
		}
		
        try
        {
            // criar arquivo
			Document novo = null;
			if(isInternal)
			{
				novo = documentService.gravar(new DocumentInt(
					req.getName(), 
					DocumentType.FILE, 
					req.getSize(), 
					diretorio, 
					user, 
					user.getGroups(workspace.getId()).get(0), workspace));
			}
			else
			{
				novo = documentService.criarExterno(
					provider,
					null, 
					null, 
					req.getName(), 
					DocumentUtil.getExtension(req.getName(), DocumentType.FILE),
					null, 
					req.getSize(),
					diretorio,
					user, 
					ZonedDateTime.now(),
					null, 
					null,
					workspace);
			}
        	
            // recuperar caminho do arquivo temporário para o pós processamento
        	var caminhoCompleto = Paths.get(
				tusDataPath, "uploads", uploadInfo.getId().toString(), "data")
				.toAbsolutePath()
					.normalize()
						.toString();
            
			if(isInternal)
			{
				// enviar para o queue para gerar o thumbnail, enviar para elastisearch e subir conteúdo do arquivo para a núvem
        		documentService.getQueue().enviar(
					new DocIntCreatedMessage(novo.getId(), caminhoCompleto));
			}
			else
			{
				// enviar para o queue para subir no serviço de armazenamento
        		documentService.getQueue().enviar(
					new DocExtCreatedMessage(novo.getId(), caminhoCompleto));
			}
        	
        	return new DocumentResponse(novo, documentoUtil);
        }
        catch(Exception ex)
        {
        	logger.error(ex.getMessage());
			throw ex;
        }
	}

	private Document criarDestino(
		final DocumentService documentService,
		final Provider provider,
		final String parentId, 
		final String parentPath, 
		final User user,
		final Workspace workspace) 
	{
		if(parentId != null)
		{
			return documentService
				.findByPubIdAndWorkspace(parentId, workspace.getId());
		}

		if(parentPath != null)
		{
			return documentService
				.criarDiretorio(provider, parentPath, false, user, workspace);
		}

		throw new DocumentException("Ou parentId ou parentPath deve ser definido");
	}
}