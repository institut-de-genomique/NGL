package controllers.sra.readsets.api;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DBObjectListForm;
import controllers.sra.api.ISraSearchForm;
import models.sra.submit.sra.instance.Readset;


public class ReadsetsSearchForm  extends  DBObjectListForm<Readset> implements ISraSearchForm {
	public String stateCode;
	public List<String> stateCodes = new ArrayList<>(); 
	public List<String> pseudoStateCodes = new ArrayList<>();

	public String code;
	public List<String> codes;    
	public String codeRegex;
		
	@SuppressWarnings("unused")
	private static final play.Logger.ALogger logger = play.Logger.of(ReadsetsSearchForm.class);

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

		if(StringUtils.isNotBlank(this.code)) {
			queries.add(DBQuery.in("code", this.code));
		} else if (CollectionUtils.isNotEmpty(this.codes)) {
			queries.add(DBQuery.in("code", this.codes));
		} else if(StringUtils.isNotBlank(this.codeRegex)) {
			queries.add(DBQuery.regex("code", Pattern.compile(this.codeRegex)));
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

