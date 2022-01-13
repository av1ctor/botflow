package com.robotikflow.core.models.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.robotikflow.core.models.entities.DocumentAuth;

public interface DocumentAuthRepository 
	extends JpaRepository<DocumentAuth, Long>
{
	@Query(value = 
		   "select p.*" +
		   "	from documents_auths p" +
		   "	where p.document_id = :idDocument" +
		   "		and p.user_id = :idUser" + 
		   "			and p.reverse = 0 " +
		   "union " +
		   "select p.*" +
		   "	from documents_auths p" +
		   "	where p.document_id = :idDocument" +
		   "		and p.group_id in :idGroupList" +
		   "			and p.reverse = 0", nativeQuery = true)
	List<DocumentAuth> findAllByDocumentAndUserOrGroup(
		Long idDocument, 
		Long idUser, 
		List<Long> idGroupList);
}
