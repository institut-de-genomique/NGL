package controllers.sra.samples.api;

import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;

import controllers.DocumentController;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.play.IGBodyParsers;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.common.instance.AbstractSample;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.ExternalSample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.UserSampleType;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.Tools;
import sra.parser.UserSampleTypeParser;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;
import play.mvc.BodyParser;

public class Samples extends DocumentController<AbstractSample> {

	private static final play.Logger.ALogger logger = play.Logger.of(Samples.class);
	private final Form<SamplesSearchForm>   samplesSearchForm;
	private final Form<AbstractSample>      abstractSampleForm ;
	private final Form<UserFileSamplesForm> userFileSamplesForm;
	private final SraCodeHelper             sraCodeHelper;

	private Map<String, UserSampleType>   mapUserSamples = new HashMap<>();


	@Inject
	public Samples(NGLApplication ctx, SraCodeHelper sraCodeHelper) {
		super(ctx, InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class);
		abstractSampleForm  = ctx.form(AbstractSample.class);
		samplesSearchForm   = ctx.form(SamplesSearchForm.class);
		userFileSamplesForm = ctx.form(UserFileSamplesForm.class);
		this.sraCodeHelper  = sraCodeHelper;

	}
	
	public<T> T getRequestArgs(Class <T> clazz) {
		Form<T> formTemplate = app.form(clazz);                   // template du formulaire
		Form<T> filledForm = getFilledForm(formTemplate, clazz);  //formulaire rempli grace au template et a la classe
		if (filledForm.hasErrors())
			throw new SraValidationException(ContextValidation.createUndefinedContext(getCurrentUser(), filledForm));
		T args = filledForm.get(); // retourne l'objet correspondant à la classe
		return args;
	}
	
	@Override
	public Result get(String code) {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {
			return ok(Json.toJson(getSample(code)));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}
	
	// Pour l'instant uniquement pour les externalSample
	// appelé depuis la vue modifier experiments dans experiment.update-ctrl.js, en faisant passer l'objet externalSample dans body et non pas 
	// en passant les parametres necessaires à construction dans parametres =>
	// $http.post(jsRoutes.controllers.sra.samples.api.Samples.save().url,{"accession":experiment.sampleAccession,"_type":"ExternalSample"})
	public Result save() {
		// recuperation de l'objet userSample grace au mapping entre formulaire et nom de classe:
		Form<AbstractSample> filledForm = getFilledForm(abstractSampleForm, AbstractSample.class); 
		AbstractSample userSample = filledForm.get(); 

		ContextValidation ctxVal = ContextValidation.createCreationContext(getCurrentUser(), filledForm);
		try {
			//logger.debug("Dans controller api.save(), sample.type = "+ userSample._type);
			//logger.debug("filledForm.get()._type=" + filledForm.get()._type);
			
			if (userSample._id != null) {
				ctxVal.addError("Sample_id " + userSample._id, "sample with id " + userSample._id + " already exist");  // si solution filledForm.reject
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			if (userSample.code != null) {
				ctxVal.addError("le sample ne devrait pas contenir de code ici sample.code = " + userSample.code, "le code du sample est determine automatiquement a la creation");  // si solution filledForm.reject
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			userSample.traceInformation = new TraceInformation(); 
			userSample.traceInformation.setTraceInformation(getCurrentUser());
			if (userSample instanceof ExternalSample) {
				String externalSampleCode = sraCodeHelper.generateExternalSampleCode(userSample.accession);
				userSample.code = externalSampleCode;
				//logger.debug("Dans controller sample, dans save, externalSampleCode = " + externalSampleCode);
				//logger.debug("Dans controller sample, dans save, sample.code = " + userSample.code);

				userSample.state = new State(SUB_F, getCurrentUser());
			} else {
				ctxVal.addError("Operation save implementee uniquement pour les externalSample", "creation d'un (internal)Sample demandee", userSample); 
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			userSample.validate(ctxVal);

			if (!ctxVal.hasErrors()) {
				MongoDBDAO.save(InstanceConstants.SRA_SAMPLE_COLL_NAME, userSample);
			} else {
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			return ok(Json.toJson(userSample));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	
	public Result loadUserFileSample() {
		UserFileSamplesForm form = getFilledForm(userFileSamplesForm, UserFileSamplesForm.class).get();
		//UserFileSamplesForm form = getRequestArgs(UserFileSamplesForm.class);
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		
		try {
			logger.debug("form.base64UserFileSample", form.base64UserFileSample);
			//logger.debug("Samples::list::list.size = " + list.size());

			if (StringUtils.isBlank(form.base64UserFileSample)) {
				logger.debug("Pas de fichier utilisateur  dans sampleSearchForm.base64 XXXXXXXXX ");
				form.base64UserFileSample = "";
			} else {
				logger.debug("ok pour fichier utilisateur dans sampleSearchForm");
			}
			logger.debug("Read base64UserFileSample");
			InputStream inputStreamUserFileSamples = Tools.decodeBase64(form.base64UserFileSample);
			UserSampleTypeParser userSamplesParser = new UserSampleTypeParser();
			mapUserSamples = userSamplesParser.loadMap(inputStreamUserFileSamples);
			for (Iterator<Entry<String, UserSampleType>> iterator = mapUserSamples.entrySet().iterator(); iterator.hasNext();) {
				Entry<String, UserSampleType> entry = iterator.next();
//				logger.debug("  cle de sample = '" + entry.getKey() + "'");
//				logger.debug("       title : '" + entry.getValue().getTitle()+ "'");
//				logger.debug("Appel de validate");
				entry.getValue().validate(ctxVal);
			}
			// Renvoyer message d'erreur si le fichier utilisateur n'est pas bon:
			if (ctxVal.hasErrors()) {
				logger.debug("Erreur dans le fichier utilisateur :");
				ctxVal.displayErrors(logger, "debug");
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			} 
			return ok(Json.toJson(mapUserSamples));
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			e.printStackTrace();
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}
		

	public Result list() {	
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {
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
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}	

	// Met a jour le sample dont le code est indiqué avec les valeurs presentes dans le sample recuperé du formulaire (userSample)
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Result update(String code) {
		//Get Submission from DB 
		AbstractSample sample = getSample(code);
		Form<AbstractSample> filledForm = getFilledForm(abstractSampleForm, AbstractSample.class);

		//		ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm); 	
		//			ctxVal.setUpdateMode();
		ContextValidation ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
		try {
			if (sample == null) {
				//			filledForm.reject("Sample " +  code, "not exist in database");  // si solution filledForm.reject
				//			return badRequest(filledForm.errorsAsJson( )); // legit
				ctxVal.addError("Sample " +  code, "not exist in database");  // si solution filledForm.reject
				return badRequest(errorsAsJson(ctxVal.getErrors())); // legit
			}
			//logger.debug(" ok je suis dans controllers.sra.samples.api.Samples.javaSamples.update\n");
			AbstractSample sampleInput = filledForm.get();

			if (code.equals(sampleInput.code)) {
				//			ContextValidation ctxVal = new ContextValidation(getCurrentUser(), filledForm.errors()); 	
				//			ctxVal.getContextObjects().put("type", "sra");
				sampleInput.traceInformation.setTraceInformation(getCurrentUser());
				sampleInput.validate(ctxVal);	
				if (!ctxVal.hasErrors()) {
					//logger.debug(" ok je suis dans Samples.update et pas d'erreur\n");
					//logger.info("Update sample "+sample.code);
					MongoDBDAO.update(InstanceConstants.SRA_SAMPLE_COLL_NAME, sampleInput);
					//logger.debug(Json.toJson(sampleInput).toString());
					return ok(Json.toJson(sampleInput));
				} else {
					//logger.debug(" ok je suis dans Samples.update et erreurs \n");
					//logger.debug(Json.toJson(sampleInput).toString());
					// return badRequest(filledForm.errors-AsJson());
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}		
			} else {
				//			filledForm.reject("sample code " + code + " and sampleInput.code " + sampleInput.code , " are not the same");
				//			return badRequest(filledForm.errorsAsJson( )); // legit
				ctxVal.addError("sample code " + code + " and sampleInput.code " + sampleInput.code , " are not the same");
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
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
		SamplesSearchForm.copyPseudoStateCodesToStateCodesInFormulaire(form);

		if (CollectionUtils.isNotEmpty(form.projCodes)) { //
			queries.add(DBQuery.in("projectCode", form.projCodes)); // doit pas marcher car pour state.code
			// C'est une valeur qui peut prendre une valeur autorisee dans le formulaire. Ici on veut que 
			// l'ensemble des valeurs correspondent à l'ensemble des valeurs du formulaire independamment de l'ordre.
		}
		if (CollectionUtils.isNotEmpty(form.stateCodes)) { //all
			queries.add(DBQuery.in("state.code", form.stateCodes));
		} else if (StringUtils.isNotBlank(form.stateCode)) { //all
			//logger.debug("stateCode dans Samples.getQuery : " + form.stateCode);
			queries.add(DBQuery.in("state.code", form.stateCode));
		}
		if(StringUtils.isNotBlank(form.createUser)) {
			queries.add(DBQuery.in("traceInformation.createUser", form.createUser));
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
		if (StringUtils.isNotBlank(form.externalId)) {
			queries.add(DBQuery.in("externalId", form.externalId));
		} else if(CollectionUtils.isNotEmpty(form.externalIds)) {
			queries.add(DBQuery.in("externalId", form.externalIds));
		} else if(StringUtils.isNotBlank(form.externalIdRegex)){
			queries.add(DBQuery.regex("externalId", Pattern.compile(form.externalIdRegex)));
		}
		if (StringUtils.isNotBlank(form.type)) { //all
			queries.add(DBQuery.in("_type", form.type));
		}				
		if (queries.size() > 0) {
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		//		logger.debug("query = " + query.toString());

		return query;
	}


}
