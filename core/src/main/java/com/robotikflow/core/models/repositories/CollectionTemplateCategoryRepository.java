package com.robotikflow.core.models.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.robotikflow.core.models.entities.CollectionTemplateCategory;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;

public interface CollectionTemplateCategoryRepository 
	extends JpaRepository<CollectionTemplateCategory, Long> 
{
	@Query("select" +
			"	c" +
			"	from CollectionTemplateCategory c" +
			"	where c.pubId = :pubId" +
			"		and c.deletedBy is null")
	CollectionTemplateCategory findByPubId(
		String pubId);

	@Query("select" +
			"	c" +
			"	from CollectionTemplateCategory c" +
			"	where c.pubId = :pubId" +
			"		and (c.workspace = :workspace or c.workspace is null)" +
			"			and c.deletedBy is null")
	CollectionTemplateCategory findByPubIdAndWorkspace(
		String pubId, 
		Workspace workspace);

	@Query("select" +
			"	c" +
			"	from CollectionTemplateCategory c" +
			"	where (c.workspace = :workspace or c.workspace is null)" +
			"			and c.deletedBy is null")
	List<CollectionTemplateCategory> findAllByWorkspace(
		Workspace workspace, 
		Pageable p);

	@Query( "update" +
			"	CollectionTemplateCategory c" +
			"	set c.deletedBy = :user," +
			"		c.deletedAt = :currentDate" +
			"	where c = :category")
	@Modifying(clearAutomatically = true)
	@Transactional
	void delete(
		CollectionTemplateCategory category, 
		User user, 
		ZonedDateTime currentDate);

	@Query( "update" +
			"	CollectionTemplateCategory c" +
			"	set c.deletedBy = :user," +
			"		c.deletedAt = :currentDate" +
			"	where c.id = :idCategory")
	@Modifying(clearAutomatically = true)
	@Transactional
	void deletedById(
		Long idCategory, 
		User user, 
		ZonedDateTime currentDate);
}
