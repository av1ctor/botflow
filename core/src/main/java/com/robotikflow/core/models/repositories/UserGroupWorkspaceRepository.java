package com.robotikflow.core.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.robotikflow.core.models.entities.UserGroupWorkspace;
import com.robotikflow.core.models.entities.UserGroupWorkspaceId;

public interface UserGroupWorkspaceRepository 
    extends JpaRepository<UserGroupWorkspace, UserGroupWorkspaceId>
{
}
