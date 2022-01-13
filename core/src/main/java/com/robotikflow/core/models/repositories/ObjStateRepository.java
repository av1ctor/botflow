package com.robotikflow.core.models.repositories;

import com.robotikflow.core.models.entities.ObjState;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ObjStateRepository 
	extends JpaRepository<ObjState, Long> 
{
}
