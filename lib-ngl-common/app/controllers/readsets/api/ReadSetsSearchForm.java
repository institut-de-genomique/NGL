package controllers.readsets.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import controllers.NGLControllerHelper;
import models.laboratory.common.description.Level;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.ReadSet;

public class ReadSetsSearchForm extends DBObjectListForm<ReadSet> {
	
	public String typeCode;
	public Set<String> typeCodes;
	
	public Set<String> stateCodes;
	public String stateCode;
	
	public Set<String> submissionStateCodes;
	public String submissionStateCode;
	
	
	public Set<String> projectCodes;
	public String projectCode;
	
	public Set<String> sampleCodes;
	public String sampleCode;
	
	public Set<String> runCodes;
	public String runCode;
	
	public Set<Integer> laneNumbers;
	public Integer laneNumber;
	
	
	public String bioinformaticValidCode;
	public String productionValidCode;
	
	public Date fromEvalDate;
	public Date toEvalDate;
	
	public Set<String> runTypeCodes;
	
	public Set<String> sampleTypeCodes;
	public Set<String> sampleCategoryCodes;
	
	
	public Date fromDate;
	public Date toDate;
	
	public String code;
	public Set<String> codes;
	public Set<String> supportCodes;
	public String regexCode;
	public String regexSampleCode;
	public String regexSupportCode;
    
    public Set<String> instrumentCodes;	
    public Set<String> productionResolutionCodes;
    public Set<String> bioinformaticResolutionCodes;
    
    public String productionValuationUser;
    public String productionValuationCriteriaCode;
    
    public String location;
    
    public String regexArchiveId;
    public Set<String> archiveIds;
    
    
    public String ncbiScientificName;
    public String ncbiScientificNameRegex;
    
    public Set<String> existingFields, notExistingFields;
    
    //public String isSentCCRT, isSentCollaborator;

    public Map<String, List<String>> properties = new HashMap<>();
    public Map<String, List<String>> sampleOnContainerProperties = new HashMap<>();
    public Map<String, Map<String, List<String>>> treatmentProperties = new HashMap<>();
    
    @Override
    public Query getQuery() {
        List<Query> queries = new ArrayList<>();
        Query query = null;

        if (StringUtils.isNotBlank(this.typeCode)) { //all
            queries.add(DBQuery.is("typeCode", this.typeCode));
        }else if(CollectionUtils.isNotEmpty(this.typeCodes)){
            queries.add(DBQuery.in("typeCode", this.typeCodes));
        }

        if (StringUtils.isNotBlank(this.submissionStateCode)) { 
            queries.add(DBQuery.is("submissionState.code", this.submissionStateCode));
        }else if(CollectionUtils.isNotEmpty(this.submissionStateCodes)){
            queries.add(DBQuery.in("submissionStateCode", this.submissionStateCodes));
        }

        if (StringUtils.isNotBlank(this.runCode)) { //all
            queries.add(DBQuery.is("runCode", this.runCode));
        }else if(CollectionUtils.isNotEmpty(this.runCodes)){
            queries.add(DBQuery.in("runCode", this.runCodes));
        }

        if (null != this.laneNumber) { //all
            queries.add(DBQuery.is("laneNumber", this.laneNumber));
        }else if(CollectionUtils.isNotEmpty(this.laneNumbers)){
            queries.add(DBQuery.in("laneNumber", this.laneNumbers));
        }

        if (StringUtils.isNotBlank(this.stateCode)) { //all
            queries.add(DBQuery.is("state.code", this.stateCode));
        }else if (CollectionUtils.isNotEmpty(this.stateCodes)) { //all
            queries.add(DBQuery.in("state.code", this.stateCodes));
        }

        if (StringUtils.isNotBlank(this.productionValidCode)) { //all
            queries.add(DBQuery.is("productionValuation.valid", TBoolean.valueOf(this.productionValidCode)));
        }

        if (StringUtils.isNotBlank(this.bioinformaticValidCode)) { //all
            queries.add(DBQuery.is("bioinformaticValuation.valid", TBoolean.valueOf(this.bioinformaticValidCode)));
        }

        if (CollectionUtils.isNotEmpty(this.projectCodes)) { //all
            queries.add(DBQuery.in("projectCode", this.projectCodes));
        }else if (StringUtils.isNotBlank(this.projectCode)) { //all
            queries.add(DBQuery.is("projectCode", this.projectCode));
        }

        if (CollectionUtils.isNotEmpty(this.sampleCodes)) { //all
            queries.add(DBQuery.in("sampleCode", this.sampleCodes));
        }else if (StringUtils.isNotBlank(this.sampleCode)) { //all
            queries.add(DBQuery.is("sampleCode", this.sampleCode));
        }

        if (CollectionUtils.isNotEmpty(this.runTypeCodes)) { //all
            queries.add(DBQuery.in("runTypeCode", this.runTypeCodes));
        }

        if(null != this.fromDate){
            queries.add(DBQuery.greaterThanEquals("runSequencingStartDate", this.fromDate));
        }

        if(null != this.toDate){
            queries.add(DBQuery.lessThanEquals("runSequencingStartDate", this.toDate));
        }
        
        if(null != this.fromEvalDate && null != this.toEvalDate){
            queries.add(DBQuery.or(
                    DBQuery.and(DBQuery.greaterThanEquals("productionValuation.date", this.fromEvalDate),DBQuery.lessThan("productionValuation.date", (DateUtils.addDays(this.toEvalDate,1)))),
                    DBQuery.and(DBQuery.greaterThanEquals("bioinformaticValuation.date", this.fromEvalDate),DBQuery.lessThan("bioinformaticValuation.date", (DateUtils.addDays(this.toEvalDate,1))))
            ));
        }else if(null != this.fromEvalDate && null == this.toEvalDate){
            queries.add(DBQuery.or(DBQuery.greaterThanEquals("productionValuation.date", this.fromEvalDate),DBQuery.greaterThanEquals("bioinformaticValuation.date", this.fromEvalDate)));
        }else if(null != this.toEvalDate && null == this.fromEvalDate){
            queries.add(DBQuery.or(DBQuery.lessThan("productionValuation.date", (DateUtils.addDays(this.toEvalDate,1))),DBQuery.lessThan("bioinformaticValuation.date", (DateUtils.addDays(this.toEvalDate,1)))));
        }
        
        if(StringUtils.isNotBlank(this.location)){
            queries.add(DBQuery.is("location", this.location));
        }

        if (StringUtils.isNotBlank(this.code)) { //all
            queries.add(DBQuery.is("code", this.code));
        }else if(CollectionUtils.isNotEmpty(this.codes)){
            queries.add(DBQuery.in("code", this.codes));
        }else if (StringUtils.isNotBlank(this.regexCode)) { //all
            queries.add(DBQuery.regex("code", Pattern.compile(this.regexCode)));
        }

        if (StringUtils.isNotBlank(this.regexSampleCode)) { //all
            queries.add(DBQuery.regex("sampleCode", Pattern.compile(this.regexSampleCode)));
        }

        if(CollectionUtils.isNotEmpty(this.supportCodes)){
            queries.add(DBQuery.in("sampleOnContainer.containerSupportCode", this.supportCodes));
        }else if(StringUtils.isNotBlank(this.regexSupportCode)){
            queries.add(DBQuery.regex("sampleOnContainer.containerSupportCode", Pattern.compile(this.regexSupportCode)));
        }
        
        if(StringUtils.isNotBlank(this.ncbiScientificName)){
            queries.add(DBQuery.is("sampleOnContainer.ncbiScientificName", this.ncbiScientificName));
        }else if(StringUtils.isNotBlank(this.ncbiScientificNameRegex)){
            queries.add(DBQuery.regex("sampleOnContainer.ncbiScientificName", Pattern.compile(this.ncbiScientificNameRegex)));
        }
        
        if (CollectionUtils.isNotEmpty(this.instrumentCodes)) { //all
            queries.add(DBQuery.regex("runCode", Pattern.compile(findRegExpFromStringList(this.instrumentCodes))));
        }

        if (CollectionUtils.isNotEmpty(this.productionResolutionCodes)) { //all
            queries.add(DBQuery.in("productionValuation.resolutionCodes", this.productionResolutionCodes));
        }

        if (CollectionUtils.isNotEmpty(this.bioinformaticResolutionCodes)) { //all
            queries.add(DBQuery.in("bioinformaticValuation.resolutionCodes", this.bioinformaticResolutionCodes));
        }

        if(null != this.productionValuationUser){
            queries.add(DBQuery.is("productionValuation.user", this.productionValuationUser));
        }

        if(null != this.productionValuationCriteriaCode){
            queries.add(DBQuery.is("productionValuation.criteriaCode", this.productionValuationCriteriaCode));
        }
        
        if (CollectionUtils.isNotEmpty(this.sampleCategoryCodes)) { //all
            queries.add(DBQuery.in("sampleOnContainer.sampleCategoryCode", this.sampleCategoryCodes));
        }

        if (CollectionUtils.isNotEmpty(this.sampleTypeCodes)) { //all
            queries.add(DBQuery.in("sampleOnContainer.sampleTypeCode", this.sampleTypeCodes));
        }

        if(CollectionUtils.isNotEmpty(this.archiveIds)){
            queries.add(DBQuery.in("archiveId", this.archiveIds));
        }else if(StringUtils.isNotBlank(this.regexArchiveId)){
            queries.add(DBQuery.regex("archiveId", Pattern.compile(this.regexArchiveId)));
        }
        
        // AJ: must be change to used a generic system (see below)
        /*
        if (StringUtils.isNotBlank(this.isSentCCRT)) {
            if (Boolean.valueOf(this.isSentCCRT)) { 
                queries.add(DBQuery.is("properties.isSentCCRT.value", Boolean.valueOf(this.isSentCCRT)));
            }
            else {
                queries.add(DBQuery.notEquals("properties.isSentCCRT.value", !Boolean.valueOf(this.isSentCCRT))); 
            }
        }
        if (StringUtils.isNotBlank(this.isSentCollaborator)) {
            if (Boolean.valueOf(this.isSentCollaborator)) { 
                queries.add(DBQuery.is("properties.isSentCollaborator.value", Boolean.valueOf(this.isSentCollaborator)));
            }
            else {
                queries.add(DBQuery.notEquals("properties.isSentCollaborator.value", !Boolean.valueOf(this.isSentCollaborator))); 
            }
        }
         */
        // END TO-DO

        queries.addAll(NGLControllerHelper.generateQueriesForProperties(this.properties, Level.CODE.ReadSet, "properties"));
        queries.addAll(NGLControllerHelper.generateQueriesForProperties(this.sampleOnContainerProperties, Level.CODE.Content, "sampleOnContainer.properties"));
        queries.addAll(NGLControllerHelper.generateQueriesForTreatmentProperties(this.treatmentProperties, Level.CODE.ReadSet, "treatments"));

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
    
    private String findRegExpFromStringList(Set<String> searchList) {
        String regex = ".*("; 
        for (String itemList : searchList) {
            regex += itemList + "|"; 
        }
        regex = regex.substring(0,regex.length()-1);
        regex +=  ").*";
        return regex;
    }
}
