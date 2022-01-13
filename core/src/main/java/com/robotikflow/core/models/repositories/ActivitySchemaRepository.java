package com.robotikflow.core.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.ActivitySchema;

public interface ActivitySchemaRepository 
    extends JpaRepository<ActivitySchema, Long> 
{
    @Query("select "+
            "   s" +
            "   from ActivitySchema s" +
            "   where" +
            "       s.pubId = :pubId")
    public ActivitySchema findByPubId(
        String pubId);

    @Query("select "+
            "   s" +
            "   from ActivitySchema s" +
            "   where" +
            "       s.name = :name")
    public ActivitySchema findByName(
            final String name);
}
