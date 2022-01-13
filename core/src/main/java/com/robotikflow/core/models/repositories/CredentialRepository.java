package com.robotikflow.core.models.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.Credential;
import com.robotikflow.core.models.entities.Workspace;

import java.util.List;

public interface CredentialRepository 
    extends JpaRepository<Credential, Long> 
{
    @Query("select "+
            "   c" +
            "   from Credential c" +
            "   inner join CredentialSchema schema" +
            "       on c.schema.id = schema.id" +
            "   where" +
            "       c.pubId = :pubId" +
            "           and c.workspace = :workspace")
    public Credential findByPubIdAndWorkspace(
        String pubId, 
        Workspace workspace);

    @Query("select "+
            "   c" +
            "   from Credential c" +
            "   inner join CredentialSchema schema" +
            "       on c.schema.id = schema.id" +
            "   where" +
            "       schema.name = :name" +
            "           and c.workspace = :workspace")
    public Credential findByNameAndWorkspace(
            final String name, 
            final Workspace workspace);
            
    @Query("select "+
            "   c" +
            "   from Credential c" +
            "   inner join CredentialSchema schema" +
            "       on c.schema.id = schema.id" +
            "   where" +
            "       c.workspace = :workspace")
    public List<Credential> findAllByWorkspace(
        Workspace workspace, 
        Pageable pageable);
}
