package controllers.sra.submissions.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import controllers.sra.api.ISraSearchForm;
import models.sra.submit.sra.instance.Submission;

public class SubmissionsSearchForm  extends DBObjectListForm<Submission> implements ISraSearchForm {
	public List<String> projCodes; // meme nom que dans la vue et les services .js
	public String stateCode;
	public List<String> stateCodes; 
	public List<String> pseudoStateCodes; 
	public List<String> accessions;
	public String accessionRegex;
	public List<String> codes;
	public String codeRegex;
	public String createUser;      // pour recherche pour un proprietaire (update-ctrl.js)
	public String type;


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

	if (CollectionUtils.isNotEmpty(this.projCodes)) { 
		queries.add(DBQuery.in("projectCodes", this.projCodes)); // doit pas marcher car pour state.code
		// C'est une valeur qui peut prendre une valeur autorisee dans le formulaire. Ici on veut que 
		// l'ensemble des valeurs correspondent à l'ensemble des valeurs du formulaire independamment de l'ordre.
	}
	if(StringUtils.isNotBlank(this.createUser)) {
		queries.add(DBQuery.in("traceInformation.createUser", this.createUser));
	}
	if(StringUtils.isNotBlank(this.type)) {
		queries.add(DBQuery.in("type", this.type));
	}
	if (CollectionUtils.isNotEmpty(this.stateCodes)) { //all
		queries.add(DBQuery.in("state.code", this.stateCodes));
	} else if (StringUtils.isNotBlank(this.stateCode)) { //all
		queries.add(DBQuery.in("state.code", this.stateCode));
	}
	if (CollectionUtils.isNotEmpty(this.accessions)) { //all
		queries.add(DBQuery.in("accession", this.accessions));
	}	
	if (CollectionUtils.isNotEmpty(this.accessions)) {
		queries.add(DBQuery.in("accession", this.accessions));
	} else if(StringUtils.isNotBlank(this.accessionRegex)) {
		queries.add(DBQuery.regex("accession", Pattern.compile(this.accessionRegex)));
	}
	if (CollectionUtils.isNotEmpty(this.codes)) { //all
		queries.add(DBQuery.in("code", this.codes));
	}
	if (CollectionUtils.isNotEmpty(this.codes)) {
		queries.add(DBQuery.in("code", this.codes));
	} else if(StringUtils.isNotBlank(this.codeRegex)) {
		queries.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
	}
	if (queries.size() > 0) {
		query = DBQuery.and(queries.toArray(new Query[queries.size()]));
	}
	return query;
}




}