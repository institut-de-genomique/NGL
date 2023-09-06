package controllers.processes.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import models.laboratory.processes.instance.Process;

public class ProcessesSearchForm extends DBObjectListForm<Process> {
	private static final play.Logger.ALogger logger = play.Logger.of(ProcessesSearchForm.class);

	public String typeCode;
	public List<String> typeCodes;
	public String categoryCode;
	public List<String> categoryCodes;
	public String sampleCode;
	public Set<String> sampleCodes;
	public Set<String> sampleTypeCodes;
	public String projectCode;
	public Set<String> projectCodes;
	public String supportCode;
	public String supportCodeRegex;
	public Set<String> supportCodes;

	public String ncbiScientificNameRegex;
	public Set<String> ncbiScientificNameRegexs;
	
	public String containerCode;
	public String containerCodeRegex;
	public Set<String> containerCodes;
	
	public Set<String> outputContainerCodes;
	public Set<String> inputContainerCodes;
	
	public String stateCode;
	public Set<String> stateCodes;
	public List<String> stateResolutionCodes;
	public String containerSupportCategory;
	public Date fromDate;
	public Date toDate;
	public String createUser;
	public Set<String> users;
	public String experimentCode;
	public List<String> experimentCodes;
	public String experimentCodeRegex;
	public String code;
	public List<String> codes;
	public String codeRegex;
	public Map<String, List<String>> properties = new HashMap<>();
	public String fromSupportCode;
	public Map<String, List<String>> sampleOnInputContainerProperties = new HashMap<>();

	public String taxonCode;

	public List<String> taxonCodes;
	
	@Override
	public String toString() {
		return "ProcessesSearchForm [typeCode=" + typeCode + ", categoryCode="
				+ categoryCode + ", sampleCode=" + sampleCode
				+ ", sampleCodes=" + sampleCodes + ", projectCode="
				+ projectCode + ", projectCodes=" + projectCodes
				+ ", supportCode=" + supportCode + ", stateCode=" + stateCode
				+ ", stateCodes=" + stateCodes + ", containerSupportCategory="
				+ containerSupportCategory + ", fromDate=" + fromDate
				+ ", toDate=" + toDate + ", users=" + users + ", createUser=" + createUser 
				+ ", experimentCode=" + experimentCode +", code="+ code + ", properties="
				+ properties + ", fromSupportCode="+fromSupportCode +"]";
	}
	
	@Override
	public Query getQuery() {
		List<Query> queryElts = new ArrayList<>();
		Query query = null;

		logger.info("Process Query : " + this);

		if (CollectionUtils.isNotEmpty(this.projectCodes)) { //all
			queryElts.add(DBQuery.in("projectCodes", this.projectCodes));
		} else if(StringUtils.isNotBlank(this.projectCode)) {
			queryElts.add(DBQuery.is("projectCodes", this.projectCode));
		}

		if (CollectionUtils.isNotEmpty(this.sampleCodes)) { //all
			queryElts.add(DBQuery.in("sampleCodes", this.sampleCodes));
		} else if(StringUtils.isNotBlank(this.sampleCode)) {
			queryElts.add(DBQuery.is("sampleCodes", this.sampleCode));
		}
		
		if (CollectionUtils.isNotEmpty(this.sampleTypeCodes)) { //all
			queryElts.add(DBQuery.in("sampleOnInputContainer.sampleTypeCode", this.sampleTypeCodes));
		}

		if (StringUtils.isNotBlank(this.code)) {
			queryElts.add(DBQuery.is("code", this.code));
		} else if (CollectionUtils.isNotEmpty(this.codes)) {
			queryElts.add(DBQuery.in("code", this.codes));
		} else if(StringUtils.isNotBlank(this.codeRegex)) {
			queryElts.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
		}
		
		if (StringUtils.isNotBlank(this.experimentCode)) {
			queryElts.add(DBQuery.in("experimentCodes", this.experimentCode));
		} else if(CollectionUtils.isNotEmpty(this.experimentCodes)) {
			queryElts.add(DBQuery.in("experimentCodes", this.experimentCodes));
		} else if(StringUtils.isNotBlank(this.experimentCodeRegex)) {
			queryElts.add(DBQuery.regex("experimentCodes", Pattern.compile(this.experimentCodeRegex)));
		}

		if(StringUtils.isNotBlank(this.ncbiScientificNameRegex)){
			queryElts.add(DBQuery.regex("sampleOnInputContainer.ncbiScientificName", Pattern.compile(this.ncbiScientificNameRegex)));
		}

		if(CollectionUtils.isNotEmpty(this.ncbiScientificNameRegexs)) {
			DBQuery.Query queryTmp = DBQuery.empty();
			Iterator<String> iterator = this.ncbiScientificNameRegexs.iterator();

			while (iterator.hasNext()) {
				String ncbiNameLoop = iterator.next();
				queryTmp.or(
					DBQuery.regex("sampleOnInputContainer.ncbiScientificName", Pattern.compile(ncbiNameLoop))
				);
			}

			queryElts.add(queryTmp);
		}

		if (StringUtils.isNotBlank(this.typeCode)) {
			queryElts.add(DBQuery.is("typeCode", this.typeCode));
		} else if(CollectionUtils.isNotEmpty(this.typeCodes)) {
			queryElts.add(DBQuery.in("typeCode", this.typeCodes));
		}

		if(StringUtils.isNotBlank(taxonCode)){
			queryElts.add(DBQuery.is("sampleOnInputContainer.taxonCode", taxonCode));
		}
		if(CollectionUtils.isNotEmpty(taxonCodes)){
			queryElts.add(DBQuery.in("sampleOnInputContainer.taxonCode", taxonCodes));
		}	

		if (StringUtils.isNotBlank(this.categoryCode)) {
			queryElts.add(DBQuery.is("categoryCode", this.categoryCode));
		} else if(CollectionUtils.isNotEmpty(this.categoryCodes)) {
			queryElts.add(DBQuery.in("categoryCode", this.categoryCodes));
		}

		if (CollectionUtils.isNotEmpty(this.stateCodes)) {
			queryElts.add(DBQuery.in("state.code", this.stateCodes));
		} else if(StringUtils.isNotBlank(this.stateCode)) {
			queryElts.add(DBQuery.is("state.code", this.stateCode));
		}
		if (CollectionUtils.isNotEmpty(this.users)) {
			queryElts.add(DBQuery.in("traceInformation.createUser", this.users));
		}

		if (StringUtils.isNotBlank(this.createUser)) {   
			queryElts.add(DBQuery.is("traceInformation.createUser", this.createUser));
		}

		if (null != this.fromDate) {
			queryElts.add(DBQuery.greaterThanEquals("traceInformation.creationDate", this.fromDate));
		}

		if (null != this.toDate) {
			queryElts.add(DBQuery.lessThanEquals("traceInformation.creationDate", (DateUtils.addDays(this.toDate, 1))));
		}
		
		if (StringUtils.isNotBlank(this.supportCode)) {
			queryElts.add(DBQuery.or(DBQuery.is("inputContainerSupportCode",this.supportCode), DBQuery.is("outputContainerSupportCodes",this.supportCode)));
		} else if(StringUtils.isNotBlank(this.supportCodeRegex)) {
			queryElts.add(DBQuery.or(DBQuery.regex("inputContainerSupportCode",Pattern.compile(this.supportCodeRegex)), 
					DBQuery.regex("outputContainerSupportCodes",Pattern.compile(this.supportCodeRegex))));			
		} else if(CollectionUtils.isNotEmpty(this.supportCodes)) {
			queryElts.add(DBQuery.or(DBQuery.in("inputContainerSupportCode",this.supportCodes), DBQuery.in("outputContainerSupportCodes",this.supportCodes)));
		}
		
		if (StringUtils.isNotBlank(this.containerCode)) {
			queryElts.add(DBQuery.or(DBQuery.is("inputContainerCode",this.containerCode), DBQuery.is("outputContainerCodes",this.containerCode)));
		} else if(StringUtils.isNotBlank(this.containerCodeRegex)) {
			queryElts.add(DBQuery.or(DBQuery.regex("inputContainerCode",Pattern.compile(this.containerCodeRegex)), 
					DBQuery.regex("outputContainerCodes",Pattern.compile(this.containerCodeRegex))));			
		} else if(CollectionUtils.isNotEmpty(this.containerCodes)) {
			queryElts.add(DBQuery.or(DBQuery.in("inputContainerCode",this.containerCodes), DBQuery.in("outputContainerCodes",this.containerCodes)));
		} else if(CollectionUtils.isNotEmpty(this.outputContainerCodes)) {
			queryElts.add(DBQuery.in("outputContainerCodes",this.outputContainerCodes));
		} else if(CollectionUtils.isNotEmpty(this.inputContainerCodes)) {
			queryElts.add(DBQuery.in("inputContainerCode",this.inputContainerCodes));
		}
		
		if (CollectionUtils.isNotEmpty(this.stateResolutionCodes)) { //all
			queryElts.add(DBQuery.in("state.resolutionCodes", this.stateResolutionCodes));
		}
		
		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(this.properties, Level.CODE.Process, "properties"));
		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(this.sampleOnInputContainerProperties, Level.CODE.Content, "sampleOnInputContainer.properties"));

		if (queryElts.size() > 0) {
			query = DBQuery.and(queryElts.toArray(new Query[queryElts.size()]));
		}

		return query;
	}
}