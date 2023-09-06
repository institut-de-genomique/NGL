package controllers.sra.analyzes.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import controllers.sra.api.ISraSearchForm;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Analysis;

public class AnalyzesSearchForm extends  DBObjectListForm<Analysis> implements ISraSearchForm {

	public List<String> projCodes        = new ArrayList<>();
	public String stateCode;
	public List<String> stateCodes       = new ArrayList<>();
	public List<String> pseudoStateCodes = new ArrayList<>();
	public String accession              = null;
	public List<String> accessions       = new ArrayList<>();
	public String accessionRegex;
	public String code;
	public List<String> codes            = new ArrayList<>();
	public String codeRegex;
	public String createUser;       // pour recherche pour un proprietaire (update-ctrl.js)
	public String sampleIdentifier; // identifiant de type ERS ou SAM
	public String studyIdentifier;  // identifiant de type ERP ou PRJ
	public String submissionCode;
	private AbstractSampleAPI abstractSampleAPI = AbstractSampleAPI.get();  // pour eviter injection problematique ici
	private AbstractStudyAPI abstractStudyAPI   = AbstractStudyAPI.get();
	


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
		}
		if (CollectionUtils.isNotEmpty(this.codes)) {
			queries.add(DBQuery.in("code", this.codes));
		} 
		if(StringUtils.isNotBlank(this.codeRegex)) {
			queries.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
		}


		if (StringUtils.isNotBlank(this.sampleIdentifier)) { 
			List<String> identifiers = new ArrayList<String>();
			AbstractSample abstSample = null;
			if(abstractSampleAPI.dao_checkObjectExist("accession", this.sampleIdentifier)) {
				//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX sample existe");
			}
			if (this.sampleIdentifier.startsWith("ERS") && abstractSampleAPI.dao_checkObjectExist("accession", this.sampleIdentifier)) {
				//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX  sample existe et ERS");
				abstSample = abstractSampleAPI.dao_findOne(DBQuery.in("accession", this.sampleIdentifier));
				//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX  recuperation du sample ");

			} else if (this.sampleIdentifier.startsWith("SAM") && abstractSampleAPI.dao_checkObjectExist("externalId", this.sampleIdentifier)) {
				abstSample = abstractSampleAPI.dao_findOne(DBQuery.in("externalId", this.sampleIdentifier));
			} 
			if(abstSample != null) {
				//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX sampleCode =" + abstSample.code);
				if( StringUtils.isNotBlank(abstSample.accession) && ! identifiers.contains(abstSample.accession)) {
					identifiers.add(abstSample.accession);
				}
				if( StringUtils.isNotBlank(abstSample.externalId) && ! identifiers.contains(abstSample.externalId)) {
					identifiers.add(abstSample.externalId);
				}
			} else {
				identifiers.add(this.sampleIdentifier); 
			}
			queries.add(DBQuery.in("sampleAccession", identifiers)); 
//			logger.debug("Recherche des analyse avec sample dans ");
//			for (String id : identifiers) {
//				logger.debug(id);
//			}
		}
		if (StringUtils.isNotBlank(this.studyIdentifier)) { 
			List<String> identifiers = new ArrayList<String>();
			AbstractStudy abstStudy = null;
			if (this.studyIdentifier.startsWith("ERP") && abstractStudyAPI.dao_checkObjectExist("accession", this.studyIdentifier)) {
				abstStudy = abstractStudyAPI.dao_findOne(DBQuery.in("accession", this.studyIdentifier));
			} else if (this.studyIdentifier.startsWith("PRJ") && abstractStudyAPI.dao_checkObjectExist("externalId", this.studyIdentifier)) {
				abstStudy = abstractStudyAPI.dao_findOne(DBQuery.in("externalId", this.studyIdentifier));
			} 
			if(abstStudy != null) {
				if( StringUtils.isNotBlank(abstStudy.accession) && ! identifiers.contains(abstStudy.accession)) {
					identifiers.add(abstStudy.accession);
				}
				if( StringUtils.isNotBlank(abstStudy.externalId) && ! identifiers.contains(abstStudy.externalId)) {
					identifiers.add(abstStudy.externalId);
				}
			} else {
				identifiers.add(this.studyIdentifier);
			}
			queries.add(DBQuery.in("studyAccession", identifiers)); 
//			logger.debug("Recherche des analyse avec study dans ");
//			for (String id : identifiers) {
//				logger.debug(id);
//			}
		}
		
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}


}
