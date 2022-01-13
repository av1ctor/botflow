package com.robotikflow.core.models.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.User;
import com.robotikflow.core.models.entities.Workspace;

import java.util.List;
import java.util.Optional;

public interface WorkspaceRepository 
extends JpaRepository<Workspace, Long> 
{
	Workspace findByNameIgnoreCase(
		String name);
	
	Optional<Workspace> findByPubId(
		String pubId);

	@Query(
		"select w" +
		"	from Workspace w" +
		"	where w.id in (" +
		"		select" +
		"			uwp.workspace.id" +
		"			from UserRoleWorkspace uwp" +
		"			where uwp.user = :user)")
	List<Workspace> findAllByUser(
		User user, 
		Pageable pageable);

	List<Workspace> findByNameContaining(
		String name, 
		Pageable pageable);
}
