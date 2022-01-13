package com.robotikflow.core.services.apps;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.robotikflow.core.exception.WorkspaceException;
import com.robotikflow.core.models.entities.App;
import com.robotikflow.core.models.entities.AppAuth;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.repositories.AppAuthRepository;
import com.robotikflow.core.models.repositories.GroupRepository;
import com.robotikflow.core.models.repositories.UserRepository;
import com.robotikflow.core.models.request.PageAuthRequest;

@Service
@Lazy
public class PageAuthService 
{
	@Autowired
	private AppAuthRepository appAuthRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private GroupRepository groupRepo;
	
	/**
	 * 
	 * @param pubId
	 * @return
	 */
	public List<AppAuth> findAllByPage(
		final App app, 
		final Pageable appable) 
	{
		return appAuthRepo.findAllByPage(app, appable);
	}

	/**
	 * 
	 * @param pubId
	 * @return
	 */
	public AppAuth findByPubIdAndPage(
		final String pubId, 
		final App app) 
	{
		return appAuthRepo.findByPubIdAndPage(pubId, app);
	}

	/**
	 * 
	 * @param app
	 * @param user
	 * @return
	 */
	public List<AppAuth> findAllByPageAndUser(
		final App app,
		final User user) 
	{
		return appAuthRepo.findAllByPageAndUserAndWorkspace(
			app, user, app.getWorkspace());
	}

	/**
	 * 
	 * @param app
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	public AppAuth criar(
		final App app, 
		final PageAuthRequest req, 
		final User user_, 
		final Workspace workspace)
	{
		var auth = new AppAuth();
		
		var user = req.getUser() != null? 
			userRepo.findByPubIdAndIdWorkspace(req.getUser().getId(), workspace.getId()): 
			null;
		var group = req.getGroup() != null? 
			groupRepo.findByPubIdAndWorkspace(req.getGroup().getId(), workspace.getId()).get(): 
			null;

		if(user != null)
		{
			if(group != null)
			{
				throw new WorkspaceException("User and group can't be selected at same time");
			}
		}
		else if(group == null)
		{
			throw new WorkspaceException("No user or group selected");
		}
	
		auth.setApp(app);
		auth.setUser(user);
		auth.setGroup(group);
        auth.setCreatedAt(ZonedDateTime.now());
		auth.setCreatedBy(user);
        setCommonFields(auth, req, workspace);

		return appAuthRepo.save(auth);
		
	}

	/**
	 * 
	 * @param auth
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	public AppAuth atualizar(
		final AppAuth auth, 
		final PageAuthRequest req, 
		final User user, 
		final Workspace workspace)
	{
        auth.setUpdatedAt(ZonedDateTime.now());
		auth.setUpdatedBy(user);
        setCommonFields(auth, req, workspace);

		return appAuthRepo.save(auth);
	}

	private void setCommonFields(
		final AppAuth auth, 
		final PageAuthRequest req, 
		final Workspace workspace) 
	{
		auth.setReverse(req.isReverse());
	}
}
