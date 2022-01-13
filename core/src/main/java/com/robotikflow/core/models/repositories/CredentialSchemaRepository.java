package com.robotikflow.core.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.CredentialSchema;

public interface CredentialSchemaRepository 
    extends JpaRepository<CredentialSchema, Long> 
{
    @Query("select "+
            "   s" +
            "   from CredentialSchema s" +
            "   where" +
            "       s.pubId = :pubId")
    public CredentialSchema findByPubId(
        String pubId);

    @Query("select "+
            "   s" +
            "   from CredentialSchema s" +
            "   where" +
            "       s.name = :name")
    public CredentialSchema findByName(
            final String name);
}
