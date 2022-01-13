package com.robotikflow.core.models.repositories;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.robotikflow.core.models.entities.Group;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;

public interface GroupRepository extends JpaRepository<Group, Long>
{
	@Query(	"select" +
			"	g" +
			"	from Group g" +
			"	where lower(g.name) = lower(:name)" +
			"		and g.workspace.id = :idWorkspace" +
			"			and g.deletedBy is null")
	Group findByNameAndWorkspace(
		String name, 
		Long idWorkspace);

	@Query( "select" +
			"	g" +
			"	from Group g" +
			"	where g.pubId = :pubId" +
			"		and g.workspace.id = :idWorkspace" +
			"			and g.deletedBy is null")
	Optional<Group> findByPubIdAndWorkspace(
		String pubId, 
		Long idWorkspace);
	
	@Query(	"select" +
			"	g" + 
			"	from Group g" + 
			"	where" + 
			"    	g.workspace = :workspace" + 
			"			and g.deletedBy is null")
	List<Group> findAllByWorkspace(
		Workspace workspace, 
		Pageable pageable);

	@Query(	"select" +
			"	g" + 
			"	from Group g" + 
			"	where" + 
			"    	lower(g.name) like lower(concat(:name,'%'))" + 
			"    		and g.workspace = :workspace" + 
			"				and g.deletedBy is null")
	List<Group> findAllByNameContainingAndWorkspace(
		String name, 
		Workspace workspace, 
		Pageable pageable);

	@Query(	"select" +
			"	g" + 
			"	from Group g" + 
			"	where" + 
			"    	g.workspace.id = :idWorkspace" + 
			"        	and g.id in (" + 
			"				select ga.child.id" + 
			"              		from UserGroupWorkspace urg" + 
			"					inner join GroupTree ga" + 
			"    					on ga.parent.id = urg.group.id" + 
			"    				where urg.workspace.id = g.workspace.id" +
			"						and urg.user.id = :idUser" + 
			"            	)" +
			"			and g.deletedBy is null")
	List<Group> findAllByUserAndWorkspace(
		Long idUser, 
		Long idWorkspace);
	
	@Query(	"select" +
			"	g" + 
			"	from Group g" + 
			"	where" + 
			"    	g.workspace.id = :idWorkspace" + 
			"        	and g.id in (" + 
			"				select ga.parent.id" + 
			"              		from UserGroupWorkspace urg" + 
			"					inner join GroupTree ga" + 
			"    					on ga.parent.id = urg.group.id" + 
			"    				where urg.workspace.id = g.workspace.id" +
			"						and urg.user.id = :idUser" +
			"							and ga.child.id in :childGroups" + 
			"            	)" +
			"			and g.deletedBy is null")
	List<Group> findAllByUserAndWorkspaceAndChildGroups(
		Long idUser, 
		Long idWorkspace, 
		List<Long> childGroups);

	@Query(	"select" +
			"	g" + 
			"	from Group g" + 
			"	where" + 
			"    	g.parent = :parent" + 
			"			and g.deletedBy is null" +
			"	order by id asc")
	List<Group> findAllByParent(
		Group parent);

	@Query( "update" +
			"	Group g" +
			"	set g.deletedBy = :user," +
			"		g.deletedAt = :currentDate," +
			"		g.name = concat('deleted|', :currentDate, '|', g.name)" +
			"	where g = :group")
	@Modifying(clearAutomatically = true)
	@Transactional
	void delete(
		Group group, 
		User user, 
		ZonedDateTime currentDate);

	@Query( "update" +
			"	Group g" +
			"	set g.deletedBy = :user," +
			"		g.deletedAt = :currentDate," +
			"		g.name = concat('deleted|', :currentDate, '|', g.name)" +
			"	where g.id = :idGroup")
	@Modifying(clearAutomatically = true)
	@Transactional
	void deletedById(
		Long idGroup, 
		User user, 
		ZonedDateTime currentDate);
}
