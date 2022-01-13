package com.robotikflow.core.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.robotikflow.core.models.entities.CollectionVersionChange;
import com.robotikflow.core.models.entities.CollectionVersionChangeId;

public interface CollectionVersionChangeRepository 
    extends JpaRepository<CollectionVersionChange, CollectionVersionChangeId>
{
}
