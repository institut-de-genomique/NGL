package controllers.samples.api;

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

import com.fasterxml.jackson.annotation.JsonIgnore;

import controllers.DBObjectListForm;
import controllers.NGLControllerHelper;
import models.laboratory.common.description.Level;
import models.laboratory.sample.instance.Sample;

public class SamplesSearchForm extends DBObjectListForm<Sample> {

	public String code; 
	public String codeRegex;
	public String treeOfLifePathRegex;
	public List<String> lifeFromProjectCodes;
	public List<String> lifeFromSampleCodes;
	public List<String> lifeFromSampleTypeCodes;
	public Set<String> codes;
	public String projectCode;
	public List<String> projectCodes;
	public List<String> typeCodes;
	public Date fromDate;
	public Date toDate;
	public String createUser; 
	public List<String> createUsers;
	public String commentRegex;
	public Map<String, List<String>> properties = new HashMap<>();
	public Map<String, Boolean> existingFields;
	public String referenceCollabRegex;
	public List<String> referenceCollabs;
	public List<String> umbrellaCodes;

	public String importTypeCode; 
	public Set<String> importTypeCodes;
	
	public String existingTransformationTypeCode;
	public String notExistingTransformationTypeCode;
	public String existingProcessTypeCode;
	public String notExistingProcessTypeCode;

	public List<String> experimentProtocolCodes;
	public String ncbiScientificNameRegex;
	public List<String> ncbiScientificNames;

	public String taxonCode;
	public List<String> taxonCodes;
	
	public String lastrResolutionCode;
	public List<String> lastResolutionCodes;
	
	public List<String> resolutionCodes;
	public Date fromResolutionDate;
	public Date toResolutionDate;
	public List<String> resolutionUsers;

	public Map<String, List<String>> experimentProperties = new HashMap<>();

	@Override
	public String toString() {
		return "SamplesSearchForm [projectCode=" + projectCode
				+ ", projectCodes=" + projectCodes
				+ ", sampleCode=" + ", code=" + code
				+ ", typeCodes"
				+ ", createUser=" + createUser 
				+ ", fromDate=" + fromDate
				+ ", toDate=" + toDate
				+ "]";
	}

	@Override
	@JsonIgnore
	public DBQuery.Query getQuery() {
		List<DBQuery.Query> queryElts = new ArrayList<>();

		if(CollectionUtils.isNotEmpty(codes)){
			queryElts.add(DBQuery.in("code", codes));
		} else if(StringUtils.isNotBlank(code)){
			queryElts.add(DBQuery.is("code", code));
		} else if(StringUtils.isNotBlank(codeRegex)){
			queryElts.add(DBQuery.regex("code", Pattern.compile(codeRegex)));
		}

		if(CollectionUtils.isNotEmpty(typeCodes)){
			queryElts.add(DBQuery.in("typeCode", typeCodes));
		}

		if(CollectionUtils.isNotEmpty(this.importTypeCodes)){
			queryElts.add(DBQuery.in("importTypeCode", this.importTypeCodes));
		}else if(StringUtils.isNotBlank(this.importTypeCode)){
			queryElts.add(DBQuery.is("importTypeCode", this.importTypeCode));
		}

		if(StringUtils.isNotBlank(referenceCollabRegex)){
			queryElts.add(DBQuery.regex("referenceCollab", Pattern.compile(referenceCollabRegex,Pattern.CASE_INSENSITIVE)));
		}
		if(CollectionUtils.isNotEmpty(referenceCollabs)){
			queryElts.add(DBQuery.in("referenceCollab", referenceCollabs));
		}

		if(StringUtils.isNotBlank(projectCode)){
			queryElts.add(DBQuery.in("projectCodes", projectCode));
		}

		if(CollectionUtils.isNotEmpty(projectCodes)){ 				//projectCodes != null && projectCodes.size() > 0
			queryElts.add(DBQuery.in("projectCodes", projectCodes));
		}

		if(StringUtils.isNotBlank(treeOfLifePathRegex)){
			queryElts.add(DBQuery.regex("life.path", Pattern.compile(treeOfLifePathRegex)));
		}
		if(CollectionUtils.isNotEmpty(lifeFromProjectCodes)){
			queryElts.add(DBQuery.in("life.from.projectCode", lifeFromProjectCodes));
		}
		if(CollectionUtils.isNotEmpty(lifeFromSampleCodes)){
			queryElts.add(DBQuery.in("life.from.sampleCode", lifeFromSampleCodes));
		}
		if(CollectionUtils.isNotEmpty(lifeFromSampleTypeCodes)){
			queryElts.add(DBQuery.in("life.from.sampleTypeCode", lifeFromSampleTypeCodes));
		}
//
//		Query query = DBQuery.empty();
//		
//		if(queryElts.size() > 0){
//			query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
//		}


		if(null != fromDate){
			queryElts.add(DBQuery.greaterThanEquals("traceInformation.creationDate", fromDate));
		}

		if(null != toDate){
			queryElts.add(DBQuery.lessThan("traceInformation.creationDate", (DateUtils.addDays(toDate, 1))));
		}

		if(CollectionUtils.isNotEmpty(createUsers)){
			queryElts.add(DBQuery.in("traceInformation.createUser", createUsers));
		}else if(StringUtils.isNotBlank(createUser)){
			queryElts.add(DBQuery.is("traceInformation.createUser", createUser));
		}

		if(StringUtils.isNotBlank(commentRegex)){
			queryElts.add(DBQuery.elemMatch("comments", DBQuery.regex("comment", Pattern.compile(commentRegex))));
		}

		if(StringUtils.isNotBlank(taxonCode)){
			queryElts.add(DBQuery.is("taxonCode", taxonCode));
		}
		if(CollectionUtils.isNotEmpty(taxonCodes)){
			queryElts.add(DBQuery.in("taxonCode", taxonCodes));
		}
		
		if(StringUtils.isNotBlank(lastrResolutionCode)){
			queryElts.add(DBQuery.in("resolutionCodes", lastrResolutionCode));
		}
		if(CollectionUtils.isNotEmpty(lastResolutionCodes)){
			queryElts.add(DBQuery.in("resolutionCodes", lastResolutionCodes));
		}

		if(StringUtils.isNotBlank(ncbiScientificNameRegex)){
			queryElts.add(DBQuery.regex("ncbiScientificName", Pattern.compile(ncbiScientificNameRegex)));
		}
		if(CollectionUtils.isNotEmpty(ncbiScientificNames)){
			queryElts.add(DBQuery.in("ncbiScientificName", ncbiScientificNames));
		}

		if(StringUtils.isNotBlank(existingProcessTypeCode)
				&& StringUtils.isNotBlank(existingTransformationTypeCode)
				&& StringUtils.isNotBlank(notExistingTransformationTypeCode)){
			queryElts.add(DBQuery.elemMatch("processes", DBQuery.is("typeCode",existingProcessTypeCode)
					.and(DBQuery.is("experiments.typeCode",existingTransformationTypeCode), DBQuery.notEquals("experiments.typeCode",notExistingTransformationTypeCode))));

		}else if(StringUtils.isNotBlank(existingTransformationTypeCode)
				&& StringUtils.isNotBlank(notExistingTransformationTypeCode)){
			queryElts.add(DBQuery.and(DBQuery.is("processes.experiments.typeCode",existingTransformationTypeCode)
					,DBQuery.notEquals("processes.experiments.typeCode",notExistingTransformationTypeCode)));		

		}else if(StringUtils.isNotBlank(existingProcessTypeCode)
				&& StringUtils.isNotBlank(existingTransformationTypeCode)){
			queryElts.add(DBQuery.elemMatch("processes", DBQuery.is("typeCode",existingProcessTypeCode).is("experiments.typeCode",existingTransformationTypeCode)));		

		}else if(StringUtils.isNotBlank(existingProcessTypeCode)
				&& StringUtils.isNotBlank(notExistingTransformationTypeCode)){
			queryElts.add(DBQuery.elemMatch("processes", DBQuery.is("typeCode",existingProcessTypeCode).notEquals("experiments.typeCode",notExistingTransformationTypeCode)));		

		}else if(StringUtils.isNotBlank(existingProcessTypeCode)){
			queryElts.add(DBQuery.is("processes.typeCode",existingProcessTypeCode));

		}else if(StringUtils.isNotBlank(notExistingProcessTypeCode)){
			queryElts.add(DBQuery.notEquals("processes.typeCode",notExistingProcessTypeCode));

		}else if(StringUtils.isNotBlank(existingTransformationTypeCode)){
			queryElts.add(DBQuery.is("processes.experiments.typeCode",existingTransformationTypeCode));

		}else if(StringUtils.isNotBlank(notExistingTransformationTypeCode)){
			queryElts.add(DBQuery.notEquals("processes.experiments.typeCode",notExistingTransformationTypeCode));

		}

		if(CollectionUtils.isNotEmpty(experimentProtocolCodes)){
			queryElts.add(DBQuery.in("processes.experiments.protocolCode",experimentProtocolCodes));
		}
		
		if(isResolutionHistoryFilter()) {
			this.addResolutionHistoryQuery(queryElts);
		}

		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(properties,Level.CODE.Sample, "properties"));
		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(experimentProperties,Level.CODE.Experiment, "processes.experiments.properties"));

		queryElts.addAll(NGLControllerHelper.generateExistsQueriesForFields(existingFields));


//		Query query = DBQuery.empty();
//		if(queryElts.size() > 0){
//			query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
//		}		
//
//		return query;
		if (queryElts.size() == 0)
			return  DBQuery.empty();
		return DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
	}

	private boolean isResolutionHistoryFilter() {
		return CollectionUtils.isNotEmpty(this.resolutionCodes) || this.fromResolutionDate != null || this.toResolutionDate != null || CollectionUtils.isNotEmpty(this.resolutionUsers);
	}
	
	private void addResolutionHistoryQuery(List<Query> queryElts) {
		List<Query> elemMatches = new ArrayList<>(4);
		
		if(CollectionUtils.isNotEmpty(this.resolutionCodes)) {
			elemMatches.add(DBQuery.in("resolutionCodes", this.resolutionCodes));
		}
		
		if(this.fromResolutionDate != null){
			elemMatches.add(DBQuery.greaterThanEquals("date", this.fromResolutionDate));
	    }
		
		if(this.toResolutionDate != null){
			elemMatches.add(DBQuery.lessThan("date", DateUtils.addDays(this.toResolutionDate, 1)));
	    }
		
		if(CollectionUtils.isNotEmpty(this.resolutionUsers)) {
			elemMatches.add(DBQuery.in("user", this.resolutionUsers));
		}
		
		queryElts.add(DBQuery.elemMatch("resolutionHistory", DBQuery.and(elemMatches.toArray(new Query[elemMatches.size()]))));
	}
	
}
