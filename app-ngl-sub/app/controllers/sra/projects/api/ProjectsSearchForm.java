package controllers.sra.projects.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import controllers.sra.api.ISraSearchForm;
import models.sra.submit.sra.instance.Project;

public class ProjectsSearchForm extends DBObjectListForm<Project> implements ISraSearchForm {

	//public String projCode;
	public List<String> projCodes   = new ArrayList<>();
	public Boolean confidential     = null;
	public String stateCode         = null;
	public String accession         = null;
	public List<String> accessions  = new ArrayList<>();
	public String accessionRegex;
	public List<String> codes       = new ArrayList<>();
	public String codeRegex;
	public String externalId        = null;
	public List<String> externalIds = new ArrayList<>();
	public String externalIdRegex;
	public String createUser;             // pour recherche pour un proprietaire (update-ctrl.js)
	public String submissionProjectType;  // pour pouvoir distinguer project classique des project umbrella
	public List<String> stateCodes = new ArrayList<>(); 
	public List<String> pseudoStateCodes = new ArrayList<>();
	
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
		//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX   Dans getQuery");
		if (CollectionUtils.isNotEmpty(this.projCodes)) { //
			queries.add(DBQuery.in("projectCodes", this.projCodes)); // doit pas marcher car pour state.code
			// C'est une valeur qui peut prendre une valeur autorisee dans le thisulaire. Ici on veut que 
			// l'ensemble des valeurs correspondent à l'ensemble des valeurs du thisulaire independamment de l'ordre.
		}
		if(StringUtils.isNotBlank(this.createUser)) {
			queries.add(DBQuery.in("traceInthisation.createUser", this.createUser));
		}
		//logger.debug("this.submissionProjectType = " +this.submissionProjectType);
		if(StringUtils.isNotBlank(this.submissionProjectType)) {
			queries.add(DBQuery.in("submissionProjectType", this.submissionProjectType));
		}
		if (CollectionUtils.isNotEmpty(this.stateCodes)) { //all
			queries.add(DBQuery.in("state.code", this.stateCodes));
		} else if (StringUtils.isNotBlank(this.stateCode)) { //all
			queries.add(DBQuery.in("state.code", this.stateCode));
		}
		if (StringUtils.isNotBlank(this.accession)) {
			queries.add(DBQuery.in("accession", this.accession));
		} else if (CollectionUtils.isNotEmpty(this.accessions)) {
			queries.add(DBQuery.in("accession", this.accessions));
		} else if(StringUtils.isNotBlank(this.accessionRegex)) {
			queries.add(DBQuery.regex("accession", Pattern.compile(this.accessionRegex)));
		}

		if (StringUtils.isNotBlank(this.externalId)) {
			queries.add(DBQuery.in("externalId", this.externalId));
		} else if(CollectionUtils.isNotEmpty(this.externalIds)){
			queries.add(DBQuery.in("externalId", this.externalIds));
		} else if(StringUtils.isNotBlank(this.externalIdRegex)){
			queries.add(DBQuery.regex("externalId", Pattern.compile(this.externalIdRegex)));
		}

		if (CollectionUtils.isNotEmpty(this.codes)) { //all
			queries.add(DBQuery.in("code", this.codes));
		}

		if (CollectionUtils.isNotEmpty(this.codes)) {
			queries.add(DBQuery.in("code", this.codes));
		} else if(StringUtils.isNotBlank(this.codeRegex)){
			queries.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
		}


		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}


	// methode list appelee avec url suivante :
	//localhost:9000/api/sra/projects?datatable=true&paginationMode=local&projCode=BCZ
	// url construite dans services.js 
	//search : function(){
	//	this.datatable.search({projCode:this.form.projCode, state:'N'});
	//},
	// Renvoie le Json correspondant à la liste des study ayant le projectCode indique dans la variable du formulaire projectCode et stockee dans
	// l'instance studiesSearchForm	
}



