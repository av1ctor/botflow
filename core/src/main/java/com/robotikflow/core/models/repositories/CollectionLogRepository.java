package com.robotikflow.core.models.repositories;

import java.util.List;

import com.robotikflow.core.models.entities.CollectionLog;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.Workspace;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CollectionLogRepository 
    extends JpaRepository<CollectionLog, Long>
{
	@Query("select" +
			"	l" +
			"	from CollectionLog l" +
			"	where l.collection = :collection" +
			"	    and l.workspace = :workspace")
	List<CollectionLog> findAllByCollectionAndWorkspace(
        CollectionWithSchema collection, 
        Workspace workspace, 
        Pageable pageable);
}
