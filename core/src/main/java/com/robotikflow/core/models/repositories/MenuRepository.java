package com.robotikflow.core.models.repositories;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.robotikflow.core.models.entities.Menu;
import com.robotikflow.core.models.entities.Role;
import com.robotikflow.core.models.entities.Workspace;

public interface MenuRepository 
extends JpaRepository<Menu, Long>
{
	@Query( "select" +
			"	m" +
			"	from Menu m" +
			"	where m.parent is null" +
			"		and (m.workspace is null or m.workspace = :workspace)" +
			"	order by m.order asc")
	List<Menu> findAllTopByWorkspace(
		Workspace workspace);

	@Query( "select" +
			"	m" +
			"	from Menu m" +
			"	inner join MenuRole mr" +
			"		on mr.role in :roleList" +
			"			and mr.menu = m" +
			"	where m.parent is null" +
			"		and (m.workspace is null or m.workspace = :workspace)" +
			"	order by m.order asc")
	List<Menu> findAllTopByRoleAndWorkspace(
		@Param("roleList") Set<Role>roles, 
		Workspace workspace);
}
