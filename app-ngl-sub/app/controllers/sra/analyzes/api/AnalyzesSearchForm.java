package controllers.sra.analyzes.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import models.sra.submit.sra.instance.Analysis;
import controllers.sra.api.ISraSearchForm;
import controllers.sra.tools.Tools;

public class AnalyzesSearchForm extends  DBObjectListForm<Analysis> implements ISraSearchForm {

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

	@Override
	public Query getQuery() {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		//logger.debug("xxxxxxTOTO getQuery de analysis , this.pseudoStateCodes :" + this.pseudoStateCodes);
		this.copyPseudoStateCodesToStateCodesInFormulaire();

		//logger.debug("xxxxxxTOTO getQuery de analysis , this.stateCodes :" + this.stateCodes);

		//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   Dans getQuery");
		if (CollectionUtils.isNotEmpty(this.projCodes)) { //
			queries.add(DBQuery.in("projectCodes", this.projCodes)); // doit pas marcher car pour state.code
			// C'est une valeur qui peut prendre une valeur autorisee dans le formulaire. Ici on veut que 
			// l'ensemble des valeurs correspondent à l'ensemble des valeurs du formulaire independamment de l'ordre.
		}
		
		if(StringUtils.isNotBlank(this.createUser)) {
			queries.add(DBQuery.in("traceInformation.createUser", this.createUser));
		}
		if (CollectionUtils.isNotEmpty(this.stateCodes)) { //all
			//logger.debug("XXXXXXXXXXXXXXXXXXX this.stateCodes non vide");

//			for(String stateCode: this.stateCodes) {
//				logger.debug("ajout recherche analysis avec stateCode  = " + stateCode);
//			}
			queries.add(DBQuery.in("state.code", this.stateCodes));

		} else if (StringUtils.isNotBlank(this.stateCode)) { //all
			queries.add(DBQuery.in("state.code", this.stateCode));
		}
		if (StringUtils.isNotBlank(this.accession)) {
			queries.add(DBQuery.in("accession", this.accession));
		} 
		if (CollectionUtils.isNotEmpty(this.accessions)) {
			queries.add(DBQuery.in("accession", this.accessions));
		} 
		if(StringUtils.isNotBlank(this.accessionRegex)) {
			queries.add(DBQuery.regex("accession", Pattern.compile(this.accessionRegex)));
		}
		if(StringUtils.isNotBlank(this.code)) {
			queries.add(DBQuery.in("code", this.code));
		} else if (CollectionUtils.isNotEmpty(this.codes)) {
			queries.add(DBQuery.in("code", this.codes));
		} else if(StringUtils.isNotBlank(this.codeRegex)) {
			queries.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
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
		if (CollectionUtils.isNotEmpty(this.submissionCodes)) {
			List<String> expCodes = new ArrayList<String>();
			Tools.completeListAnalysisCodesFromSubmissionCodes(this.submissionCodes, expCodes);
			queries.add(DBQuery.in("code", expCodes)); 
		} else if(StringUtils.isNotBlank(this.submissionCodeRegex)) {
			List<String> expCodes = new ArrayList<String>();
			Tools.completeListAnalysisCodesFromSubmissionRegex(this.submissionCodeRegex, expCodes);
			queries.add(DBQuery.in("code", expCodes)); 
		}
		
				
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}


}
