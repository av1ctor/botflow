package com.robotikflow.core.models.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.Timezone;

public interface TimezoneRepository 
	extends JpaRepository<Timezone, String> 
{
	@Query("select" +
			"	t" +
			"	from Timezone t")
	List<Timezone> findAll_(Pageable p);
}
