package controllers.sra.samples.api;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.ngl.NGLApplication;
import models.sra.submit.common.instance.AbstractSample;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class Samples extends DocumentController<AbstractSample> {
	
	private static final play.Logger.ALogger logger = play.Logger.of(Samples.class);

	private final Form<AbstractSample> sampleForm ;

//	@Inject
//	public Samples(NGLContext ctx) {
//		super(ctx,InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class);
////		samplesSearchForm = ctx.form(SamplesSearchForm.class);
//		sampleForm = ctx.form(AbstractSample.class);
//	}

	@Inject
	public Samples(NGLApplication ctx) {
		super(ctx, InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class);
		sampleForm = ctx.form(AbstractSample.class);
	}

	@Override
	public Result get(String code) {
		return ok(Json.toJson(getSample(code)));
	}

	public Result list() {	
		/*
		Form<SamplesSearchForm> samplesSearchFilledForm = filledFormQueryString(samplesSearchForm, SamplesSearchForm.class);
		SamplesSearchForm samplesSearchForm = samplesSearchFilledForm.get();
		//Logger.debug(samplesSearchForm.state);
		Query query = getQuery(samplesSearchForm);
		MongoDBResult<AbstractSample> results = mongoDBFinder(samplesSearchForm, query);				
		List<AbstractSample> samplesList = results.toList();
		if(samplesSearchForm.datatable){
			return ok(Json.toJson(new DatatableResponse<AbstractSample>(samplesList, samplesList.size())));
		}else{
			return ok(Json.toJson(samplesList));
		}
	*/
		SamplesSearchForm form = filledFormQueryString(SamplesSearchForm.class);
		Query query = getQuery(form);
		//MongoDBResult<AbstractSample> results = mongoDBFinder(form, query);							
		//List<AbstractSample> list = results.toList();
		List<AbstractSample> list = MongoDBDAO.find(InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class, query).toList();
		if (form.datatable)
			return ok(Json.toJson(new DatatableResponse<>(list, list.size())));
		return ok(Json.toJson(list));
	}	
	
	
	public Result update(String code) {
		//Get Submission from DB 
		AbstractSample sample = getSample(code);
		Form<AbstractSample> filledForm = getFilledForm(sampleForm, AbstractSample.class);
//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
//			ctxVal.setUpdateMode();
		ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
		if (sample == null) {
//			filledForm.reject("Sample " +  code, "not exist in database");  // si solution filledForm.reject
//			return badRequest(filledForm.errorsAsJson( )); // legit
			ctxVal.addError("Sample " +  code, "not exist in database");  // si solution filledForm.reject
			return badRequest(errorsAsJson(ctxVal.getErrors())); // legit
		}
		System.out.println(" ok je suis dans Samples.update\n");
		AbstractSample sampleInput = filledForm.get();

		if (code.equals(sampleInput.code)) {
//			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
//			ctxVal.getContextObjects().put("type", "sra");
			ctxVal.putObject("type", "sra");
			sampleInput.traceInformation.setTraceInformation(getCurrentUser());
			sampleInput.validate(ctxVal);	
			if (!ctxVal.hasErrors()) {
				logger.debug(" ok je suis dans Samples.update et pas d'erreur\n");
				logger.info("Update sample "+sample.code);
				MongoDBDAO.update(InstanceConstants.SRA_SAMPLE_COLL_NAME, sampleInput);
				logger.debug(Json.toJson(sampleInput).toString());
				return ok(Json.toJson(sampleInput));
			} else {
				logger.debug(" ok je suis dans Samples.update et erreurs \n");
				logger.debug(Json.toJson(sampleInput).toString());
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}		
		} else {
//			filledForm.reject("sample code " + code + " and sampleInput.code " + sampleInput.code , " are not the same");
//			return badRequest(filledForm.errorsAsJson( )); // legit
			ctxVal.addError("sample code " + code + " and sampleInput.code " + sampleInput.code , " are not the same");
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	private AbstractSample getSample(String code) {
		AbstractSample sample = MongoDBDAO.findByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class, code);
		return sample;
	}

	private Query getQuery(SamplesSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		if (CollectionUtils.isNotEmpty(form.projCodes)) { //
			queries.add(DBQuery.in("projectCode", form.projCodes)); // doit pas marcher car pour state.code
			// C'est une valeur qui peut prendre une valeur autorisee dans le formulaire. Ici on veut que 
			// l'ensemble des valeurs correspondent à l'ensemble des valeurs du formulaire independamment de l'ordre.
		}
		if (CollectionUtils.isNotEmpty(form.stateCodes)) { //all
			queries.add(DBQuery.in("state.code", form.stateCodes));
		}
		if (StringUtils.isNotBlank(form.stateCode)) { //all
			queries.add(DBQuery.in("state.code", form.stateCode));
		}
		if(CollectionUtils.isNotEmpty(form.codes)) {
			queries.add(DBQuery.in("code", form.codes));
		} else if(StringUtils.isNotBlank(form.codeRegex)) {
			queries.add(DBQuery.regex("code", Pattern.compile(form.codeRegex)));
		}
		if(CollectionUtils.isNotEmpty(form.accessions)) {
			queries.add(DBQuery.in("accession", form.accessions));
		} else if(StringUtils.isNotBlank(form.accessionRegex)){
			queries.add(DBQuery.regex("accession", Pattern.compile(form.accessionRegex)));
		}
		if(CollectionUtils.isNotEmpty(form.externalIds)) {
			queries.add(DBQuery.in("externalId", form.externalIds));
		} else if(StringUtils.isNotBlank(form.externalIdRegex)){
			queries.add(DBQuery.regex("externalId", Pattern.compile(form.externalIdRegex)));
		}
		if (queries.size() > 0) {
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}
	
}
