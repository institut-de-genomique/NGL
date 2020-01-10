package controllers.containers.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import controllers.DBObjectListForm;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.container.instance.Container;
import models.laboratory.container.instance.ContainerSupport;
import models.utils.InstanceConstants;

public class ContainerSupportsSearchForm extends DBObjectListForm<ContainerSupport> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(ContainerSupports.class);
	
	public String code;
	public List<String> codes;
	public String codeRegex;
	public String containerSupportCategory;
	public List<String> containerSupportCategories;
	public String categoryCode;
	public String stateCode;
	public List<String> stateCodes;
	public String nextExperimentTypeCode;
	public String processTypeCode;
	public List<String> projectCodes;
	public List<String> sampleCodes;
	public List<String> fromTransformationTypeCodes;
	public String createUser;
	//public List<String> valuations;   23/05/2016 FDS supression
	public Date fromDate;
	public Date toDate;
	public List<String> users;
	public String storageCodeRegex;     //  23/05/2016 FDS ajout
	public String storageCode;          //  23/05/2016 FDS ajout
	
	// No information available about why and how the deprecation is to be done
	// @Deprecated
	public Map<String, List<String>> properties = new HashMap<>();
	
	@Override
	public String toString() {
		// 23/05/2016 FDS NGL-825 ajout storageCode
		return "SupportsSearchForm "
				+ "[ code=" + code 
				+ ", categoryCode="+ categoryCode 
				+ ", stateCode=" + stateCode
				+ ", nextExperimentTypeCode=" + nextExperimentTypeCode
				+ ", processTypeCode=" + processTypeCode 
				+ ", projectCodes="+ projectCodes 
				+ ", sampleCodes=" + sampleCodes
				+ ", containerSupportCategory=" + containerSupportCategory
				+ ", containerSupportCategories=" + containerSupportCategories
				+ ", fromTransformationTypeCodes=" + fromTransformationTypeCodes
				+ ", fromDate=" + fromDate 
				+ ", toDate=" + toDate
				+ ", createUser=" + createUser 
				+ ", users=" + users 
				+ ", storageCode="+ storageCode
				+ "]";
	}

	@Override
	public Query getQuery() {
		List<DBQuery.Query> queryElts = new ArrayList<>();
		queryElts.add(DBQuery.exists("_id"));
		if (StringUtils.isNotBlank(this.categoryCode)) {
			queryElts.add(DBQuery.is("categoryCode", this.categoryCode));
		}
		if (StringUtils.isNotBlank(this.containerSupportCategory)) {
			queryElts.add(DBQuery.is("categoryCode", this.containerSupportCategory));
		}
		if (CollectionUtils.isNotEmpty(this.containerSupportCategories)) {
			queryElts.add(DBQuery.in("categoryCode", this.containerSupportCategories));
		}
		if (CollectionUtils.isNotEmpty(this.fromTransformationTypeCodes)) {
			if (this.fromTransformationTypeCodes.contains("none")) {
				queryElts.add(DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),DBQuery.notExists("fromTransformationTypeCodes")
				,DBQuery.in("fromTransformationTypeCodes", this.fromTransformationTypeCodes)));
			} else {
				queryElts.add(DBQuery.in("fromTransformationTypeCodes", this.fromTransformationTypeCodes));
			}			
		}		
		//These fields are not in the ContainerSupport collection then we use the Container collection
		
		// GA: allways used ?????
		if (StringUtils.isNotBlank(this.nextExperimentTypeCode) || StringUtils.isNotBlank(this.processTypeCode)) {
			logger.error("Allready used nextExperimentTypeCode in search container support. Please find where in java code");

			/*Don't need anymore 09/01/2015
			//If the categoryCode is null or empty, we use the ContainerSupportCategory data table to enhance the query
			if(StringUtils.isNotEmpty(supportsSearch.experimentTypeCode) && StringUtils.isEmpty(supportsSearch.categoryCode)){
				List<ContainerSupportCategory> containerSupportCategories = ContainerSupportCategory.find.findByExperimentTypeCode(supportsSearch.experimentTypeCode);
				List<String> ls = new ArrayList<String>();
				for(ContainerSupportCategory c:containerSupportCategories){
					ls.add(c.code);
				}
				if(ls.size() > 0){
					queryElts.add(DBQuery.in("categoryCode", ls));
				}
			}
			 */

			//Using the Container collection for reaching container support
			ContainersSearchForm cs = new ContainersSearchForm();
			cs.nextExperimentTypeCode = this.nextExperimentTypeCode;
			cs.processTypeCode = this.processTypeCode;		
			cs.processProperties = this.properties;	
			BasicDBObject keys = new BasicDBObject();
			keys.put("_id", 0);//Don't need the _id field
			keys.put("support", 1);
			Query queryContainer = cs.getQuery();
			if (queryContainer != null) {
				List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, queryContainer, keys).toList();
				logger.debug("Containers " + containers.size());
				List<String> supports  =new ArrayList<>();
				for(Container c: containers){
					supports.add(c.support.code);
				}
				if (StringUtils.isNotBlank(cs.nextExperimentTypeCode) || StringUtils.isNotBlank(cs.processTypeCode)) {
					queryElts.add(DBQuery.in("code", supports));
				}
			} else {
				return null;
			}
		}

		/*23/05/2016  NGL-825 FDS : this criteria is meaningless for supports with multiple containers ( plates..)
		if(CollectionUtils.isNotEmpty(supportsSearch.valuations)){
			queryElts.add(DBQuery.or(DBQuery.in("valuation.valid", supportsSearch.valuations)));
		}
		*/
		
		/* 23/05/2016 FDS NGL-825: add search by storageCode */
		if (StringUtils.isNotBlank(this.storageCode)) {
			queryElts.add(DBQuery.in("storageCode", this.storageCode));
		} else if(StringUtils.isNotBlank(this.storageCodeRegex)) {
			queryElts.add(DBQuery.regex("storageCode", Pattern.compile(this.storageCodeRegex)));
		}

		if (StringUtils.isNotBlank(this.stateCode)) {
			queryElts.add(DBQuery.in("state.code", this.stateCode));
		}
		
		if (CollectionUtils.isNotEmpty(this.stateCodes)) {
			queryElts.add(DBQuery.in("state.code", this.stateCodes));
		}

		if (CollectionUtils.isNotEmpty(this.codes)) {
			queryElts.add(DBQuery.in("code", this.codes));
		} else if(StringUtils.isNotBlank(this.code)) {
			queryElts.add(DBQuery.is("code", this.code));
		} else if(StringUtils.isNotBlank(this.codeRegex)) {
			queryElts.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
		}
		
		if (CollectionUtils.isNotEmpty(this.projectCodes)) {
			queryElts.add(DBQuery.in("projectCodes", this.projectCodes));
		}

		if (null != this.fromDate) {
			queryElts.add(DBQuery.greaterThanEquals("traceInformation.creationDate", this.fromDate));
		}

		if (null != this.toDate) {
			queryElts.add(DBQuery.lessThan("traceInformation.creationDate", (DateUtils.addDays(this.toDate, 1))));
		}
		
		if(StringUtils.isNotBlank(this.createUser)){   
			queryElts.add(DBQuery.is("traceInformation.createUser", this.createUser));
		}

		if(CollectionUtils.isNotEmpty(this.users)){
			queryElts.add(DBQuery.in("traceInformation.createUser", this.users));
		}

		if(CollectionUtils.isNotEmpty(this.sampleCodes)){
			queryElts.add(DBQuery.in("sampleCodes", this.sampleCodes));
		}

		return DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
	}
}
