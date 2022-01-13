package com.robotikflow.core.models.repositories;

import java.util.List;

import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.WorkspaceLog;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkspaceLogRepository 
    extends JpaRepository<WorkspaceLog, Long>
{
	@Query("select" +
			"	l" +
			"	from WorkspaceLog l" +
			"	where l.workspace = :workspace")
	List<WorkspaceLog> findAllByWorkspace(
		Workspace workspace, 
		Pageable pageable);
}
