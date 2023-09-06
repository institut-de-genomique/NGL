package controllers.experiments.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import com.mongodb.BasicDBObject;

import controllers.DBObjectListForm;
import controllers.NGLControllerHelper;
import fr.cea.ig.MongoDBDAO;
import models.laboratory.common.description.Level;
import models.laboratory.container.instance.Container;
import models.laboratory.experiment.instance.Experiment;
import models.utils.InstanceConstants;

public class ExperimentSearchForm extends DBObjectListForm<Experiment> {
	
	public String code;
	public Set<String> codes;
	public String typeCode;
	public Set<String> typeCodes;
	public String categoryCode;
	public Set<String> categoryCodes;
	public String projectCode;
	public Set<String> projectCodes;
	public Set<String> sampleCodes;
	public String sampleCode;
	public Date fromDate;
	public Date toDate;
	public String stateCode;
	public Set<String> stateCodes;
	public List<String> stateResolutionCodes;
	public Set<String> users;
	public String containerSupportCode;
	public Set<String> containerSupportCodes;
	public String containerSupportCodeRegex;
	
	public String containerCode;
	public Set<String> containerCodes;
	public Set<String> inputContainerCodes;
	public String containerCodeRegex;
	
	public String containerFromTransformationTypeCode;
	public String atomicTransfertMethods;
	public String reagentOrBoxCode;

	public String ncbiScientificNameRegex;
	public Set<String> ncbiScientificNameRegexs;

	public String instrument;
	public Set<String> instruments;
	
	public String instrumentCode;
	public Set<String> instrumentCodes;
	
	public Set<String> protocolCodes;
	
	public String taxonCode;

	public List<String> taxonCodes;
	
	public Set<String> sampleTypeCodes;
	public Map<String, List<String>> atomicTransfertMethodsInputContainerUsedsContentsProperties = new HashMap<>();
	public Map<String, List<String>> atomicTransfertMethodsOutputContainerUsedsContentsProperties = new HashMap<>(); //not used
	
	public Map<String, List<String>> experimentProperties = new HashMap<>();
	public Map<String, List<String>> instrumentProperties = new HashMap<>();
	
	
	// FDS 21/08/2015 pour debug only???  ajouter tags et sampleTypeCodes dans le return 
	@Override
	public String toString() {
		return "ExperimentSearchForm [code="+code+", codes="+ codes +", typeCode=" + typeCode
				+ ", categoryCode="	+ categoryCode + ", projectCodes=" + projectCodes
				+ ", sampleCodes=" + sampleCodes + ", fromDate=" + fromDate
				+ ", toDate=" + toDate + ", stateCode=" + stateCode
				+ ", users=" + users +", containerSupportCode=" + containerSupportCode 
				+ ", atomicTransfertMethods="+ atomicTransfertMethods + ", instrument"+ instrument 
				+ ", sampleTypeCodes="+ sampleTypeCodes
				+ "]";
	}
	
	/**
	 * Construct the experiment query
	 * @return the query
	 */
	@Override
	public DBQuery.Query getQuery() {
		List<DBQuery.Query> queryElts = new ArrayList<>();
		Query query=DBQuery.empty();
		
		if(CollectionUtils.isNotEmpty(this.codes)){
			queryElts.add(DBQuery.in("code", this.codes));
		}else if(StringUtils.isNotBlank(this.code)){
			queryElts.add(DBQuery.regex("code", Pattern.compile(this.code)));
		}

		if(CollectionUtils.isNotEmpty(this.categoryCodes)){
			queryElts.add(DBQuery.in("categoryCode", this.categoryCodes));
		}else if(StringUtils.isNotBlank(this.categoryCode)){
			queryElts.add(DBQuery.is("categoryCode", this.categoryCode));
		}
		
		if(CollectionUtils.isNotEmpty(this.typeCodes)){
			queryElts.add(DBQuery.in("typeCode", this.typeCodes));
		}else if(StringUtils.isNotBlank(this.typeCode)){
			queryElts.add(DBQuery.is("typeCode", this.typeCode));
		}

		if(CollectionUtils.isNotEmpty(this.projectCodes)){
			queryElts.add(DBQuery.in("projectCodes", this.projectCodes));
		}

		if(StringUtils.isNotBlank(this.projectCode)){
			queryElts.add(DBQuery.in("projectCodes", this.projectCode));
		}

		if(null != this.fromDate){
			queryElts.add(DBQuery.greaterThanEquals("traceInformation.creationDate", this.fromDate));
		}

		if(null != this.toDate){
			queryElts.add(DBQuery.lessThanEquals("traceInformation.creationDate", (DateUtils.addDays(this.toDate, 1))));
		}

		if(CollectionUtils.isNotEmpty(this.sampleCodes)){
			queryElts.add(DBQuery.in("sampleCodes", this.sampleCodes));
		}

		
		Set<String> containerCodes = new TreeSet<>();
		if(MapUtils.isNotEmpty(this.atomicTransfertMethodsInputContainerUsedsContentsProperties)){
			List<DBQuery.Query> listContainerQuery = NGLControllerHelper.generateQueriesForProperties(this.atomicTransfertMethodsInputContainerUsedsContentsProperties, Level.CODE.Content, "contents.properties");
			
			Query containerQuery = DBQuery.and(listContainerQuery.toArray(new DBQuery.Query[listContainerQuery.size()]));
			BasicDBObject keys = new BasicDBObject();
			keys.append("code", 1);
			List<Container> containers = MongoDBDAO.find(InstanceConstants.CONTAINER_COLL_NAME, Container.class, containerQuery,keys).toList();
			
			if(containers.size() == 0){
				containerCodes.add("########"); //to force to have zero results
			}else{
				for(Container p : containers){
					containerCodes.add(p.code);
				}	
			}				
		}
		
		if(StringUtils.isNotBlank(this.containerCode)){			
			containerCodes.add(this.containerCode);
		}else if(CollectionUtils.isNotEmpty(this.containerCodes)){			
			containerCodes.addAll(this.containerCodes);
		}

		if(CollectionUtils.isNotEmpty(inputContainerCodes)){
			List<DBQuery.Query> qs = new ArrayList<>();
			qs.add(DBQuery.in("inputContainerCodes",inputContainerCodes));
			queryElts.add(DBQuery.or(qs.toArray(new DBQuery.Query[qs.size()])));
		}
		
		if(containerCodes.size() > 0){
			List<DBQuery.Query> qs = new ArrayList<>();
			qs.add(DBQuery.in("inputContainerCodes",containerCodes));
			qs.add(DBQuery.in("outputContainerCodes",containerCodes));
			queryElts.add(DBQuery.or(qs.toArray(new DBQuery.Query[qs.size()])));
		}else if(StringUtils.isNotBlank(this.containerCodeRegex)){			
			List<DBQuery.Query> qs = new ArrayList<>();
			qs.add(DBQuery.regex("inputContainerCodes",Pattern.compile(this.containerCodeRegex)));
			qs.add(DBQuery.regex("outputContainerCodes",Pattern.compile(this.containerCodeRegex)));
			queryElts.add(DBQuery.or(qs.toArray(new DBQuery.Query[qs.size()])));
		}
		
		
		if(StringUtils.isNotBlank(this.containerSupportCode)){			
			List<DBQuery.Query> qs = new ArrayList<>();

			qs.add(DBQuery.in("inputContainerSupportCodes",this.containerSupportCode));
			qs.add(DBQuery.in("outputContainerSupportCodes",this.containerSupportCode));
			queryElts.add(DBQuery.or(qs.toArray(new DBQuery.Query[qs.size()])));
		}else if(CollectionUtils.isNotEmpty(this.containerSupportCodes)){			
			List<DBQuery.Query> qs = new ArrayList<>();

			qs.add(DBQuery.in("inputContainerSupportCodes",this.containerSupportCodes));
			qs.add(DBQuery.in("outputContainerSupportCodes",this.containerSupportCodes));
			queryElts.add(DBQuery.or(qs.toArray(new DBQuery.Query[qs.size()])));
		}else if(StringUtils.isNotBlank(this.containerSupportCodeRegex)){			
			List<DBQuery.Query> qs = new ArrayList<>();

			qs.add(DBQuery.regex("inputContainerSupportCodes",Pattern.compile(this.containerSupportCodeRegex)));
			qs.add(DBQuery.regex("outputContainerSupportCodes",Pattern.compile(this.containerSupportCodeRegex)));
			queryElts.add(DBQuery.or(qs.toArray(new DBQuery.Query[qs.size()])));
		}
		
		

		if(StringUtils.isNotBlank(this.sampleCode)){
			queryElts.add(DBQuery.in("sampleCodes", this.sampleCode));
		}

		if(CollectionUtils.isNotEmpty(this.users)){
			queryElts.add(DBQuery.in("traceInformation.createUser", this.users));
		}

		if(StringUtils.isNotBlank(ncbiScientificNameRegex)){
			queryElts.add(DBQuery.or(
								DBQuery.regex("atomicTransfertMethods.inputContainerUseds.contents.ncbiScientificName", Pattern.compile(ncbiScientificNameRegex)),
								DBQuery.regex("atomicTransfertMethods.outputContainerUseds.contents.ncbiScientificName", Pattern.compile(ncbiScientificNameRegex))
						 ));
		}

		if(CollectionUtils.isNotEmpty(this.ncbiScientificNameRegexs)) {
			DBQuery.Query queryTmp = DBQuery.empty();
			Iterator<String> iterator = this.ncbiScientificNameRegexs.iterator();

			while (iterator.hasNext()) {
				String ncbiNameLoop = iterator.next();
				queryTmp.or(
					DBQuery.regex("atomicTransfertMethods.inputContainerUseds.contents.ncbiScientificName", Pattern.compile(ncbiNameLoop)),
					DBQuery.regex("atomicTransfertMethods.outputContainerUseds.contents.ncbiScientificName", Pattern.compile(ncbiNameLoop))
				);
			}

			queryElts.add(queryTmp);
		}

		if(StringUtils.isNotBlank(taxonCode)){
			queryElts.add(DBQuery.or(
								DBQuery.is("atomicTransfertMethods.inputContainerUseds.contents.taxonCode", taxonCode),
								DBQuery.is("atomicTransfertMethods.outputContainerUseds.contents.taxonCode", taxonCode)
						 ));
		}
		if(CollectionUtils.isNotEmpty(taxonCodes)){
			queryElts.add(DBQuery.or(
				DBQuery.in("atomicTransfertMethods.inputContainerUseds.contents.taxonCode", taxonCodes),
				DBQuery.in("atomicTransfertMethods.outputContainerUseds.contents.taxonCode", taxonCodes)
		 	));
		}	

		if(StringUtils.isNotBlank(this.reagentOrBoxCode)){
			queryElts.add(DBQuery.or(DBQuery.regex("reagents.boxCode", Pattern.compile(this.reagentOrBoxCode+"_|_"+this.reagentOrBoxCode)),DBQuery.regex("reagents.code", Pattern.compile(this.reagentOrBoxCode+"_|_"+this.reagentOrBoxCode))));
		}

		if(CollectionUtils.isNotEmpty(this.stateCodes)){
			queryElts.add(DBQuery.in("state.code", this.stateCodes));
		}else if(StringUtils.isNotBlank(this.stateCode)){
			queryElts.add(DBQuery.is("state.code", this.stateCode));
		}

		if(StringUtils.isNotBlank(this.instrument)){
			queryElts.add(DBQuery.is("instrument.code", this.instrument));
		}else if(CollectionUtils.isNotEmpty(this.instruments)){
			queryElts.add(DBQuery.in("instrument.code", this.instruments));
		}else if(StringUtils.isNotBlank(this.instrumentCode)){
			queryElts.add(DBQuery.is("instrument.code", this.instrumentCode));
		}else if(CollectionUtils.isNotEmpty(this.instrumentCodes)){
			queryElts.add(DBQuery.in("instrument.code", this.instrumentCodes));
		}
		
		if(CollectionUtils.isNotEmpty(this.protocolCodes)){
			queryElts.add(DBQuery.in("protocolCode", this.protocolCodes));
		}
		
		// FDS 21/08/2015 ajout filtrage sur les types d'echantillon
		if(CollectionUtils.isNotEmpty(this.sampleTypeCodes)){
			queryElts.add(DBQuery.in("atomicTransfertMethods.inputContainerUseds.contents.sampleTypeCode", this.sampleTypeCodes));
		}
		
		if(StringUtils.isNotBlank(this.containerFromTransformationTypeCode)){
			if(this.containerFromTransformationTypeCode.contains("none")){
				queryElts.add(DBQuery.or(DBQuery.size("atomicTransfertMethods.inputContainerUseds.fromTransformationTypeCodes", 0),
						DBQuery.notExists("atomicTransfertMethods.inputContainerUseds.fromTransformationTypeCodes"),
						DBQuery.regex("atomicTransfertMethods.inputContainerUseds.fromTransformationTypeCodes", Pattern.compile("^ext-to.*$"))));
			}else if(!this.containerFromTransformationTypeCode.contains("none")){
				queryElts.add(DBQuery.in("atomicTransfertMethods.inputContainerUseds.fromTransformationTypeCodes", this.containerFromTransformationTypeCode));				
			}else{
				queryElts.add(DBQuery.or(DBQuery.size("fromTransformationTypeCodes", 0),DBQuery.notExists("fromTransformationTypeCodes"),
						DBQuery.regex("atomicTransfertMethods.inputContainerUseds.fromTransformationTypeCodes", Pattern.compile("^ext-to.*$")),
						DBQuery.in("atomicTransfertMethods.inputContainerUseds.fromTransformationTypeCodes", this.containerFromTransformationTypeCode)));
			}
		}
		
		if (CollectionUtils.isNotEmpty(this.stateResolutionCodes)) { //all
			queryElts.add(DBQuery.in("state.resolutionCodes", this.stateResolutionCodes));
		}
		
		//queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(this.atomicTransfertMethodsInputContainerUsedsContentsProperties, Level.CODE.Content, "atomicTransfertMethods.inputContainerUseds.contents.properties"));
		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(this.experimentProperties, Level.CODE.Experiment, "experimentProperties"));
		queryElts.addAll(NGLControllerHelper.generateQueriesForProperties(this.instrumentProperties, Level.CODE.Instrument, "instrumentProperties"));

		if(queryElts.size() > 0){
			query = DBQuery.and(queryElts.toArray(new DBQuery.Query[queryElts.size()]));
		}

		return query;

	}

}
