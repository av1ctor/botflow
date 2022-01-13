package com.robotikflow.core.models.repositories;

import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.DocumentExt;
import com.robotikflow.core.models.entities.DocumentType;
import com.robotikflow.core.models.entities.Provider;
import com.robotikflow.core.models.entities.Workspace;
import com.robotikflow.core.models.entities.User;

public interface DocumentRepository 
	extends JpaRepository<Document, Long>
{
	@Query( "select" +
			"	d" +
			"	from Document d" +
			"	where d.pubId = :pubId" +
			"		and d.workspace.id = :idWorkspace" +
			"			and d.deletedBy is null")
	Document findByPubIdAndWorkspace(
		String pubId, 
		Long idWorkspace);
	
	@Query( "select d" +
			"	from Document d" +
			"	where d.parent = :parent" +
			"			and d.deletedBy is null")
	List<Document> findAllByParent(
		Document parent, 
		Pageable p);

	@Query( "select d\r\n" +
			"	from Document d\r\n" +
			"	where d.parent = :parent\r\n" +
			"		and d.deletedBy is null" +
			"		and (\r\n" +
			"			(d.ownerAuth != 0\r\n" +
			"				and d.owner = :user)\r\n" + 
			"			or (d.groupAuth != 0\r\n" +
			"				 and d.group.id in (\r\n" + 
			"			 		select ga.child.id \r\n" + 
			"			 			from GroupTree ga\r\n" + 
			"			 			inner join UserGroupWorkspace urg\r\n" + 
			"			 				on urg.group = ga.parent\r\n" + 
			"			 					and urg.workspace = d.workspace\r\n" + 
			"			 						and urg.user = :user))\r\n" + 
			"			or d.othersAuth != 0\r\n" +
			"			or exists (\r\n" + 
			"					select p.id\r\n" + 
			"						from DocumentAuth p\r\n" + 
			"						where p.document = d\r\n" + 
			"							and p.user = :user\r\n" + 
			"								and p.reverse = false)\r\n" + 
			"			or exists (\r\n" + 
			"					select p.id\r\n" + 
			"						from DocumentAuth p\r\n" + 
			"						where p.document = d\r\n" + 
			"							and p.group.id in (\r\n" + 
			"								select ga.child.id\r\n" + 
			"			 						from GroupTree ga\r\n" + 
			"				 					inner join UserGroupWorkspace urg\r\n" + 
			"				 						on urg.group = ga.parent\r\n" + 
			"									where urg.user = :user\r\n" + 
			"										and urg.workspace = d.workspace)\r\n" + 
			"								and p.reverse = false)\r\n" + 
			"			)")
	List<Document> findAllByParentAndUser(
		Document parent, 
		User user, 
		Pageable p);
	
	@Query(
			"select d\r\n" +
			"	from Document d\r\n"  +
			"	where d.parent = :parent\r\n" +
			"		and lower(d.name) like lower(concat(:name,'%'))" +
			"		and d.deletedBy is null" +
			"		and (\r\n" +
			"			(d.ownerAuth != 0\r\n" +
			"				and d.owner = :user)\r\n" + 
			"			or (d.groupAuth != 0\r\n" +
			"				 and d.group.id in (\r\n" + 
			"			 		select ga.child.id \r\n" + 
			"			 		from GroupTree ga\r\n" + 
			"			 			inner join UserGroupWorkspace urg\r\n" + 
			"				 			on urg.group = ga.parent\r\n" + 
			"				 				and urg.workspace = d.workspace\r\n" + 
			"				 					and urg.user = :user))\r\n" + 
			"			or d.othersAuth != 0\r\n" +
			"			or exists (\r\n" + 
			"					select p.id\r\n" + 
			"						from DocumentAuth p\r\n" + 
			"						where p.document = d\r\n" + 
			"							and p.user = :user\r\n" + 
			"								and p.reverse = false)\r\n" + 
			"			or exists (\r\n" + 
			"					select p.id\r\n" + 
			"						from DocumentAuth p\r\n" + 
			"						where p.document = d\r\n" + 
			"							and p.group.id in (\r\n" + 
			"								select ga.child.id\r\n" + 
			"			 						from GroupTree ga\r\n" + 
			"			 						inner join UserGroupWorkspace urg\r\n" + 
			"			 							on urg.group = ga.parent\r\n" + 
			"									where urg.user = :user\r\n" + 
			"										and urg.workspace = d.workspace)\r\n" + 
			"								and p.reverse = false)\r\n" + 
			"			)")
	List<Document> findAllByParentAndUserAndNameContainingIgnoreCase(
		Document parent, 
		User user, 
		String name, 
		Pageable p);

	@Query(
			"select d\r\n" +
			"	from Document d\r\n" +
			"	where d.parent = :parent\r\n" +
			"		and d.type = :type\r\n" +
			"		and d.deletedBy is null" +
			"		and (\r\n" +
			"			(d.ownerAuth != 0\r\n" +
			"				and d.owner = :user)\r\n" + 
			"			or (d.groupAuth != 0\r\n" +
			"				 and d.group.id in (\r\n" + 
			"			 		select ga.child.id \r\n" + 
			"			 			from GroupTree ga\r\n" + 
			"			 			inner join UserGroupWorkspace urg\r\n" + 
			"			 				on urg.group = ga.parent\r\n" + 
			"			 					and urg.workspace = d.workspace\r\n" + 
			"			 						and urg.user = :user))\r\n" + 
			"			or d.othersAuth != 0\r\n" +
			"			or exists (\r\n" + 
			"					select p.id\r\n" + 
			"						from DocumentAuth p\r\n" + 
			"						where p.document = d\r\n" + 
			"							and p.user = :user\r\n" + 
			"								and p.reverse = false)\r\n" + 
			"			or exists (\r\n" + 
			"					select p.id\r\n" + 
			"						from DocumentAuth p\r\n" + 
			"						where p.document = d\r\n" + 
			"							and p.group.id in (\r\n" + 
			"								select ga.child.id\r\n" + 
			"			 						from GroupTree ga\r\n" + 
			"				 					inner join UserGroupWorkspace urg\r\n" + 
			"				 						on urg.group = ga.parent\r\n" + 
			"									where urg.user = :user\r\n" + 
			"										and urg.workspace = d.workspace)\r\n" + 
			"								and p.reverse = false)\r\n" + 
			"			)")
	List<Document> findAllByParentAndUserAndType(
		Document parent, 
		User user, 
		DocumentType type, 
		Pageable p);
	
	@Query(
			"select d\r\n" +
			"	from Document d\r\n" +
			"	where d.parent = :parent\r\n" +
			"		and d.type = :type\r\n" +
			"		and lower(d.name) like lower(concat(:name,'%'))\r\n" +
			"		and d.deletedBy is null" +
			"		and (\r\n" +
			"			(d.ownerAuth != 0\r\n" +
			"				and d.owner = :user)\r\n" + 
			"			or (d.groupAuth != 0\r\n" +
			"				 and d.group.id in (\r\n" + 
			"				 	select ga.child.id \r\n" + 
			"			 			from GroupTree ga\r\n" + 
			"			 			inner join UserGroupWorkspace urg\r\n" + 
			"			 				on urg.group = ga.parent\r\n" + 
			"				 				and urg.workspace = d.workspace\r\n" + 
			"				 					and urg.user = :user))\r\n" + 
			"			or d.othersAuth != 0\r\n" +
			"			or exists (\r\n" + 
			"					select p.id\r\n" + 
			"						from DocumentAuth p\r\n" + 
			"						where p.document = d\r\n" + 
			"							and p.user = :user\r\n" + 
			"								and p.reverse = false)\r\n" + 
			"			or exists (\r\n" + 
			"					select p.id\r\n" + 
			"						from DocumentAuth p\r\n" + 
			"						where p.document = d\r\n" + 
			"							and p.group.id in (\r\n" + 
			"								select ga.child.id\r\n" + 
			"				 					from GroupTree ga\r\n" + 
			"				 					inner join UserGroupWorkspace urg\r\n" + 
			"			 							on urg.group = ga.parent\r\n" + 
			"									where urg.user = :user\r\n" + 
			"										and urg.workspace = d.workspace)\r\n" + 
			"								and p.reverse = false)\r\n" + 
			"			)")
	List<Document> findAllByParentAndUserAndTypeAndNameContainingIgnoreCase(
		Document parent, 
		User user, 
		DocumentType type, 
		String name, 
		Pageable p);

	@Query(
			"select d" +
			"	from Document d" +
			"	where d.name = :name" +
			"		and d.parent = :parent" +
			"			and d.workspace = :workspace" +
			"				and d.deletedBy is null")
	Document findByNameAndParentAndWorkspace(
		String name, 
		Document parent, 
		Workspace workspace);

	@Query(
			"select d" +
			"	from Document d" +
			"	where d.name = :name" +
			"			and d.provider = :provider" +
			"				and d.workspace = :workspace" +
			"					and d.deletedBy is null")
	Document findByNameAndProviderAndWorkspace(
		String name, 
		Provider provider, 
		Workspace workspace);

	@Query(
			"select d" +
			"	from Document d" +
			"	where TYPE(d) = DocumentExt" +
			"		and d.fileId = :fileId" +
			"			and d.provider = :provider" +
			"				and d.workspace = :workspace" +
			"					and d.deletedBy is null")
	DocumentExt findByFiledIdAndProviderAndWorkspace(
		Provider provider, 
		String fileId, 
		Workspace workspace);

	@Query(value = 
		"select \r\n" + 
		"	d.*\r\n" + 
		"	from documents d\r\n" + 
		"	inner join documents_exts ext\r\n" + 
		"		on ext.id = d.id\r\n" +
		"	where d.provider_id = :providerId\r\n" +
		"		and d.workspace_id = :idWorkspace\r\n" +
		"			and (select\r\n"+
		"				string_agg(lower(p.name), '/' order by da.\"depth\" desc)\r\n"+
		"				from documents_tree da\r\n"+
		"				inner join documents p\r\n"+
		"					on p.id = da.parent_id\r\n"+
		"				where da.child_id = d.id\r\n"+
		"					and da.\"depth\" > 0\r\n"+
		"			) =	:path\r\n" + 
		"	and d.removido_por_id is null\r\n", 
		nativeQuery = true)
	Document findByProviderAndPathAndWorkspace(
		Long providerId, 
		String path, 
		Long idWorkspace);		

	@Query(value = 
			"select \r\n" + 
			"	(select \r\n" + 
			"		string_agg(p.name, '/' order by da.\"depth\" desc)\r\n" + 
			"		from documents_tree da\r\n" + 
			"		inner join documents p\r\n" + 
			"			on p.id = da.parent_id\r\n" + 
			"		where da.child_id = d.id) as caminho\r\n" + 
			"	from documents d\r\n" + 
			"	where d.id = :id",
			nativeQuery = true)
	String getPathById(Long id);
	
	@Query( "update" +
			"	Document d" +
			"	set d.deletedBy = :user," +
			"		d.deletedAt = :currentDate" +
			"	where d = :document")
	@Modifying(clearAutomatically = true)
	@Transactional
	void delete(
		Document document, 
		User user, 
		ZonedDateTime currentDate);

	@Query( "update" +
			"	Document d" +
			"	set d.deletedBy = :user," +
			"		d.deletedAt = :currentDate" +
			"	where d.id = :idDocument")
	@Modifying(clearAutomatically = true)
	@Transactional
	void deletedById(
		Long idDocument, 
		User user, 
		ZonedDateTime currentDate);
}
