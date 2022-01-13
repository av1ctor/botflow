package com.robotikflow.core.services.nosql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.robotikflow.core.models.nosql.Filter;
import com.robotikflow.core.models.nosql.Reference;
import com.robotikflow.core.models.nosql.Sort;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import javafx.util.Pair;

public interface NoSqlService 
{
	public ObjectId toObjectId(
		final String id);

	public ObjectId toObjectId(
		final Object id);
	
	public void initialize(
		final String db);
	
	public void createCollection(
		final String db, 
		final String name, 
		final String locale);
	
	public void createCollection(
		final String db, 
		final String name);

	public void dropCollection(
		final String db, 
		final String name);
	
	public void createSequence(
		final String db, 
		final String autoGenId);
	
	public void deleteSequence(
		final String db, 
		final String autoGenId);

	public void createIndex(
		final String db, 
		final String collection, 
		final List<String> columns, 
		final boolean isAsc, 
		final boolean isUnique);

	public void dropIndex(
		final String db, 
		final String collection, 
		final List<String> columns, 
		final boolean isAsc);

	public void dropIndexes(
		final String db, 
		final String collection);

	public Object autoGen(
		final String db, 
		final String autoGenId);
	
	public String insert(
		final String db, 
		final String collection, 
		final Map<String, Object> variables);

	public void update(
		final String db, 
		final String collection, 
		final Object id, 
		final Map<String, Object> variables);
	
	public void update(
		final String db, 
		final String collection, 
		final Filter filters, 
		final Map<String, Object> variables, 
		final Map<String, Integer> incs);

	public void update(
		final String db, 
		final String collection, 
		final Filter filters, 
		final Map<String, Object> variables);
	
	public String upsert(
		final String db, 
		final String collection, 
		final String id, 
		final HashMap<String, Object> variables);
	
	public List<Document> findAll(
		final String db, 
		final String collection, 
		final Document projection, 
		final List<Reference> references, 
		final Filter docFilters, 
		final Filter refFilters, 
		final List<Sort> sort, 
		final int offset, 
		final int limit, 
		final List<Bson> aggregates);
	
	public List<Document> findAll(
		final String db, 
		final String collection, 
		final Document projection, 
		final List<Reference> references, 
		final Filter docFilters, 
		final Filter refFilters, 
		final List<Sort> sort, 
		final int offset, 
		final int limit);

	public Document findOne(
		final String db, 
		final String collection, 
		final Document projection, 
		final List<Reference> references, 
		final Filter docFilters, 
		final Filter refFilters);

	public Document findById(
		final String db, 
		final String collection, 
		final Document projection, 
		final List<Reference> references, 		
		final String id,
		final Filter refFilters);

	public Document findById(
		final String db, 
		final String collection, 
		final Document projection, 
		final List<Reference> references, 		
		final String id);

	public void deleteAll(
		final String db, 
		final String collection);

	public void deleteMany(
		final String db, 
		final String collection, 
		final Filter docFilters);

	public void deleteOne(
		final String db, 
		final String collection, 
		final String id);

	public void deleteFields(
		final String db, 
		final String collection, 
		final List<String> fields);

	public void renameField(
		final String db, 
		final String collection, 
		final String currentName, 
		final String newName);

	public void renameCollection(
		final String db, 
		final String currentName, 
		final String newName);
	
	public Object findMax(
		final String db, 
		final String collection, 
		final String column, 
		final Filter filters);
	
	public Object findMin(
		final String db, 
		final String collection, 
		final String column, 
		final Filter filters);

	public Bson createUnwindAggregate(
		final String fieldName);

	public Bson createGroupByAggregate(
		final Map<String, Object> id, 
		final Map<String, Pair<AggregateOperator, Object>> ops);

	public Bson createProjectAggregate(
		final Document projection);

	public Bson createSortAggregate(
		final Document sort);

	public void dupData(
		final String db, 
		final String from, 
		final String to);
}
