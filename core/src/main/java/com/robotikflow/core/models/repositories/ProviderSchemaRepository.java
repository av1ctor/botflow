package com.robotikflow.core.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.ProviderSchema;

public interface ProviderSchemaRepository 
    extends JpaRepository<ProviderSchema, Long> 
{
    @Query("select "+
            "   s" +
            "   from ProviderSchema s" +
            "   where" +
            "       s.pubId = :pubId")
    public ProviderSchema findByPubId(
        String pubId);

    @Query("select "+
            "   s" +
            "   from ProviderSchema s" +
            "   where" +
            "       s.name = :name")
    public ProviderSchema findByName(
            final String name);
}
