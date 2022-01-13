package com.robotikflow.core.models.repositories;

import com.robotikflow.core.models.entities.CollectionAutomationActivity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionAutomationActivityRepository 
    extends JpaRepository<CollectionAutomationActivity, Long>
{
}
