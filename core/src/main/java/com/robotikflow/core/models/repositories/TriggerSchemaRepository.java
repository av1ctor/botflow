package com.robotikflow.core.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.TriggerSchema;

public interface TriggerSchemaRepository 
    extends JpaRepository<TriggerSchema, Long> 
{
    @Query("select "+
            "   s" +
            "   from TriggerSchema s" +
            "   where" +
            "       s.pubId = :pubId")
    public TriggerSchema findByPubId(
        String pubId);

    @Query("select "+
            "   s" +
            "   from TriggerSchema s" +
            "   where" +
            "       s.name = :name")
    public TriggerSchema findByName(
            final String name);
}
