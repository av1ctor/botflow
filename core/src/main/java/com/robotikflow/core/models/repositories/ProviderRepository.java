package com.robotikflow.core.models.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;

import java.util.List;

public interface ProviderRepository 
    extends JpaRepository<Provider, Long> 
{
    @Query("select "+
            "   p" +
            "   from Provider p" +
            "   inner join ProviderSchema schema" +
            "       on p.schema.id = schema.id" +
            "   where" +
            "       p.pubId = :pubId" +
            "           and p.workspace = :workspace")
    public Provider findByPubIdAndWorkspace(
        String pubId, 
        Workspace workspace);

    @Query("select "+
            "   p" +
            "   from Provider p" +
            "   inner join ProviderSchema schema" +
            "       on p.schema.id = schema.id" +
            "   where" +
            "       schema.name = :name" +
            "           and p.workspace = :workspace")
    public Provider findByNameAndWorkspace(
            final String name, 
            final Workspace workspace);

    @Query("select "+
            "   p" +
            "   from Provider p" +
            "   inner join ProviderSchema schema" +
            "       on p.schema.id = schema.id" +
            "   where" +
            "       schema.name = 'internalStorageProvider'" +
            "           and p.workspace = :workspace")
    public Provider findInternalStorage(
        Workspace workspace
    );

    @Query("select "+
            "   p" +
            "   from Provider p" +
            "   inner join ProviderSchema schema" +
            "       on p.schema.id = schema.id" +
            "   where" +
            "       p.workspace = :workspace")
    public List<Provider> findAllByWorkspace(
        Workspace workspace, 
        Pageable pageable);
}
