package com.robotikflow.core.services;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.robotikflow.core.exception.WorkspaceException;
import com.robotikflow.core.interfaces.IStorageProviderService;
import com.robotikflow.core.models.entities.Collection;
import com.robotikflow.core.models.entities.CollectionType;
import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.DocumentType;
import com.robotikflow.core.models.entities.Group;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.DocumentAuthType;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.WorkspaceLog;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.filters.WorkspaceFilter;
import com.robotikflow.core.models.filters.WorkspaceLogFilter;
import com.robotikflow.core.models.repositories.CollectionRepository;
import com.robotikflow.core.models.repositories.DocumentRepository;
import com.robotikflow.core.models.repositories.GroupRepository;
import com.robotikflow.core.models.repositories.ProviderRepository;
import com.robotikflow.core.models.repositories.ProviderSchemaRepository;
import com.robotikflow.core.models.repositories.WorkspaceLogRepository;
import com.robotikflow.core.models.repositories.WorkspaceRepository;
import com.robotikflow.core.models.request.AccessType;
import com.robotikflow.core.services.collections.CollectionService;
import com.robotikflow.core.util.ProviderUtil;
import com.google.common.collect.ImmutableMap;

@Service
@Lazy
public class WorkspaceService 
{
	@Autowired
	private WorkspaceRepository workspaceRepo;
	@Autowired
	private WorkspaceLogRepository workspaceLogRepo;
	@Autowired
	private GroupRepository groupRepo;
	@Autowired
	private DocumentRepository documentRepo;
	@Autowired
	private IStorageProviderService internalStorageProvider;
	@Autowired
	private CollectionService collectionService;
	@Autowired
	private CollectionRepository collectionRepo;
	@Autowired
	private ProviderRepository providerRepo;
	@Autowired
	private ProviderSchemaRepository providerSchemaRepo;

    // lista de groups que serão criados por padrão em cada novo área de trabalho
	private final static Map<String, String> defaultGroups = ImmutableMap.of(
		"Administrators", "",
		"Users", "Administrators",
		"Partners", "Users"
	);
	
	private ExampleMatcher logMatcher = ExampleMatcher.matching()
		.withIgnoreNullValues()
		.withIgnoreCase();

	/**
	 * 
	 * @param name
	 * @return
	 */
	public Workspace create(
		final String name) 
	{
		if(workspaceRepo.findByNameIgnoreCase(name) != null)
		{
			throw new WorkspaceException(String.format(
				"A área de trabalho (%s) já existe", name));
		}
		
		var workspace = workspaceRepo.save(
			new Workspace(
				name, 
				ZonedDateTime.now()));
		
		var pubId = workspace.getPubId();

		try
		{
			adicionarGroupsPadrao(workspace);
		}
		catch(Exception e) 
		{
			throw new WorkspaceException(String.format(
				"Criação da área de trabalho (%s) falhou: %s", name, e.getMessage()));
		}

		try 
		{
			internalStorageProvider.createWorkspace(pubId, false);
			internalStorageProvider.createWorkspace(pubId + "-pub", true);

			try 
			{
				//indexService.criar(pubId);
				
				try
				{
					collectionService.iniciar(pubId);
				}
				catch(Exception e)
				{
					//indexService.destruir(pubId);
					throw new WorkspaceException(String.format(
						"Criação da área de trabalho (%s) falhou: %s", name, e.getMessage()));
				}
			} 
			catch(WorkspaceException e)
			{
				internalStorageProvider.destroyWorkspace(pubId + "-pub");
				internalStorageProvider.destroyWorkspace(pubId);
				throw e;
			}
			catch(Exception e) 
			{
				internalStorageProvider.destroyWorkspace(pubId + "-pub");
				internalStorageProvider.destroyWorkspace(pubId);
				throw new WorkspaceException(String.format(
					"Criação da área de trabalho (%s) falhou: %s", name, e.getMessage()));
			}
		} 
		catch(WorkspaceException e)
		{
			workspaceRepo.delete(workspace);
			throw e;
		}
		catch(Exception e) 
		{
			workspaceRepo.delete(workspace);
			throw new WorkspaceException(String.format(
				"Criação da área de trabalho (%s) falhou: %s", name, e.getMessage()));
		}
		
		return workspace;
	}
	
	/**
	 * Cria os groups padrão dentro do área de trabalho
	 * @param workspace
	 */
	public void adicionarGroupsPadrao(
		final Workspace workspace)
	{
		defaultGroups.forEach(
			(name, superior) -> 
			{
				var parent = superior.length() != 0? 
					groupRepo.findByNameAndWorkspace(superior, workspace.getId()): 
					null;
				groupRepo.save(new Group(name, workspace, parent, false));
			}
		);
	}

	/**
	 * 
	 * @param workspace
	 * @param user
	 */
	public Document createRootDoc(
		final Workspace workspace, 
		final User user) 
	{
		var rootDoc = new Document(
			workspace.getProvider(),
			"", 
			DocumentType.FOLDER, 
			0L, 
			null, 
			user, 
			user.getGroups(workspace.getId()).get(0), 
			DocumentAuthType.MODIFY,
			DocumentAuthType.READ,
			DocumentAuthType.NONE,
			workspace);
		
		return documentRepo.save(rootDoc);
	}

	public Collection createRootCollection(
		final Workspace workspace,
		final User user)
	{
		return collectionRepo.save(
			new Collection(CollectionType.FOLDER, "", null, user, workspace));
	}

	/**
	 * 
	 * @param workspace
	 * @return
	 */
	public Workspace save(Workspace workspace)
	{
		return workspaceRepo.save(workspace);
	}

	/**
	 * 
	 * @param nomeWorkspace
	 * @return
	 */
	public Workspace findByNome(
		final String nomeWorkspace) 
	{
		var workspace = workspaceRepo
			.findByNameIgnoreCase(nomeWorkspace);
		if(workspace == null)
		{
			throw new WorkspaceException("Área de trabalho inexistente");
		}
		
		return workspace;
	}

	/**
	 * 
	 * @param pubId
	 * @return
	 */
	public Workspace findByPubId(
		final String pubId) 
	{
		return workspaceRepo
			.findByPubId(pubId)
				.orElseThrow(() -> new WorkspaceException("Área de trabalho inexistente"));
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public Workspace findById(
		final Long id) 
	{
		return workspaceRepo
			.findById(id)
				.orElseThrow(() -> new WorkspaceException("Área de trabalho inexistente"));
	}

	/**
	 * 
	 * @param user
	 * @param pageable
	 * @return
	 */
	public List<Workspace> findAllByUser(
		final User user, 
		final Pageable pageable) 
	{
		return workspaceRepo.findAllByUser(user, pageable);
	}


	public List<Workspace> findAllByUser(
		final User user, 
		final Pageable pageable, 
		final WorkspaceFilter filtros) 
	{
		if(filtros.getId() != null)
		{
			var workspace = workspaceRepo.findById(filtros.getId());
			return workspace.isPresent()?
				List.of(workspace.get()):
				List.of();
		}

		if(filtros.getName() != null)
		{
			return workspaceRepo.findByNameContaining(filtros.getName(), pageable);
		}
		
		return List.of();
	}

	/**
	 * 
	 * @param workspace
	 * @param user
	 * @return
	 */
	public Provider createDefaultStorageProvider(
		final Workspace workspace, 
		final User user) 
	{
		var provider = ProviderUtil
			.createDefault(user, workspace, providerSchemaRepo);

		return providerRepo.save(provider);
	}

	public Provider changeDefaultStorageProviderRoot(
		final Provider provider, 
		final Document rootDoc) 
	{
		provider.getFields().put("root", rootDoc.getPubId());
		return providerRepo.save(provider);
	}

	/**
	 * 
	 */
	public List<WorkspaceLog> findAllLogs(
		final Workspace workspace, 
		final Pageable pageable) 
	{
		return workspaceLogRepo
			.findAllByWorkspace(workspace, pageable);
	}

	/**
	 * 
	 * @param workspace
	 * @param pageable
	 * @param filters
	 * @return
	 */
	public List<WorkspaceLog> findAllLogs(
		final Workspace workspace, 
		final Pageable pageable,
		final WorkspaceLogFilter filters) 
	{
		filters.workspace = new WorkspaceFilter(workspace.getId());
		var example = Example.of(new WorkspaceLog(filters), logMatcher);
		return workspaceLogRepo
			.findAll(example, pageable).getContent();
	}

	/**
	 * 
	 * @param type
	 * @param workspace
	 * @param user
	 */
	public void validarAcesso(
		final AccessType type, 
		final Workspace workspace,
		final User user) 
	{
		if(!user.isSuperAdmin())
		{
			if(!user.isAdmin(workspace.getId()))
			{
				var role = user.getRole(workspace.getId());
				if(role == null)
				{
					throw new WorkspaceException("Usuário não autorizado");
				}

				switch(type)
				{
					case DELETE:
					case UPDATE:
					case CREATE:
						throw new WorkspaceException("Usuário não autorizado");
					case READ:
						break;
					default:
						break;
				}
			}
		}
	}
}
