package com.robotikflow.core.models.repositories;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.CollectionWithSchema;
import com.robotikflow.core.models.entities.CollectionIntegration;

public interface CollectionIntegrationRepository 
	extends JpaRepository<CollectionIntegration, Long> 
{
	@Query("select" +
			"	i.id" +
			"	from CollectionIntegration i" +
			"	where" +
			"		i.started = TRUE and" +
			"			i.active = TRUE and" +
			"				i.freq is null" +			
			"	order by i.priority desc")
	List<Long> findAllIds();
	
	@Query("select" +
			"	i.id" +
			"	from CollectionIntegration i" +
			"	where" +
			"		i.started = TRUE and" +
			"			i.active = TRUE and" +
			"				i.freq = :freq" +			
			"	order by i.priority desc")
	List<Long> findAllIdsByFreq(
		int freq);

		@Query("select" +
			"	i.id as id" +
			"	from CollectionIntegration i" +
			"	where" +
			"		i.started = TRUE and" +
			"			i.active = TRUE and" +
			"				i.minOfDay = :minOfDay" +
			"	order by i.priority desc")
	List<Long> findAllByMinOfDay(
		int minOfDay);

	@Query("select" +
			"	i" +
			"	from CollectionIntegration i"+
			"	where" +
			"		i.started = FALSE and" +
			"			i.active = TRUE and" +
			"				i.start >= :now" +
			"	order by i.priority desc")
	List<CollectionIntegration> findAllToInitiate(
		ZonedDateTime now);

	@Query("select" +
			"	i.id as id" +
			"	from CollectionIntegration i" +
			"	where" +
			"		i.started = TRUE and" +
			"			i.active = TRUE and" +
			"				i.rerunAt = :atDate" +
			"	order by i.priority desc")
	List<Long> findAllToRerun(
		ZonedDateTime atDate);

	@Query("select" +
			"	i" +
			"	from CollectionIntegration i"+
			"	where" +
			"		i.collection = :collection" +
			"	order by i.priority desc")
	List<CollectionIntegration> findAllByCollection(
		CollectionWithSchema collection, 
		Pageable p);

	Optional<CollectionIntegration> findById(
		Long id);

	@Query("select" +
			"	i" +
			"	from CollectionIntegration i"+
			"	where" +
			"		i.collection = :collection and" +
			"			i.pubId = :pubId")
	CollectionIntegration findByPubIdAndCollection(
		String pubId, 
		CollectionWithSchema collection);
}
