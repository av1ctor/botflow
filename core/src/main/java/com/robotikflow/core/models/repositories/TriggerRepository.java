package com.robotikflow.core.models.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.Trigger;
import com.robotikflow.core.models.entities.Workspace;

import java.util.List;

public interface TriggerRepository 
    extends JpaRepository<Trigger, Long> 
{
    @Query("select "+
            "   c" +
            "   from Trigger c" +
            "   inner join TriggerSchema s" +
            "       on c.schema.id = s.id" +
            "   where" +
            "       c.pubId = :pubId" +
            "           and c.workspace = :workspace")
    public Trigger findByPubIdAndWorkspace(
        String pubId, 
        Workspace workspace);

    @Query("select "+
            "   c" +
            "   from Trigger c" +
            "   inner join TriggerSchema s" +
            "       on c.schema.id = s.id" +
            "   where" +
            "       s.name = :name" +
            "           and c.workspace = :workspace")
    public Trigger findByNameAndWorkspace(
            final String name, 
            final Workspace workspace);
            
    public List<Trigger> findAllByWorkspace(
        Workspace workspace, 
        Pageable pageable);
}
