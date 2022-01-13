package com.robotikflow.core.models.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.robotikflow.core.models.entities.Collection;
import com.robotikflow.core.models.entities.CollectionAuxIdOrderProj;
import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.CollectionVersion;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;

public interface CollectionRepository 
	extends JpaRepository<Collection, Long> 
{
	@Query("select" +
			"	c" +
			"	from Collection c" +
			"	where c.pubId = :pubId" +
			"		and c.deletedBy is null")
	Collection findByPubId(
		String pubId);

	@Query("select" +
			"	c" +
			"	from Collection c" +
			"	where c.pubId = :pubId" +
			"		and c.workspace.id = :workspace" +
			"			and c.deletedBy is null")
	Collection findByPubIdAndWorkspace(
		String pubId, 
		Long workspace);

	@Query("select" +
			"	c" +
			"	from Collection c" +
			"	where lower(c.name) = lower(:name)" +
			"		and c.parent.id = :idParent" +
			"			and c.workspace.id = :workspace" +
			"				and c.deletedBy is null")
	Collection findByNameAndWorkspace(
		String name, 
		Long workspace, 
		Long idParent);

	@Query("select" +
			"	c" +
			"	from Collection c" +
			"	where c.pubId = :pubId" +
			"		and c.workspace = :workspace" +
			"			and c.deletedBy is null" +
			"				and (" +
			"					exists (" + 
			"						select cup.user.id" +
			"							from CollectionAuth cup" +
			"							where cup.collection = c" +
			"								and cup.user is not null" +
			"									and cup.user = :user" +
			"										and cup.reverse = false" +
			"					)" +
			"					or (" +
			"						exists (" +
			"							select cup.group.id" +
			"								from CollectionAuth cup" +
			"								left join CollectionAuth cuprev" +
			"									on cuprev.collection = c" +
			"										and cuprev.group is not null" +
			"											and cuprev.group = cup.group" +
			"												and cuprev.reverse = true" +		
			"								where cup.collection = c" +
			"									and cup.group is not null" +
			"										and cup.group.id in (" +
			"					 						select ga.child.id " + 
			"					 							from GroupTree ga" + 
			"				 								inner join UserGroupWorkspace urg" + 
			"			 										on urg.group = ga.parent" + 
			"			 											and urg.workspace = :workspace" + 
			"			 												and urg.user = :user" +
			" 										)" +
			"											and cup.reverse = false" +
			"												and cuprev.collection is null" +
			"						) and not exists (" +
			"							select cup.user.id" +
			"								from CollectionAuth cup" +
			"								where cup.collection = c" +
			"									and cup.user is not null" +
			"										and cup.user = :user" +
			"											and cup.reverse = true" +
			"						)" +
			"					)" +
			"				)"			
	)
	Collection findByPubIdAndUserAndWorkspace(
		String pubId, 
		User user, 
		Workspace workspace);

	@Query("select" +
			"	c" +
			"	from Collection c" +
			"	where c.workspace = :workspace" +
			"			and c.deletedBy is null")
	List<Collection> findAllByWorkspace(
		Workspace workspace, 
		Pageable p);

	@Query("select" +
			"	c" +
			"	from Collection c" +
			"	where c.workspace = :workspace" +
			"			and c.parent.pubId = :idParent" +
			"				and c.deletedBy is null")
	List<Collection> findAllByWorkspace(
		Workspace workspace, 
		String idParent, 
		Pageable p);

	@Query("select" +
			"	c" +
			"	from Collection c" +
			"	where c.workspace = :workspace" +
			"			and c.parent.pubId = :idParent" +
			"				and c.deletedBy is null" +
			"					and c.publishedAt is not null")
	List<Collection> findAllPublishedByWorkspace(
		Workspace workspace, 
		String idParent, 
		Pageable pageable);

	@Query("select" +
		"	c" +
		"	from Collection c" +
		"	where c.workspace = :workspace" +
		"		and c.parent.pubId = :idParent" +
		"			and c.deletedBy is null" +
		"				and c.publishedAt is not null" +
		"					and (" +
		"						exists (" + 
		"							select cup.user.id" +
		"								from CollectionAuth cup" +
		"								where cup.collection = c" +
		"									and cup.user is not null" +
		"										and cup.user = :user" +
		"											and cup.reverse = false" +
		"						)" +
		"						or (" +
		"							exists (" +
		"								select cup.group.id" +
		"									from CollectionAuth cup" +
		"									left join CollectionAuth cuprev" +
		"										on cuprev.collection = c" +
		"											and cuprev.group is not null" +
		"												and cuprev.group = cup.group" +
		"													and cuprev.reverse = true" +		
		"									where cup.collection = c" +
		"										and cup.group is not null" +
		"											and cup.group.id in (" +
		"						 						select ga.child.id " + 
		"					 								from GroupTree ga" + 
		"				 									inner join UserGroupWorkspace urg" + 
		"			 											on urg.group = ga.parent" + 
		"			 												and urg.workspace = :workspace" + 
		"			 													and urg.user = :user" +
		" 											)" +
		"												and cup.reverse = false" +
		"													and cuprev.collection is null" +
		"							) and not exists (" +
		"								select cup.user.id" +
		"									from CollectionAuth cup" +
		"									where cup.collection = c" +
		"										and cup.user is not null" +
		"											and cup.user = :user" +
		"												and cup.reverse = true" +
		"							)" +
		"						)" +
		"					)"
	)
	List<Collection> findAllPublishedByUserAndWorkspace(
		User user, 
		Workspace workspace, 
		String idParent, 
		Pageable pageable);

	@Query("select" +
			"	c" +
			"	from Collection c" +
			"	inner join CollectionAux a" +
			"		on a.principal.id = :principalId" +
			"	where c.workspace = :workspace" +
			"		and c.deletedBy is null" +
			"			and c.id = a.aux.id")
	List<Collection> findAllAuxsByCollection(
		Long principalId, 
		Workspace workspace, 
		Pageable pageable);

	@Query("select" +
			"	a.aux.id as id," +
			"	a.order as order" +
			"	from Collection c" +
			"	inner join CollectionAux a" +
			"		on a.principal.id = :principalId" +
			"	where c.deletedBy is null" +
			"		and c.id = a.aux.id" +
			"	order by a.order asc")
	List<CollectionAuxIdOrderProj> findAllAuxsOrdersByCollection(
		Long principalId);

	@Query("select" +
		"	c" +
		"	from Collection c" +
		"	inner join CollectionAux a" +
		"		on a.principal.id = :principalId" +
		"	where c.workspace = :workspace" +
		"		and c.deletedBy is null" +
		"			and c.id = a.aux.id" +
		"				and (" +
		"						exists (" + 
		"							select cup.user.id" +
		"								from CollectionAuth cup" +
		"								where cup.collection = c" +
		"									and cup.user is not null" +
		"										and cup.user = :user" +
		"											and cup.reverse = false" +
		"						)" +
		"						or (" +
		"							exists (" +
		"								select cup.group.id" +
		"									from CollectionAuth cup" +
		"									left join CollectionAuth cuprev" +
		"										on cuprev.collection = c" +
		"											and cuprev.group is not null" +
		"												and cuprev.group = cup.group" +
		"													and cuprev.reverse = true" +		
		"									where cup.collection = c" +
		"										and cup.group is not null" +
		"											and cup.group.id in (" +
		"						 						select ga.child.id " + 
		"					 								from GroupTree ga" + 
		"				 									inner join UserGroupWorkspace urg" + 
		"			 											on urg.group = ga.parent" + 
		"			 												and urg.workspace = :workspace" + 
		"			 													and urg.user = :user" +
		" 											)" +
		"												and cup.reverse = false" +
		"													and cuprev.collection is null" +
		"							) and not exists (" +
		"								select cup.user.id" +
		"									from CollectionAuth cup" +
		"									where cup.collection = c" +
		"										and cup.user is not null" +
		"											and cup.user = :user" +
		"												and cup.reverse = true" +
		"							)" +
		"						)" +
		"				)"
	)
	List<Collection> findAllAuxsByCollectionAndUser(
		Long principalId, 
		User user, 
		Workspace workspace, 
		Pageable pageable);

	@Query( "update" +
			" 	Collection c" +
			" 	set c.order = coalesce(c.order, 0) + :qtd" +
			"	where c.workspace = :workspace" +
			"		and coalesce(c.order, 0) between :first and :last" +
			"			and c.publishedAt is not null")
	@Modifying(clearAutomatically = true)
	@Transactional
	void updatePosicao(
		short first, 
		short last, 
		short qtd, 
		Workspace workspace);

	@Query(value = 
			"insert into" +
			"	collections_auxs" +
			"		(principal_id, aux_id, \"order\")" +
			"	values" +
			" 		(:principalId, :auxId, (select coalesce(max(\"order\")+1,0) from collections_auxs where principal_id = :principalId))",
			nativeQuery = true)
	@Modifying
	@Transactional
	void insertAux(
		Long principalId, 
		Long auxId);

	@Query(value = 
			"insert into" +
			"	collections_auxs" +
			"		(principal_id, aux_id, \"order\")" +
			"	values" +
			" 		(:principalId, :auxId, :order)",
			nativeQuery = true)
	@Modifying
	@Transactional
	void insertAuxAt(
		Long principalId, 
		Long auxId, 
		int order);

	@Modifying
	@Transactional
	@Query( "update" +
			"	CollectionAux a" +
			"	set a.order = a.order + :inc" +
			"	where a.principal.id = :principalId" +
			"		and a.order >= :from")
	void updateAuxsOrder(
		Long principalId, 
		int from, 
		int inc);

	@Query( "update" +
			" 	CollectionAux a" +
			" 	set a.order = coalesce(a.order, 0) + :qtd" +
			"	where a.aux.id = :auxId" +
			"		and a.principal.id = :principalId" +
			"			and coalesce(a.order, 0) between :first and :last")
	@Modifying
	@Transactional
	void updateAuxsOrder(
		Long principalId, 
		Long auxId, 
		int first, 
		int last, 
		int qtd);

	@Query( "update" +
			" 	CollectionAux a" +
			" 	set a.order = :order" +
			"	where a.aux.id = :auxId" +
			"		and a.principal.id = :principalId")
	@Modifying
	@Transactional
	void updateAuxOrder(
		Long principalId, 
		Long auxId, 
		int order);

	@Query("select ver" +
		"		from CollectionVersion ver" +
		"		where ver.collection = :collection")
	List<CollectionVersion> findAllVersions(
		CollectionWithSchema collection, 
		Pageable pageable);

	@Query("select ver" +
		"		from CollectionVersion ver" +
		"		where ver.collection = :collection and" +
		"			ver.createdAt > :data")
	List<CollectionVersion> findAllVersionsAfter(
		CollectionWithSchema collection, 
		ZonedDateTime data);

	@Query("select ver" +
		"		from CollectionVersion ver" +
		"		where ver.collection = :collection and" +
		"			ver.pubId = :pubId" +
		"		order by ver.createdAt desc")
	CollectionVersion findVersionById(
		CollectionWithSchema collection, 
		String pubId);

	@Query( "update" +
			"	Collection c" +
			"	set c.deletedBy = :user," +
			"		c.deletedAt = :currentDate," +
			"		c.name = concat('deleted|', :currentDate, '|', c.name)" +
			"	where c = :collection")
	@Modifying(clearAutomatically = true)
	@Transactional
	void delete(
		Collection collection, 
		User user, 
		ZonedDateTime currentDate);

	@Query( "update" +
			"	Collection c" +
			"	set c.deletedBy = :user," +
			"		c.deletedAt = :currentDate," +
			"		c.name = concat('deleted|', :currentDate, '|', c.name)" +
			"	where c.id = :idCollection")
	@Modifying(clearAutomatically = true)
	@Transactional
	void deletedById(
		Long idCollection, 
		User user, 
		ZonedDateTime currentDate);
}
