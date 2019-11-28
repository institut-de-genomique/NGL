package fr.cea.ig.mongo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;

import controllers.NGLControllerHelper;
import controllers.samples.api.SamplesSearchForm;
import models.laboratory.common.description.Level;

// Try to provide static methods shortcuts to provide declarative support
// for the query form to mongojack query.
// Could possibly extends Optional<DBQuery.Query>. Yet, moving
// from functional to non functional is porbably a lost cause.
public class QueryBuilder { 
	
	// This is some implicit and
	// private List<DBQuery.Query> queryElts = new ArrayList<DBQuery.Query>();
	// Use non static, non fluent stuff.
	
	private DBQuery.Query query;
	
	public QueryBuilder(DBQuery.Query query) {
		this.query = query;
	}
	
	public DBQuery.Query query() {
		return query;
	}
	// public void qadd(DBQuery.Query q) {	queryElts.add(q); }
	// public void and(QueryBuilder q) { qadd(q); }
	
	/*public void firstOf(QueryBuilder... qbs) {
		for (QueryBuilder b : qbs)
			if (b.isEffective()) {
				//queryElts.add(b);
				return;
			}
	}*/
	
	public static Optional<QueryBuilder> in(String key, Collection<String> strs) {
		if (CollectionUtils.isNotEmpty(strs))
			return Optional.of(new QueryBuilder(DBQuery.in(key,strs)));
		return Optional.empty();
	}
	// Seems dubious, should be equals, not in
	public static Optional<QueryBuilder> in(String key, String str) {
		if (StringUtils.isNotBlank(str))
			return Optional.of(new QueryBuilder(DBQuery.in(key,str)));
		return Optional.empty();
	}
	public static Optional<QueryBuilder> is(String key, String str) {
		if(StringUtils.isNotBlank(str))
			return Optional.of(new QueryBuilder(DBQuery.is(key,str)));
		return Optional.empty();
	}
	public static Optional<QueryBuilder> regex(String key, String regex) {
		if (StringUtils.isNotBlank(regex))
			return Optional.of(new QueryBuilder(DBQuery.regex(key, Pattern.compile(regex))));
		return Optional.empty();
	}
	public static Optional<QueryBuilder> greaterThanEquals(String key, Object value) {
		if (value != null)
			return Optional.of(new QueryBuilder(DBQuery.greaterThanEquals(key, value)));
		return Optional.empty();
	}
	public static Optional<QueryBuilder> lessThan(String key, Object value) {
		if (value != null)
			return Optional.of(new QueryBuilder(DBQuery.lessThan(key, value)));
		return Optional.empty();
	}
	public static Optional<QueryBuilder> notEquals(String key, Object value) {
		if (value != null)
			return Optional.of(new QueryBuilder(DBQuery.notEquals(key, value)));
		return Optional.empty();
	}
	public static Optional<QueryBuilder> elemMatch(String key, Optional<QueryBuilder> q) {
		return q.map(r -> new QueryBuilder(DBQuery.elemMatch("comments",r.query())));
	}
	public static Optional<QueryBuilder> exists(String key) {
		if (key != null)
			return Optional.of(new QueryBuilder(DBQuery.exists(key)));
		return Optional.empty();
	}
	public static Optional<QueryBuilder> notExists(String key) {
		if (key != null)
			return Optional.of(new QueryBuilder(DBQuery.notExists(key)));
		return Optional.empty();
	}
	public static QueryBuilder and(QueryBuilder a, QueryBuilder b) {
		return new QueryBuilder(DBQuery.and(a.query(),b.query()));
	}
	@SafeVarargs
	public static Optional<QueryBuilder> firstOf(Optional<QueryBuilder>... bs) {
		for (Optional<QueryBuilder> b : bs) 
			if (b.isPresent())
				return b;
		return Optional.empty();
	}
	public static Optional<QueryBuilder> and(Optional<QueryBuilder> q0, Optional<QueryBuilder> q1) {
		if (q0.isPresent()) {
			if (q1.isPresent()) {
				return Optional.of(QueryBuilder.and(q0.get(),q1.get()));
			} else {
				return q0;
			}
		} else {
			if (q1.isPresent())
				return q1;
			else 
				return q0; // empty, could be q1
		}
		
	}
	
	public static DBQuery.Query query(Optional<QueryBuilder> b) {
		return b.map(x -> x.query()).orElse(DBQuery.empty());
	}
	
	// Pointless functional fun, plain iteration on the entry set
	// would be simpler and faster.
	public static Optional<QueryBuilder> generateQueriesForExistingProperties(Optional<QueryBuilder> b, Map<String, Boolean> existingFields) {
		return existingFields.entrySet().stream()
		.map(e -> {
			if (e.getValue().booleanValue()) 
				return exists(e.getKey());
			else 
				return notExists(e.getKey());
		}).reduce(b,(q0,q1) -> and(q0,q1));
	}
	
	public static DBQuery.Query getQuery(SamplesSearchForm samplesSearch) {
		
		Optional<QueryBuilder> qb = Optional.empty();
		
		List<DBQuery.Query> queryElts = new ArrayList<>();
		
		qb = and(qb,firstOf(in   ("code", samplesSearch.codes),
							is   ("code", samplesSearch.code),
							regex("code", samplesSearch.codeRegex)));
		qb = and(qb,in("typeCode", samplesSearch.typeCodes));
		qb = and(qb,regex("referenceCollab",samplesSearch.referenceCollabRegex));
		qb = and(qb,in("projectCodes", samplesSearch.projectCode));
		qb = and(qb,in("projectCodes", samplesSearch.projectCodes));
		qb = and(qb,regex("life.path",samplesSearch.treeOfLifePathRegex));
		qb = and(qb,greaterThanEquals("traceInformation.creationDate", samplesSearch.fromDate));
		qb = and(qb,lessThan("traceInformation.creationDate",samplesSearch.toDate));
		qb = and(qb,
				firstOf(in("traceInformation.createUser", samplesSearch.createUsers),
			 		    is("traceInformation.createUser", samplesSearch.createUser)));
		qb = and(qb,elemMatch("comments",regex("comment",samplesSearch.commentRegex)));
		qb = and(qb,is("taxonCode", samplesSearch.taxonCode));
		qb = and(qb,regex("ncbiScientificName",samplesSearch.ncbiScientificNameRegex));
		
		if(StringUtils.isNotBlank(samplesSearch.existingProcessTypeCode)
				&& StringUtils.isNotBlank(samplesSearch.existingTransformationTypeCode)
				&& StringUtils.isNotBlank(samplesSearch.notExistingTransformationTypeCode)){
			queryElts.add(DBQuery.elemMatch("processes", DBQuery.is("typeCode",samplesSearch.existingProcessTypeCode)
					      .and(DBQuery.is("experiments.typeCode",samplesSearch.existingTransformationTypeCode),
					    	   DBQuery.notEquals("experiments.typeCode",samplesSearch.notExistingTransformationTypeCode))));
		
		}else if(StringUtils.isNotBlank(samplesSearch.existingTransformationTypeCode)
				&& StringUtils.isNotBlank(samplesSearch.notExistingTransformationTypeCode)){
			queryElts.add(DBQuery.and(DBQuery.is("processes.experiments.typeCode",samplesSearch.existingTransformationTypeCode)
					,DBQuery.notEquals("processes.experiments.typeCode",samplesSearch.notExistingTransformationTypeCode)));		
		
		}else if(StringUtils.isNotBlank(samplesSearch.existingProcessTypeCode)
				&& StringUtils.isNotBlank(samplesSearch.existingTransformationTypeCode)){
			queryElts.add(DBQuery.elemMatch("processes", DBQuery.is("typeCode",samplesSearch.existingProcessTypeCode).is("experiments.typeCode",samplesSearch.existingTransformationTypeCode)));		
					
		}else if(StringUtils.isNotBlank(samplesSearch.existingProcessTypeCode)
				&& StringUtils.isNotBlank(samplesSearch.notExistingTransformationTypeCode)){
			queryElts.add(DBQuery.elemMatch("processes", DBQuery.is("typeCode",samplesSearch.existingProcessTypeCode).notEquals("experiments.typeCode",samplesSearch.notExistingTransformationTypeCode)));		
		
		}else if(StringUtils.isNotBlank(samplesSearch.existingProcessTypeCode)){
			queryElts.add(DBQuery.is("processes.typeCode",samplesSearch.existingProcessTypeCode));
		
		}else if(StringUtils.isNotBlank(samplesSearch.notExistingProcessTypeCode)){
			queryElts.add(DBQuery.notEquals("processes.typeCode",samplesSearch.notExistingProcessTypeCode));
		
		}else if(StringUtils.isNotBlank(samplesSearch.existingTransformationTypeCode)){
			queryElts.add(DBQuery.is("processes.experiments.typeCode",samplesSearch.existingTransformationTypeCode));
		
		}else if(StringUtils.isNotBlank(samplesSearch.notExistingTransformationTypeCode)){
			queryElts.add(DBQuery.notEquals("processes.experiments.typeCode",samplesSearch.notExistingTransformationTypeCode));
		
		}
		
		qb = and(qb,in("processes.experiments.protocolCode",samplesSearch.experimentProtocolCodes));
		
		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(samplesSearch.properties,Level.CODE.Sample, "properties"));
		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(samplesSearch.experimentProperties,Level.CODE.Experiment, "processes.experiments.properties"));

		qb = generateQueriesForExistingProperties(qb,samplesSearch.existingFields);
		
		return query(qb); 
	}
	
}

