package com.robotikflow.core.models.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.UserRoleWorkspace;
import com.robotikflow.core.models.entities.UserRoleWorkspaceId;

public interface UserRoleWorkspaceRepository 
	extends JpaRepository<UserRoleWorkspace, UserRoleWorkspaceId>
{

	@Query( "select" +
			"	urp" +
			"	from UserRoleWorkspace urp" +
			"	where urp.user.id = :idUser" +
			"		and urp.workspace.id = :idWorkspace")
	List<UserRoleWorkspace> findAllByUserAndWorkspace(
		Long idUser, 
		Long idWorkspace);
}
