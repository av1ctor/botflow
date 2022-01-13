package com.robotikflow.core.services.indexing;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.robotikflow.core.models.indexing.SearchResponse;

public interface DocumentIndexService 
{
	boolean indexar(String name, String extensao, String pubId, String parentId, Long tamanho, 
			Date createdAt, String workspaceId, String conteudo) throws Exception;

	boolean remover(String pubId, String workspaceId) throws Exception;

	boolean atualizar(String pubId, String parentId, Long tamanho, Date updatedAt, 
			String workspaceId) throws Exception;

	boolean copiar(String fontePubId, String name, String extensao, String pubId, String parentId, 
			Long tamanho, Date createdAt, String workspaceId) throws Exception;

	boolean criar(String workspaceId) throws Exception;
	
	void destruir(String workspaceId) throws Exception;

	List<SearchResponse> pesquisar(String query, String parentId, String workspaceId, Pageable pageable) throws Exception;
}