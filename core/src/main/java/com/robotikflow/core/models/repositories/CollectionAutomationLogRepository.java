package com.robotikflow.core.models.repositories;

import java.util.List;

import com.robotikflow.core.models.entities.CollectionAutomation;
import com.robotikflow.core.models.entities.CollectionAutomationLog;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionAutomationLogRepository 
    extends JpaRepository<CollectionAutomationLog, Long>
{
    List<CollectionAutomationLog> findAllByAutomation(
        CollectionAutomation automation, 
        Pageable pageable);
}
