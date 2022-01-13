package com.robotikflow.core.models.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.util.Pair;
import org.springframework.util.ReflectionUtils;

import com.robotikflow.core.models.filters.FilterOperation;
import com.robotikflow.core.models.filters.FilterOperationType;

public class BaseCustomRepository<T, F> 
{
	@Autowired
	@Qualifier("robotikflowEntityManagerFactory")
    protected EntityManager em;
    
    private static Map<FilterOperationType, String> filterOp2StringMap = Map.of(
    		FilterOperationType.EQ, " = ",
    		FilterOperationType.NE, " != ",
    		FilterOperationType.LT, " < ",
    		FilterOperationType.LE, " <= ",
    		FilterOperationType.GT, " > ",
    		FilterOperationType.GE, " >= "
    );
    
    List<T> findAllByQuery(String jpqlQuery, Map<String, Object> params, F filtros, Pageable pageable, Class<T> domainClass, Class<F> filterClass)
    {
		// filtragem
		var filterQuery = new StringBuilder();
		var filterValues = new HashMap<String, Pair<FilterOperationType, Object>>();
		if(filtros != null) 
		{
			@SuppressWarnings("deprecation")
			var tableAlias = QueryUtils.detectAlias(jpqlQuery);
			var alias = tableAlias != null? String.format("%s.", tableAlias): "";
			
			ReflectionUtils.doWithFields(filterClass, field ->
			{
				var op = FilterOperationType.EQ;
				var isCaseInsensitive = false;
				if(field.isAnnotationPresent(FilterOperation.class))
				{
					var filterOpAnnot = field.getAnnotationsByType(FilterOperation.class)[0];
					op = filterOpAnnot.value();
					isCaseInsensitive = filterOpAnnot.isCaseInsensitive();
				}

				String column = null;
				if(field.isAnnotationPresent(Column.class))
				{
					var columnAnnot = field.getAnnotationsByType(Column.class)[0];
					column = columnAnnot.name();
				}
				else
				{
					column = field.getName();
				}
				
				var name = field.getName();
				
				var value = field.get(filtros);
				if(value != null)
				{
					if(filterQuery.length() == 0)
					{
						filterQuery.append(jpqlQuery);
					}
					
					filterQuery.append(" and ");
					
					filterValues.put(name, Pair.of(op, value));
					
					switch(op)
					{
						case EQ:
						case GE:
						case GT:
						case LE:
						case LT:
						case NE:
							if(isCaseInsensitive)
							{
								filterQuery.append("lower(").append(alias).append(column).append(')').append(filterOp2StringMap.get(op)).append("lower(:").append(name).append(')');
							}
							else
							{
								filterQuery.append(alias).append(column).append(filterOp2StringMap.get(op)).append(":").append(name);
							}
							break;
						
						case LIKE:
							if(isCaseInsensitive)
							{
								filterQuery.append("lower(").append(alias).append(column).append(") like lower(:").append(name).append(')');
							}
							else
							{
								filterQuery.append(alias).append(column).append(" like :").append(name);
							}
						
							break;
							
						default:
							break;
					}
				}
				else
				{
					switch(op)
					{
						case IS_NOT_NULL:
							filterQuery.append(alias).append(column).append(" is not null");
							break;
						
						case IS_NULL:
							filterQuery.append(alias).append(column).append(" is null");
							break;
							
						default:
							break;
					}
				}
			});
		}
		
		var filteredQuery = filterQuery.length() > 0? filterQuery.toString(): jpqlQuery; 
		
		// ordenação
		var sortedQuery = QueryUtils.applySorting(filteredQuery, pageable.getSort());
		
		var query = em.createQuery(sortedQuery, domainClass);
		
		// parâmetros
		for(var param : params.entrySet())
		{
			query.setParameter(param.getKey(), param.getValue());
		}
		
		// filtros
		for(var filter: filterValues.entrySet())
		{
			var value = filter.getValue();
			switch(value.getFirst())
			{
				case LIKE:
					query.setParameter(filter.getKey(), String.format("%s%%", value.getSecond()));
					break;
				case IS_NOT_NULL:
				case IS_NULL:
					break;
				default:
					query.setParameter(filter.getKey(), value.getSecond());
					break;
			}
		}
		
		// paginação
		if(pageable.isPaged())
		{
			query.setFirstResult((int)pageable.getOffset())
				.setMaxResults(pageable.getPageSize());
		}
		
		// resultado
		return query.getResultList();    	
    }
}
