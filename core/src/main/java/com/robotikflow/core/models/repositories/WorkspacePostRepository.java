package com.robotikflow.core.models.repositories;

import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.WorkspacePost;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface WorkspacePostRepository 
    extends JpaRepository<WorkspacePost, Long>
{
	@Query("select" +
			"	p" +
			"	from WorkspacePost p" +
			"	where p.workspace = :workspace" +
			"	    	and p.pubId = :pubId")
	WorkspacePost findByPubIdAndWorkspace(
		String pubId,
		Workspace workspace);
}
