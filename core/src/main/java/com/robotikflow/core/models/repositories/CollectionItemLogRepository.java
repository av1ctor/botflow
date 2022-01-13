package com.robotikflow.core.models.repositories;

import java.util.List;

import com.robotikflow.core.models.entities.CollectionItemLog;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.Workspace;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CollectionItemLogRepository 
    extends JpaRepository<CollectionItemLog, Long>
{
	@Query("select" +
			"	l" +
			"	from CollectionItemLog l" +
			"	where l.collection = :collection" +
			"		and l.itemId = :itemId" +
			"			and l.workspace = :workspace")
	List<CollectionItemLog> findAllByCollectionAndItemIdAndWorkspace(
        CollectionWithSchema collection, 
        String itemId, 
        Workspace workspace,
		Pageable pageable);
}
