package com.robotikflow.core.models.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.robotikflow.core.models.entities.CollectionTemplate;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;

public interface CollectionTemplateRepository extends JpaRepository<CollectionTemplate, Long> 
{
	@Query("select" +
			"	t" +
			"	from CollectionTemplate t" +
			"	where t.pubId = :pubId" +
			"		and t.deletedBy is null")
	CollectionTemplate findByPubId(
		String pubId);

	@Query("select" +
			"	t" +
			"	from CollectionTemplate t" +
			"	where t.pubId = :pubId" +
			"		and (t.workspace = :workspace or t.workspace is null)" +
			"			and t.deletedBy is null")
	CollectionTemplate findByPubIdAndWorkspace(
		String pubId, 
		Workspace workspace);

	@Query("select" +
			"	t" +
			"	from CollectionTemplate t" +
			"	where (t.workspace = :workspace or t.workspace is null)" +
			"			and t.deletedBy is null" +
			"				and t.category.deletedBy is null")
	List<CollectionTemplate> findAllByWorkspace(
		Workspace workspace, 
		Pageable p);

	@Query( "update" +
			"	CollectionTemplate t" +
			"	set t.deletedBy = :user," +
			"		t.deletedAt = :currentDate" +
			"	where t = :template")
	@Modifying(clearAutomatically = true)
	@Transactional
	void delete(
		CollectionTemplate template, 
		User user, 
		ZonedDateTime currentDate);

	@Query( "update" +
			"	CollectionTemplate t" +
			"	set t.deletedBy = :user," +
			"		t.deletedAt = :currentDate" +
			"	where t.id = :idTemplate")
	@Modifying(clearAutomatically = true)
	@Transactional
	void deletedById(
		Long idTemplate, 
		User user, 
		ZonedDateTime currentDate);
}
