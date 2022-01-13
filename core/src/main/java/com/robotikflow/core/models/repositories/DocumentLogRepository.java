package com.robotikflow.core.models.repositories;

import com.robotikflow.core.models.entities.DocumentLog;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentLogRepository 
    extends JpaRepository<DocumentLog, Long>
{
}
