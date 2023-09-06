package controllers.analyses.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import controllers.NGLControllerHelper;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.Analysis;

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
     
}
