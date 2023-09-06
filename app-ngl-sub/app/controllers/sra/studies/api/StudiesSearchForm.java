package controllers.sra.studies.api;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import controllers.DBObjectListForm;
import controllers.sra.api.ISraSearchForm;
import models.sra.submit.sra.instance.AbstractStudy;

public class StudiesSearchForm  extends DBObjectListForm<AbstractStudy> implements ISraSearchForm { 

	public List<String> projCodes       = new ArrayList<>();
	public List<String> stateCodes      = new ArrayList<>(); 
	public List<String>pseudoStateCodes = new ArrayList<>();
	public Boolean confidential         = null;
	public String stateCode             = null;
	public String accession             = null;
	public List<String> accessions      = new ArrayList<>();
	public String accessionRegex;
	public List<String> codes           = new ArrayList<>();
	public String codeRegex;
	public String externalId            = null;
	public List<String> externalIds     = new ArrayList<>();
	public String externalIdRegex;
	public String type;                 // si on veut selectionner Study ou ExternalStudy
	public String createUser;           // pour recherche pour un proprietaire (update-ctrl.js)


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
			// C'est une valeur qui peut prendre une valeur autorisee dans le formulaire. Ici on veut que 
			// l'ensemble des valeurs correspondent à l'ensemble des valeurs du formulaire independamment de l'ordre.
		}
		if(StringUtils.isNotBlank(this.createUser)) {
			queries.add(DBQuery.in("traceInformation.createUser", this.createUser));
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

		if ((this.confidential != null) && (this.confidential==true)) {
			Calendar calendar = Calendar.getInstance();
			Date date_courante  = calendar.getTime();
			queries.add(DBQuery.greaterThan("releaseDate", date_courante));
		}
		if (StringUtils.isNotBlank(this.type)) { //all
			queries.add(DBQuery.in("_type", this.type));
		}	
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}
}
