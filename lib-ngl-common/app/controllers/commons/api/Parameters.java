package controllers.commons.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;

import models.laboratory.parameter.Parameter;
import java.util.regex.Pattern; //FDS 
import org.apache.commons.lang.time.DateUtils;

import models.utils.InstanceConstants;
import models.utils.ListObject;
import models.utils.dao.DAOException;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import views.components.datatable.DatatableResponse;

/* FDS/EJ 27/11/2018 Il faudra creer une ressource "Index/Tag" distincte des parametres avec sa propre collection Mongo...
 * au CNS il y des type de parametres qui n'ont rien a voir avec les tag/index de sequencage...
	 => "BBP11", "map-parameter", "context-description"
   NGL-836: Pour l'instant completer Parameter... 
            oui mais filtrer les categoryCode ci-dessus qui posent probleme car ne mappent pas le modele...package models.laboratory.parameter.index;
   */

public class Parameters extends DocumentController<Parameter> {
	
	private final static play.Logger.ALogger logger = play.Logger.of(Parameters.class);
	
	// GA 24/07/2015 implementaton de la form +  params list et datatable SOL
	private final Form<ParametersSearchForm> form;
	
	@Inject
	public Parameters(NGLApplication app){
		super(app,InstanceConstants.PARAMETER_COLL_NAME, Parameter.class);
		this.form = getNGLContext().form(ParametersSearchForm.class);
	}

	public Result list() {
		Form<ParametersSearchForm> filledForm = filledFormQueryString(form, ParametersSearchForm.class);
		ParametersSearchForm parametersSearch = filledForm.get();
		return list(parametersSearch);
	}

	public Result listByCode(String typeCode) {
		Form<ParametersSearchForm> filledForm = filledFormQueryString(form, ParametersSearchForm.class);
		ParametersSearchForm parametersSearch = filledForm.get();
		parametersSearch.typeCode=typeCode;
		return list(parametersSearch);
	}
	
	private Result list(ParametersSearchForm parametersSearch) {
		Query query = getQuery(parametersSearch);		
		
		List<Parameter> values=MongoDBDAO.find(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, query).toList();
		
		if (parametersSearch.datatable) {
		    return ok(Json.toJson(new DatatableResponse<>(values, values.size())));
		} else if (parametersSearch.list) {
		    List<ListObject> valuesListObject = new ArrayList<>();
		    for (Parameter s : values) {
		    	valuesListObject.add(new ListObject(s.code, s.name));
		    }
		    return ok(Json.toJson(valuesListObject));
		} else {
			return ok(Json.toJson(values));
		}
	}
	
	public Result get(String typeCode, String code) throws DAOException {
		Parameter index=MongoDBDAO.findOne(InstanceConstants.PARAMETER_COLL_NAME, Parameter.class, DBQuery.is("typeCode", typeCode).is("code", code));
		if (index != null)
			return ok(Json.toJson(index));
		return notFound();
	}
	
	/**
	 * build a query from a Form
	 * @param form
	 * @return query
	 */
	private static Query getQuery(ParametersSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		
		//!!utiliser CollectionUtils.isNotEmpty pour tester les parametres qui sont de type Set<String>
		
		if(CollectionUtils.isNotEmpty(form.typeCodes)){  
			queries.add(DBQuery.in("typeCode", form.typeCodes));
		} else if (StringUtils.isNotBlank(form.typeCode)) { 
			queries.add(DBQuery.is("typeCode", form.typeCode));
		}else {
			//------------------ filtres pour NGL-836------------------------------------
			// Attention au CNS il y des types parametres qui n'ont rien a voir avec les tag/index de sequencage...
			queries.add(DBQuery.in("typeCode","index-illumina-sequencing","index-nanopore-sequencing")); // !! rend cette classe specifique aux index de sequencage...
		}
		//modifié FDS NGL-836
		if(CollectionUtils.isNotEmpty(form.sequences)){
			queries.add(DBQuery.in("sequence", form.sequences));
		}else if (StringUtils.isNotBlank(form.sequence)) { 
			queries.add(DBQuery.is("sequence", form.sequence)); // necessaire quand ???
		} else if(StringUtils.isNotBlank(form.sequenceRegex)){
			queries.add(DBQuery.in("sequence",  Pattern.compile(form.sequenceRegex)));
		}
		
		if(CollectionUtils.isNotEmpty(form.categoryCodes)){
				queries.add(DBQuery.in("categoryCode", form.categoryCodes));
		}else if(StringUtils.isNotBlank(form.categoryCode)){
			queries.add(DBQuery.is("categoryCode", form.categoryCode));
		}
		
		
		if(CollectionUtils.isNotEmpty(form.codes)){
			queries.add(DBQuery.in("code", form.codes));
		} else if(StringUtils.isNotBlank(form.codeRegex)){
			queries.add(DBQuery.regex("code", Pattern.compile(form.codeRegex)));
		}
		
		if(CollectionUtils.isNotEmpty(form.names)){
			queries.add(DBQuery.in("name", form.names));
		} else if(StringUtils.isNotBlank(form.nameRegex)){
			queries.add(DBQuery.regex("name", Pattern.compile(form.nameRegex)));
		}
		
		if(CollectionUtils.isNotEmpty(form.groupNames)){
			queries.add(DBQuery.in("groupNames", form.groupNames));
		}
		
		if(CollectionUtils.isNotEmpty(form.shortNames)){
			queries.add(DBQuery.in("shortName", form.shortNames));
		} else if(StringUtils.isNotBlank(form.shortNameRegex)){
			queries.add(DBQuery.regex("shortName", Pattern.compile(form.shortNameRegex)));
		}
		
		if(StringUtils.isNotBlank(form.supplierNameRegex)){
			queries.add(DBQuery.regex("supplierName", Pattern.compile(form.supplierNameRegex)));
		}
		
		if(null != form.size ) {
			if ( StringUtils.isNumeric(form.size)) {
				//queries.add(DBQuery.where("function() {return (this.sequence.length=="+form.size+")}"));
				// !! les index DUAL et POOL contiennent des "-"
				queries.add(DBQuery.where("function() {return (this.sequence.replace(/-/g,'').length=="+form.size+")}"));
			} else {
				//empecher autrement ???
				logger.error("size not numeric:"+ form.size);
				queries.add(DBQuery.where("function() {return (this.sequence.length==9999)}"));
			}
		}
		
		if(null != form.fromDate){
			queries.add(DBQuery.greaterThanEquals("traceInformation.creationDate", form.fromDate));
		}
		if(null != form.toDate){
			queries.add(DBQuery.lessThan("traceInformation.creationDate", (DateUtils.addDays(form.toDate, 1))));
		}

		if(StringUtils.isNotBlank(form.createUser)){
			queries.add(DBQuery.is("traceInformation.createUser", form.createUser));
		}
		
		if(queries.size() > 0){
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}

		
		return query;
	}
}
