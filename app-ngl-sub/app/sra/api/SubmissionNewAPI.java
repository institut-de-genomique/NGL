package sra.api;

import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUBR_SMD_FE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
//import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.authentication.Authentication;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import mail.MailServiceException;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Attribute;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.Submission.TypeRawDataSubmitted;
import models.sra.submit.sra.instance.UserRefCollabType;
import ngl.refactoring.state.SRASubmissionStateNames;
import services.sra.EbiAPI;
import services.sra.NGLSUBConfig;
import services.sra.SraException;
import services.sra.SraValidationException;
import services.taxonomy.TaxonomyServices;
import sra.services.ActivateServices;
import sra.services.CreateServices;
import sra.services.DatabaseConsistencyTools;
import sra.services.EbiFileResponseServices;
import sra.services.ReleaseServices;
import sra.services.UpdateServices;
import sra.services.XmlServices;
import validation.ContextValidation;

/**
 * Point d'entrée des differentes actions dans ngl-sub. 
 * Les controlleurs et le workflow ne devraient passer que par cette API pour declencher des actions dans ngl-sub.
 * @author sgas
 *
 */
// Attention les methodes qui modifient le state de l'objet submission devraient prendre l'objet submission 
// en argument (et non la string submissionCode) pour eviter d'avoir a gerer des rechargement de la soumission
// depuis la base dans le workflow.

public class SubmissionNewAPI {

	private final String ebiCurlProd       = "webin-9_PROD";  // valeur dans application.conf pour ebiCurl si PROD
	private final String ebiCurlTest9      = "webin-9_TEST";  // valeur possible dans application.conf pour ebiCurl si TEST
	private final String ebiCurlTest261    = "webin-261_TEST";// valeur possible dans application.conf pour ebiCurl si TEST
	private final String ebiCurl;

	private final CreateServices            createServices;
	private final XmlServices               xmlServices;
	private final ActivateServices          activateServices;
	private final ReleaseServices           releaseServices;
	private final UpdateServices            updateServices;
    private final EbiFileResponseServices   ebiFileResponseServices;
	private final SubmissionDAO             submissionDAO;
    private final SubmissionNewAPITools     submissionNewAPITools;
    private final DatabaseConsistencyTools  correctExternalStudyAndSample;
	public final  EbiAPI                    ebiAPI;

	private static final play.Logger.ALogger logger = play.Logger.of(SubmissionNewAPI.class);
	@Inject
	SubmissionNewAPI(CreateServices            createServices,
					 XmlServices               xmlServices,
//					 FileAcServices            fileAcServices,
					 ActivateServices          activateServices,
					 ReleaseServices           releaseServices,
					 UpdateServices            updateServices,
					 EbiFileResponseServices   ebiFileResponseServices, 
					 SubmissionDAO             submissionDAO,
					 SubmissionNewAPITools     submissionNewAPITools,
					 DatabaseConsistencyTools  correctExternalStudyAndSample,
					 EbiAPI                    ebiAPI,
					 NGLSUBConfig              config
					 ) {
		this.createServices                = createServices;
		this.xmlServices                   = xmlServices;
		this.activateServices              = activateServices;
		this.releaseServices               = releaseServices;
		this.updateServices                = updateServices;
		this.ebiFileResponseServices       = ebiFileResponseServices;
		this.submissionDAO                 = submissionDAO;
	    this.submissionNewAPITools         = submissionNewAPITools;
	    this.correctExternalStudyAndSample = correctExternalStudyAndSample;
	    this.ebiAPI                        = ebiAPI;
		this.ebiCurl                       = config.getEbiCurl();
		
	}
	
	/**
	 * deprecated dans SubmissionWorkflows si {link TransitionAutomaticState}, <br>
	 * replaced by {@link #ebiCurl(Submission, ContextValidation)}. <br>
	 * ok dans SubmissionWorkflows si {link TransitionManualState},
	 * @param submission    soumission
	 * @param ctx           contexte de validation  
	 * @throws SraException SraException
	 */
	public void ebiCurlForRelease(Submission submission, ContextValidation ctx) throws SraException {
		logger.debug("iiiiii    entree dans ebiCurlForRelease avec submission=" + submission.code + " et etat=" + submission.state.code);
		// Attention, on peut venir de SUBR_SMD_F si on est passé par SUB_FE ou SUBR_FE
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBR_SMD_IP);
		ebiCurl(submission, ctx);			
		String user = ctx.getUser();
		// String user = Authentication.getUser(); pose probleme si on fait tourner cron en isoprod sur ngl-data
		
		// installation etat après action:
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBR_SMD_F, user));
		ctx.setUpdateMode();
		// validation + cascade du state et traceInformation à tous les objets de la soumission :
		submission.validate(ctx);
		SraException.assertNoError(ctx);
		submissionDAO.saveStateAndTrace(submission);
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}

	
	public void ebiCurl(Submission submission, ContextValidation ctx) {
		if(this.ebiCurl.equalsIgnoreCase(this.ebiCurlTest9)) {
			ebiAPI.devWebin9Curl(submission, ctx);
		} else if (this.ebiCurl.equalsIgnoreCase(ebiCurlTest261)) {
			ebiAPI.devWebin261Curl(submission, ctx);
		} else if (this.ebiCurl.equalsIgnoreCase(ebiCurlProd)) {
			ebiAPI.prodWebin9Curl(submission, ctx);
		} else {
			ctx.addError("fichier de configuration application.conf ", "variable ebiCurl avec valeur inconnue " + this.ebiCurl); 
			return;
		} 
	}


	
	
	public Study createAndSaveStudyEntity(List<String> projectCodes, 
										  String existingStudyType, 
										  String centerProjectName, 
										  String title, 
										  String description, 
										  String dropBox, 
										  String strTaxonId, 
										  String strIdsPubmeds, 
										  String strLocusTagPrefixs, 
										  String strParentProjectAccessions,
										  String strAttributes, 
										  ContextValidation contextValidation) throws SraException, SraValidationException, IOException, APIValidationException, APIException {
		
		//logger.debug("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY                             Dans submissionNewAPI.createAndSaveStudyEntity");
		return createServices.createAndSaveStudyEntity(projectCodes, 
													   existingStudyType, 
													   centerProjectName, 
													   title, 
													   description, 
													   dropBox, 
												       strTaxonId, 
												       strIdsPubmeds, 
												       strLocusTagPrefixs, 
												       strParentProjectAccessions, 
												       strAttributes, 
												       contextValidation);
	}
	
	
	// utilisé par CreateProjectAtlasea : permet de creer un study sans le sauver et changer son code et son status avant de sauver en base :
	public Study createStudyEntity(List<String> projectCodes, 
								   String existingStudyType, 
								   String centerProjectName, 
								   String title, 
								   String description, 
								   String dropBox, 
								   String strTaxonId, 
								   String strIdsPubmeds, 
								   String strLocusTagPrefixs, 
								   String strParentProjectAccessions,
								   String strAttributes, 
								   ContextValidation contextValidation) {
		//logger.debug("YYYYYYYYYYYYYYYYYYYYYYYYYYYYYYYY                             Dans submissionNewAPI.createStudyEntity");
		return createServices.createStudyEntity(projectCodes, 
												existingStudyType, 
												centerProjectName, 
												title, 
												description, 
												dropBox, 
												strTaxonId, 
												strIdsPubmeds, 
												strLocusTagPrefixs, 
												strParentProjectAccessions, 
												strAttributes, 
												contextValidation);
	}
	// utilisé par CreateProjectAtlasea : permet de creer un project umbrella sans le sauver et changer son code et son status avant de sauver en base :
	public Project createUmbrellaEntity (String title, 
			                             String description,  
			                             String dropBox, 
			                             String strTaxonId, 
			                             String attributes,
			                             List<String>idsPubmed, 
			                             List<String>childrenProjectAccessions, 
			                             List<String>parentProjectAccessions, 
			                             ContextValidation ctxVal ) {
		return createServices.createUmbrellaEntity(title, description,  dropBox, 
				                    strTaxonId, attributes, idsPubmed, 
				                    childrenProjectAccessions, parentProjectAccessions, ctxVal);
	}
	
	
	
	// synchronise les valeurs de certains champs du study
	public void synchronyseValuesInStudy(Study study, ContextValidation contextValidation) {
		submissionNewAPITools.synchronyseValuesInStudy(study, contextValidation);
	}
	
	// synchronise les valeurs de certains champs de l'umbrella
	public void synchronyseValuesInUmbrella(Project umbrella, ContextValidation contextValidation) {
		submissionNewAPITools.synchronyseValuesInUmbrella(umbrella, contextValidation);
	}
	
	public String initPrimarySubmission(List<String> readSetCodes, 
			String studyCode, 
			String configCode, 
			String acStudy, 
			String acSample, 
			Map<String, UserRefCollabType>mapUserRefCollab,
			ContextValidation contextValidation) throws SraException, SraValidationException, APIValidationException, APIException {
		return initPrimarySubmission(readSetCodes, studyCode, configCode, acStudy, acSample, mapUserRefCollab, false, contextValidation);
	}
	
	/**
	 * Construit la soumission correspondant à des nouvelles données à soumettre à l'EBI.
	 * Met la soumission dans l'etat {@link ngl.refactoring.state.States#SUB_N}, valide 
	 * et sauve la soumission dans la base et cascade l'etat à tous les objets de la soumission.
	 * Declenche une  {@link SraException} en cas de probleme
	 * @param readSetCodes 		 liste des readsetCodes
	 * @param studyCode   	     Code du study 
	 * @param configCode         Code de la configuration de soumission
	 * @param acStudy            AC du study si soumission avec un seul study fournit par collaborateur
	 * @param acSample           AC du sample si soumission avec un seul sample fournit par collaborateur
	 * @param mapUserRefCollab   Correspondance entre refCollaborateur et ac du study et/ou du sample à utiliser pour la soumission
	 * @param contextValidation  Context de validation qui contient erreurs si problemes
	 * @return                   Code de la soumission crée
	 * @throws SraException           error
	 * @throws APIValidationException error
	 * @throws APIException           error
	 */
	// appelé depuis controller Submissions.viaServices()
	public String initPrimarySubmission(List<String> readSetCodes, 
										String studyCode, 
										String configCode, 
										String acStudy, 
										String acSample, 
										Map<String, UserRefCollabType>mapUserRefCollab,
										boolean noCheckTaxonSampleCollab,
										ContextValidation contextValidation) throws SraException, SraValidationException, APIValidationException, APIException {
		
		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		Submission submission;
		//logger.debug("XXXXXXXXXXXXXXXXXX     initPrimarySubmission:: acSample = " + acSample);

		long startTime = System.currentTimeMillis();
		try {
			
			submission = createServices.initPrimarySubmission(readSetCodes, studyCode, 
				                                          configCode  , acStudy, 
												          acSample    , mapUserRefCollab, 
												          noCheckTaxonSampleCollab, contextValidation);
	    	long endTime = System.currentTimeMillis();
		    logger.debug("WWWWWWW   Total elapsed time in execution of method initPrimarySubmission() is :"+ (endTime-startTime));

		
		} catch (IOException e) {
		    long endTime = System.currentTimeMillis();
		    logger.debug("WWWWWWW   Total elapsed time in execution of method initPrimarySubmission() is :"+ (endTime-startTime));

			//logger.debug("initPrimarySubmission " + e.getMessage());
			throw new SraException("initPrimarySubmission ", e.getMessage());
		} 
		// laisser filer les SraException, SraValidationException qui seront gerees dans le controlleur

		// Dans le cas d'un create, ne pas utiliser updateSubmissionState ni updateSubmissionChilObject
		// (les objets sont crées dans le bon etat)  car sinon duplication du state au niveau de l'historique.
		submission.setTraceUpdateStamp(contextValidation.getUser());
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		SraException.assertNoError(contextValidation);
		return submission.code;
	}	
	
	
	
	public void initPrimarySubmissionAtlaseaForOneTolid(String tolid, ContextValidation ctxVal, ContextValidation ctxSuccess) {

		logger.debug("Dans SubmissionNewAPI.initPrimarySubmissionAtlaseaForOneTolid, tolid='{}'", tolid);
		//StringBuffer sb = new StringBuffer("");
		
		ArrayList<Submission> submissions = createServices.initPrimarySubmissionAtlaseaForOneTolid(tolid, ctxVal, ctxSuccess);
		//logger.debug("ZZZZZZZZZZZ  dans NewAPI, message=" + sb);
		if(ctxVal.hasErrors()) {
			return;
		}
//		// Dans le cas d'un create, ne pas utiliser updateSubmissionState ni updateSubmissionChilObject
//		// (les objets sont crées dans le bon etat)  car sinon duplication du state au niveau de l'historique.
//		for (Submission submission : submissions) {
//			submission.setTraceUpdateStamp(ctxVal.getUser());
//			ctxVal.setUpdateMode();
//			submission.validate(ctxVal);
//			submissionCodes.add(submission.code);
//		}
//		if (ctxVal.hasErrors()) {
//			throw new SraValidationException(ctxVal);
//		}
//		SraException.assertNoError(ctxVal);		
		//logger.debug("ZZZZZZZZZZZ  dans NewAPI, message=" + sb);
		//return sb.toString();
	}

	
	public ArrayList<Submission> initPrimarySubmissionsAtlaseaForOneTolid(String tolid, ContextValidation ctxVal, ContextValidation ctxSuccess) {
		logger.debug("Dans SubmissionNewAPI.initPrimarySubmissionsAtlaseaForOneTolid, tolid='{}'", tolid);
		return createServices.initPrimarySubmissionAtlaseaForOneTolid(tolid, ctxVal, ctxSuccess);
	}
	
	public String initPrimarySubmissionWithoutRawData(
			String studyCode, 
			List<String>sampleCodes,
			ContextValidation contextValidation) throws SraException, SraValidationException, APIValidationException, APIException {

		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		Submission submission;
		try {
			submission = createServices.initPrimarySubmissionWithoutRawData(studyCode, sampleCodes, contextValidation);
		} catch (IOException e) {
			throw new SraException("initPrimarySubmissionWithoutRawData ", e.getMessage());
		} 
		// laisser filer les SraException, SraValidationException qui seront gerees dans le controlleur

		// Dans le cas d'un create, ne pas utiliser updateSubmissionState ni updateSubmissionChilObject
		// (les objets sont crées dans le bon etat)  car sinon duplication du state au niveau de l'historique.
		submission.setTraceUpdateStamp(contextValidation.getUser());
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		SraException.assertNoError(contextValidation);
		return submission.code;
	}	
	
	
	
	public Submission buildSubmissionWithStudyAndValidate(Study study, ContextValidation contextValidation) throws SraException, SraValidationException, IOException, APIValidationException, APIException {
		return createServices.buildSubmissionWithStudyAndValidate(study, contextValidation);
	}
	
	public String initPrimarySubmissionBionano(
			List<String> readSetCodes,
			List<String> listPathcmaps, 
			String analysisCode, 
			ContextValidation contextValidation) throws SraException, SraValidationException, APIValidationException, APIException {

		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		Submission submission;
		//logger.debug("XXXXXXXXXXXXXXXXXX     initPrimarySubmission:: acSample = " + acSample);
		try {
			submission = createServices.initPrimarySubmissionBionano (readSetCodes, listPathcmaps, analysisCode, contextValidation); 
		} catch (IOException e) {
			//logger.debug("initPrimarySubmission " + e.getMessage());
			throw new SraException("initPrimarySubmission ", e.getMessage());
		} 
		// laisser filer les SraException, SraValidationException qui seront gerees dans le controlleur

		// Dans le cas d'un create, ne pas utiliser updateSubmissionState ni updateSubmissionChilObject
		// (les objets sont crées dans le bon etat)  car sinon duplication du state au niveau de l'historique.
		submission.setTraceUpdateStamp(contextValidation.getUser());
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		SraException.assertNoError(contextValidation);
		return submission.code;
	}		
	

	/**
	 * Construit la soumission correspondant à un project umbrella à soumettre.
	 * Met la soumission dans l'etat {@link ngl.refactoring.state.States#SUB_N}, valide 
	 * et sauve la soumission dans la base et cascade l'etat à tous les objets de la soumission.
	 * Declenche une  {@link SraException} en cas de probleme
	 * @param contextValidation          Context de validation qui contient erreurs si problemes
	 * @param title        	             Titre du project umbrella
	 * @param description                Description du project umbrella
	 * @param parentProjectAccessions    Liste des PRJEB parents
	 * @param childrenProjectAccessions  Liste des PRJEB enfants
	 * @param idsPubmed                  idPubme
	 * @param strTaxonId                 strTaxonId
	 * @param attributes                 attributs
	 * @param dropBox                    dropbox
	 * @return                           Retourne la soumission crée
	 * @throws SraException              erreur
	 * @throws SraValidationException    erreur
	 * @throws APIValidationException    erreur
	 * @throws APIException              erreur
	 */

	public Submission initPrimarySubmissionForUmbrella(ContextValidation contextValidation, String title, String description, List<String> parentProjectAccessions, List<String> childrenProjectAccessions, List<String> idsPubmed, String strTaxonId, String attributes, String dropBox) 
			throws SraException, SraValidationException, APIValidationException, APIException {

		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		Submission submission;
		try {
			submission = createServices.initPrimarySubmissionForUmbrella(contextValidation, title, description, parentProjectAccessions, childrenProjectAccessions, idsPubmed, strTaxonId, attributes, dropBox);
		} catch (IOException e) {
			//logger.debug("initPrimarySubmissionForUmbrella 111111111111111  " + e.getMessage());
			throw new SraException("initPrimarySubmissionForUmbrella ", e.getMessage());
		} 
		// laisser filer les SraException, SraValidationException qui seront gerees dans le controlleur

		// Dans le cas d'un create, ne pas utiliser updateSubmissionState ni updateSubmissionChilObject
		// (les objets sont crées dans le bon etat)  car sinon duplication du state au niveau de l'historique.
		submission.setTraceUpdateStamp(contextValidation.getUser());
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		SraException.assertNoError(contextValidation);
		return submission;
	}	


	public Submission initPrimarySubmissionForStudy(List<String> projectCodes, 
            String existingStudyType, 
            String centerProjectName, 
            String title, 
            String description, 
            String dropBox, 
            String strTaxonId, 
            String strIdsPubmeds, 
            String strLocusTagPrefixs, 
            String strParentProjectAccessions,
            String strAttributes, 
            ContextValidation contextValidation) throws SraException, SraValidationException, APIValidationException, APIException {

		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		Submission submission = null;
		try {
			Study study = createServices.createStudyEntity(projectCodes, 
                    									   existingStudyType, 
                    									   centerProjectName, 
                    									   title, 
                    									   description, 
                    									   dropBox, 
                    									   strTaxonId, 
                    									   strIdsPubmeds, 
                    									   strLocusTagPrefixs, 
                    									   strParentProjectAccessions,
                    									   strAttributes, 
                    									   contextValidation);

			//logger.debug("KKKKKKKKKKKKKKKKKKKKKKKKKK     study.code={}", study.code);
			submission = createServices.buildSubmissionWithStudyAndValidate(study, contextValidation);
		} catch (IOException e) {
			//logger.debug("initPrimarySubmissionForStudy 111111111111111  " + e.getMessage());
			throw new SraException("initPrimarySubmissionForStudy ", e.getMessage());
		} 
		// laisser filer les SraException, SraValidationException qui seront gerees dans le controlleur

		// Dans le cas d'un create, ne pas utiliser updateSubmissionState ni updateSubmissionChilObject
		// (les objets sont crées dans le bon etat)  car sinon duplication du state au niveau de l'historique.
		submission.setTraceUpdateStamp(contextValidation.getUser());
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);
		// demarrer la soumission :
		
		if (contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		SraException.assertNoError(contextValidation);
		return submission;
	}	
	

	public Submission initPrimarySubmissionForSample(String projectCode, 
            										 int    taxonId, 
            										 String attributes, 
            										 String dropbox, 
            										 String title, 
            										 String anonymizedName, 
            										 ContextValidation contextValidation) throws SraException, SraValidationException, APIValidationException, APIException {

		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		Submission submission = null;
		try {
			Sample sample = createServices.createSampleEntity(projectCode, 
		            										  taxonId, 
		            										  attributes, 
		            										  dropbox, 
		            										  title, 
		            										  anonymizedName, 
		            										  contextValidation);
			logger.debug("KKKKKKKKKKKKKKKKKKKKKKKKKK     sample.code={}", sample.code);
			submission = createServices.buildSubmissionWithSampleAndValidate(sample, contextValidation);
		} catch (IOException e) {
			logger.debug("initPrimarySubmissionForSample 111111111111111  " + e.getMessage());
			throw new SraException("initPrimarySubmissionForSample ", e.getMessage());
		} 
		// laisser filer les SraException, SraValidationException qui seront gerees dans le controlleur

		// Dans le cas d'un create, ne pas utiliser updateSubmissionState ni updateSubmissionChilObject
		// (les objets sont crées dans le bon etat)  car sinon duplication du state au niveau de l'historique.
		submission.setTraceUpdateStamp(contextValidation.getUser());
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);
		
		if (contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		SraException.assertNoError(contextValidation);
		return submission;
	}	
	
	/**
	 * Verifie que la soumission est dans le bon etat pour etre demarré. Crée le repertoire
	 * de soumission, fait les liens sur les données brutes si données locales et met la 
	 * soumission dans l'un des 2 état permettant d'etre prise en charge par Birds ({@link ngl.refactoring.state.States#SUB_SRD_IW}  ou {@link ngl.refactoring.state.States#SUB_SMD_IW}), 
	 * validation et sauvegarde dans base avec cascade de l'etats à tous les objets de la soumission.
	 * @param  contextValidation         Context de validation
	 * @param  submission                Soumission à demarrer (dans etat SUB_V)
	 * @throws SraValidationException    error
	 * @throws SraException              error
	 */
	public void startPrimarySubmission(ContextValidation contextValidation, Submission submission) throws SraValidationException, SraException {
		
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUB_V);	
		String user = contextValidation.getUser();
		// String user = Authentication.getUser(); pose probleme si on fait tourner cron en isoprod sur ngl-data

		activateServices.activationPrimarySubmission(contextValidation, submission);
//		if(submission.experimentCodes != null && submission.experimentCodes.size() > 0) {
//			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_SRD_IW, user));
//		} else {
//			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_SMD_IW, user));
//		}
		if(submission.typeRawDataSubmitted.equals(TypeRawDataSubmitted.withoutRawData)) {
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_SMD_IW, user));
		} else {
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_SRD_IW, user));
		}
		submission.validate(contextValidation);
		SraException.assertNoError(contextValidation);
		if (contextValidation.hasErrors()) {
			//logger.debug("Dans startPrimarySubmission, detection d'erreur :");
			contextValidation.displayErrors(logger, "debug");
			return;
		}
		submissionDAO.saveStateAndTrace(submission);
		// executionContext.exec(submissionDAO.update(submission));
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}
	

	
//	public void activationPrimarySubmission(ContextValidation contextValidation, Submission submission) throws SraException {
		
//		Ec ec = new Ec();
//		try {
//			assertSubmissionStateCode(submission, SRASubmissionStateNames.SUB_V);	
//		
//		String user = VariableSRA.admin;
//		submissionWorkflowsHelper.activationPrimarySubmission(contextValidation, submission, ec);
//		if (contextValidation.hasErrors()) {
//			throw new SraException("Soumission invalide :" + contextValidation.getErrors());
//		}
//		setSubmissionState(submission, new State(SRASubmissionStateNames.SUB_SRD_IW, user));
//		ec.add(submissionDAO.update(submission));
//		//executionContext.exec(submissionDAO.update(submission));
//		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
//		updateSubmissionChildObject(submission, contextValidation);
//		ec.commit;
//		} catch(Exception e){
//			ec.rollBack();
//		}
//	}
//		
//	public void m(Runnable leGrosAppel) {
//		Ec ec = new Ec();
//		try {
//			leGrosAppel.run();
//			ec.commit;
//		} catch(Exception e){
//			ec.rollBack();
//		}
//	}
//	
		
	
	/**
	 * Lors d'une soumission primaire de données, ecrit les fichiers de metadonnées dans le repertoire 
	 * de soumission et met la soumission dans l'etat {@link ngl.refactoring.state.States#SUB_SMD_IW}.
	 * Sauvegarde la soumission dans la base et cascade etat dans tous les objets de la soumission.
	 * @param submission          objet submission
	 * @param contextValidation   context de validation
	 * @throws SraException       error
	 */
	public void writeMetaData(Submission submission, ContextValidation contextValidation) throws SraException {
		//logger.debug("Dans writeMetaData");
		//Submission submission = submissionDAO.getSubmission(submissionCode);
//		String user = VariableSRA.admin;
		String user = contextValidation.getUser();
		// String user = Authentication.getUser(); pose probleme si on fait tourner cron en isoprod sur ngl-data

		//SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUB_SRD_F);
		// La soumission est mise a jour dans xmlServices pour les champs xml.	
		xmlServices.writeAllXml(submission);
		// installation etat après action:
//		updateSubmissionStateAndTrace(submission, new State(SRASubmissionStateNames.SUB_SMD_IW, user));
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_SMD_IW, user));
		// validation + cascade du state et traceInformation à tous les objets de la soumission :
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);
		SraException.assertNoError(contextValidation);
		//submissionDAO.update(submission);	
		submissionDAO.saveStateAndTrace(submission);
		// sauver les champs xmlstudys...... mis à jour dans l'objet
		submissionDAO.save(submission);  
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}
	

	
	/**
	 * Parse le fichier des AC de l'EBI recu en retour de l'envoie 
	 * des metadonnées pour une soumission primaire de données de type CREATION. Met à jour les differents objets de la 
	 * soumission pour les AC, les dates (creation, releaseDate), le status et TraceInformation
	 * envoie un mail au createur de la soumission 
	 * @param  contextValidation  contexte de validation
	 * @param  submission         objet soumission
	 * @throws SraException       error
	 */
	public void loadRespEbiForCreation(ContextValidation contextValidation, Submission submission) throws SraException {
		loadRespEbiForCreation(contextValidation, submission, true, true);
	}
	
	/**
	 * Parse le fichier des AC de l'EBI recu en retour de l'envoie 
	 * des metadonnées pour une soumission primaire de données. Met à jour les differents objets de la 
	 * soumission pour les AC, les dates (creation, releaseDate), le status et TraceInformation
	 * envoie un mail au createur de la soumission si demandé
	 * @param  contextValidation  contexte de validation
	 * @param  submission         objet soumission
	 * @param  sendMail           envoie mail recapitulatif si true. 
	 * @param  confidential       met date de confidentialite du study à date de premiere soumission + 2 ans si confidential=true; met date de confidentialité du study à date de premiere soumission si confidential= false
	 * @throws SraException       error
	 */
	public void loadRespEbiForCreation(ContextValidation contextValidation, Submission submission, boolean sendMail, boolean confidential) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUB_SMD_F);	
		SraException.assertParamForLoadEbiReport(submission);	
		loadRespEbiForCreation(contextValidation, submission, submission.submissionDirectory + "/" + submission.ebiResult, sendMail, confidential);
	}
	
	
	/**
	 * Parse le fichier des AC de l'EBI recu en retour de l'envoie 
	 * des metadonnées pour une soumission primaire de données. Met à jour les differents objets de la 
	 * soumission pour les AC, les dates (creation, releaseDate), le status et TraceInformation
	 * envoie un mail au createur de la soumission si demandé
	 * @param  ctxVal             contexte de validation
	 * @param  submission         objet soumission
	 * @param  pathEbiReport      chemin complet du rapport de soumission
	 * @param  sendMail           envoie mail recapitulatif si true. 
	 * @param  confidential       met date de confidentialite du study à date de premiere soumission + 2 ans si confidential=true; met date de confidentialité du study à date de premiere soumission si confidential= false
	 * @throws SraException       error
	 */
	public void loadRespEbiForCreation(ContextValidation ctxVal, Submission submission, String pathEbiReport, boolean sendMail, boolean confidential) throws SraException {
		logger.debug("pathEbiReport={}", pathEbiReport);
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUB_SMD_F);	
		if (StringUtils.isBlank(pathEbiReport)) {
			throw new SraException("pathReportEbi non renseigne");
		}	
		String user;
		ctxVal.setUpdateMode();
		if (StringUtils.isNotBlank(submission.traceInformation.createUser)) {
			user = submission.traceInformation.createUser;
		} else {
			user = ctxVal.getUser();
			//user = Authentication.getUser();	pose probleme si on fait tourner cron en isoprod sur ngl-data
		}
		// si rechargement des données, on veut que les samples de cette soumission soient mis avec 
		try {
			//logger.debug("Dans SubmissionNewAPI.loadEbiResponseAC, avant appel de ebiFileResponseServices.loadEbiResp");
			ebiFileResponseServices.loadEbiRespForCreation(ctxVal, 
														   submission, 
														   new File(pathEbiReport), 
														   sendMail,
														   confidential);
			//logger.debug("XXXX    Dans SubmissionNewAPI.loadEbiResponseAC, apres appel de ebiFileResponseServices.loadEbiResp");

		} catch (SraException | APIException e) {
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_FE, user));
			submissionDAO.saveStateAndTrace(submission);
			submissionNewAPITools.updateSubmissionChildObject(submission);
			logger.debug("YYYYYYYYYYYYY  Dans SubmissionNewAPI.loadEbiResponseAC, Soumission invalide :" + e.getMessage());
			throw new SraException("SubmissionNewAPI::loadRespEbiForCreation: " + e.getMessage(), e);
		}
		// validation :
		submission.validate(ctxVal);	
		if (ctxVal.hasErrors()) {
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_FE, user));
			submissionDAO.saveStateAndTrace(submission);
			submissionNewAPITools.updateSubmissionChildObject(submission);
			//logger.debug("ZZZZZZZZZZZZZZ   Soumission invalide :" + contextValidation.getErrors());
			throw new SraValidationException(ctxVal);
		} 
		//logger.debug("AAAAAAAAAAAAAAAAAA");
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_F, user));
		submissionDAO.saveStateAndTrace(submission);
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
		// NGL-3666
		// si probleme, declenche une erreur, mais soumission qui n'est pas mise en erreur car soumission bien passé, c'est un
		// probleme de gestion des externalStudySample
		try {
			correctExternalStudyAndSample.completeDatabaseForExternalSampleAndStudy(submission.code);
		} catch (SraException e) {
			throw new SraException("SubmissionNewAPI::loadRespEbiForCreation: " + e.getMessage() , e);
		} catch (APIValidationException  e) {
			throw new SraException("SubmissionNewAPI::loadRespEbiForCreation: " + e.getMessage(), e);
		} catch (APIException e) {
			throw new SraException("SubmissionNewAPI::loadRespEbiForCreation: " + e.getMessage(), e);
		}
	}

	
	/**
	 * Construit la soumission correspondant à la release du study dont le code est passé
	 * en parametre et sauve la soumission dans la database avec l'etat SUBR_N
	 * Ajoute une erreur dans le contexte de validation si une erreur survient.
	 * @param  ctxVal             contexte de validation
	 * @param  studyCode          code du study
	 * @return               	  Soumission nouvellement cree pour la release du study
	 * @throws SraException       error
	 */
	public Submission createSubmissionFromStudyRelease(ContextValidation ctxVal, String studyCode) throws SraException {		
		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		String user = ctxVal.getUser();
		//String user = Authentication.getUser();	pose probleme si on fait tourner cron en isoprod sur ngl-data
		String submissionCode = releaseServices.createSubmissionForReleaseStudy(ctxVal, studyCode);
		Submission submission = submissionDAO.getObject(submissionCode);
		submission.setTraceUpdateStamp(ctxVal.getUser());
		ctxVal.setUpdateMode();
		submission.validate(ctxVal);
//		if (ctxVal.hasErrors()) {
//			throw new SraValidationException(ctxVal);
//		}
		SraException.assertNoError(ctxVal);
		return submissionDAO.getObject(submissionCode);		
	}

	
	/**
	 * Construit la soumission correspondant à la release du sample dont le code est passé
	 * en parametre et sauve la soumission dans la database avec l'etat SUBR_N
	 * Ajoute une erreur dans le contexte de validation si une erreur survient.
	 * @param  ctxVal             contexte de validation
	 * @param  sampleCode         code du sample
	 * @return      	          Soumission nouvellement cree pour la release du sample
	 * @throws SraException       error
	 */
	public Submission createSubmissionFromSampleRelease(ContextValidation ctxVal, String sampleCode) throws SraException {		
		// La soumission n'existe pas à ce stade donc pas de validation de state à faire
		String user = ctxVal.getUser();
		//String user = Authentication.getUser();	pose probleme si on fait tourner cron en isoprod sur ngl-data
		String submissionCode = releaseServices.createSubmissionForReleaseSample(ctxVal, sampleCode, user);
		Submission submission = submissionDAO.getObject(submissionCode);
		submission.setTraceUpdateStamp(ctxVal.getUser());
		ctxVal.setUpdateMode();
		submission.validate(ctxVal);
		SraException.assertNoError(ctxVal);
		return submissionDAO.getObject(submissionCode);		
	}
	
	
	/**
	 * Cree le repertoire de soumission pour la release du study. Ecrit les metadonnées
	 * et sauve dans la base la soumission et le study dans l'etat {@link ngl.refactoring.state.States#SUBR_SMD_IP}.
	 * Met a jour le context de validation si problemes ou declenche une {@link SraException}
	 * @param contextValidation   contexte de validation
	 * @param submission          soumission
	 * @throws SraException       error
	 */
	public void writeAndActivateSubmissionRelease(ContextValidation contextValidation, 
												  Submission submission) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBR_N);
		String user = contextValidation.getUser();
		//String user = Authentication.getUser();	pose probleme si on fait tourner cron en isoprod sur ngl-data
		submission.createDirSubmission();
		xmlServices.writeAllXml(submission);
		// sauver la soumission avec les informations de fichier xml :
		submissionDAO.save(submission);
		// installation etat après action:
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBR_SMD_IP, user));
		contextValidation.setUpdateMode();
		// validation + cascade du state et traceInformation à tous les objets de la soumission :
		submission.validate(contextValidation);
		SraException.assertNoError(contextValidation);
		submissionDAO.saveStateAndTrace(submission);
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}
	

	/**
	 * Parse le fichier retour de l'EBI dans le cadre d'une release de study. Sauve la date de release 
	 * du study et met la soumission et le study dans l'etat {@link ngl.refactoring.state.States#SUB_F},
	 * si aucun probleme. En cas de probleme, met la soumission et le study dans l'etat
	 * {@link ngl.refactoring.state.States#SUB_FE}
	 * envoie un mail au createur de la soumission 
	 * @param contextValidation   context de validation
	 * @param submission          soumission
	 * @throws SraException       error
	 */
	public void loadRespEbiForRelease(ContextValidation contextValidation, Submission submission) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBR_SMD_F);
		SraException.assertParamForLoadEbiReport(submission);		
		loadRespEbiForRelease(contextValidation, submission, submission.submissionDirectory + "/" + submission.ebiResult, true);
	}

	/**
	 * Parse le fichier retour de l'EBI dans le cadre d'une release de study. Sauve la date de release 
	 * du study et met la soumission et le study dans l'etat {@link ngl.refactoring.state.States#SUB_F},
	 * si aucun probleme. En cas de probleme, met la soumission et le study dans l'etat
	 * {@link ngl.refactoring.state.States#SUB_FE}
	 * envoie un mail au createur de la soumission si demande
	 * @param ctxVal              context de validation
	 * @param submission          soumission
	 * @param pathEbiReport       chemin complet du rapport de soumission
	 * @param sendMail            envoie mail au createur de la soumission si sendMail à true
	 * @throws SraException       error
	 */
	public void loadRespEbiForRelease(ContextValidation ctxVal, 
								      Submission        submission, 
								      String            pathEbiReport,
								      boolean           sendMail) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBR_SMD_F);
		if (StringUtils.isBlank(pathEbiReport)) {
			throw new SraException("pathReportEbi non renseigne");
		}	
		String user = ctxVal.getUser();
		//String user = Authentication.getUser();	pose probleme si on fait tourner cron en isoprod sur ngl-data
		
		//logger.debug("loadRespEbiForRelease");
		try {
			ebiFileResponseServices.loadEbiRespForRelease(ctxVal, 
					   submission, 
					   new File(pathEbiReport), 
					   sendMail);
		} catch(APIException e) {
			throw new SraException("SubmissionNewAPI::loadRespEbiForUpdate", e.getMessage());			
		} catch(SraException e) {
			//logger.debug("je catch bien SraException");
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBR_FE, user));
			//logger.debug("j'ai mis le state a jour ");
			submissionDAO.saveStateAndTrace(submission);
			//logger.debug("j'ai sauvé le state");
			submissionNewAPITools.updateSubmissionChildObject(submission);
			//logger.debug("j'ai propagé le state et je relance la SraException");
			throw new SraException("SubmissionNewAPI::loadRespEbiForUpdate", e.getMessage());
		} 
		
		// validation :
		ctxVal.setUpdateMode();
		submission.validate(ctxVal);	
		if (ctxVal.hasErrors()) {
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBR_FE, user));
			submissionDAO.saveStateAndTrace(submission);
			submissionNewAPITools.updateSubmissionChildObject(submission);
			throw new SraValidationException(ctxVal);
		} 
//		updateSubmissionStateAndTrace(submission, new State(SRASubmissionStateNames.SUB_F, user));
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_F, user));
		submissionDAO.saveStateAndTrace(submission);
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}
	
	
	/**
	 * Construit la soumission correspondant au study, samples ou experiments presents dans la base
	 * et qui doivent etre soumis à l'EBI pour une mise à jour. (les objets modifiés sont dans la base mais
	 * il faut les soumettre à l'EBI pour que les modifications soient prises en compte.).
	 * Renvoie la soumission avec ses sous-objets dans l'etat {@link ngl.refactoring.state.States#SUBU_N}.
	 * Ajoute une erreur dans le contexte de validation si une erreur survient.
	 * @param  contextValidation  contexte de validation
	 * @param umbrella            objet umbrella à mettre à jour à l'EBI
	 * @param  study              objet study à mettre à jour à l'EBI
	 * @param  samples            liste des samples à mettre à jour à l'EBI
	 * @param  experiments        liste des experiments à mettre à jour à l'EBI
	 * @return                    soumission nouvellement cree, et sauvée dans base.
	 * @throws SraException           error
	 * @throws APIValidationException error
	 * @throws APIException           error
	 */
	public Submission createSubmissionForUpdate(ContextValidation contextValidation, 
												Project umbrella,
												Study study, 
												List<Sample> samples,
												List<Experiment> experiments,
												boolean noCheckTaxonSampleCollab) throws SraException, APIValidationException, APIException {	
		Submission submission = null;
		submission = updateServices.createSubmissionForUpdate(contextValidation, umbrella, study, samples, experiments, noCheckTaxonSampleCollab);
		return submission;
	}
	
	/**
	 * Cree le repertoire de soumission pour une mise à jour des données. Ecrit les metadonnées
	 * et sauve dans la base la soumission et le study dans l'etat {@link SRASubmissionStateNames#SUBU_SMD_IP}.
	 * @param contextValidation   contexte de validation
	 * @param submission          soumission
	 * @throws SraException       error
	 */
	public void writeAndActivateSubmissionUpdate(ContextValidation contextValidation, 
												 Submission submission) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBU_N);
		String user = contextValidation.getUser();
		//String user = Authentication.getUser();	pose probleme si on fait tourner cron en isoprod sur ngl-data

		submission.createDirSubmission();
		xmlServices.writeAllXml(submission);
		// sauver la soumission avec les informations de fichier xml :
		submissionDAO.save(submission);
		// installation etat après action:
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_SMD_IP, user));
		contextValidation.setUpdateMode();
		// validation + cascade du state et traceInformation à tous les objets de la soumission :
		submission.validate(contextValidation);
		SraException.assertNoError(contextValidation);
		submissionDAO.saveStateAndTrace(submission);
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
	}
	
	/**
	 * Parse le fichier retour de l'EBI dans le cadre d'une mise à jour de données (study, samples ou experiments)
	 * Met la soumission et ses objets dans l'etat {@link ngl.refactoring.state.States#SUB_F},
	 * si aucun probleme. En cas de probleme, met la soumission et ses objets dans l'etat
	 * envoie un mail au createur de la soumission 
	 * {@link ngl.refactoring.state.States#SUB_FE}
	 * @param contextValidation   context de validation
	 * @param submission          soumission
	 * @throws SraException       error
	 */	
	public void loadRespEbiForUpdate(ContextValidation contextValidation, Submission submission) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBU_SMD_F);
		SraException.assertParamForLoadEbiReport(submission);		
		loadRespEbiForUpdate(contextValidation, submission, submission.submissionDirectory + "/" + submission.ebiResult, true);
	}
	
	/**
	 * Parse le fichier retour de l'EBI dans le cadre d'une mise à jour de données (study, samples ou experiments)
	 * Met la soumission et ses objets dans l'etat {@link ngl.refactoring.state.States#SUB_F},
	 * si aucun probleme. En cas de probleme, met la soumission et ses objets dans l'etat
	 * {@link ngl.refactoring.state.States#SUB_FE}
	 * envoie un mail au createur de la soumission si demandé
	 * @param contextValidation   context de validation
	 * @param submission          soumission
	 * @param  sendMail           envoie mail recapitulatif si true. 
	 * @throws SraException       error
	 */	
	public void loadRespEbiForUpdate(ContextValidation contextValidation, 
									Submission         submission, 
									boolean            sendMail) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBU_SMD_F);
		SraException.assertParamForLoadEbiReport(submission);		
		loadRespEbiForUpdate(contextValidation, submission, submission.submissionDirectory + "/" + submission.ebiResult, sendMail);
	}	
	
	
	/**
	 * Parse le fichier retour de l'EBI dans le cadre d'une mise à jour de données (study, samples ou experiments)
	 * Met la soumission et ses objets dans l'etat {@link ngl.refactoring.state.States#SUB_F},
	 * si aucun probleme. En cas de probleme, met la soumission et ses objets dans l'etat
	 * {@link ngl.refactoring.state.States#SUB_FE}
	 * Envoie un mail au createur de la soumission si demandé
	 * @param contextValidation   context de validation
	 * @param submission          soumission
	 * @param pathEbiReport       chemin complet du rapport de soumission
	 * @param sendMail            indique si envoie d'un mail 
	 * @throws SraException       error
	 */	
	public void loadRespEbiForUpdate(ContextValidation contextValidation, 
			                         Submission        submission,
			                         String            pathEbiReport,
			                         boolean           sendMail) throws SraException {
		SraException.assertSubmissionStateCode(submission, SRASubmissionStateNames.SUBU_SMD_F);
		if (StringUtils.isBlank(pathEbiReport)) {
			throw new SraException("pathReportEbi non renseigne");
		}	
		String user = contextValidation.getUser();
		//String user = Authentication.getUser();	pose probleme si on fait tourner cron en isoprod sur ngl-data
		//logger.debug("loadRespEbiForUpdate");
		try {
			ebiFileResponseServices.loadEbiRespForUpdate(contextValidation, 
														 submission, 
														 new File(pathEbiReport),
														 sendMail);
		} catch(APIException e) {
			throw new SraException("SubmissionNewAPI::loadRespEbiForUpdate", e.getMessage());			
		} catch(SraException e) {
			//logger.debug("je catch bien SraException");
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_FE, user));
			//logger.debug("j'ai mis le state a jour ");
			submissionDAO.saveStateAndTrace(submission);
			//logger.debug("j'ai sauvé le state");
			submissionNewAPITools.updateSubmissionChildObject(submission);
			//logger.debug("j'ai propagé le state et je relance la SraException");
			throw new SraException("SubmissionNewAPI::loadRespEbiForUpdate", e.getMessage());
		} 
		// validation :
		contextValidation.setUpdateMode();
		submission.validate(contextValidation);	
		if (contextValidation.hasErrors()) {
			submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUBU_FE, user));
			submissionDAO.saveStateAndTrace(submission);
			submissionNewAPITools.updateSubmissionChildObject(submission);
			throw new SraValidationException(contextValidation);
		} 
		submission.updateStateAndTrace(new State(SRASubmissionStateNames.SUB_F, user));
		submissionDAO.saveStateAndTrace(submission);
		// cascade du state et traceInformation tous les objets de la soumission dans la base de données:
		submissionNewAPITools.updateSubmissionChildObject(submission);
		// NGL-3666
		// si probleme, declenche une erreur, mais soumission qui n'est pas mise en erreur car soumission bien passé, c'est un
		// probleme de gestion des externalStudySample
		
		// NGL-4373 : la mise à jour des releaseDate des study, exp, analysis, samples updates dans cette soumission a été faite à la creation de cette soumission
		// dans UpdateServices.createSubmissionForUpdate
		try {
			correctExternalStudyAndSample.completeDatabaseForExternalSampleAndStudy(submission.code);
		} catch (SraException | APIException e) {
			throw new SraException("SubmissionNewAPI::loadRespEbiForUpdate", e.getMessage());
		}

	}
}