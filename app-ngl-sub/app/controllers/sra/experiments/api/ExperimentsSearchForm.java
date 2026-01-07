package controllers.sra.experiments.api;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import controllers.sra.api.ISraSearchForm;
import controllers.sra.tools.Tools;
import models.sra.submit.sra.instance.Experiment;


public class ExperimentsSearchForm  extends  DBObjectListForm<Experiment> implements ISraSearchForm {
	public String runCode; // utilisé par ExperimentsRawDatas.java
	public String base64UserFileExperiment; // utilise par le controller 

	public List<String> projCodes = new ArrayList<>();
	public String stateCode;
	public List<String> stateCodes = new ArrayList<>(); 
	public List<String> pseudoStateCodes = new ArrayList<>();
	public String createUser;      // pour recherche pour un proprietaire (update-ctrl.js)
	
	public String code;
	public List<String> codes;    
	public String codeRegex;
	
	public String accession;
	public List<String> accessions = new ArrayList<>();
	public String accessionRegex;
		
	public String studyCode;       // ajout pour interface release et update-ctrl.js  
	public List<String> studyCodes = new ArrayList<>();
	public String studyCodeRegex;

	public String sampleCode;       // ajout pour interface release et update-ctrl.js  
	public List<String> sampleCodes = new ArrayList<>();
	public String sampleCodeRegex;
	
	public String studyIdentifier;    
	public List<String> studyIdentifiers = new ArrayList<>();
	public String studyIdentifierRegex;

	public String sampleIdentifier;   // identifiant de type ERS ou SRS ou SAM 
	public List<String> sampleIdentifiers = new ArrayList<>();
	public String sampleIdentifierRegex;
	
	public String submissionCode;
	public List<String> submissionCodes = new ArrayList<>();
	public String submissionCodeRegex;


	private static final play.Logger.ALogger logger = play.Logger.of(ExperimentsSearchForm.class);

	// plus besoin car dans NGLAPIController, on utilise un ListForm sans limite
	//public Integer limit = -1;    // redefinir le limit car sinon on est avec limit de ListForm fixé à 5000
	                              // et si limite de 5000 renvoie les 5000 premiers resultats seulement quand on passe 
								  // par controllers.MongoCommonController.mongoDBFinder(ListForm, DBQuery.Query) appelé par
	                              // controllers.sra.experiments.api.Experiments.list(), sans error ni warning.


	
	@Override
	public Query getQuery() {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		//logger.info("DANS QUERY : pseudoStateCodes[0] = '" + form.pseudoStateCodes.get(0) + "'");

		this.copyPseudoStateCodesToStateCodesInFormulaire();
		//logger.debug("form.pseudoStateCodes :" + form.pseudoStateCodes);
		//logger.debug("xxxxxxTOTO getQuery de experiments , form.stateCodes :" + form.stateCodes);

		if (CollectionUtils.isNotEmpty(this.projCodes)) { //
			//logger.debug("getQuery::this.projCodes=" + this.projCodes);
			queries.add(DBQuery.in("projectCode", this.projCodes)); 
		}
		if (StringUtils.isNotBlank(this.stateCode)) { //all
			//if (this.stateCode.equalsIgnoreCase("ebiKnown")) {
			//	logger.debug("this.stateCode:: getQuery:: this.stateCode = " + this.stateCode);				
			//	queries.add(DBQuery.in("state.code", list_ebiKnown));
			//} else {
				//logger.debug("this.stateCode:: getQuery::this.stateCode=" + this.stateCode);
				queries.add(DBQuery.in("state.code", this.stateCode));
			//}
		} else if (CollectionUtils.isNotEmpty(this.stateCodes)) { //all
			//logger.debug("getQuery::this.stateCodes=" + this.stateCodes);
			queries.add(DBQuery.in("state.code", this.stateCodes));
		}  
		if(StringUtils.isNotBlank(this.createUser)) {
			queries.add(DBQuery.in("traceInformation.createUser", this.createUser));
		}
		if(StringUtils.isNotBlank(this.code)) {
			queries.add(DBQuery.in("code", this.code));
		} else if (CollectionUtils.isNotEmpty(this.codes)) {
			queries.add(DBQuery.in("code", this.codes));
		} else if(StringUtils.isNotBlank(this.codeRegex)) {
			queries.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
		}
		if(StringUtils.isNotBlank(this.accession)) {
			queries.add(DBQuery.in("accession", this.accession));
		} else if (CollectionUtils.isNotEmpty(this.accessions)) {
			queries.add(DBQuery.in("accession", this.accessions));
		} else if(StringUtils.isNotBlank(this.accessionRegex)) {
			queries.add(DBQuery.regex("accession", Pattern.compile(this.accessionRegex)));
		}
		if (StringUtils.isNotBlank(this.studyCode)) { 
			queries.add(DBQuery.in("studyCode", this.studyCode)); 
		} else if (CollectionUtils.isNotEmpty(this.studyCodes)) {
			queries.add(DBQuery.in("studyCode", this.studyCodes)); 
		} else if(StringUtils.isNotBlank(this.studyCodeRegex)) {
			queries.add(DBQuery.regex("studyCode", Pattern.compile(this.studyCodeRegex)));
		}
		if (StringUtils.isNotBlank(this.studyIdentifier)) { 
			List<String> identifiers = new ArrayList<String>();
			Tools.completeListStudyIdentifiers(this.studyIdentifier, identifiers);
			queries.add(DBQuery.in("studyIdentifier", identifiers)); 
		} else if (CollectionUtils.isNotEmpty(this.studyIdentifiers)) {
			List<String> identifiers = new ArrayList<String>();
			for(String identifier : this.studyIdentifiers) {
				logger.debug("appel de Tools.completeListStudyIdentifiers avec identifier '{}'", identifier);
				Tools.completeListStudyIdentifiers(identifier, identifiers);
			}
			queries.add(DBQuery.in("studyIdentifier", identifiers)); 
		} else if(StringUtils.isNotBlank(this.studyIdentifierRegex)) {
			List<String> identifiers = new ArrayList<String>();
			logger.debug("appel de Tools.completeListStudyIdentifiersFromIdentifierRegex avec studyIdentifierRegex '{}'", this.studyIdentifierRegex);
			Tools.completeListStudyIdentifiersFromIdentifierRegex(this.studyIdentifierRegex, identifiers);
			queries.add(DBQuery.in("studyIdentifier", identifiers)); 
		}
		if (StringUtils.isNotBlank(this.sampleCode)) { 
			queries.add(DBQuery.in("sampleCode", this.sampleCode)); 
		} else if (CollectionUtils.isNotEmpty(this.sampleCodes)) {
			queries.add(DBQuery.in("sampleCode", this.sampleCodes)); 
		} else if(StringUtils.isNotBlank(this.sampleCodeRegex)) {
			queries.add(DBQuery.regex("sampleCode", Pattern.compile(this.sampleCodeRegex)));
		}
		if (StringUtils.isNotBlank(this.sampleIdentifier)) { 
			List<String> identifiers = new ArrayList<String>();
			Tools.completeListSampleIdentifiers(this.sampleIdentifier, identifiers);
			queries.add(DBQuery.in("sampleIdentifier", identifiers)); 
		} else if (CollectionUtils.isNotEmpty(this.sampleIdentifiers)) {
			List<String> identifiers = new ArrayList<String>();
			for(String identifier : this.sampleIdentifiers) {
				//logger.debug("appel de Tools.completeListSampleIdentifiers avec identifier '{}'", identifier);
				Tools.completeListSampleIdentifiers(identifier, identifiers);
			}
			queries.add(DBQuery.in("sampleIdentifier", identifiers)); 
		} else if(StringUtils.isNotBlank(this.sampleIdentifierRegex)) {
			List<String> identifiers = new ArrayList<String>();
			//logger.debug("appel de Tools.completeListSampleIdentifiersFromIdentifierRegex avec sampleIdentifierRegex '{}'", this.sampleIdentifierRegex);
			Tools.completeListSampleIdentifiersFromIdentifierRegex(this.sampleIdentifierRegex, identifiers);
			queries.add(DBQuery.in("sampleIdentifier", identifiers)); 
		}
		if (CollectionUtils.isNotEmpty(this.submissionCodes)) {
			List<String> expCodes = new ArrayList<String>();
			Tools.completeListExperimentCodesFromSubmissionCodes(this.submissionCodes, expCodes);
			queries.add(DBQuery.in("code", expCodes)); 
		} else if(StringUtils.isNotBlank(this.submissionCodeRegex)) {
			List<String> expCodes = new ArrayList<String>();
			Tools.completeListExperimentCodesFromSubmissionRegex(this.submissionCodeRegex, expCodes);
			queries.add(DBQuery.in("code", expCodes)); 
		}
		
		
		if(queries.size() > 0){
			//logger.debug("getQuery::queries");
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}


	@Override
	public List<String> getStateCodes() {
		return this.stateCodes;
	}


	@Override
	public void setStateCodes(List<String> stateCodes) {
		this.stateCodes = stateCodes;
	}


	@Override
	public void addStateCode(String stateCode) {
		this.stateCodes.add(stateCode);
	}


	@Override
	public List<String> getPseudoStateCodes() {
		return this.pseudoStateCodes;
	}


	@Override
	public void setPseudoStateCodes(List<String> pseudoStateCodes) {
		 this.pseudoStateCodes = pseudoStateCodes;
	}

}

