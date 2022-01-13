package com.robotikflow.core.models.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.Document;
import com.robotikflow.core.models.entities.DocumentWithPathProjection;

public interface DocumentsWithPathRepository extends JpaRepository<Document, Long>
{
	@Query(value = 
			"select \r\n" + 
			"	d.id as id,\r\n" + 
			"	(select \r\n" + 
			"		string_agg(p.name, '/' order by da.\"depth\" desc)\r\n" + 
			"		from documents_tree da\r\n" + 
			"		inner join documents p\r\n" + 
			"			on p.id = da.parent_id\r\n" + 
			"		where da.child_id = d.id) as path\r\n" + 
			"	from documents d\r\n" + 
			"	where (select\r\n"+
			"				string_agg(lower(p.name), '/' order by da.\"depth\" desc)\r\n"+
			"				from documents_tree da\r\n"+
			"				inner join documents p\r\n"+
			"					on p.id = da.parent_id\r\n"+
			"				where da.child_id = d.id\r\n"+
			"					and da.\"depth\" > 0\r\n"+
			"			) =	:path\r\n" + 
			"	and d.deleted_by_id is null" +
			"	and (\r\n" +
			"		(d.owner_auth != 0\r\n" + 
			"			and d.owner_id = :idUser)\r\n" +
			"		or (d.group_auth != 0\r\n" + 
			"			 and d.group_id in (\r\n" + 
			"			 	select ga.child_id \r\n" + 
			"			 		from groups_tree ga\r\n" + 
			"			 		inner join user_group_workspace urg\r\n" + 
			"			 			on urg.group_id = ga.parent_id\r\n" + 
			"			 				and urg.workspace_id = :idWorkspace\r\n" + 
			"			 					and urg.user_id = :idUser))\r\n" + 
			"		or d.others_auth != 0\r\n" + 
			"		or exists\r\n" + 
			"				(\r\n" + 
			"				select p.id\r\n" + 
			"					from documents_auths p\r\n" + 
			"					where p.document_id = d.id\r\n" + 
			"						and p.user_id = :idUser\r\n" + 
			"							and p.reverse = false \r\n" + 
			"				union \r\n" + 
			"				select p.id\r\n" + 
			"					from documents_auths p\r\n" + 
			"					where p.document_id = d.id\r\n" + 
			"						and p.group_id in (\r\n" + 
			"							select\r\n" + 
			"								urg.group_id\r\n" + 
			"								from user_group_workspace urg\r\n" + 
			"								where urg.user_id = :idUser\r\n" + 
			"									and urg.workspace_id = d.workspace_id\r\n" + 
			"							)\r\n" + 
			"							and p.reverse = false)\r\n" + 
			"	)\r\n", 
			nativeQuery = true)
	List<DocumentWithPathProjection> findAllByPathAndUserAndWorkspace(
		String path, 
		Long idUser, 
		Long idWorkspace, 
		Pageable p);	
	
	@Query(value = 
			"select \r\n" + 
			"	d.id as id,\r\n" + 
			"	(select \r\n" + 
			"		string_agg(p.name, '/' order by da.\"depth\" desc)\r\n" + 
			"		from documents_tree da\r\n" + 
			"		inner join documents p\r\n" + 
			"			on p.id = da.parent_id\r\n" + 
			"		where da.child_id = d.id) as path\r\n" + 
			"	from documents d\r\n" + 
			"	where lower(d.name) like lower(concat(cast(:name as varchar), '%'))\r\n" + 
			"		and (select\r\n"+
			"				string_agg(lower(p.name), '/' order by da.\"depth\" desc)\r\n"+
			"				from documents_tree da\r\n"+
			"				inner join documents p\r\n"+
			"					on p.id = da.parent_id\r\n"+
			"				where da.child_id = d.id\r\n"+
			"					and da.\"depth\" > 0\r\n"+
			"			) =	:path\r\n" + 
			"	and d.deleted_by_id is null" +
			"	and (\r\n" +
			"		(d.owner_auth != 0\r\n" + 
			"			and d.owner_id = :idUser)\r\n" +
			"		or (d.group_auth != 0\r\n" + 
			"			 and d.group_id in (\r\n" + 
			"			 	select ga.child_id \r\n" + 
			"			 		from groups_tree ga\r\n" + 
			"			 		inner join user_group_workspace urg\r\n" + 
			"			 			on urg.group_id = ga.parent_id\r\n" + 
			"			 				and urg.workspace_id = :idWorkspace\r\n" + 
			"			 					and urg.user_id = :idUser))\r\n" + 
			"		or d.others_auth != 0\r\n" + 
			"		or exists\r\n" + 
			"				(\r\n" + 
			"				select p.id\r\n" + 
			"					from documents_auths p\r\n" + 
			"					where p.document_id = d.id\r\n" + 
			"						and p.user_id = :idUser\r\n" + 
			"							and p.reverse = false \r\n" + 
			"				union \r\n" + 
			"				select p.id\r\n" + 
			"					from documents_auths p\r\n" + 
			"					where p.document_id = d.id\r\n" + 
			"						and p.group_id in (\r\n" + 
			"							select\r\n" + 
			"								urg.group_id\r\n" + 
			"								from user_group_workspace urg\r\n" + 
			"								where urg.user_id = :idUser\r\n" + 
			"									and urg.workspace_id = d.workspace_id\r\n" + 
			"							)\r\n" + 
			"							and p.reverse = false)\r\n" + 
			"	)\r\n", 
			nativeQuery = true)
	List<DocumentWithPathProjection> findAllByPathAndNameAndUserAndWorkspace(
		String path, 
		String name, 
		Long idUser, 
		Long idWorkspace, 
		Pageable p);
}
