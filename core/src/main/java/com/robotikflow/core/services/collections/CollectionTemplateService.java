package com.robotikflow.core.services.collections;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.robotikflow.core.exception.CollectionTemplateException;
import com.robotikflow.core.models.entities.CollectionTemplate;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.filters.CollectionTemplateFilter;
import com.robotikflow.core.models.filters.WorkspaceFilter;
import com.robotikflow.core.models.repositories.CollectionTemplateRepository;
import com.robotikflow.core.models.request.AccessType;

@Service
@Lazy
public class CollectionTemplateService 
{
	@Autowired
	private CollectionTemplateRepository templateRepo;
	
	private ExampleMatcher matcher = ExampleMatcher.matching()
			.withIgnoreNullValues()
			.withIgnoreCase()
			.withMatcher("name", m -> m.startsWith());
		
	public List<CollectionTemplate> findAllByWorkspace(Workspace workspace, Pageable pageable) 
	{
		return templateRepo.findAllByWorkspace(workspace, pageable);
	}

	public List<CollectionTemplate> findAllByWorkspace(Workspace workspace, Pageable pageable, CollectionTemplateFilter filtros) 
	{
		filtros.setWorkspace(new WorkspaceFilter() {{ setId(workspace.getId()); }});
		var example = Example.of(new CollectionTemplate(filtros), matcher);
		return templateRepo.findAll(example, pageable).getContent();
	}

	public CollectionTemplate findByPubId(String pubId) 
	{
		return templateRepo.findByPubId(pubId);
	}
	
	public CollectionTemplate findByPubIdAndWorkspace(String id, Workspace workspace) 
	{
		return templateRepo.findByPubIdAndWorkspace(id, workspace);
	}

	public void validarAcesso(AccessType acesso, User user, Workspace workspace) 
	{
		//TODO: validar acesso
	}

	public void validarAcesso(AccessType acesso, CollectionTemplate template, User user, Workspace workspace) 
	{
		if(template.getWorkspace().getId() != workspace.getId())
		{
			throw new CollectionTemplateException("acesso negado");
		}
		
		//TODO: validar acesso
	}
}
