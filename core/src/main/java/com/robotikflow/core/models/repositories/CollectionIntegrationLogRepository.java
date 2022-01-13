package com.robotikflow.core.models.repositories;

import java.util.List;

import com.robotikflow.core.models.entities.CollectionIntegration;
import com.robotikflow.core.models.entities.CollectionIntegrationLog;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionIntegrationLogRepository 
    extends JpaRepository<CollectionIntegrationLog, Long>
{

    List<CollectionIntegrationLog> findAllByIntegration(
        CollectionIntegration integration, 
        Pageable pageable);
}
