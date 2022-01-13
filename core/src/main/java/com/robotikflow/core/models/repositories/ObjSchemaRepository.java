package com.robotikflow.core.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.ObjSchema;

public interface ObjSchemaRepository 
    extends JpaRepository<ObjSchema, Long> 
{
    @Query("select "+
            "   s" +
            "   from ObjSchema s" +
            "   where" +
            "       s.pubId = :pubId")
    public ObjSchema findByPubId(
        String pubId);

    @Query("select "+
            "   s" +
            "   from ObjSchema s" +
            "   where" +
            "       s.name = :name")
    public ObjSchema findByName(
            final String name);
}
