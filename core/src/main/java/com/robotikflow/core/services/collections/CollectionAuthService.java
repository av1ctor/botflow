package com.robotikflow.core.services.collections;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.robotikflow.core.exception.CollectionException;
import com.robotikflow.core.models.entities.Collection;
import com.robotikflow.core.models.entities.CollectionAuth;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.repositories.CollectionAuthRepository;
import com.robotikflow.core.models.repositories.GroupRepository;
import com.robotikflow.core.models.repositories.UserRepository;
import com.robotikflow.core.models.request.CollectionAuthRequest;

@Service
@Lazy
public class CollectionAuthService 
{
	@Autowired
	private CollectionAuthRepository collectionAuthRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private GroupRepository groupRepo;
	
	/**
	 * 
	 * @param pubId
	 * @return
	 */
	public List<CollectionAuth> findAllByCollection(
		final Collection collection, 
		final Pageable pageable) 
	{
		return collectionAuthRepo.findAllByCollection(collection, pageable);
	}

	/**
	 * 
	 * @param pubId
	 * @return
	 */
	public CollectionAuth findByPubIdAndCollection(
		final String pubId, 
		final Collection collection) 
	{
		return collectionAuthRepo.findByPubIdAndCollection(pubId, collection);
	}

	/**
	 * 
	 * @param collection
	 * @param user
	 * @return
	 */
	public List<CollectionAuth> findAllByCollectionAndUser(
		final Collection collection,
		final User user) 
	{
		return collectionAuthRepo.findAllByCollectionAndUserAndWorkspace(
			collection, user, collection.getWorkspace());
	}

	/**
	 * 
	 * @param collection
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	public CollectionAuth criar(
		final Collection collection, 
		final CollectionAuthRequest req, 
		final User user_, 
		final Workspace workspace)
	{
		var auth = new CollectionAuth();
		
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
				throw new CollectionException("Somente usuário ou group deve ser definido, nunca os dois");
			}
		}
		else if(group == null)
		{
			throw new CollectionException("Usuário ou group deve ser definido");
		}
	
		auth.setCollection(collection);
		auth.setUser(user);
		auth.setGroup(group);
        auth.setCreatedAt(ZonedDateTime.now());
		auth.setCreatedBy(user);
        setCommonFields(auth, req, workspace);

		return collectionAuthRepo.save(auth);
		
	}

	/**
	 * 
	 * @param auth
	 * @param req
	 * @param user
	 * @param workspace
	 * @return
	 */
	public CollectionAuth atualizar(
		final CollectionAuth auth, 
		final CollectionAuthRequest req, 
		final User user, 
		final Workspace workspace)
	{
        auth.setUpdatedAt(ZonedDateTime.now());
		auth.setUpdatedBy(user);
        setCommonFields(auth, req, workspace);

		return collectionAuthRepo.save(auth);
	}

	private void setCommonFields(
		final CollectionAuth auth, 
		final CollectionAuthRequest req, 
		final Workspace workspace) 
	{
		auth.setRole(req.getRole());
		auth.setReverse(req.isReverse());
	}
}
