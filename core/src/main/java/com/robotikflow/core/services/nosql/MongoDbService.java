package com.robotikflow.core.services.nosql;

import static com.mongodb.client.model.Accumulators.max;
import static com.mongodb.client.model.Accumulators.min;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.inc;
import static com.mongodb.client.model.Updates.set;
import static com.mongodb.client.model.Updates.unset;
import static com.mongodb.client.model.Updates.rename;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import com.robotikflow.core.models.nosql.Filter;
import com.robotikflow.core.models.nosql.FilterOperator;
import com.robotikflow.core.models.nosql.Reference;
import com.robotikflow.core.models.nosql.ReferenceType;
import com.robotikflow.core.models.nosql.Sort;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoNamespace;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
import com.mongodb.client.model.CreateCollectionOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Variable;

import javafx.util.Pair;

public class MongoDbService implements NoSqlService
{
	private final MongoClient mongoClient;

	public static final String ID_SEQ_COLLECTION = "__bd#sequence";
	private static final String DEFAULT_LOCATE = "pt";
	private static final String ID_NAME = "_id";
	
	public MongoDbService(
		final String hostAddr, 
		final int hostPort)
	{
		mongoClient = MongoClients.create(
			MongoClientSettings.builder()
				.applyToClusterSettings(builder ->
					builder.hosts(Arrays.asList(new ServerAddress(hostAddr, hostPort))))
				.build());		
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public ObjectId toObjectId(
		final String id)
	{
		if(id != null && id.length() == 0)
		{
			return (ObjectId)null;
		}

		return new ObjectId(id);
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@Override
	public ObjectId toObjectId(
		final Object id)
	{
		if(id instanceof String)
		{
			return toObjectId((String)id);
		}

		return (ObjectId)id;
	}
	
	/**
	 * 
	 * @param db
	 */
	@Override
	public void initialize(
		final String db) 
	{
		this.createIndex(db, MongoDbService.ID_SEQ_COLLECTION, Arrays.asList("id"), true, true);
	}
	
	/**
	 * 
	 * @param db
	 * @param name
	 * @param locale
	 */
	@Override
	public void createCollection(
		final String db, 
		final String name, 
		final String locale)
	{
		mongoClient.getDatabase(db).createCollection(name, 
				new CreateCollectionOptions().collation(Collation.builder()
					.collationStrength(CollationStrength.PRIMARY)
					.locale(locale)
					.build()));
	}
	
	/**
	 * 
	 * @param db
	 * @param name
	 */
	@Override
	public void createCollection(
		final String db, 
		final String name)
	{
		createCollection(db, name, DEFAULT_LOCATE);
	}

	/**
	 * 
	 * @param db
	 * @param name
	 */
	@Override
	public void dropCollection(
		final String db, 
		final String name)
	{
		mongoClient.getDatabase(db).getCollection(name).drop();
	}
	
	/**
	 * 
	 * @param db
	 * @param collection
	 * @param autoGenId
	 */
	@Override
	public void createSequence(
		final String db, 
		final String autoGenId)
	{
		mongoClient.getDatabase(db).getCollection(ID_SEQ_COLLECTION)
			.insertOne(
				new Document("id", autoGenId)
					.append("seq", 0));
	}
	
	/**
	 * 
	 * @param db
	 * @param collection
	 * @param autoGenId
	 */
	@Override
	public void deleteSequence(
		final String db, 
		final String autoGenId)
	{
		mongoClient.getDatabase(db).getCollection(ID_SEQ_COLLECTION)
			.deleteOne(new Document("id", autoGenId));
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param columns
	 * @param isAsc
	 * @param isUnique
	 */
	@Override
	public void createIndex(
		final String db, 
		final String collection, 
		final List<String> columns, 
		final boolean isAsc, 
		final boolean isUnique)
	{
		mongoClient.getDatabase(db).getCollection(collection)
			.createIndex(isAsc? 
				Indexes.ascending(columns): 
				Indexes.descending(columns),
				new IndexOptions().unique(isUnique).background(true));
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param columns
	 */
	@Override
	public void dropIndex(
		final String db, 
		final String collection, 
		final List<String> columns, 
		final boolean isAsc)
	{
		var keys = new Document();
		for(var column: columns)
		{
			keys.append(column, isAsc? 1: -1);
		}
		
		mongoClient.getDatabase(db).getCollection(collection).dropIndex(keys);
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 */
	@Override
	public void dropIndexes(
		final String db, 
		final String collection)
	{
		mongoClient.getDatabase(db).getCollection(collection).dropIndexes();
	}

	private static FindOneAndUpdateOptions returnNewFindOneOptions = 
		new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
	private static Document incSeqByOneDocument = 
		new Document("$inc", new Document("seq", 1));
	
	/**
	 * 
	 * @param db
	 * @param collection
	 * @param autoGenId
	 * @return
	 */
	@Override
	public Object autoGen(
		final String db, 
		final String autoGenId)
	{
		var doc = mongoClient.getDatabase(db).getCollection(ID_SEQ_COLLECTION)
				.findOneAndUpdate(
					new Document("id", autoGenId), 
						incSeqByOneDocument, 
							returnNewFindOneOptions);
			
		return doc.get("seq");
	}
	
	/**
	 * 
	 * @param db
	 * @param collection
	 * @param ids
	 * @param variables
	 * @return
	 */
	@Override
	public String insert(
		final String db, 
		final String collection, 
		final Map<String, Object> variables) 
	{
		var doc = new Document(variables);
		
		mongoClient.getDatabase(db).getCollection(collection)
			.insertOne(doc);

		return ((ObjectId)doc.get(ID_NAME)).toHexString();
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param id
	 * @param variables
	 * @return
	 */
	@Override
	public void update(
		final String db, 
		final String collection, 
		final Object id, 
		final Map<String, Object> variables) 
	{
		var updates = buildSetQuery(variables);

		mongoClient.getDatabase(db).getCollection(collection)
			.updateOne(buildIdQuery(toObjectId(id)), updates);
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param filters
	 * @param variables
	 * @param incs
	 */
	@Override
	public void update(
		final String db, 
		final String collection, 
		final Filter filters, 
		final Map<String, Object> variables, 
		final Map<String, Integer> incs) 
	{
		var updates = buildSetQuery(variables);
		if(incs != null)
		{
			var incUpdates = buildIncQuery(incs);
			updates = updates != null? combine(updates, incUpdates): incUpdates;
		}
		
		mongoClient.getDatabase(db).getCollection(collection)
			.updateMany(buildFilters(filters), updates);
		
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param filters
	 * @param variables
	 */
	@Override
	public void update(
		final String db, 
		final String collection, 
		final Filter filters, 
		final Map<String, Object> variables) 
	{
		update(db, collection, filters, variables, null);
	}
	
	private static UpdateOptions upsertTrueUpdateOptions = new UpdateOptions().upsert(true);

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param id
	 * @param variables
	 * @return
	 */
	@Override
	public String upsert(
		final String db, 
		final String collection, 
		final String id, 
		final HashMap<String, Object> variables) 
	{
		var set = buildSetQuery(variables);
		
		var res = mongoClient.getDatabase(db).getCollection(collection)
			.updateOne(buildIdQuery(id), set, upsertTrueUpdateOptions);
		
		return (res.getUpsertedId().asObjectId().getValue()).toHexString();
	}
	
	private Bson buildSetQuery(
		final Map<String, Object> variables)
	{
		if(variables == null)
		{
			return null;
		}
		
		var updates = new ArrayList<Bson>();
		for(var variable : variables.entrySet())
		{
			var key = variable.getKey();
			var value = variable.getValue();
			if(value != null && value instanceof Map<?,?>)
			{
				@SuppressWarnings("unchecked")
				var fields = (Map<String, Object>)value;
				if(fields.size() > 0)
				{
					for(var field: fields.entrySet())
					{
						updates.add(set(String.format("%s.%s", key, field.getKey()), field.getValue()));
					}
				}
				else
				{
					updates.add(set(key, value));
				}
			}
			else
			{
				updates.add(set(key, value));
			}
		}
		
		return combine(updates);
	}
	
	private Bson buildIncQuery(
		final Map<String, Integer> incs)
	{
		if(incs == null)
		{
			return null;
		}
		
		var updates = new ArrayList<Bson>();
		for(var variable : incs.entrySet())
		{
			updates.add(inc(variable.getKey(), variable.getValue()));
		}
		
		return combine(updates);
	}
	
	private Bson buildIdQuery(
		final ObjectId value) 
	{
		return eq(ID_NAME, value);
	}

	private Bson buildIdQuery(
		final String value) 
	{
		return eq(ID_NAME, toObjectId(value));
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param projection
	 * @param references
	 * @param docFilters
	 * @param refFilters
	 * @param sort
	 * @param offset
	 * @param limit
	 * @param aggregates
	 * @return
	 */
	@Override
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
		final List<Bson> aggregates) 
	{
		var col = mongoClient.getDatabase(db).getCollection(collection);
		
		var bsDocFilters = buildFilters(docFilters);
		var bsRefFilters = buildFilters(refFilters);
		
		if(aggregates != null || (references != null && references.size() > 0))
		{
			var pipeline = buildPipeline(
				projection, references, docFilters, refFilters, bsDocFilters, bsRefFilters, aggregates);
			
			if(sort != null)
			{
				pipeline.add(Aggregates.sort(buildSort(sort)));
			}
			
			if(offset > 0)
			{
				pipeline.add(Aggregates.skip(offset));
			}
			if(limit > 0)
			{
				pipeline.add(Aggregates.limit(limit));
			}
			
			var res = new ArrayList<Document>();
			
			var cursor = col
					.aggregate(pipeline)
						.allowDiskUse(true)
							.iterator();
			
			while(cursor.hasNext())
			{
				res.add(cursor.next());
			}
			
			return res;
		}
		else
		{
			var allFilters = bsDocFilters != null? 
					(bsRefFilters == null? bsDocFilters: Filters.and(bsDocFilters, bsRefFilters)): 
					(bsRefFilters != null? bsRefFilters: null);
					
			var find = (allFilters != null? 
							col.find(allFilters): 
							col.find())
					.sort(buildSort(sort));
			
			if(offset > 0)
			{
				find = find.skip(offset);
			}
			if(limit > 0)
			{
				find = find.limit(limit);
			}
					
			return find 
				.projection(projection)
					.into(new ArrayList<Document>());
		}
	}
	
	/**
	 * 
	 * @param db
	 * @param collection
	 * @param projection
	 * @param references
	 * @param docFilters
	 * @param refFilters
	 * @param sort
	 * @param offset
	 * @param limit
	 * @return
	 */
	@Override
	public List<Document> findAll(
		final String db, 
		final String collection, 
		final Document projection, 
		final List<Reference> references, 
		final Filter docFilters, 
		final Filter refFilters, 
		final List<Sort> sort, 
		final int offset, 
		final int limit) 
	{
		return findAll(db, collection, projection, references, docFilters, refFilters, sort, offset, limit, null);
	}
	
	private ArrayList<Bson> buildPipeline(
		final Document preProjection, 
		final List<Reference> references, 
		final Filter docFilters, 
		final Filter refFilters, 
		final Bson bsDocFilters, 
		final Bson bsRefFilters, 
		final List<Bson> aggregates) 
	{
		var pipeline = new ArrayList<Bson>();

		if(preProjection != null)
		{
			pipeline.add(Aggregates.project(preProjection));
		}
		
		if(docFilters != null)
		{
			pipeline.add(Aggregates.match(bsDocFilters));
		}

		if(references != null)
		{
			var cnt = 0;
			for(var ref: references)
			{
				var refPipeline = new ArrayList<Bson>();
				ArrayList<Variable<String>> let = null;
				if(ref.getFilters() == null)
				{
					let = new ArrayList<>();
					var temp = String.format("rf_let_%03d", cnt++);
					let.add(new Variable<String>(temp, "$" + ref.getLocal()));
					refPipeline.add(Aggregates.match(Filters.expr(
						new Document("$eq", Arrays.asList("$$" + temp, "$" + ref.getForeign())))));
				}
				else
				{
					refPipeline.add(Aggregates.match(buildFilters(ref.getFilters())));
				}

				pipeline.add(Aggregates.lookup(ref.getFrom(), let, refPipeline, "rf$" + ref.getAs()));
			}
		}
		
		if(references != null)
		{
			var postProjection = preProjection != null? new Document(preProjection): new Document();
			for(var ref: references)
			{
				if(ref.getType().equals(ReferenceType.single))
				{
					postProjection.append(
						ref.getAs(), 
						new Document("$arrayElemAt", Arrays.asList("$rf$" + ref.getAs(), 0)));
				}
			}

			if(postProjection.size() > (preProjection != null? preProjection.size(): 0))
			{
				pipeline.add(Aggregates.project(postProjection));
			}
		}
		
		if(refFilters != null)
		{
			pipeline.add(Aggregates.match(bsRefFilters));
		}
		
		if(aggregates != null)
		{
			pipeline.addAll(aggregates);
		}
		
		return pipeline;
	}
	
	/**
	 * 
	 * @param db
	 * @param collection
	 * @param projection
	 * @param references
	 * @param docFilters
	 * @param refFilters
	 * @return
	 */
	@Override
	public Document findOne(
		final String db, 
		final String collection, 
		final Document projection, 
		final List<Reference> references, 
		final Filter docFilters, 
		final Filter refFilters) 
	{
		List<Document> res = null;
		
		var col = mongoClient.getDatabase(db).getCollection(collection);
		
		var bsDocFilters = buildFilters(docFilters);
		var bsRefFilters = buildFilters(refFilters);

		if(references != null && references.size() > 0)
		{
			var pipeline = buildPipeline(projection, references, docFilters, refFilters, bsDocFilters, bsRefFilters, null);
			
			res = col
					.aggregate(pipeline)
						.allowDiskUse(true)
							.into(new ArrayList<Document>());
		}
		else
		{
			var allFilters = bsDocFilters != null? 
					(bsRefFilters == null? bsDocFilters: Filters.and(bsDocFilters, bsRefFilters)): 
					(bsRefFilters != null? bsRefFilters: null);
					
			res = (allFilters != null? col.find(allFilters): col.find())
					.projection(projection)
						.into(new ArrayList<Document>());
			
		}

		return res != null && res.size() > 0? res.get(0): null;
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param projection
	 * @param references
	 * @param id
	 * @param refFilters
	 * @return
	 */
	@Override
	public Document findById(
		final String db, 
		final String collection, 
		final Document projection, 
		final List<Reference> references, 		
		final String id,
		final Filter refFilters) 
	{
		return findOne(db, collection, projection, references, 
			new Filter(FilterOperator.eq, ID_NAME, toObjectId(id)), refFilters);
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param projection
	 * @param references
	 * @param id
	 * @return
	 */
	@Override
	public Document findById(
		final String db, 
		final String collection, 
		final Document projection, 
		final List<Reference> references, 		
		final String id) 
	{
		return findById(db, collection, projection, references, id, null);
	}

	private Bson buildSort(
		final List<Sort> sort) 
	{
		var doc = new Document();
		for(var s: sort)
		{
			doc.append(s.getName(), s.isAsc()? 1: -1);
		}
		return doc;
	}
	
	@SuppressWarnings("unchecked")
	private Bson buildFilters(
		final Filter filter) 
	{
		if(filter == null)
		{
			return null;
		}
		
		var op = filter.getOp() != null? filter.getOp(): FilterOperator.eq;
		switch(op)
		{
		case and:
			return Filters.and(buildFilters(filter.getLhs()), buildFilters(filter.getRhs()));
		case or:
			return Filters.or(buildFilters(filter.getLhs()), buildFilters(filter.getRhs()));
		case eq:
			return Filters.eq(filter.getName(), filter.getValue1());
		case ne:
			return Filters.ne(filter.getName(), filter.getValue1());
		case like:
			return Filters.regex(filter.getName(), (String)filter.getValue1() + ".*", "i");
		case in:
			return Filters.in(filter.getName(), (ArrayList<Object>)filter.getValue1());
		case notin:
			return Filters.nin(filter.getName(), (ArrayList<Object>)filter.getValue1());
		case gt:
			return Filters.gt(filter.getName(), filter.getValue1());
		case gte:
			return Filters.gte(filter.getName(), filter.getValue1());
		case lt:
			return Filters.lt(filter.getName(), filter.getValue1());
		case lte:
			return Filters.lte(filter.getName(), filter.getValue1());
		case exists:
			return Filters.exists(filter.getName(), (boolean)filter.getValue1());
		case notexists:
			return Filters.not(Filters.exists(filter.getName(), (boolean)filter.getValue1()));
		case between:
			return Filters.and(Filters.gte(filter.getName(), filter.getValue1()), Filters.lte(filter.getName(), filter.getValue2()));
		case isnull:
			return Filters.eq(filter.getName(), null);
		case notnull:
			return Filters.ne(filter.getName(), null);
		}
		
		return null;
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 */
	@Override
	public void deleteAll(
		final String db, 
		final String collection) 
	{
		mongoClient.getDatabase(db).getCollection(collection)
			.deleteMany(new Document());
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param docFilters
	 */
	@Override
	public void deleteMany(
		final String db, 
		final String collection, 
		final Filter docFilters) 
	{
		mongoClient.getDatabase(db).getCollection(collection)
			.deleteMany(buildFilters(docFilters));
	}

	/**
	 * 
	 * @param pubId
	 * @param name
	 * @param itemId
	 */
	@Override
	public void deleteOne(
		final String db, 
		final String collection, 
		final String id) 
	{
		mongoClient.getDatabase(db).getCollection(collection)
			.deleteOne(buildIdQuery(id));
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param fields
	 */
	@Override
	public void deleteFields(
		final String db, 
		final String collection, 
		final List<String> fields) 
	{
		var updates = new ArrayList<Bson>();
		for(var field : fields)
		{
			updates.add(unset(field));
		}
		
		mongoClient.getDatabase(db).getCollection(collection)
			.updateMany(new Document(), combine(updates));
	}

	/**
	 * 
	 * @param db
	 * @param collection
	 * @param currentName
	 * @param newName
	 */
	@Override
	public void renameField(
		final String db, 
		final String collection, 
		final String currentName, 
		final String newName) 
	{
		var updates = new ArrayList<Bson>();
		updates.add(rename(currentName, newName));
		
		mongoClient.getDatabase(db).getCollection(collection)
			.updateMany(new Document(), combine(updates));
	}

	/**
	 * 
	 * @param db
	 * @param currentName
	 * @param newName
	 */
	@Override
	public void renameCollection(
		final String db, 
		final String currentName, 
		final String newName) 
	{
		mongoClient.getDatabase(db).getCollection(currentName)
			.renameCollection(new MongoNamespace(db, newName));
	}
	
	/**
	 * 
	 * @param db
	 * @param collection
	 * @param column
	 * @param filters
	 * @return
	 */
	@Override
	public Object findMax(
		final String db, 
		final String collection, 
		final String column, 
		final Filter filters) 
	{
		var pipeline = new ArrayList<Bson>();
		if(filters != null)
		{
			pipeline.add(Aggregates.match(buildFilters(filters)));
		}
		pipeline.add(Aggregates.group(null, max("max", "$" + column)));
		var res = mongoClient.getDatabase(db).getCollection(collection)
			.aggregate(pipeline)
				.into(new ArrayList<Document>());
		return res != null && res.size() == 1? res.get(0).get("max"): null;
	}
	
	/**
	 * 
	 * @param db
	 * @param collection
	 * @param column
	 * @param filters
	 * @return
	 */
	@Override
	public Object findMin(
		final String db, 
		final String collection, 
		final String column, 
		final Filter filters) 
	{
		var pipeline = new ArrayList<Bson>();
		if(filters != null)
		{
			pipeline.add(Aggregates.match(buildFilters(filters)));
		}
		pipeline.add(Aggregates.group(null, min("min", "$" + column)));
		var res = mongoClient.getDatabase(db).getCollection(collection)
			.aggregate(pipeline)
				.into(new ArrayList<Document>());
		return res != null && res.size() == 1? res.get(0).get("min"): null;
	}

	/**
	 * 
	 * @param fieldName
	 * @return
	 */
	@Override
	public Bson createUnwindAggregate(
		final String fieldName) 
	{
		return Aggregates.unwind('$' + fieldName);
	}

	/**
	 * 
	 * @param id
	 * @param ops
	 * @return
	 */
	@Override
	public Bson createGroupByAggregate(
		final Map<String, Object> id, 
		final Map<String, Pair<AggregateOperator, Object>> ops) 
	{
		var accumulators = new ArrayList<BsonField>();
		
		for(var entry : ops.entrySet())
		{
			var column = entry.getKey();
			var op = entry.getValue().getKey();
			var value = entry.getValue().getValue();
			
			BsonField acc = null;
			switch(op)
			{
			case count:
				acc = Accumulators.sum(column, 1);
				break;
			case avg:
				acc = Accumulators.avg(column, value);
				break;
			case sum:
				acc = Accumulators.sum(column, value);
				break;
			case min:
				acc = Accumulators.min(column, value);
				break;
			case max:
				acc = Accumulators.max(column, value);
				break;
			case first:
				acc = Accumulators.first(column, value);
				break;
			case last:
				acc = Accumulators.last(column, value);
				break;
			default:
				break;
			}
			
			if(acc != null)
			{
				accumulators.add(acc);
			}
		}
		
		return Aggregates.group(id, accumulators);
	}

	/**
	 * 
	 * @param projection
	 * @return
	 */
	@Override
	public Bson createProjectAggregate(
		final Document projection) 
	{
		return Aggregates.project(projection);
	}

	/**
	 * 
	 * @param sort
	 * @return
	 */
	@Override
	public Bson createSortAggregate(
		final Document sort) 
	{
		return Aggregates.sort(sort);
	}

	/**
	 * 
	 * @param db
	 * @param from
	 * @param to
	 */
	@Override
	public void dupData(
		final String db, 
		final String from, 
		final String to) 
	{
		var target = mongoClient.getDatabase(db).getCollection(to);		
		var cursor = mongoClient.getDatabase(db).getCollection(from).find().iterator();
		try 
		{
			while(cursor.hasNext()) 
			{
				target.insertOne(cursor.next());
			}
		 } 
		 finally 
		 {
			cursor.close();
		 }		
	}
}
