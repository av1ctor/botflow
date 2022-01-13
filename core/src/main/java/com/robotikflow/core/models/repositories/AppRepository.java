package com.robotikflow.core.models.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.robotikflow.core.models.entities.App;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;

public interface AppRepository 
	extends JpaRepository<App, Long> 
{
	@Query("select" +
			"   a" +
			"	from App a" +
			"	where a.pubId = :pubId" +
			"		and a.deletedBy is null")
	App findByPubId(
		String pubId);

	@Query("select" +
			"   a" +
			"	from App a" +
			"	where a.pubId = :pubId" +
			"		and a.workspace.id = :workspace" +
			"			and a.deletedBy is null")
	App findByPubIdAndWorkspace(
		String pubId, 
		Long workspace);

	@Query("select" +
			"   a" +
			"	from App a" +
			"	where lower(a.title) = lower(:title)" +
			"		and a.workspace.id = :workspace" +
			"			and a.deletedBy is null")
	App findByTitleAndWorkspace(
		String title, 
		Long workspace);

	@Query("select" +
			"   a" +
			"	from App a" +
			"	where a.pubId = :pubId" +
			"		and a.workspace = :workspace" +
			"			and a.deletedBy is null" +
			"				and (" +
			"					exists (" + 
			"						select aa.user.id" +
			"							from AppAuth aa" +
			"							where aa.app = a" +
			"								and aa.user is not null" +
			"									and aa.user = :user" +
			"										and aa.reverse = false" +
			"					)" +
			"					or (" +
			"						exists (" +
			"							select aa.group.id" +
			"								from AppAuth aa" +
			"								left join AppAuth aarev" +
			"									on aarev.app = a" +
			"										and aarev.group is not null" +
			"											and aarev.group = aa.group" +
			"												and aarev.reverse = true" +		
			"								where aa.app = a" +
			"									and aa.group is not null" +
			"										and aa.group.id in (" +
			"					 						select ga.child.id " + 
			"					 							from GroupTree ga" + 
			"				 								inner join UserGroupWorkspace urg" + 
			"			 										on urg.group = ga.parent" + 
			"			 											and urg.workspace = :workspace" + 
			"			 												and urg.user = :user" +
			" 										)" +
			"											and aa.reverse = false" +
			"												and aarev.app is null" +
			"						) and not exists (" +
			"							select aa.user.id" +
			"								from AppAuth aa" +
			"								where aa.app = a" +
			"									and aa.user is not null" +
			"										and aa.user = :user" +
			"											and aa.reverse = true" +
			"						)" +
			"					)" +
			"				)"			
	)
	App findByPubIdAndUserAndWorkspace(
		String pubId, 
		User user, 
		Workspace workspace);

	@Query("select" +
			"   a" +
			"	from App a" +
			"	where a.workspace = :workspace" +
			"			and a.deletedBy is null")
	List<App> findAllByWorkspace(
		Workspace workspace, 
		Pageable p);

	@Query( "update" +
			"	App a" +
			"	set a.deletedBy = :user," +
			"		a.deletedAt = :currentDate" +
			"	where a = :app")
	@Modifying(clearAutomatically = true)
	@Transactional
	void delete(
		App app, 
		User user, 
		ZonedDateTime currentDate);

	@Query( "update" +
			"	App a" +
			"	set a.deletedBy = :user," +
			"		a.deletedAt = :currentDate" +
			"	where a.id = :idApp")
	@Modifying(clearAutomatically = true)
	@Transactional
	void deletedById(
		Long idApp, 
		User user, 
		ZonedDateTime currentDate);
}
