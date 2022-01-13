package com.robotikflow.core.models.repositories;

import java.util.List;

import com.robotikflow.core.models.entities.CollectionPost;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.WorkspacePostType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CollectionPostRepository 
    extends JpaRepository<CollectionPost, Long>
{
	@Query("select" +
			"	p" +
			"	from CollectionPost p" +
			"	where p.collection = :collection" +
			"	    and p.workspace = :workspace" +
			"	    	and p.type = :type")
	List<CollectionPost> findAllByCollectionAndWorkspaceAndType(
        CollectionWithSchema collection, 
		Workspace workspace, 
		WorkspacePostType type,
        Pageable pageable);
}
