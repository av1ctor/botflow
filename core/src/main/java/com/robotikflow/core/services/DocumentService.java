package com.robotikflow.core.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.robotikflow.core.exception.DocumentException;
import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.DocumentWithPath;
import com.robotikflow.core.models.entities.DocumentWithPathProjection;
import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.models.entities.DocumentInt;
import com.robotikflow.core.models.entities.DocumentType;
import com.robotikflow.core.models.entities.DocumentAuthType;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.queue.DocIntCreatedMessage;
import com.robotikflow.core.models.queue.DocIntDeletedMessage;
import com.robotikflow.core.models.repositories.DocumentsWithPathRepository;
import com.robotikflow.core.models.repositories.DocumentRepository;
import com.robotikflow.core.models.repositories.GroupRepository;
import com.robotikflow.core.models.repositories.DocumentAuthRepository;
import com.robotikflow.core.interfaces.IStorageProviderService;
import com.robotikflow.core.interfaces.props.DocProps;
import com.robotikflow.core.services.indexing.DocumentIndexService;
import com.robotikflow.core.services.queue.QueueService;
import com.robotikflow.core.util.DocumentUtil;
import com.robotikflow.core.util.ProviderUtil;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.JpaSort;

public class DocumentService 
{
	@Autowired
	private Logger logger;
	@Autowired
	private DocumentRepository documentRepo;
	@Autowired
	private DocumentsWithPathRepository documentWithPathRepo;
	@Autowired
	private GroupRepository groupRepo;
	@Autowired
	private DocumentAuthRepository authRepo;
	@Autowired
	@Lazy
    @Qualifier("docsQueueService")
	private QueueService docsQueueService;
	@Lazy
    @Autowired
	private DocumentIndexService indexService;
	private IStorageProviderService storageProvider;
	
	private static String INVALID_FILE_NAME_CHARS = "[\\\\|/|:|*|?|\"|<|>|\\|]+";
	private static Pattern invalidFileCharsRe = Pattern.compile(".*[\\\\|/|:|*|?|\"|<|>|\\|]+.*|\\.\\.|\\.");
	
	public DocumentService(
		final Environment env, 
		final IStorageProviderService provider)
	{
		this.storageProvider = provider;
	}

	public DocumentRepository getRepo()
	{
		return documentRepo;
	}
	
	public QueueService getQueue()
	{
		return docsQueueService;
	}
	
	public DocumentIndexService getIndex()
	{
		return indexService;
	}

	public IStorageProviderService getStorageProvider()
	{
		return storageProvider;
	}

	/**
	 * 
	 * @param provider
	 * @param fileId
	 * @param workspace
	 * @return
	 */
	public DocumentExt findByProviderAndFileIdAndWorkspace(
		final Provider provider, 
		final String fileId,
		final Workspace workspace) 
	{
		return documentRepo
			.findByFiledIdAndProviderAndWorkspace(provider, fileId, workspace);
	}

	/**
	 * 
	 * @param name
	 */
	public void validarNome(String name) 
	{
		if(invalidFileCharsRe.matcher(name).matches())
		{
			throw new DocumentException(String.format("Nome do document \"%s\" contém caracteres inválidos", name));
		}
	}
	
	/**
	 * 
	 * @param name
	 * @return
	 */
	public String limparNome(
		final String name)
	{
		return name.replaceAll(INVALID_FILE_NAME_CHARS, "-");
	}
	
	/**
	 * 
	 * @param isDir
	 * @param document
	 * @param user
	 * @param authTipo
	 * @param workspace
	 */
	public void validarAcesso(
		final boolean isDir, 
		final Document doc, 
		final User user, 
		final DocumentAuthType authTipo, 
		final Workspace workspace) 
	{
		if(!DocumentUtil.isInterno(doc))
		{
			return;
		}

		var document = DocumentUtil.toInterno(doc);
		
		var nomeTipo = isDir? "Diretório": "Document";
		
		// usuário é dono do document?
		if(document.getOwner().getId() == user.getId())
		{
			// sua permissão está negada para leitura ou modificação?
			if(document.getOwnerAuth().ordinal() < authTipo.ordinal())
			{
				throw new DocumentException(nomeTipo + " sem permissão para o dono");
			}
		}
		else
		{
			var groupsIdList = groupRepo.findAllByUserAndWorkspace(user.getId(), workspace.getId()).stream()
				.map(g -> g.getId())
					.collect(Collectors.toList());
			
			// usuário faz parte de algum group que tenha acesso ao diretório?
			if(groupsIdList.contains(document.getGroup().getId()))
			{
				// a permissão do group está negada para leitura ou modificação?
				if(document.getGroupAuth().ordinal() < authTipo.ordinal())
				{
					throw new DocumentException(nomeTipo + " sem permissão para o group");
				}
			}
			else
			{
				// outros podem acessar o document?
				if(document.getOthersAuth().ordinal() < authTipo.ordinal())
				{
					// não há permissão específica definida para o usuário ou seu group?
					var auths = authRepo.findAllByDocumentAndUserOrGroup(document.getId(), user.getId(), groupsIdList);
					if(auths == null || auths.isEmpty())
					{
						throw new DocumentException(nomeTipo + " sem permissão para terceiros");
					}
					
					var temAuth = false;
					for(var auth : auths)
					{
						if(auth.getType().ordinal() >= authTipo.ordinal())
						{
							temAuth = true;
							break;
						}
					}
					if(!temAuth)
					{
						throw new DocumentException(nomeTipo + " sem permissão para terceiros");
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param document
	 * @param user
	 * @param authTipo
	 * @param workspace
	 */
	public void validarAcessoAoDiretorio(
		final Document document, 
		final User user, 
		final DocumentAuthType authTipo, 
		final Workspace workspace)
	{
		if(document == null)
		{
			throw new DocumentException("Diretório não encontrado no área de trabalho");
		}
		
		if(document.getType() != DocumentType.FOLDER)
		{
			throw new DocumentException("Document não é um diretório");
		}
		
		validarAcesso(true, document, user, authTipo, workspace);
	}

	/**
	 * 
	 * @param document
	 * @param user
	 * @param authTipo
	 * @param workspace
	 */
	public void validarAcessoAoDocument(
		final Document document, 
		final User user, 
		final DocumentAuthType authTipo, 
		final Workspace workspace)
	{
		if(document == null)
		{
			throw new DocumentException("Document não encontrado no área de trabalho");
		}
		
		validarAcesso(false, document, user, authTipo, workspace);
	}
	
	private DocumentInt criarInterno(
		final String name, 
		final long tamanho, 
		final byte[] conteudo, 
		final DocumentInt noDiretorio, 
		final User user, 
		final Workspace workspace) 
		throws IOException
	{
		validarNome(name);
		
		var document = documentRepo.save(new DocumentInt(
			name, 
			DocumentType.FILE, 
			tamanho, 
			noDiretorio, 
			user, 
			user.getGroups(workspace.getId()).get(0
			), workspace));
        
        var file = File.createTempFile("robotikflow-", ".tmp");
    	
    	try(var fos = new FileOutputStream(file))
    	{
    		fos.write(conteudo);
    	}
    	catch (Exception e)
    	{
    		throw new DocumentException(String.format("Erro ao gravar arquivo '%s' na pasta de temporários", name), e);
    	}

        try 
    	{
        	var caminhoCompleto = file.getAbsolutePath();
        	
			docsQueueService.enviar(
				new DocIntCreatedMessage(document.getId(), caminhoCompleto, false));
		} 
    	catch (Exception e) 
    	{
    		throw new DocumentException(String.format("Erro ao colocar arquivo '%s' na fila", name), e);
		}
		
    	return document;
	}

	private Document criarExterno(
		final Provider provider,
		final String name, 
		final long tamanho, 
		final byte[] conteudo, 
		final DocumentExt destino,
		final User user, 
		final Workspace workspace) 
		throws IOException
	{
		validarNome(name);

		var document = new DocumentExt(
			provider, 
			name, 
			DocumentType.FILE, 
			tamanho, 
			destino, 
			user, 
			null,
			null, 
			null, 
			null, 
			workspace);
		
		var res = storageProvider.createFile(
			conteudo, 
			workspace.getPubId(), 
			name, 
			document.getExtension(), 
			new DocProps(destino.getFileId(), destino.getFilePath(), null, null));

		document.setFileId(res.getId());
		document.setFilePath(res.getPath());
		document.setMimeType(res.getMimeType());
		document.setUrlDownload(res.getUrlDownload());
		
		return documentRepo.save(document);
	}

	/**
	 * 
	 * @param provider
	 * @param name
	 * @param tamanho
	 * @param conteudo
	 * @param destino
	 * @param user
	 * @param workspace
	 * @return
	 * @throws IOException
	 */
	public Document criar(
		final Provider provider,
		final String name, 
		final long tamanho, 
		final byte[] conteudo, 
		final Document destino, 
		final User user, 
		final Workspace workspace) 
		throws IOException
	{
		if(provider == null || ProviderUtil.isInternalStorage(provider))
		{
			return criarInterno(
				name, tamanho, conteudo, (DocumentInt)destino, user, workspace);
		}
		else
		{
			return criarExterno(
				provider, name, tamanho, conteudo, (DocumentExt)destino, user, workspace);
		}
	}

	/**
	 * 
	 * @param provider
	 * @param id
	 * @param name
	 * @param extensao
	 * @param mimeType
	 * @param tamanho
	 * @param parent
	 * @param user
	 * @param createdAt
	 * @param updatedAt
	 * @param urlDownload
	 * @param workspace
	 * @return
	 */
	public DocumentExt criarExterno(
		final Provider provider, 
		final String id, 
		final String path, 
		final String name, 
		final String extensao, 
		final String mimeType, 
		final Long tamanho,
		final Document parent,
		final User user, 
		final ZonedDateTime createdAt, 
		final ZonedDateTime updatedAt, 
		final String urlDownload,
		final Workspace workspace) 
	{
		validarNome(name);
		
		var document = new DocumentExt(
			provider, 
			name, 
			DocumentType.FILE, 
			tamanho, 
			parent,
			user, 
			createdAt,
			updatedAt,
			id,
			path, 
			extensao, 
			mimeType, 
			urlDownload, 
			workspace);
			
        return documentRepo.save(document);
	}

	/**
	 * 
	 * @param provider
	 * @param caminho
	 * @param user
	 * @param workspace
	 * @return
	 */
	public Document criarDiretorio(
		final Provider provider, 
		final String caminho, 
		final boolean isRaiz, 
		final User user, 
		final Workspace workspace)
	{
		Document document = null;

		var isInterno = provider == null || 
			ProviderUtil.isInternalStorage(provider);

		var parent = isInterno? 
			workspace.getRootDoc(): 
			(isRaiz? null: storageProvider.getRootDoc());

		var nomes = caminho.split("/");
		for(var name : nomes)
		{
			name = limparNome(name);
			validarNome(name);

			document = parent != null? 
				findByNameAndParentAndWorkspace(name, parent, workspace):
				findByNameAndProviderAndWorkspace(name, provider, workspace);
			if(document == null)
			{
				if(!isInterno)
				{
					var extParent = DocumentUtil.toExterno(parent);
					var res = storageProvider.createFolder(
						name, 
						parent != null? 
							new DocProps(
								extParent.getFileId(), extParent.getFilePath(), null, null): 
							null);

					document = documentRepo.save(new DocumentExt(
						provider, 
						name, 
						DocumentType.FOLDER, 
						0L, 
						parent, 
						user, 
						res.getId(), 
						res.getPath(),
						res.getMimeType(), 
						res.getUrlDownload(), 
						workspace));
				}
				else
				{
					document = documentRepo.save(new DocumentInt(
						name, 
						DocumentType.FOLDER, 
						0L, 
						parent, 
						user, 
						user.getGroups(workspace.getId()).get(0), 
						workspace));
				}

			}

			parent = document;
		}
        
        return document;
	}
	
	private Document findByNameAndProviderAndWorkspace(
		final String name, 
		final Provider provider, 
		final Workspace workspace) 
	{
		return documentRepo
			.findByNameAndProviderAndWorkspace(name, provider, workspace);
	}

	/**
	 * 
	 * @param pubId
	 * @param idWorkspace
	 * @return
	 */
	public Document findByPubIdAndWorkspace(
		final String pubId, 
		final Long idWorkspace) 
	{
		return documentRepo
			.findByPubIdAndWorkspace(pubId, idWorkspace);
	}

	/**
	 * 
	 * @param name
	 * @param diretorio
	 * @param workspace
	 * @return
	 */
	public Document findByNameAndParentAndWorkspace(
		final String name, 
		final Document diretorio, 
		final Workspace workspace) 
	{
		return documentRepo
			.findByNameAndParentAndWorkspace(name, diretorio, workspace);
	}

	public Document findByProviderAndPathAndWorkspace(
		final Provider provider,
		final String path,
		final Workspace workspace
	)
	{
		return documentRepo
			.findByProviderAndPathAndWorkspace(
				provider.getId(), path, workspace.getId());
	}

	/**
	 * 
	 * @param document
	 * @return
	 */
	public Document gravar(Document document) 
	{
		return documentRepo.save(document);
	}
	
	/**
	 * 
	 * @param pubId
	 * @param user
	 * @param workspace
	 * @param validadeEmSegundos
	 * @return
	 */
	public String getDownloadUrl(
		final Document document, 
		final User user, 
		final Workspace workspace, 
		final int validadeEmSegundos) 
	{
		validarAcessoAoDocument(document, user, DocumentAuthType.READ, workspace);
		
		if(document.getType() == DocumentType.FOLDER)
		{
			throw new DocumentException(
				String.format(
					"Download de diretório não suportado: %s", document.getName()));
		}

		String url = null;
		if(DocumentUtil.isInterno(document))
		{
			var doc = DocumentUtil.toInterno(document);
			url = storageProvider.getDownloadUrl(
				workspace.getPubId(), 
				doc.getBlobId(), 
				document.getExtension(), 
				validadeEmSegundos <= 0? 60: validadeEmSegundos);
		}
		else
		{
			var doc = DocumentUtil.toExterno(document);
			url = storageProvider.getDownloadUrl(
				doc, 
				validadeEmSegundos <= 0? 60: validadeEmSegundos);
		}
		
		return url;
	}

	/**
	 * 
	 * @param pubId
	 * @param user
	 * @param workspace
	 * @param validadeEmSegundos
	 * @return
	 */
	public String getPreviewUrl(
		final Document document, 
		final User user, 
		final Workspace workspace) 
	{
		validarAcessoAoDocument(document, user, DocumentAuthType.READ, workspace);
		
		if(document.getType() == DocumentType.FOLDER)
		{
			throw new DocumentException(String.format("Preview de diretório não suportado: %s", document.getName()));
		}

		String url = null;
		if(DocumentUtil.isInterno(document))
		{
			var doc = DocumentUtil.toInterno(document);
			url = storageProvider.getPreviewUrl(
				workspace.getPubId(), 
				doc.getBlobId(), 
				document.getExtension());
		}
		else
		{
			var doc = DocumentUtil.toExterno(document);
			url = storageProvider.getPreviewUrl(doc);
		}
		
		return url;
	}

	/**
	 * 
	 * @param pubId
	 * @param user
	 * @param workspace
	 */
	public void remover(
		final String pubId, 
		final User user, 
		final Workspace workspace) 
	{
		var document = documentRepo
			.findByPubIdAndWorkspace(pubId, workspace.getId());

		remover(document, user, workspace);
	}

	/**
	 * 
	 * @param document
	 * @param user
	 * @param workspace
	 */
	public void remover(
		final Document document, 
		final User user, 
		final Workspace workspace) 
	{
		if(!DocumentUtil.isInterno(document))
		{
			throw new DocumentException("Somente documentos internos são suportados");
		}

		var docInt = DocumentUtil.toInterno(document);

		if(docInt.getParent() == null)
		{
			throw new DocumentException("Não é possível apagar o diretório raiz");
		}
		
		validarAcessoAoDocument(docInt, user, DocumentAuthType.MODIFY, workspace);
		
        // se o arquivo já existir e estiver bloqueado, só o usuário que o bloqueou pode exclui-lo
		if(docInt.isLocked() && 
			docInt.getLockedBy().getId() != user.getId())
    	{
    		throw new DocumentException("Document está bloqueado. Somente quem o bloqueou pode exclui-lo");
    	}
        
        // se o arquivo já existir e estiver checkado, só o usuário que fez o check in pode exclui-lo
    	if(docInt.getChecked())
    	{
    		if(docInt.getCheckedBy().getId() != user.getId())
    		{
    			throw new DocumentException("Document está com check in. Somente quem fez o check in pode exclui-lo");
    		}
    	}
    	
    	var deletavel = true;
    	if(docInt.getType() != DocumentType.FOLDER)
    	{    		
    		try 
    		{
    			// remover blob
    			docsQueueService.enviar(
					new DocIntDeletedMessage(docInt.getId()));
			} 
    		catch (Exception e) 
    		{
    			logger.error(e.getMessage());
    			deletavel = false;
			}
    	}
    	else
    	{
    		deletavel = delBlobDosFilhos(docInt, user) == 0;
    	}

    	if(deletavel)
    	{
	    	//TODO: remover versões de cada document
	    	//TODO: remover integrações que dependem do arquivo
    		documentRepo.delete(docInt, user, ZonedDateTime.now());
    	}
	}

	private int delBlobDosFilhos(
		final Document pai, 
		final User user) 
	{
		var todos = documentRepo.findAllByParent(pai, null);
		var filhos = documentRepo.findAllByParentAndUser(pai, user, null);
		
		var undeletables = todos.size() - filhos.size();
		
		for(var document : filhos)
		{
			var deletavel = true;
			if(document.getType() == DocumentType.FOLDER)
			{
				 var undeletablesDosFilhos = delBlobDosFilhos(document, user);
				 
				 undeletables += undeletablesDosFilhos;
				 
				 deletavel = undeletablesDosFilhos == 0;
			}
			
			if(deletavel)
			{
	    		try 
	    		{
					// remover blob
	    			docsQueueService.enviar(
						new DocIntDeletedMessage(document.getId()));
				} 
	    		catch (Exception e) 
	    		{
	    			logger.error(e.getMessage());
	    			deletavel = false;
	    			++undeletables;
				}
	    		
	    		if(deletavel)
	    		{
	    	    	//TODO: remover versões de cada document
					//TODO: remover integrações em Coleções que dependem do arquivo
	        		documentRepo.delete(document, user, ZonedDateTime.now());
	    		}
			}
		}
		
		return undeletables;
	}

	/**
	 * 
	 * @param caminhoStr
	 * @param user
	 * @param workspace
	 * @return
	 */
	public List<DocumentWithPath> findAllByCaminhoAndUserAndWorkspace(
		String caminhoStr, 
		final User user, 
		final Workspace workspace) 
	{
		var paging = PageRequest.of(0, 20, JpaSort.unsafe(Direction.ASC, "lower(d.name)"));

		caminhoStr = caminhoStr.trim().toLowerCase();
		
		if(!caminhoStr.startsWith("/")) 
		{
			caminhoStr = "/" + caminhoStr;
		}

		// termina com /? retornar qualquer arquivo ou diretório no caminho
		if(caminhoStr.endsWith("/"))
		{
			return documentoIntComCaminhoProjectionToDTO(documentWithPathRepo
				.findAllByPathAndUserAndWorkspace
					(caminhoStr.substring(0, caminhoStr.length()-1), user.getId(), workspace.getId(), paging));
		}

		// caminho inicial? retornar qualquer arquivo ou diretório na raiz
		var caminhoSplit = caminhoStr.split("/");
		if(caminhoSplit.length == 2)
		{
			var documentos = documentRepo
				.findAllByParentAndUserAndNameContainingIgnoreCase
					(workspace.getRootDoc(), user, caminhoSplit[1], paging);
			
					return documentos.stream()
				.map(d -> new DocumentWithPath(d, '/' + d.getName()))
					.collect(Collectors.toList());
		}

		// caminho informado: pesquisar por name no local
		var name = caminhoSplit[caminhoSplit.length-1];
		caminhoStr = String.join("/", Arrays.copyOf(caminhoSplit, caminhoSplit.length-1));
		
		return documentoIntComCaminhoProjectionToDTO(documentWithPathRepo
			.findAllByPathAndNameAndUserAndWorkspace
				(caminhoStr, name, user.getId(), workspace.getId(), paging));
	}

	private List<DocumentWithPath> documentoIntComCaminhoProjectionToDTO(
		final List<DocumentWithPathProjection> docs) 
	{
		if(docs == null)
		{
			return null;
		}

		var res = new ArrayList<DocumentWithPath>();
		for(var doc : docs)
		{
			var document = documentRepo.findById(doc.getId()).get();
			res.add(new DocumentWithPath(document, doc.getPath()));
		}

		return res;
	}
}