package com.robotikflow.core.models.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.Icon;

public interface IconRepository extends JpaRepository<Icon, Long> 
{
	@Query("select" +
			"	i.name" +
			"	from Icon i")
	List<String> findAll_(Pageable p);
}
