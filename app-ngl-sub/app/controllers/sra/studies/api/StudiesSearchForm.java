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
	public String identifier            = null;
	public List<String> identifiers     = new ArrayList<>();
	public String identifierRegex;	
	public List<String> codes           = new ArrayList<>();
	public String codeRegex;
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
		if (StringUtils.isNotBlank(this.identifier)) {
            queries.add(DBQuery.or(DBQuery.in("accession", identifier),DBQuery.in("externalId", identifier)));
		} else if (CollectionUtils.isNotEmpty(this.identifiers)) {
			List<String> identifiers_ERP     = new ArrayList<>();
			List<String> identifiers_PRJ     = new ArrayList<>();
			for(String identifier : this.identifiers) {
				if(identifier.startsWith("PRJ")) {
					identifiers_PRJ.add(identifier);
				} else {
					identifiers_ERP.add(identifier);
				}
			}
            queries.add(DBQuery.or(DBQuery.in("accession", identifiers_ERP),DBQuery.in("externalId", identifiers_PRJ)));
		} else if(StringUtils.isNotBlank(this.identifierRegex)) {
			// ne passe pas car trop volumineux, il faut distinguer les 2 cas :
            //queries.add(DBQuery.or(DBQuery.in("accession", Pattern.compile(this.identifierRegex)),DBQuery.in("externalId", Pattern.compile(this.identifierRegex)));
			if (this.identifierRegex.startsWith("PRJ")) {
				queries.add(DBQuery.regex("externalId", Pattern.compile(this.identifierRegex)));
			} else {
				queries.add(DBQuery.regex("accession", Pattern.compile(this.identifierRegex)));
			} 
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
