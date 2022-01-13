package com.robotikflow.core.models.repositories;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;

public interface UserRepository extends JpaRepository<User, Long> 
{
	User findByEmail(
		String email);

	Optional<User> findByPubId(
		String pubId);
	
	@Query( "select" +
			"	u" +
			"	from User u" +
			"	where u.pubId = :pubId" +
			"		and exists" +
			"			(select" +
			"				urp.id" +
			"				from UserRoleWorkspace urp" +
			"				where urp.user = u" +
			"					and urp.workspace.id = :idWorkspace" +
			"			)" +
			"			and u.deletedBy is null")
	User findByPubIdAndIdWorkspace(
		String pubId, 
		Long idWorkspace);
	
	@Query( "select" +
			"	u" +
			"	from User u" +
			"	where lower(u.email) = lower(:email)" +
			"		and exists" +
			"			(select" +
			"				urp.id" +
			"				from UserRoleWorkspace urp" +
			"				inner join Workspace r" +
			"					on r = urp.workspace" +
			"						and lower(r.name) = lower(:workspaceName)" +
			"				where urp.user = u" +
			"			)" +
			"			and u.deletedBy is null")
	User findByEmailAndWorkspaceName(
		String email, 
		String workspaceName);

	@Query( "select" +
			"	u" +
			"	from User u" +
			"	where lower(u.email) = lower(:email)" +
			"		and exists" +
			"			(select" +
			"				urp.id" +
			"				from UserRoleWorkspace urp" +
			"				where urp.user = u" +
			"					and urp.workspace.id = :idWorkspace" +
			"			)" +
			"			and u.deletedBy is null")
	User findByEmailAndIdWorkspace(
		String email, 
		Long idWorkspace);

	@Query( "select" +
			"	u" +
			"	from User u" +
			"	where u.id in" +
			"		(select" +
			"			urp.user.id" +
			"			from UserRoleWorkspace urp" +
			"			where urp.workspace.id = :idWorkspace" +
			"		)" +
			"		and u.deletedBy is null")
	List<User> findAllByIdWorkspace(
		Long idWorkspace, 
		Pageable p);
	
	@Query( "select" +
			"	u" +
			"	from User u" +
			"	where lower(u.email) like lower(concat(:email,'%'))" +
			"		and u.id in" +
			"			(select" +
			"				urp.user.id" +
			"				from UserRoleWorkspace urp" +
			"				where urp.workspace.id = :idWorkspace" +
			"			)" +
			"			and u.deletedBy is null")
	List<User> findAllByEmailContainingAndWorkspace(
		String email, 
		Long idWorkspace, 
		Pageable p);

	@Query( " select" + 
			"	u" +
			"	from User u" + 
			"	inner join UserGroupWorkspace urg" + 
			"		on urg.workspace = :workspace" + 
			"			and urg.user.id = u.id" + 
			"				and urg.group.id in (" + 
			"					select ga.parent.id" + 
			"		          		from UserGroupWorkspace urg2" + 
			"						inner join GroupTree ga" + 
			"							on ga.child.id = urg2.group.id" + 
			"						where urg2.workspace = :workspace" + 
			"							and urg2.user = :user" + 
			"								and ga.depth > 0)" +
			"	where u.deletedBy is null")
	List<User> findAllSuperioresByUserAndWorkspace(
		User user, 
		Workspace workspace);
	
	@Query( "update" +
			"	User u" +
			"	set u.deletedBy = :deletor," +
			"		u.deletedAt = :currentDate" +
			"	where u = :user")
	@Modifying(clearAutomatically = true)
	@Transactional
	void delete(
		User user, 
		User deletor, 
		ZonedDateTime currentDate);

	@Query( "update" +
			"	User u" +
			"	set u.deletedBy = :deletor," +
			"		u.deletedAt = :currentDate" +
			"	where u.id = :idUser")
	@Modifying(clearAutomatically = true)
	@Transactional
	void deletedById(
		Long idUser, 
		User deletor, 
		ZonedDateTime currentDate);
	
}
