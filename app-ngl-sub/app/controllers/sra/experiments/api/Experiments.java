package controllers.sra.experiments.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import controllers.QueryFieldsForm;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.MongoDBResult;
import fr.cea.ig.ngl.NGLApplication;
import fr.cea.ig.play.IGBodyParsers;
import models.sra.submit.common.instance.UserExperimentExtendedType;
import models.sra.submit.common.instance.UserExperimentType;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import models.utils.InstanceConstants;
import play.data.Form;
import play.libs.Json;
import play.mvc.Result;
import services.Tools;
import sra.parser.UserExperimentExtendedTypeParser;
import sra.parser.UserExperimentTypeParser;
import play.mvc.BodyParser;
import validation.ContextValidation;
import views.components.datatable.DatatableResponse;

public class Experiments extends DocumentController<Experiment> {

	private static final play.Logger.ALogger logger = play.Logger.of(Experiments.class);
	private static final List<String>        authorizedUpdateFields = Arrays.asList("accession");

	private Map<String, UserExperimentType>  mapUserExperiments = new HashMap<>();
	private Map<String, UserExperimentExtendedType>  mapUserExperimentsExtended = new HashMap<>();

	final Form<ExperimentsSearchForm>        experimentsSearchForm;
	final Form<Experiment>                   experimentForm;
	final Form<QueryFieldsForm>              updateForm;
	private final Form<UserFileExperimentsForm>      userFileExperimentsForm;
	
	
	
	@Inject
	public Experiments(NGLApplication app) {
		super(app,InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class);
		userFileExperimentsForm = app.form(UserFileExperimentsForm.class);
		experimentsSearchForm = app.form(ExperimentsSearchForm.class);
		experimentForm = app.form(Experiment.class);
		updateForm = app.form(QueryFieldsForm.class);
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
			Experiment exp  = MongoDBDAO.findOne(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, 

					Experiment.class, 
					DBQuery.is("code", code));
			if (exp != null) {
				return ok(Json.toJson(exp));
			} else {
				return notFound();
			}	
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}



	public Result loadUserFileExperiment(String typeParser) {
		UserFileExperimentsForm form = getFilledForm(userFileExperimentsForm, UserFileExperimentsForm.class).get();
		//UserFileExperimentsForm form = getRequestArgs(UserFileExperimentsForm.class);
		
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		if (typeParser == null ) {
			String message = "typeParser autorises: userExperiment ou userExperimentExtented et ici typeParser passe en arg = " + typeParser;
			ctxVal.addError("RuntimeException", message);
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
		//logger.debug("typeParser=" + typeParser);
		try {
			//logger.debug("form.base64UserFileExperiment", form.base64UserFileExperiment);
			//logger.debug("Experiments::list::list.size = " + list.size());

			if (StringUtils.isBlank(form.base64UserFileExperiment)) {
				//logger.debug("Pas de fichier utilisateur  dans experimentSearchForm.base64 XXXXXXXXX ");
				form.base64UserFileExperiment = "";
			} else {
				//logger.debug("ok pour fichier utilisateur dans experimentSearchForm");
			}
			//logger.debug("Read base64UserFileExperiment");
			InputStream inputStreamUserFileExperiments = Tools.decodeBase64(form.base64UserFileExperiment);
			
			if (typeParser.equalsIgnoreCase("userExperiment")) {

				UserExperimentTypeParser userExperimentsParser = new UserExperimentTypeParser();
				mapUserExperiments = userExperimentsParser.loadMap(inputStreamUserFileExperiments);
				for (Iterator<Entry<String, UserExperimentType>> iterator = mapUserExperiments.entrySet().iterator(); iterator.hasNext();) {
					Entry<String, UserExperimentType> entry = iterator.next();
//					logger.debug("  cle de exp = '" + entry.getKey() + "'");
//					logger.debug("       nominal_length : '" + entry.getValue().getNominalLength()+ "'");
//					logger.debug("Appel de validate");
					entry.getValue().validate(ctxVal);
				}
			
				// Renvoyer message d'erreur si le fichier utilisateur n'est pas bon:
				if (ctxVal.hasErrors()) {
					logger.debug("Erreur dans le fichier utilisateur :");
					ctxVal.displayErrors(logger, "debug");
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				} 
				return ok(Json.toJson(mapUserExperiments));
			} else if (typeParser.equalsIgnoreCase("userExperimentExtended")) {
				UserExperimentExtendedTypeParser userExperimentsExtendedParser = new UserExperimentExtendedTypeParser();
				mapUserExperimentsExtended = userExperimentsExtendedParser.loadMap(inputStreamUserFileExperiments);
				for (Iterator<Entry<String, UserExperimentExtendedType>> iterator = mapUserExperimentsExtended.entrySet().iterator(); iterator.hasNext();) {
					Entry<String, UserExperimentExtendedType> entry = iterator.next();
					//logger.debug("  XXXXXXXXXXXX    cle de exp = '" + entry.getKey() + "'");
					//logger.debug("       study_ac : '" + entry.getValue().getStudyAccession()+ "'");
					//logger.debug("Appel de validate");
					entry.getValue().validate(ctxVal);
				}
				// Renvoyer message d'erreur si le fichier utilisateur n'est pas bon:
				if (ctxVal.hasErrors()) {
					logger.debug("Erreur dans le fichier utilisateur :");
					ctxVal.displayErrors(logger, "debug");
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				} 
				return ok(Json.toJson(mapUserExperimentsExtended));
			} else {
				String message = "typeParser autorises: userExperiment ou userExperimentExtented et ici typeParser passe en arg = " + typeParser;
				ctxVal.addError("RuntimeException", message);
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
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
		//if (true){return ok(Json.toJson(new ArrayList<Experiment>()));}

		Form<ExperimentsSearchForm> experimentssSearchFilledForm = filledFormQueryString(experimentsSearchForm, ExperimentsSearchForm.class);
		ExperimentsSearchForm form = experimentssSearchFilledForm.get();
		//ExperimentsSearchForm form = getRequestArgs(ExperimentsSearchForm.class);

		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {
			Query query = getQuery(form);
			MongoDBResult<Experiment> results = mongoDBFinder(form, query);							
			List<Experiment> list = results.toList();
			// Remaniement de la liste des experiments avec valeurs utilisateurs
			// si pas d'erreur dans le fichier utilisateur et si experiment
			// recuperés dans base est bien editable :
//			updateExperimentDbWithUserData(form, list, mapUserExperiments);
			if (form.datatable) {
				return ok(Json.toJson(new DatatableResponse<>(list, list.size())));
			} else {
				return ok(Json.toJson(list));
			} 
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			e.printStackTrace();
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}


	// Met a jour l'experiment dont le code est indiqué avec les valeurs presentes dans l'experiment recuperé du formulaire (userExperiment)
	@BodyParser.Of(value = IGBodyParsers.Json15MB.class)
	public Result update(String code) {
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser());
		try {
			Form<Experiment> filledForm = getFilledForm(experimentForm, Experiment.class);
			ctxVal = ContextValidation.createUndefinedContext(this.getCurrentUser(), filledForm);
			Experiment userExperiment = filledForm.get();
			Form<QueryFieldsForm> filledQueryFieldsForm = filledFormQueryString(updateForm, QueryFieldsForm.class);
			QueryFieldsForm queryFieldsForm = filledQueryFieldsForm.get();

			Experiment experiment = getObject(code);
			//logger.debug(" ok je suis dans controllers.sra.experiments.api.Experiments.javaExperiments.update\n");

			if (experiment == null) {
				//return badRequest("Submission with code "+code+" not exist");
				ctxVal.addError("experiments ", " not exist");
				// return badRequest(filledForm.errors-AsJson());
				return badRequest(errorsAsJson(ctxVal.getErrors()));
			}
			//logger.debug("TOTO_1  updateExperiment : {}", userExperiment.code );
			if (queryFieldsForm.fields == null) {
				
				if (code.equals(userExperiment.code)) {
					ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm); 
					userExperiment.traceInformation.setTraceInformation(getCurrentUser());
					userExperiment.validate(ctxVal);
					if (!ctxVal.hasErrors()) {
						MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, userExperiment);
						return ok(Json.toJson(userExperiment));
					} else {
						ctxVal.displayErrors(logger, "debug");
						return badRequest(errorsAsJson(ctxVal.getErrors()));
					}
				} else {
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}	
			} else {
				ctxVal = ContextValidation.createUpdateContext(getCurrentUser(), filledForm);
				validateAuthorizedUpdateFields(ctxVal, queryFieldsForm.fields, authorizedUpdateFields);
				validateIfFieldsArePresentInForm(ctxVal, queryFieldsForm.fields, filledForm);
				if (!ctxVal.hasErrors()) {
					updateObject(DBQuery.and(DBQuery.is("code", code)), 
							getBuilder(userExperiment, queryFieldsForm.fields).set("traceInformation", getUpdateTraceInformation(experiment.traceInformation)));

					return ok(Json.toJson(getObject(code)));
				} else {
					return badRequest(errorsAsJson(ctxVal.getErrors()));
				}		
			}
		} catch(RuntimeException e) {
			ctxVal.addError("RuntimeException", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		} catch(Exception e) {
			ctxVal.addError("Exception", e.getMessage());
			return badRequest(errorsAsJson(ctxVal.getErrors()));
		}
	}

	private Query getQuery(ExperimentsSearchForm form) {
		List<Query> queries = new ArrayList<>();
		Query query = null;
		//logger.info("DANS QUERY : pseudoStateCodes[0] = '" + form.pseudoStateCodes.get(0) + "'");
//		final List<String>  list_ebiKnown = Arrays.asList(
//				SRASubmissionStateNames.SUB_F,
//				SRASubmissionStateNames.SUBR_N,
//				SRASubmissionStateNames.SUBR_SMD_F,
//				SRASubmissionStateNames.SUBR_SMD_IW,
//				SRASubmissionStateNames.SUBR_SMD_IP,
//				SRASubmissionStateNames.SUBR_SMD_F,
//				SRASubmissionStateNames.SUBR_SMD_FE,
//				SRASubmissionStateNames.SUBU_N,
//				SRASubmissionStateNames.SUBU_SMD_IW,
//				SRASubmissionStateNames.SUBU_SMD_IP,
//				SRASubmissionStateNames.SUBU_SMD_F,
//				SRASubmissionStateNames.SUBU_SMD_FE,
//				SRASubmissionStateNames.SUBU_FE);	
		//final List<String>  list_ebiKnown = VariableSRA.mapPseudoStateCodeToStateCodes().get("pseudo_ebiKnown");

		ExperimentsSearchForm.copyPseudoStateCodesToStateCodesInFormulaire(form);
		//logger.debug("form.pseudoStateCodes :" + form.pseudoStateCodes);
		//logger.debug("form.stateCodes :" + form.stateCodes);

		if (StringUtils.isNotBlank(form.studyCode)) { //all
			//logger.debug("getQuery::form.studyCode=" + form.studyCode);
			queries.add(DBQuery.in("studyCode", form.studyCode));
		}
		if(StringUtils.isNotBlank(form.createUser)) {
			queries.add(DBQuery.in("traceInformation.createUser", form.createUser));
		}
		if (StringUtils.isNotBlank(form.studyAccession)) { //all
			//logger.debug("getQuery::form.studyAccession=" + form.studyAccession);
			queries.add(DBQuery.in("studyAccession", form.studyAccession));
		}
		if (StringUtils.isNotBlank(form.sampleAccession)) { //all
			//logger.debug("getQuery::form.sampleAccession=" + form.sampleAccession);
			queries.add(DBQuery.in("sampleAccession", form.sampleAccession));
		}
		if (StringUtils.isNotBlank(form.sampleCode)) { //all
			//logger.debug("getQuery::form.sampleCode=" + form.sampleCode);
			queries.add(DBQuery.in("sampleCode", form.sampleCode));
		}
		if (CollectionUtils.isNotEmpty(form.projCodes)) { //
			//logger.debug("getQuery::form.projCodes=" + form.projCodes);
			queries.add(DBQuery.in("projectCode", form.projCodes)); 
		}
		if (CollectionUtils.isNotEmpty(form.stateCodes)) { //all
			//logger.debug("getQuery::form.stateCodes=" + form.stateCodes);
			queries.add(DBQuery.in("state.code", form.stateCodes));
		} else if (StringUtils.isNotBlank(form.stateCode)) { //all
			//if (form.stateCode.equalsIgnoreCase("ebiKnown")) {
			//	logger.debug("form.stateCode:: getQuery:: form.stateCode = " + form.stateCode);				
			//	queries.add(DBQuery.in("state.code", list_ebiKnown));
			//} else {
				//logger.debug("form.stateCode:: getQuery::form.stateCode=" + form.stateCode);
				queries.add(DBQuery.in("state.code", form.stateCode));
			//}
		}
		if(CollectionUtils.isNotEmpty(form.codes)) {
			//logger.debug("form.codes:: getQuery::form.codes=" + form.codes);
			queries.add(DBQuery.in("code", form.codes));
		} else if(StringUtils.isNotBlank(form.codeRegex)) {
			//logger.debug("form.codeRegex ::getQuery = " + form.code);
			queries.add(DBQuery.regex("code", Pattern.compile(form.codeRegex)));
		}
		if(CollectionUtils.isNotEmpty(form.accessions)) {
			//logger.debug("form.accessions ::getQuery = " + form.accessions);
			for (String ac: form.accessions) {
				logger.debug("form.accessions:: getQuery = " + ac);
			}
			queries.add(DBQuery.in("accession", form.accessions));
		} else if(StringUtils.isNotBlank(form.accessionRegex)){
			//logger.debug("form.accessionRegex:: getQuery = " + form.accessionRegex);
			queries.add(DBQuery.regex("accession", Pattern.compile(form.accessionRegex)));
		}
		if(queries.size() > 0){
			//logger.debug("getQuery::queries");
			query = DBQuery.and(queries.toArray(new Query[queries.size()]));
		}
		return query;
	}

}
