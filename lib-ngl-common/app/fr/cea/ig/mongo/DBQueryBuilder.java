package fr.cea.ig.mongo;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.NGLControllerHelper;
import models.laboratory.common.description.Level;

// Should be a different implementation of the QueryBuilder with methods
// that are compatible signature wise (just change import static and 
// this behaves the same).
// We could use null instead of DBQuery.empty() but the effectiveness depends on the 
// DBQuery optimization level.
public class DBQueryBuilder {

	// The minimal implementation would use DBQuery.empty() and hope
	// for the best. We need things like first that need to know if the
	// query exists or not.
	
	// Eagerness builds the query.
	public static Query cond(boolean b, Query q) {
		if (b)
			return q;
		return null;
	}
	
	public static Query in(String key, Collection<String> strs) {
		if (CollectionUtils.isNotEmpty(strs))
			return DBQuery.in(key,strs);
		return null;
	}

	public static Query is(String key, String value) {
		if (StringUtils.isNotBlank(value))
			return DBQuery.is(key,value);
		return null;
	}
	
	public static Query regex(String key, String regex) {
		if (StringUtils.isNotBlank(regex))
			return DBQuery.regex(key, Pattern.compile(regex));
		return null;
	}
	
	public static Query greaterThanEquals(String key, Object value) {
		if (value != null)
			return DBQuery.greaterThanEquals(key, value);
		return null;
	}

	public static Query lessThan(String key, Object value) {
		if (value != null)
			return DBQuery.lessThan(key, value);
		return null;
	}
	
	public static Query notEquals(String key, Object value) {
		if (value != null)
			return DBQuery.notEquals(key, value);
		return null;
	}

	public static Query elemMatch(String key, Query q) {
		if (q == null)
			return null;
		return DBQuery.elemMatch("comments",q);
	}
	// Alias
	public static Query exists(String key) {
		return DBQuery.exists(key);
	}
	// Alias
	public static Query notExists(String key) {
		return DBQuery.notExists(key);
	}
	public static int count(Query... queries) {
		int count = 0;
		for (int i=0; i<queries.length; i++)
			if (queries[i] != null)
				count ++;
		return count;
	}
	// does not check if there are enough non null queries to
	// take the requested count.
	public static Query[] takeUnsafe(int count, Query... queries) {
		Query[] r = new Query[count];
		int j  = 0;
		for (int i=0; i<queries.length; i++)
			if (queries[i] != null)
				r[j++] = queries[i];
		return r;		
	}
	
	public static Query[] take(int count, Query... queries) {
		return takeUnsafe(Math.min(count, count(queries)));
	}
	
	public static Query and(Query... queries) {
		int count = count(queries);
		switch (queries.length) {
		case 0 : return null;
		case 1 : return first(queries);
		default: return DBQuery.and(takeUnsafe(count,queries));
		}
	}
	
	public static Query and(Collection<Query> queries) {
		return and(queries.toArray(new Query[queries.size()]));
	}
	
	public static Query first(Query... queries) {
		for (int i=0;i<queries.length; i++)
			if (queries[i] != null)
				return queries[i];
		return null;
	}
	
	public static Query query(Query q) {
		if (q == null)
			return DBQuery.empty();
		return q;
	}
	public static Query generateQueriesForProperties(Map<String, List<String>> properties, Level.CODE level, String prefixPropertyPath) {
		List<Query> qs = NGLControllerHelper.generateQueriesForProperties(properties,level,prefixPropertyPath);
		return and(qs.toArray(new Query[qs.size()]));
	}
	public static Query generateQueriesForExistingProperties(Map<String, Boolean> existingFields) {
		Query[] qs = new Query[existingFields.size()];
		int i = 0;
		for (Map.Entry<String, Boolean> e : existingFields.entrySet()) {
			if (e.getValue().booleanValue()) 
				qs[i++] = exists(e.getKey());
			else 
				qs[i++] = notExists(e.getKey());
		}
		return and(qs);
	}

	
	public static Date addDays(Date d, int count) {
		if (d == null)
			return null;
		return DateUtils.addDays(d, count);
	}
}
