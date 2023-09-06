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
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Experiment;


public class ExperimentsSearchForm  extends  DBObjectListForm<Experiment> implements ISraSearchForm {
	public String code;
	public List<String> codes;    
	public String codeRegex;
	public String submissionCode;
	public String studyCode;       // ajout pour interface release et update-ctrl.js    
	public String sampleCode;      // ajout pour update-ctrl.js     
	public String runCode;
	public List<String> projCodes = new ArrayList<>();
	public String stateCode;
	public List<String> stateCodes = new ArrayList<>(); 
	public List<String> pseudoStateCodes = new ArrayList<>();
	public String accession;
	public List<String> accessions = new ArrayList<>();
	public String accessionRegex;
	public String studyAccession;  // ajout pour interface bilan et update-ctrl.js
	public String sampleAccession; // ajout pour interface bilan et update-ctrl.js
	public String createUser;      // pour recherche pour un proprietaire (update-ctrl.js)

	// NGL-3666 :
	public String sampleIdentifier; // identifiant de type ERS ou SAM
	public String studyIdentifier;  // identifiant de type ERP ou PRJ

	private AbstractSampleAPI abstractSampleAPI = AbstractSampleAPI.get();  // pour eviter injection problematique ici
	private AbstractStudyAPI abstractStudyAPI   = AbstractStudyAPI.get();

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

		if (StringUtils.isNotBlank(this.studyCode)) { //all
			//logger.debug("getQuery::form.studyCode=" + form.studyCode);
			queries.add(DBQuery.in("studyCode", this.studyCode));
		}
		if(StringUtils.isNotBlank(this.createUser)) {
			queries.add(DBQuery.in("traceInformation.createUser", this.createUser));
		}
		// NGL-3666
		// autre solution pour NGL3666 : on pourrait recuperer le code du sample mais faut verifier l'info dans base
		if (StringUtils.isNotBlank(this.sampleIdentifier)) { 
			List<String> identifiers = new ArrayList<String>();
			AbstractSample abstSample = null;
			if (this.sampleIdentifier.startsWith("ERS") && abstractSampleAPI.dao_checkObjectExist("accession", this.sampleIdentifier)) {
				abstSample = abstractSampleAPI.dao_findOne(DBQuery.in("accession", this.sampleIdentifier));
			} else if (this.sampleIdentifier.startsWith("SAM") && abstractSampleAPI.dao_checkObjectExist("externalId", this.sampleIdentifier)) {
				abstSample = abstractSampleAPI.dao_findOne(DBQuery.in("externalId", this.sampleIdentifier));
			} 
			if(abstSample != null) {
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
			//logger.debug("Recherche des exp avec experiment.sampleAccession dans ");
			for (String id : identifiers) {
				//logger.debug(id);
			}
		}
		// autre solution pour NGL3666 : on pourrait recuperer le code du sample
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
			//logger.debug("Recherche des exp avec experiment.studyAccession dans ");
			for (String id : identifiers) {
				//logger.debug(id);
			}
		}
		if (StringUtils.isNotBlank(this.code)) { //all
			//logger.debug("getQuery::this.code=" + this.code);
			queries.add(DBQuery.in("code", this.code));
		}		
		if (StringUtils.isNotBlank(this.studyAccession)) { //all
			//logger.debug("getQuery::this.studyAccession=" + this.studyAccession);
			queries.add(DBQuery.in("studyAccession", this.studyAccession));
		}
		if (StringUtils.isNotBlank(this.sampleAccession)) { //all
			//logger.debug("getQuery::this.sampleAccession=" + this.sampleAccession);
			queries.add(DBQuery.in("sampleAccession", this.sampleAccession));
		}
		if (StringUtils.isNotBlank(this.sampleCode)) { //all
			//logger.debug("getQuery::this.sampleCode=" + this.sampleCode);
			queries.add(DBQuery.in("sampleCode", this.sampleCode));
		}
		if (CollectionUtils.isNotEmpty(this.projCodes)) { //
			//logger.debug("getQuery::this.projCodes=" + this.projCodes);
			queries.add(DBQuery.in("projectCode", this.projCodes)); 
		}
		if (CollectionUtils.isNotEmpty(this.stateCodes)) { //all
			//logger.debug("getQuery::this.stateCodes=" + this.stateCodes);
			queries.add(DBQuery.in("state.code", this.stateCodes));
		} else if (StringUtils.isNotBlank(this.stateCode)) { //all
			//if (this.stateCode.equalsIgnoreCase("ebiKnown")) {
			//	logger.debug("this.stateCode:: getQuery:: this.stateCode = " + this.stateCode);				
			//	queries.add(DBQuery.in("state.code", list_ebiKnown));
			//} else {
				//logger.debug("this.stateCode:: getQuery::this.stateCode=" + this.stateCode);
				queries.add(DBQuery.in("state.code", this.stateCode));
			//}
		}
		
		if(CollectionUtils.isNotEmpty(this.codes)) {
			//logger.debug("this.codes:: getQuery::this.codes=" + this.codes);
			queries.add(DBQuery.in("code", this.codes));
		} else if(StringUtils.isNotBlank(this.codeRegex)) {
			//logger.debug("this.codeRegex ::getQuery = " + this.code);
			queries.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
		}
		if(CollectionUtils.isNotEmpty(this.accessions)) {
			//logger.debug("this.accessions ::getQuery = " + this.accessions);
//			for (String ac: this.accessions) {
//				logger.debug("this.accessions:: getQuery = " + ac);
//			}
			queries.add(DBQuery.in("accession", this.accessions));
		} else if(StringUtils.isNotBlank(this.accessionRegex)){
			//logger.debug("this.accessionRegex:: getQuery = " + this.accessionRegex);
			queries.add(DBQuery.regex("accession", Pattern.compile(this.accessionRegex)));
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

