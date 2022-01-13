package com.robotikflow.core.models.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.CollectionItemDocument;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;

public interface CollectionItemDocumentRepository 
	extends JpaRepository<CollectionItemDocument, Long> 
{
	@Query("select" +
			"	c" +
			"	from CollectionItemDocument c" +
			"	where c.collection = :collection" +
			"		and c.itemId = :itemId" +
			"			and c.workspace = :workspace" +
			"				and c.deletedBy is null")
	List<CollectionItemDocument> findAllByCollectionAndItemIdAndWorkspace(
		CollectionWithSchema collection, 
		String itemId, 
		Workspace workspace, 
		Pageable p);

	@Query( "update" +
			"	CollectionItemDocument c" +
			"	set c.deletedBy = :user," +
			"		c.deletedAt = :currentDate" +
			"	where c = :collectionItemDoc")
	@Modifying(clearAutomatically = true)
	@Transactional
	void delete(
		CollectionItemDocument collectionItemDoc, 
		User user, 
		ZonedDateTime currentDate);

	@Query( "update" +
			"	CollectionItemDocument c" +
			"	set c.deletedBy = :user," +
			"		c.deletedAt = :currentDate" +
			"	where c.id = :id")
	@Modifying(clearAutomatically = true)
	@Transactional
	void deletedById(
		Long id, 
		User user, 
		ZonedDateTime currentDate);
}
