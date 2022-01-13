package com.robotikflow.core.models.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.Activity;
import com.robotikflow.core.models.entities.Workspace;

import java.util.List;

public interface ActivityRepository 
    extends JpaRepository<Activity, Long> 
{
    @Query("select "+
            "   c" +
            "   from Activity c" +
            "   inner join ActivitySchema s" +
            "       on c.schema.id = s.id" +
            "   where" +
            "       c.pubId = :pubId" +
            "           and c.workspace = :workspace")
    public Activity findByPubIdAndWorkspace(
        String pubId, 
        Workspace workspace);

    @Query("select "+
            "   c" +
            "   from Activity c" +
            "   inner join ActivitySchema s" +
            "       on c.schema.id = s.id" +
            "   where" +
            "       s.name = :name" +
            "           and c.workspace = :workspace")
    public Activity findByNameAndWorkspace(
            final String name, 
            final Workspace workspace);
            
    public List<Activity> findAllByWorkspace(
        Workspace workspace, 
        Pageable pageable);
}
