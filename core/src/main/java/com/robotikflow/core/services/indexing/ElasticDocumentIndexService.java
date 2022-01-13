package com.robotikflow.core.services.indexing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;

import com.robotikflow.core.models.indexing.SearchResponse;

public class ElasticDocumentIndexService 
	implements DocumentIndexService 
{
	private RestHighLevelClient client;

	public ElasticDocumentIndexService(
		final String hostAddr, 
		final int hostPort) 
		throws Exception
	{
		client = new RestHighLevelClient(
			RestClient.builder(
					new HttpHost(hostAddr, hostPort, "http")));		
	}
	
	@Override
	public boolean criar(String workspaceId) throws Exception
	{
		var request = new CreateIndexRequest(workspaceId);
		
		var builder = XContentFactory.jsonBuilder();
		
		builder.startObject()
			.startObject("_doc")
				.startObject("properties")
					.startObject("name")
						.field("type", "keyword")
					.endObject()
					.startObject("extensao")
						.field("type", "keyword")
					.endObject()
					.startObject("pubId")
						.field("type", "keyword")
						.field("doc_values", false)
					.endObject()
					.startObject("parentId")
						.field("type", "keyword")
					.endObject()
					.startObject("workspaceId")
						.field("type", "keyword")
					.endObject()
					.startObject("createdAt")
						.field("type", "date")
						.field("format", "dateOptionalTime")
					.endObject()
					.startObject("updatedAt")
						.field("type", "date")
						.field("format", "dateOptionalTime")
					.endObject()
					.startObject("content")
						.field("type", "text")
						.field("analyzer", "brazilian")
					.endObject()
					.startObject("tamanho")
						.field("type", "long")
					.endObject()
					.startObject("indexadoEm")
						.field("type", "date")
						.field("format", "dateOptionalTime")
						.field("doc_values", false)
					.endObject()
				.endObject()
			.endObject()
		.endObject();
		
		var response = client.indices().create(request.mapping("_doc",  builder), RequestOptions.DEFAULT);
		
		return response.isAcknowledged();
	}

	@Override
	public void destruir(String workspaceId) throws Exception
	{
		var request = new DeleteIndexRequest(workspaceId);
		client.indices().delete(request, RequestOptions.DEFAULT);
	}
	
	@Override
	public boolean indexar(String name, String extensao, String pubId, String parentId, Long tamanho, 
			Date createdAt, String workspaceId, String conteudo) throws Exception
	{
		var request = new IndexRequest(workspaceId, "_doc", pubId)
				.opType(DocWriteRequest.OpType.CREATE);
		
		var fields = new HashMap<String, Object>();
		fields.put("name", name);
		fields.put("extensao", extensao);
		fields.put("pubId", pubId);
		fields.put("parentId", parentId);
		fields.put("workspaceId", workspaceId);
		fields.put("createdAt", createdAt);
		fields.put("indexadoEm", new Date());
		fields.put("tamanho", (long)tamanho);
		fields.put("content", conteudo);
				
		var response = client.index(request.source(fields), RequestOptions.DEFAULT); 
				   
		return response.getResult() == Result.CREATED;		
	}

	@Override
	public boolean remover(String pubId, String workspaceId) throws Exception 
	{
		var request = new DeleteRequest(workspaceId, "_doc", pubId);
		
		var response = client.delete(request, RequestOptions.DEFAULT);
		
		return response.getResult() == Result.DELETED;
	}

	@Override
	public boolean atualizar(String pubId, String parentId, Long tamanho, Date updatedAt, 
			String workspaceId) throws Exception 
	{
		var request = new UpdateRequest(workspaceId, "_doc", pubId);
		
		var fields = new HashMap<String, Object>();
		fields.put("parentId", parentId);
		fields.put("updatedAt", updatedAt);
		
		var response = client.update(request.doc(fields), RequestOptions.DEFAULT);
		
		return response.getResult() == Result.UPDATED;
	}

	@Override
	public boolean copiar(String fontePubId, String name, String extensao, String pubId, 
			String parentId, Long tamanho, Date createdAt, String workspaceId) throws Exception 
	{
		var request = new GetRequest(workspaceId, "_doc", fontePubId);
		
		var response = client.get(request, RequestOptions.DEFAULT);
		
		var fields = response.getSourceAsMap();
		
		return indexar(name, extensao, pubId, parentId, tamanho, createdAt, workspaceId, (String)fields.get("content"));
	}
	
	public List<SearchResponse> pesquisar(String query, String parentId, String workspaceId, Pageable pageable) throws Exception 
	{
		var request = new SearchRequest(workspaceId);
		
		var builder = new SearchSourceBuilder();
		builder.query(QueryBuilders.boolQuery()
					.must(QueryBuilders.termQuery("parentId", parentId))
					.must(QueryBuilders.queryStringQuery(query)));
		
		builder.from(pageable.getPageNumber() * pageable.getPageSize());
		builder.size(pageable.getPageSize());
		for(var sort : pageable.getSort())
		{
			builder.sort(new FieldSortBuilder(sort.getProperty()).order(sort.getDirection() == Direction.ASC? SortOrder.ASC: SortOrder.DESC));
		}
		
		var excludeFields = new String[] {"*"};
		builder.fetchSource(null, excludeFields);		
		
		var highlightBuilder = new HighlightBuilder();
		var highlightContent = new HighlightBuilder.Field("content");
		highlightBuilder.field(highlightContent);
		builder.highlighter(highlightBuilder);
		
		builder.timeout(new TimeValue(60, TimeUnit.SECONDS));
		request.source(builder);
		
		var searchResponse = client.search(request, RequestOptions.DEFAULT);

		var res = new ArrayList<SearchResponse>();
		
		var hits = searchResponse.getHits();
		for(var hit : hits)
		{
			var highlightFields = hit.getHighlightFields();
			var highlight = highlightFields.get("content");
			var highlightText = new StringBuilder();
			for(var fragment : highlight.fragments())
			{
				highlightText.append(fragment.toString());
				highlightText.append("\n");
			}
			
			res.add(new SearchResponse(hit.getId(), hit.getScore(), highlightText.toString()));
		}

		return res;
	}
	
}
