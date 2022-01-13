package com.robotikflow.core.models.repositories;

import java.util.List;

import com.robotikflow.core.models.entities.CollectionItemPost;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.WorkspacePostType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CollectionItemPostRepository 
    extends JpaRepository<CollectionItemPost, Long>
{
	@Query("select" +
			"	p" +
			"	from CollectionItemPost p" +
			"	where p.pubId = :id" +
			"			and p.workspace = :workspace")
	CollectionItemPost findByPubIdAndWorkspace(
		String id, 
		Workspace workspace);		

	@Query("select" +
			"	p" +
			"	from CollectionItemPost p" +
			"	where p.collection = :collection" +
			"		and p.itemId = :itemId" +
			"			and p.workspace = :workspace" +
			"				and p.type = :type")
	List<CollectionItemPost> findAllByItemIdAndType(
        CollectionWithSchema collection, 
        String itemId, 
		Workspace workspace,
		WorkspacePostType type,
		Pageable pageable);

	@Query("select" +
			"	p" +
			"	from CollectionItemPost p" +
			"	where p.collection = :collection" +
			"		and p.itemId = :itemId" +
			"			and p.workspace = :workspace" +
			"				and p.level <= :levels")
	List<CollectionItemPost> findAllByAndItemIdAndLevels(
        CollectionWithSchema collection, 
        String itemId, 
		Workspace workspace,
		short levels,
		Pageable pageable);

	@Query("select" +
			"	p" +
			"	from CollectionItemPost p" +
			"	where p.collection = :collection" +
			"		and p.itemId = :itemId" +
			"			and p.workspace = :workspace" +
			"				and p.parent.id = :postId")
	List<CollectionItemPost> findAllByPostId(
		CollectionWithSchema collection, 
		String itemId,
		Long postId, 
		Workspace workspace, 
		Pageable pageable);

	@Query("select" +
			"	p" +
			"	from CollectionItemPost p" +
			"	where p.collection = :collection" +
			"		and p.itemId = :itemId" +
			"			and p.workspace = :workspace" +
			"				and p.id in ("+
			"					select" +
			"						t.child.id" +
			"						from WorkspacePostTree t" +
			"						where t.parent.id = :postId" +
			"				)")
	List<CollectionItemPost> findTreeByPostId(
		CollectionWithSchema collection, 
		String itemId,
		Long postId, 
		Workspace workspace, 
		Pageable pageable);
}
