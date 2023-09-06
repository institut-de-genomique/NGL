package controllers.runs.api;

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
import fr.cea.ig.lfw.utils.DateConverter;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.Run;

public class RunsSearchForm extends DBObjectListForm<Run> {
	
    public List<String> codes;
    public String code;
    public String regexCode;
    
    public List<String> categoryCodes;
    public String categoryCode;
    
    public List<String> stateCodes;
    public String stateCode;

    public List<String> typeCodes;

    public List<String> projectCodes;
    public String projectCode;
    
    public List<String> sampleCodes;
    public String sampleCode;
    
    public String containerSupportCode;
    public List<String> containerSupportCodes;
    
    public String validCode;

    public Date fromDate;
    public Date toDate;
    
    public Date fromEndRGDate;
    public Date toEndRGDate;
    
    
    public String valuationUser;
    public String valuationCriteriaCode;
    
    public Boolean keep;
    
    public List<String> instrumentCodes;
    public List<String> runResolutionCodes;
    public List<String> laneResolutionCodes;
    public List<String> resolutionCodes;
    
    public List<String> existingFields, notExistingFields;
    
    public Map<String, List<String>> properties = new HashMap<>();
    public Map<String, Map<String, List<String>>> treatmentProperties = new HashMap<>();
    public Map<String, Map<String, List<String>>> treatmentLanesProperties = new HashMap<>();
    
    @Override
    public Query getQuery() {
        List<Query> queries = new ArrayList<>();
        Query query = null;
        
        if(CollectionUtils.isNotEmpty(this.codes)){
            queries.add(DBQuery.in("code", this.codes));
        }else if(StringUtils.isNotBlank(this.code)){
            queries.add(DBQuery.is("code", this.code));
        }else if (StringUtils.isNotBlank(this.regexCode)) { //all
            queries.add(DBQuery.regex("code", Pattern.compile(this.regexCode)));
        }
        
        if(CollectionUtils.isNotEmpty(this.categoryCodes)){
            queries.add(DBQuery.in("categoryCode", this.categoryCodes));
        }else if(StringUtils.isNotBlank(this.categoryCode)){
            queries.add(DBQuery.is("categoryCode", this.categoryCode));
        }
        
        
        if (StringUtils.isNotBlank(this.stateCode)) { //all
            queries.add(DBQuery.is("state.code", this.stateCode));
        }else if (CollectionUtils.isNotEmpty(this.stateCodes)) { //all
            queries.add(DBQuery.in("state.code", this.stateCodes));
        }
        
        if (StringUtils.isNotBlank(this.validCode)) { //all
            queries.add(DBQuery.is("valuation.valid", TBoolean.valueOf(this.validCode)));
        }

        if (CollectionUtils.isNotEmpty(this.projectCodes)) { //all
            queries.add(DBQuery.in("projectCodes", this.projectCodes));
        }else if(StringUtils.isNotBlank(this.projectCode)){
            queries.add(DBQuery.is("projectCodes", this.projectCode));
        }
        
        if (CollectionUtils.isNotEmpty(this.sampleCodes)) { //all
            queries.add(DBQuery.in("sampleCodes", this.sampleCodes));
        }else if(StringUtils.isNotBlank(this.sampleCode)){
            queries.add(DBQuery.in("sampleCodes", this.sampleCode));
        }
        
        if (CollectionUtils.isNotEmpty(this.typeCodes)) { //all
            queries.add(DBQuery.in("typeCode", this.typeCodes));
        }
        
        if(CollectionUtils.isNotEmpty(this.containerSupportCodes)){
            queries.add(DBQuery.in("containerSupportCode", this.containerSupportCodes));
        }else if(StringUtils.isNotBlank(this.containerSupportCode)){
            queries.add(DBQuery.is("containerSupportCode", this.containerSupportCode));
        }
        
        if(null != this.keep){
            queries.add(DBQuery.is("keep", this.keep));
        }
            
        if(null != this.fromDate){
            queries.add(DBQuery.greaterThanEquals("sequencingStartDate", DateConverter.getFromDate(this.fromDate).getTime()));
        }
        
        if(null != this.toDate){
            queries.add(DBQuery.lessThanEquals("sequencingStartDate", DateConverter.getToDate(this.toDate).getTime()));
        }
        
        if(null != this.fromEndRGDate){
            DBQuery.Query fromEndRG = DBQuery.elemMatch("state.historical", 
                    DBQuery.is("code", "F-RG").greaterThanEquals("date", DateConverter.getFromDate(this.fromEndRGDate).getTime()));           
            queries.add(fromEndRG);
        }
        
        if(null != this.toEndRGDate){
            DBQuery.Query toEndRG = DBQuery.elemMatch("state.historical", 
                    DBQuery.is("code", "F-RG").lessThanEquals("date", DateConverter.getToDate(this.toEndRGDate).getTime()));
            
            queries.add(toEndRG);
        }
        
        
        if (CollectionUtils.isNotEmpty(this.instrumentCodes)) { //all
            queries.add(DBQuery.in("instrumentUsed.code", this.instrumentCodes));
        }
        
        if (CollectionUtils.isNotEmpty(this.runResolutionCodes)) { //all
            queries.add(DBQuery.in("valuation.resolutionCodes", this.runResolutionCodes));
        }
        
        if (CollectionUtils.isNotEmpty(this.laneResolutionCodes)) { //all
            queries.add(DBQuery.in("lanes.valuation.resolutionCodes", this.laneResolutionCodes));
        }
        
        if (CollectionUtils.isNotEmpty(this.resolutionCodes)) { //all
            queries.add(DBQuery.or(DBQuery.in("valuation.resolutionCodes", this.resolutionCodes), 
                    DBQuery.in("lanes.valuation.resolutionCodes", this.resolutionCodes)));          
        }
        
        if(null != this.valuationUser){
            queries.add(DBQuery.is("valuation.user", this.valuationUser));
        }
        
        if(null != this.valuationCriteriaCode){
            queries.add(DBQuery.is("valuation.criteriaCode", this.valuationCriteriaCode));
        }
        
        queries.addAll(NGLControllerHelper.generateQueriesForProperties(this.properties, Level.CODE.Run, "properties"));
        queries.addAll(NGLControllerHelper.generateQueriesForTreatmentProperties(this.treatmentProperties, Level.CODE.Run, "treatments"));
        queries.addAll(NGLControllerHelper.generateQueriesForTreatmentProperties(this.treatmentLanesProperties, Level.CODE.Lane, "lanes.treatments"));
        
        
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
        
        if(queries.size() > 0){
            query = DBQuery.and(queries.toArray(new Query[queries.size()]));
        }

        return query;
    }
}
