package controllers.analyses.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.fasterxml.jackson.annotation.JsonIgnore;

import akka.stream.javadsl.Source;
import akka.util.ByteString;
import controllers.DBObjectListForm;
import controllers.NGLControllerHelper;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.Analysis;
import models.laboratory.sample.instance.Sample;
import models.utils.InstanceConstants;

public class AnalysesSearchForm extends DBObjectListForm<Analysis> {
	
	public List<String> stateCodes;
	public String stateCode;
	
	public List<String> projectCodes;
	public String projectCode;
	
	public List<String> sampleCodes;
	public String sampleCode;
	
	
	public String validCode;
	public List<String> resolutionCodes;
	public List<String> typeCodes;
	public String typeCode;
	
	public List<String> regexCodes;
	public String regexCode;
	public String regexSampleCode;
	public String regexReadSetCode;
	
	public String analyseValuationUser;
	
	public Date fromDate;
	public Date toDate;
	
	public List<String> readSetCodes;
	public String readSetCode;

    public List<String> umbrellaCodes;
    
	public String ncbiScientificNameRegex;
	public List<String> ncbiScientificNames;
	
	public String collabScientificNameRegex;
	public List<String> collabScientificNames;
	
	public List<String> existingFields, notExistingFields;
   
    public Map<String, List<String>> properties = new HashMap<>();
    public Map<String, Map<String, List<String>>> treatmentProperties = new HashMap<>();
    
    @Override
    public Query getQuery() {
        List<Query> queries = new ArrayList<>();
        Query query = null;
        
        if (StringUtils.isNotBlank(this.stateCode)) { //all
            queries.add(DBQuery.is("state.code", this.stateCode));
        } else if (CollectionUtils.isNotEmpty(this.stateCodes)) { //all
            queries.add(DBQuery.in("state.code", this.stateCodes));
        }
        
        if (StringUtils.isNotBlank(this.validCode)) { //all
            queries.add(DBQuery.is("valuation.valid", TBoolean.valueOf(this.validCode)));
        }
        if (CollectionUtils.isNotEmpty(this.resolutionCodes)) { //all
            queries.add(DBQuery.in("valuation.resolutionCodes", this.resolutionCodes));
        }
        
        if (CollectionUtils.isNotEmpty(this.projectCodes)) { //all
            queries.add(DBQuery.in("projectCodes", this.projectCodes));
        } else if (StringUtils.isNotBlank(this.projectCode)) { //all
            queries.add(DBQuery.in("projectCodes", this.projectCode));
        }
        
        if (CollectionUtils.isNotEmpty(this.sampleCodes)) { //all
            queries.add(DBQuery.in("sampleCodes", this.sampleCodes));
        } else if (StringUtils.isNotBlank(this.sampleCode)) { //all
            queries.add(DBQuery.in("sampleCodes", this.sampleCode));
        }
        
        if (CollectionUtils.isNotEmpty(this.typeCodes)) { //all
            queries.add(DBQuery.in("typeCode", this.typeCodes));
        }else if(StringUtils.isNotBlank(this.typeCode)) {
        	queries.add(DBQuery.is("typeCode", this.typeCode));
        }
               
        if(CollectionUtils.isNotEmpty(this.regexCodes)) {
        	queries.add(DBQuery.regex("code", Pattern.compile(StringUtils.join(this.regexCodes, "|"))));
        } else if (StringUtils.isNotBlank(this.regexCode)) { //all
            queries.add(DBQuery.regex("code", Pattern.compile(this.regexCode)));
        }
        
        if (StringUtils.isNotBlank(this.analyseValuationUser)) {
            queries.add(DBQuery.is("valuation.user", this.analyseValuationUser));
        }
        
        if(null != this.fromDate){
            queries.add(DBQuery.greaterThanEquals("traceInformation.creationDate", this.fromDate));
        }

        if(null != this.toDate){
            queries.add(DBQuery.lessThanEquals("traceInformation.creationDate", this.toDate));
        }
        
        if(CollectionUtils.isNotEmpty(this.readSetCodes)) {
        	queries.add(DBQuery.in("readSetCodes", this.readSetCodes));
        } else if(StringUtils.isNotBlank(this.readSetCode)) {
        	queries.add(DBQuery.in("readSetCodes", this.readSetCode));
        } else if(StringUtils.isNotBlank(this.regexReadSetCode)) {
        	queries.add(DBQuery.regex("readSetCodes", Pattern.compile(this.regexReadSetCode)));
        }
        
        queries.addAll(NGLControllerHelper.generateQueriesForProperties(this.properties, Level.CODE.Analysis, "properties"));
        queries.addAll(NGLControllerHelper.generateQueriesForTreatmentProperties(this.treatmentProperties, Level.CODE.Analysis, "treatments"));
        
        if (CollectionUtils.isNotEmpty(this.existingFields)) { //all
            for(String field : this.existingFields){
                queries.add(DBQuery.exists(field));
            }       
        }
        
        if (CollectionUtils.isNotEmpty(this.notExistingFields)) { //all
            for(String field : this.notExistingFields){
                queries.add(DBQuery.notExists(field));
            }
        }
        
        if (queries.size() > 0) {
            query = DBQuery.and(queries.toArray(new Query[queries.size()]));
        }
        
        return query;
    }
    
    private List<Query> getSampleAttributesFilters() {
    	List<Query> sampleFilters = new ArrayList<>(2);
    	if(CollectionUtils.isNotEmpty(this.ncbiScientificNames)) {
    		sampleFilters.add(DBQuery.in("ncbiScientificName", this.ncbiScientificNames));
    	} else if(StringUtils.isNotEmpty(this.ncbiScientificNameRegex)) {
    		sampleFilters.add(DBQuery.regex("ncbiScientificName", Pattern.compile(this.ncbiScientificNameRegex)));
    	}
		if(CollectionUtils.isNotEmpty(this.collabScientificNames)) {
    		sampleFilters.add(DBQuery.in("properties.collabScientificName.value", this.collabScientificNames));
    	} else if(StringUtils.isNotEmpty(this.collabScientificNameRegex)) {
			sampleFilters.add( DBQuery.regex("properties.collabScientificName.value", Pattern.compile(this.collabScientificNameRegex, Pattern.CASE_INSENSITIVE)));
		}
		return sampleFilters;
    }
    
    private Function<Analysis, Query> getIteratorQueryBuilder() {
    	final List<Query> filters = this.getSampleAttributesFilters();
    	switch(filters.size()) {
    	case 1 : return (Analysis analysis) -> DBQuery.and(DBQuery.in("code", analysis.sampleCodes), filters.get(0));
    	case 2 : return (Analysis analysis) -> DBQuery.and(DBQuery.in("code", analysis.sampleCodes), filters.get(0), filters.get(1));
    	default : throw new IllegalStateException("Invalid number of filters for sample attributes : " + String.valueOf(filters.size()));
    	}
    }
    
    private Predicate<? super Analysis> validateAnalysisSamples(Function<Analysis, Query> iteratorQueryBuilder) {
    	return (Analysis analysis) -> MongoDBDAO.getCount(InstanceConstants.SAMPLE_COLL_NAME, Sample.class, iteratorQueryBuilder.apply(analysis)) > 0;
    }
    
    private Iterator<Analysis> getFilteredIteratorFrom(Iterator<Analysis> iterator, Predicate<? super Analysis> filter) {
    	return new Iterator<Analysis>() {
			
			private Analysis nextAnalysis = findNextValidAnalysis();
			
			private boolean isValid(Analysis analysis) {
				return CollectionUtils.isNotEmpty(analysis.sampleCodes) && filter.test(analysis);
			}
			
			private Analysis findNextValidAnalysis() {
				if(!iterator.hasNext()) return null;
				Analysis next = iterator.next();
				while(next != null && !isValid(next)) {
					next = iterator.hasNext() ? iterator.next() : null;
				}
				return next;
			}

			@Override
			public boolean hasNext() {
				return this.nextAnalysis != null;
			}

			@Override
			public Analysis next() {
				Analysis analysisToReturn = nextAnalysis;
				this.nextAnalysis = findNextValidAnalysis();
				return analysisToReturn;
			}
			
		};
    }
    
    private Iterator<Analysis> getFilteredIteratorFrom(Iterable<Analysis> iterable) {
    	Iterator<Analysis> iterator = iterable.iterator();
		Function<Analysis, Query> queryBuilder = getIteratorQueryBuilder();
		Predicate<? super Analysis> filter = validateAnalysisSamples(queryBuilder);
		return this.getFilteredIteratorFrom(iterator, filter);
    }
    
    private Function<Iterable<Analysis>, Source<ByteString, ?>> getFilteredTransform(Function<Iterable<Analysis>, Source<ByteString, ?>> transform) {
    	return (Iterable<Analysis> iterable) -> transform.apply(() -> this.getFilteredIteratorFrom(iterable));
    }
    
    private boolean needFilteringBySample() {
    	return StringUtils.isNotEmpty(this.ncbiScientificNameRegex) || StringUtils.isNotEmpty(this.collabScientificNameRegex) || CollectionUtils.isNotEmpty(this.ncbiScientificNames) || CollectionUtils.isNotEmpty(this.collabScientificNames);
    }
    
    @Override
    @JsonIgnore
	public Function<Iterable<Analysis>, Source<ByteString, ?>> transform() {
    	return needFilteringBySample() ? getFilteredTransform(super.transform()) : super.transform();
	}
     
}
