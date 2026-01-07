package controllers.sra.samples.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import controllers.sra.api.ISraSearchForm;
//import controllers.sra.api.SraSearchForm;
import controllers.sra.tools.Tools;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.Project;

// devrait s'appeler AbstractSamplesSearchForm
public class SamplesSearchForm extends DBObjectListForm<AbstractSample> implements ISraSearchForm { 
	//public String projCode;
	public List<String> projCodes        = new ArrayList<>();
	public List<String> stateCodes       = new ArrayList<>(); 
	public List<String> pseudoStateCodes = new ArrayList<>(); 
	public String stateCode              = null;
	public List<String> codes            = new ArrayList<>();
	public String codeRegex;
	public List<String> submissionCodes  = new ArrayList<>();
	public String submissionCodeRegex;	
	public String identifier             = null;
	public List<String> identifiers      = new ArrayList<>();
	public String identifierRegex;	
	public String type;            // si on veut selectionner Sample ou ExternalSample
	public String createUser;      // pour recherche pour un proprietaire (update-ctrl.js)
	public String base64UserFileSample;
	
	
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
		this.copyPseudoStateCodesToStateCodesInFormulaire();

		if (CollectionUtils.isNotEmpty(this.projCodes)) { //
			queries.add(DBQuery.in("projectCode", this.projCodes)); // doit pas marcher car pour state.code
			// C'est une valeur qui peut prendre une valeur autorisee dans le formulaire. Ici on veut que 
			// l'ensemble des valeurs correspondent à l'ensemble des valeurs du formulaire independamment de l'ordre.
		}
		if (CollectionUtils.isNotEmpty(this.stateCodes)) { //all
			queries.add(DBQuery.in("state.code", this.stateCodes));
		} else if (StringUtils.isNotBlank(this.stateCode)) { //all
			//logger.debug("stateCode dans Samples.getQuery : " + this.stateCode);
			queries.add(DBQuery.in("state.code", this.stateCode));
		}
		if(StringUtils.isNotBlank(this.createUser)) {
			queries.add(DBQuery.in("traceInformation.createUser", this.createUser));
		}
		if(CollectionUtils.isNotEmpty(this.submissionCodes)) {
			List<String> sampleCodes = new ArrayList<String>();
			Tools.completeListSampleCodesFromSubmissionCodes(this.submissionCodes, sampleCodes);
			queries.add(DBQuery.in("code", sampleCodes));
		} else if(StringUtils.isNotBlank(this.submissionCodeRegex)) {
			List<String> sampleCodes = new ArrayList<String>();
			Tools.completeListSampleCodesFromSubmissionRegex(this.submissionCodeRegex, sampleCodes);
			queries.add(DBQuery.in("code", sampleCodes));
		}		
		if(CollectionUtils.isNotEmpty(this.codes)) {
			queries.add(DBQuery.in("code", this.codes));
		} else if(StringUtils.isNotBlank(this.codeRegex)) {
			queries.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
		}
		if (StringUtils.isNotBlank(this.identifier)) {
            queries.add(DBQuery.or(DBQuery.in("accession", identifier),DBQuery.in("externalId", identifier)));
		} else if (CollectionUtils.isNotEmpty(this.identifiers)) {
			List<String> identifiers_ERS     = new ArrayList<>();
			List<String> identifiers_SAM     = new ArrayList<>();
			for(String identifier : this.identifiers) {
				if(identifier.startsWith("SAM")) {
					identifiers_SAM.add(identifier);
				} else {
					identifiers_ERS.add(identifier);
				}
			}
            queries.add(DBQuery.or(DBQuery.in("accession", identifiers_ERS),DBQuery.in("externalId", identifiers_SAM)));
		} else if(StringUtils.isNotBlank(this.identifierRegex)) {
			// ne passe pas car trop volumineux, il faut distinguer les 2 cas :
            //queries.add(DBQuery.or(DBQuery.in("accession", Pattern.compile(this.identifierRegex)),DBQuery.in("externalId", Pattern.compile(this.identifierRegex)));
			if (this.identifierRegex.startsWith("SAM")) {
				queries.add(DBQuery.regex("externalId", Pattern.compile(this.identifierRegex)));
			} else {
				queries.add(DBQuery.regex("accession", Pattern.compile(this.identifierRegex)));
			} 
		}
		if (StringUtils.isNotBlank(this.type)) { //all
			queries.add(DBQuery.in("_type", this.type));
		}				
		if (queries.size() > 0) {
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		//logger.debug("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ    query = " + query.toString());

		return query;
	}
	

}
