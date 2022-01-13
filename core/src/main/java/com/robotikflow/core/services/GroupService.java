package com.robotikflow.core.services;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.robotikflow.core.models.entities.Group;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.filters.GroupFilter;
import com.robotikflow.core.models.repositories.GroupRepository;
import com.robotikflow.core.models.request.GroupMultiRequest;
import com.robotikflow.core.models.request.GroupRequest;
import com.robotikflow.core.models.request.AccessType;
import com.robotikflow.core.services.log.WorkspaceLogger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Lazy
public class GroupService 
{
	@Autowired
	private GroupRepository groupRepo;
	@Autowired
	private WorkspaceLogger workspaceLogger;

	/**
	 * 
	 * @param pubId
	 * @param workspace
	 * @return
	 */
	public Group findByPubIdAndWorkspace(
		final String pubId, 
		final Workspace workspace) 
	{
		return groupRepo.findByPubIdAndWorkspace(
			pubId, workspace.getId())
			.orElseGet(() -> null);
	}	
	/**
	 * 
	 * @param workspace
	 * @param pageable
	 * @return
	 */
	public List<Group> findAllByWorkspace(
		final Workspace workspace, 
		final Pageable pageable) 
	{
		return groupRepo.findAllByWorkspace(workspace, pageable);
	}

	/**
	 * 
	 * @param workspace
	 * @param pageable
	 * @param filtros
	 * @return
	 */
	public List<Group> findAllByWorkspace(
		final Workspace workspace, 
		final Pageable pageable, 
		final GroupFilter filtros) 
	{
		if(filtros.getName() != null)
		{
			return groupRepo.findAllByNameContainingAndWorkspace(
				filtros.getName(), workspace, pageable);
		}

		if(filtros.getPubId() != null)
		{
			var group = groupRepo.findByPubIdAndWorkspace(
				filtros.getPubId(), workspace.getId()).get();
			return group != null? List.of(group): List.of();
		}

		return List.of();
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	public List<Group> findAllByParent(
		final Group parent) 
	{
		return groupRepo.findAllByParent(parent);
	}

	private void setCommonFields(
		final Group group,	
		final GroupRequest req, 
		final Workspace workspace) 
	{
		group.setName(req.getName());
		var parent = req.getParentId() != null?
			groupRepo.findByPubIdAndWorkspace(req.getParentId(), workspace.getId()).orElseThrow():
			null;
		group.setParent(parent);
	}

	/**
	 * 
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	public Group criar(
		GroupRequest req, 
		User user, 
		Workspace workspace) 
	{
		var group = new Group();
		group.setDeletable(true);
		group.setWorkspace(workspace);
		group.setCreatedAt(ZonedDateTime.now());
		group.setCreatedBy(user);
		setCommonFields(group, req, workspace);
		var res = groupRepo.save(group);

		workspaceLogger.info(
			workspace, 
			user, 
			"Criou group", 
			Map.of("id", group.getPubId()));

		return res;
	}

	/**
	 * 
	 * @param group
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	public Group atualizar(
		Group group,
		GroupRequest req, 
		User user, 
		Workspace workspace) 
	{
		group.setUpdatedAt(ZonedDateTime.now());
		group.setUpdatedBy(user);
		setCommonFields(group, req, workspace);

		workspaceLogger.info(
			workspace, 
			user, 
			"Atualizou group", 
			Map.of("id", group.getPubId()));

		return groupRepo.save(group);
	}

	/**
	 * 
	 * @param group
	 * @param user
	 * @param workspace
	 */
	public void remover(
		Group group,
		User user, 
		Workspace workspace) 
	{
		var id = new String(group.getPubId());

		groupRepo.delete(group, user, ZonedDateTime.now());

		workspaceLogger.info(
			workspace, 
			user, 
			"Removeu group", 
			Map.of("id", id));
	}

	public void validarAcesso(
		final AccessType type, 
		final User user, 
		final Workspace workspace) 
	{
		//TODO: implementar
	}

	public void validarAcesso(
		AccessType type, 
		Group group, 
		User user, 
		Workspace workspace) 
	{
		//TODO: implementar
	}

	/**
	 * 
	 */
	@Transactional
	public List<Group> multiOp(
		final List<GroupMultiRequest> reqs, 
		final User user, 
		final Workspace workspace) 
	{
		var res = new ArrayList<Group>();

		Group last = null;
		for(var req : reqs)
		{
			Group group = null;
			
			switch(req.getOp())
			{
				case CREATE:
					var greq = req.getGroup();
					if(greq.getParentId() == null)
					{
						greq.setParentId(last != null? last.getPubId(): null);
					}
					group = criar(greq, user, workspace);
					last = group;
					break;

				case DELETE:
					group = findByPubIdAndWorkspace(req.getId(), workspace);
					if(group == null)
					{
						throw new RuntimeException(String.format("Group inexistente: %s", req.getId()));
					}
					
					remover(group, user, workspace);
					group = null;
					break;

				case UPDATE:
					group = findByPubIdAndWorkspace(req.getId(), workspace);
					if(group == null)
					{
						throw new RuntimeException(String.format("Group inexistente: %s", req.getId()));
					}

					group = atualizar(group, req.getGroup(), user, workspace);
					break;
			}

			if(group != null)
			{
				res.add(group);
			}
		}

		return res;
	}
}
