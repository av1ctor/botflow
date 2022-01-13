package com.robotikflow.core.services.indexing;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.robotikflow.core.models.indexing.SearchResponse;

public final class NullDocumentIndexService implements DocumentIndexService 
{
	@Override
	public boolean indexar(String name, String extensao, String pubId, String parentId, Long tamanho, Date createdAt,
			String workspaceId, String conteudo) throws Exception {
		return false;
	}

	@Override
	public boolean remover(String pubId, String workspaceId) throws Exception {
		return false;
	}

	@Override
	public boolean atualizar(String pubId, String parentId, Long tamanho, Date updatedAt, String workspaceId)
			throws Exception {
		return false;
	}

	@Override
	public boolean copiar(String fontePubId, String name, String extensao, String pubId, String parentId,
			Long tamanho, Date createdAt, String workspaceId) throws Exception {
		return false;
	}

	@Override
	public boolean criar(String workspaceId) throws Exception {
		return false;
	}

	@Override
	public void destruir(String workspaceId) throws Exception {
	}
	
	@Override
	public List<SearchResponse> pesquisar(String query, String parentId, String workspaceId, Pageable pageable) throws Exception {
		return null;
	}
}
