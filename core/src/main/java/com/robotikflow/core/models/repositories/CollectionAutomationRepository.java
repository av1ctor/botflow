package com.robotikflow.core.models.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.CollectionAutomation;
import com.robotikflow.core.models.entities.CollectionAutomationType;

public interface CollectionAutomationRepository extends JpaRepository<CollectionAutomation, Long> 
{
	@Query("select" +
			"	i.id" +
			"	from CollectionAutomation i" +
			"	where" +
			"		i.type = :type" +
			"	order by i.priority desc")
	List<Long> findAllIdsByType(
		CollectionAutomationType type);
	
	@Query("select" +
			"	i" +
			"	from CollectionAutomation i"+
			"	where" +
			"		i.collection = :collection" +
			"	order by i.priority desc")
	List<CollectionAutomation> findAllByCollection(
		CollectionWithSchema collection, 
		Pageable p);

	@Query("select" +
			"	i" +
			"	from CollectionAutomation i"+
			"	where" +
			"		i.collection = :collection" +
			"			and i.type = :type" +
			"	order by i.priority desc")
	List<CollectionAutomation> findAllByCollectionAndTypo(
		CollectionWithSchema collection, 
		CollectionAutomationType type, 
		Pageable p);

	@Query("select" +
			"	i" +
			"	from CollectionAutomation i"+
			"	where" +
			"		i.collection = :collection" +
			"			and i.type in :types" +
			"	order by i.priority desc")
	List<CollectionAutomation> findAllByCollectionAndTypeIn(
		CollectionWithSchema collection, 
		List<CollectionAutomationType> types, 
		Pageable pageable);

	Optional<CollectionAutomation> findById(Long id);

	@Query("select" +
			"	i" +
			"	from CollectionAutomation i"+
			"	where" +
			"		i.collection = :collection" +
			"			and i.pubId = :pubId")
	CollectionAutomation findByPubIdAndCollection(
		String pubId, 
		CollectionWithSchema collection);
}
