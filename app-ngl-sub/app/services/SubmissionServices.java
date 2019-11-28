package services;
import static ngl.refactoring.state.SRASubmissionStateNames.F_SUB;
import static ngl.refactoring.state.SRASubmissionStateNames.IW_SUB;
import static ngl.refactoring.state.SRASubmissionStateNames.N;
import static ngl.refactoring.state.SRASubmissionStateNames.NONE;
import static ngl.refactoring.state.SRASubmissionStateNames.V_SUB;
import static ngl.refactoring.state.SRASubmissionStateNames.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.ExternalSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.ExternalStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ReadsetAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.runs.RunsAPI;
import fr.cea.ig.ngl.dao.samples.SamplesAPI;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.project.instance.Project;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.AbstractSample;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.ExternalSample;
import models.sra.submit.common.instance.ExternalStudy;
import models.sra.submit.common.instance.Readset;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.common.instance.UserCloneType;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.ReadSpec;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.sra.submit.util.VariableSRA;
import ngl.refactoring.state.SRAStudyStateNames;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;
import validation.ContextValidation;
import workflows.sra.submission.SubmissionWorkflows;
import workflows.sra.submission.SubmissionWorkflowsHelper;



public class SubmissionServices {
	
	private static final play.Logger.ALogger logger = play.Logger.of(SubmissionServices.class);

//	public static final String NONE          = "NONE";
//							   N             = "N",
//							   N_R           = "N-R",
//							   V_SUB         = "V-SUB",
//							   IW_SUB	     = "IW-SUB",
//							   IP_SUB        = "IP-SUB",
//							   FE_SUB        = "FE-SUB",
//							   F_SUB         = "F-SUB",
//							   IW_SUB_R      = "IW-SUB-R",
//							   IP_SUB_R      = "IP-SUB-R",
//							   FE_SUB_R      = "FE_SUB-R";	

	
	// final SubmissionWorkflows submissionWorkflows = Spring.get BeanOfType(SubmissionWorkflows.class);
	// SubmissionWorkflowsHelper submissionWorkflowsHelper;

	private final SubmissionWorkflows       submissionWorkflows;
	private final SubmissionWorkflowsHelper submissionWorkflowsHelper;
//	private final NGLContext ctx;
//	private final NGLApplication ctx;

	private final StudyAPI studyAPI;
	private final AbstractStudyAPI abstractStudyAPI;
	private final ExternalStudyAPI externalStudyAPI;
	private final SampleAPI sampleAPI;
	private final AbstractSampleAPI abstractSampleAPI;
	private final ExternalSampleAPI externalSampleAPI;
	private ExperimentAPI experimentAPI;
	private SubmissionAPI submissionAPI;
	private ConfigurationAPI configurationAPI;
	private final SamplesAPI laboSampleAPI;
	private final ReadSetsAPI readsetsAPI;    // collection readset de ngl-seq
	private final ReadsetAPI  sraReadsetAPI;   // collection readset de sra

	private final RunsAPI laboRunAPI;
	private final ProjectsAPI projectAPI;
	private final WSClient ws;
    private final SraCodeHelper sraCodeHelper;

    
	@Inject
	public SubmissionServices(SubmissionWorkflows       submissionWorkflows, 
			                  SubmissionWorkflowsHelper submissionWorkflowsHelper, 
			                  //NGLContext                ctx,
			                  //NGLApplication            ctx,
			                  StudyAPI          studyAPI,
			                  AbstractStudyAPI  abstractStudyAPI,
			                  ExternalStudyAPI  externalStudyAPI,
			                  SampleAPI         sampleAPI,
			                  AbstractSampleAPI abstractSampleAPI,
			                  ExternalSampleAPI externalSampleAPI,
			                  ExperimentAPI     experimentAPI,
			                  SubmissionAPI     submissionAPI,
			                  ConfigurationAPI  configurationAPI,
			                  SamplesAPI        laboSampleAPI,
			                  ReadSetsAPI       readsetsAPI,
			                  ReadsetAPI        readsetAPI,
			                  RunsAPI           laboRunAPI,
			                  ProjectsAPI       projectAPI,
			                  WSClient          ws,
			                  SraCodeHelper     sraCodeHelper) {
		this.submissionWorkflows       = submissionWorkflows;
		this.submissionWorkflowsHelper = submissionWorkflowsHelper;
		//this.ctx = ctx;
		this.studyAPI          = studyAPI;
		this.abstractStudyAPI  = abstractStudyAPI;
		this.externalStudyAPI  = externalStudyAPI;
		this.sampleAPI         = sampleAPI;
		this.abstractSampleAPI = abstractSampleAPI;
		this.externalSampleAPI = externalSampleAPI;
		this.experimentAPI     = experimentAPI;
		this.submissionAPI     = submissionAPI;
		this.configurationAPI  = configurationAPI;
		this.laboSampleAPI     = laboSampleAPI;
		this.readsetsAPI       = readsetsAPI;
		this.sraReadsetAPI        = readsetAPI;
		this.laboRunAPI        = laboRunAPI;
		this.projectAPI        = projectAPI;
		this.ws                = ws;
		this.sraCodeHelper     = sraCodeHelper;
	}

	public String initReleaseSubmission(String studyCode, ContextValidation contextValidation) throws SraException {
		logger.debug("Dans SubmissionServices.java.initReleaseSubmission");
		String user = contextValidation.getUser();
		Study study = null;
		if (StringUtils.isNotBlank(studyCode)) {
			study = studyAPI.dao_getObject(studyCode);
		} 
		if (study == null){
			throw new SraException("SubmissionServices::initReleaseSubmission::impossible de recuperer le study '" + studyCode +"' dans la base");
		} 
		if ( ! study.state.code.equals(F_SUB)) {
			throw new SraException("Study " + study.code + " avec status incompatible avec demande de release " + study.state.code );
		} 
		
		if ( study.releaseDate == null){
			throw new SraException("Study " + study.code + " non renseigné dans base pour date de release" );
		}
		if (study.releaseDate.before(new Date())){
			throw new SraException("release date du Study " + study.releaseDate + " inferieure à date du jour: (" + new Date()+") => Le study doit deja etre public à l'EBI");
		}
		
		Submission submission = createSubmissionEntityforRelease(study, user, study.projectCodes);
		//logger.debug("AVANT submission.validate="+contextValidation.errors);

		// updater dans base si besoin le study pour le statut 'N-R' 
		if (StringUtils.isNotBlank(submission.studyCode)){
			study.state.code = IW_SUB_R;
			study.traceInformation.modifyDate = new Date();
			study.traceInformation.modifyUser = user;
			studyAPI.dao_update(DBQuery.is("code", study.code),
					        DBUpdate.set("state.code", study.state.code)
					                .set("traceInformation.modifyUser", user)
					                .set("traceInformation.modifyDate", new Date()));	
		}
		
		// puis valider et sauver submission
		contextValidation.setCreationMode();
//		contextValidation.putObject("type", "sra");		
//		logger.debug("AVANT submission.validate="+contextValidation.getErrors());
		submission.validate(contextValidation);
//		logger.debug("APRES submission.validate="+contextValidation.getErrors());
		
		if (!submissionAPI.dao_checkObjectExist("code",submission.code)) {	
			submission.validate(contextValidation);
			submissionAPI.dao_saveObject(submission);
			logger.debug("sauvegarde dans la base de la submission " + submission.code);
		}
				
		// equivalent de activate :
		createDirSubmission(submission);
		// Avancer le status de submission et study à IW-SUB-R
		State state = new State(IW_SUB_R, user);
		submissionWorkflows.setState(contextValidation, submission, state);
		if (contextValidation.hasErrors()){
			logger.debug("submission.validate produit des erreurs");
			// destruction de la submission et rallback pour etat du study: 
			submissionWorkflowsHelper.rollbackSubmission(submission, contextValidation);	
			contextValidation.displayErrors(logger);
			throw new SraException("SubmissionServices::initReleaseSubmission::probleme validation  voir log: ");
		} else {	
			// updater la soumission dans la base pour le repertoire de soumission (la date de soumission sera mise à la reception des AC)
//			MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME,  Submission.class, 
//			DBQuery.is("code", submission.code),
//			DBUpdate.set("submissionDirectory", submission.submissionDirectory));
			submissionAPI.dao_update(DBQuery.is("code", submission.code),
								 DBUpdate.set("submissionDirectory", submission.submissionDirectory));
		}
		return submission.code;
	}
	
	
	private Submission createSubmissionEntityforRelease(Study study, String user, List<String> projectCodes) throws SraException {
		Submission submission = null;
//		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");	
		Date courantDate = new java.util.Date();
//		String st_my_date = dateFormat.format(courantDate);	
		submission = new Submission(user, projectCodes);
		submission.code = sraCodeHelper.generateSubmissionCode(projectCodes);
		submission.creationDate = courantDate;
//		logger.debug("submissionCode="+ submission.code);

		submission.release = true;
	
		if (study != null){	
//			logger.debug("study != null");

			// mettre à jour l'objet submission pour le study  :
			if (! submission.refStudyCodes.contains("study.code")){
				submission.refStudyCodes.add(study.code);
//				logger.debug("ajout de studyCode dans submission.refStudyCode");

			}
			submission.studyCode = study.code;
//			logger.debug("ajout de studyCode dans submission.studyCode");

			if ( ! study.state.code.equals(F_SUB)) {
				throw new SraException("Study " + study.code + " avec status incompatible avec demande de release " + study.state.code );
			} 
			
			if ( study.releaseDate == null){
				throw new SraException("Study " + study.code + " non renseigné dans base pour date de release" );
			}
			if (study.releaseDate.before(new Date())){
				throw new SraException("release date du Study " + study.releaseDate + " inferieure à date du jour: (" + new Date()+") => Le study doit deja etre public à l'EBI");
			}
//			logger.debug("fin de study != null");
		}
		logger.debug("submissionCode="+ submission.code);

		submission.state = new State(N_R, user);
//		logger.debug("en sortie submissionCode="+ submission.code);

		return submission;
	}

	
	public String validateReadSetCodesForSubmission (List<String> readSetCodes) {
		// Verifier que tous les readSetCode passes en parametres correspondent bien a des objets en base avec
		// submissionState='NONE' cad des readSets qui n'interviennent dans aucune soumission et readset valides pour soumission (readset valides pour la bioinformatique)

		String errorMessage = "";
		int countError = 0;
		for (String readSetCode : readSetCodes) {
			if (StringUtils.isNotBlank(readSetCode)) {
				//logger.debug("!!!!!!!!!!!!!         readSetCode = " + readSetCode);
				//				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
				ReadSet readSet = readsetsAPI.get(readSetCode);
				if (readSet == null) {
					errorMessage += "Le readset " + readSetCode + " n'existe pas dans la base\n";
					countError++;
					//throw new SraException("Le readSet " + readSetCode + " n'existe pas dans database");
				} else if ((readSet.submissionState == null)||(StringUtils.isBlank(readSet.submissionState.code))) {
					errorMessage += "Le readset " + readSetCode + " n'a pas de submissionState.code\n";
					countError++;
					//throw new SraException("Le readSet " + readSet.code + " n'a pas de submissionState.code");
				} else if(!readSet.submissionState.code.equalsIgnoreCase(NONE)){
					errorMessage += "Le readSet " + readSet.code + " a un submissionState.code à la valeur " + readSet.submissionState.code + "\n";
					countError++;
					//throw new SraException("Le readSet " + readSet.code + " a un submissionState.code à la valeur " + readSet.submissionState.code);
				} else {
					Boolean alreadySubmit = experimentAPI.dao_checkObjectExist("readSetCode", readSet.code);		
					if ( alreadySubmit ) {
						// signaler erreur et passer au readSet suivant
						countError++;
						errorMessage = errorMessage + "  - Soumission deja existante dans base pour : '" + readSet.code + "' \n";
						// Recuperer exp dans mongo
						continue;
					} 					
					// Verifier que ce readSet est bien valide avant soumission :
					if (! readSet.bioinformaticValuation.valid.equals(TBoolean.TRUE)) {
						countError++;
						errorMessage = errorMessage + "  - Soumission impossible pour le readset '" + readSet.code + "' parceque non valide pour la bioinformatique \n";
						continue;
					} 
				}
			}
		}	
		logger.debug("Dans services.ValidateReadSetCodesForSubmission, nombre d'erreur = " + countError);
		logger.debug(errorMessage);
		return errorMessage;
	}
	
	
	
	
//	valide la configuration et les differents parametres de la soumission et retourne le study pour la soumission.
	public void validateParamForSubmission(List<String> readSetCodes, String studyCode, String configCode, String acStudy, String acSample, Map<String, UserCloneType>mapUserClones) throws SraException {
		Configuration config = configurationAPI.dao_getObject(configCode);	
		Study study = null;
		
		
		
		if (config == null) {
			throw new SraException("Configuration " + configCode + " n'existe pas dans database");
		} 
		if (StringUtils.isBlank(config.state.code)) {
			throw new SraException("Configuration " + config.code + " avec champs state.code non renseigne");
		}
			
		logger.debug("dans init, config.state.code='"+config.state.code+"'");
		if (config.strategyStudy.equalsIgnoreCase("strategy_internal_study")) {
			if (StringUtils.isNotBlank(acStudy)) {
				// Recuperer le study si acStudy renseigné et strategy_internal_study :
				// si ac renseigné alors study dans l'etat F-SUB, ou en cours release
				if (studyAPI.dao_checkObjectExist("accession", acStudy)) {
					//study = studyAPI.dao_findOne(DBQuery.is("accession", acStudy));
				} else {
					throw new SraException("strategy_internal_study et acStudy passé en parametre "+ acStudy + "n'existe pas dans database");	
				}	
			} else {
				// Si studyCode renseigné et strategy_internal_study :
				// study en cours de premiere soumission ou bien à NONE ou bien en cours de release
				if (StringUtils.isNotBlank(studyCode)) {
					if (studyAPI.dao_checkObjectExist("code", studyCode)) {
						study = studyAPI.dao_getObject(studyCode);	
					} else {
						throw new SraException("strategy_internal_study et studyCode passé en parametre "+ studyCode + "n'existe pas dans database");	
					}

					// si study jamais soumis (pas d'ac), alors seul etat autorise = NONE
					if (StringUtils.isBlank(study.accession) && ! NONE.equals(study.state.code)) {
						throw new SraException("strategy_internal_study et studyCode passé en parametre "+ studyCode + " sans numeros d'accession et avec un status different de NONE :" + study.state.code);	
					}
				}
			}
		} else if (config.strategyStudy.equalsIgnoreCase("strategy_external_study")) {
			
			if (StringUtils.isNotBlank(studyCode)){
				throw new SraException("Configuration " + config.code + " avec strategy_external_study incompatible avec studyCode renseigne : '" + studyCode +"'");
			}
			if ( StringUtils.isBlank(acStudy) && (mapUserClones == null || mapUserClones.isEmpty()) ) {
				throw new SraException("Configuration " + config.code + " avec strategy_study = 'strategy_external_study' incompatible avec acStudy et mapUserClone non renseignés");
			}
		} else {
			throw new SraException("Configuration " + config.code + " avec strategy_study = '"+ config.strategyStudy + "' (valeur non attendue)");
		}

		if (config.strategySample.equalsIgnoreCase("strategy_external_sample")) {
			logger.debug("Dans init, cas strategy_external_sample");
			if (mapUserClones == null || mapUserClones.isEmpty()){
				throw new SraException("Configuration " + config.code + "avec configuration.strategy_sample='strategy_external_sample' incompatible avec acSample nul et mapUserClone non renseigné");
			}
		} 
		String errorMessages = validateReadSetCodesForSubmission(readSetCodes);
		if (!errorMessages.equals("")) {
			throw new SraException("Probleme avec les readsetCodes : \n" + errorMessages);
		}
	}
	
	public void updateReadSetAndSaveIndb (List<ReadSet> readSets, String submissionStateCode, String user) throws SraException {
		// Updater les readSets pour le status dans la base: 
		for (ReadSet readSet : readSets) {
			if (readSet == null){
				throw new SraException("null readSet in read sets (should not happen)");
			} else {
				readSet.submissionState.code = submissionStateCode;
				readsetsAPI.dao_update(DBQuery.is("code", readSet.code), 
						DBUpdate.set("submissionState.code", N)
								.set("traceInformation.modifyUser", user)
								.set("traceInformation.modifyDate", new Date()));
			}
		}		
	}
	
	public void addReadsetIndb(List<Experiment> listExperiments) {
		for (Experiment experiment : listExperiments) {
			Readset readset = new Readset();
			readset.code = experiment.readSetCode;
			readset.type = experiment.typePlatform.toLowerCase();
			readset.experimentCode = experiment.code;
			readset.runCode = experiment.run.code;
			sraReadsetAPI.dao_saveObject(readset);
		}
	}
	
	public void updateStudyAndSaveIndb(Study study, String submissionStateCode, String user) {
		if (study != null && study.state != null && StringUtils.isNotBlank(study.state.code) && NONE.equals(study.state.code)) {
			study.state.code = submissionStateCode;
			study.traceInformation.modifyDate = new Date();
			study.traceInformation.modifyUser = user;
			studyAPI.dao_saveObject(study);
		}
	}
	
	public void updateConfigurationAndSaveIndb (Configuration config, String submissionStateCode, String user) {
		if (config.state != null && StringUtils.isNotBlank(config.state.code) && NONE.equals(config.state.code)) {
			config.state.code = submissionStateCode;
			config.traceInformation.modifyDate = new Date();
			config.traceInformation.modifyUser = user;
			configurationAPI.dao_saveObject(config);
		}	
	}
	
	public String initPrimarySubmission(List<String> readSetCodes, String studyCode, String configCode, String acStudy, String acSample, Map<String, UserCloneType>mapUserClones, ContextValidation contextValidation) throws SraException, IOException {
		//public String initPrimarySubmission(List<String> readSetCodes, String studyCode, String configCode, String acStudy, String acSample, Map<String, UserCloneType>mapUserClones, Map<String, UserExperimentType> mapUserExperiments, Map< String, UserSampleType> mapUserSamples, ContextValidation contextValidation) throws SraException, IOException {
		//public String initNewSubmission(List<String> readSetCodes, String studyCode, String configCode, Map<String, UserCloneType>mapUserClones, Map<String, UserExperimentType> mapUserExperiments, ContextValidation contextValidation) throws SraException, IOException {
		// Cree en base un objet submission avec state.code=N, met dans la base la configuration avec state.code='N'
		// met les readSet avec state.code = 'N', les experiments avec state.code='N', les samples à soumettre 
		// avec state.code='N' sinon laisse les samples dans leurs state, et met le study à soumettre (study avec state.code='N') avec state.code=V-SUB 

		// Pour simplifier workflow on n'autorise pas a la creation d'une soumission, l'utilisation d'un sample crée par une autre soumission
		// mais toujours dans le processus de soumission. Idem pour les study. Si la creation d'une soumission utilise un study ou un sample deja existant
		// dans la base, alors le state.code doit etre à F-SUB.
		// Pour une premiere soumission d'un readSet, on peut devoir utiliser un study ou un sample existant, deja soumis à l'EBI.
		// En revanche on ne doit pas utiliser un experiment ou un run existant

		// Verifier config et initialiser objet submission avec release a false(private) state.code='N'et 
		// pas de validation integrale de config qui a ete stocke dans la base donc valide mais verification
		// de quelques contraintes en lien avec soumission.


		String user = contextValidation.getUser();
		
		if (readSetCodes == null || readSetCodes.size() < 1 ) {
			throw new SraException("Aucun readset à soumettre n'a été indiqué");
		}

		logger.debug("Dans init : acStudy = " + acStudy + " et acSample= " + acSample);
		//logger.debug("Dans init : taille map clone = " + mapUserClones.size());
		logger.debug("Taille readSetCodes = " + readSetCodes.size());

		if (StringUtils.isBlank(configCode)) {
			throw new SraException("la configuration a un code à null");
		}
		//		Configuration config = MongoDBDAO.findByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, configCode);

		Configuration config = configurationAPI.dao_getObject(configCode);	
		Study study = null;
		validateParamForSubmission(readSetCodes, studyCode, configCode, acStudy, acSample, mapUserClones);
		
		// la config a ete verifié avant, d'ou pas une couverture totale des cas

		// Recuperation du study si strategie internal_study
		if (config.strategyStudy.equalsIgnoreCase("strategy_internal_study") && StringUtils.isNotBlank(acStudy)) {
			// Recuperer le study si acStudy renseigné et strategy_internal_study :
			// si ac renseigné alors study dans l'etat F-SUB, ou en cours release
			study = studyAPI.dao_findOne(DBQuery.is("accession", acStudy));
		} else if (config.strategyStudy.equalsIgnoreCase("strategy_internal_study") && StringUtils.isNotBlank(studyCode)) {
			// Si studyCode renseigné et strategy_internal_study :
			// study en cours de premiere soumission ou bien à NONE ou bien en cours de release
			study = studyAPI.dao_getObject(studyCode);	
		}
		if (study != null && StringUtils.isBlank(study.accession)&& ! NONE.equals(study.state.code)) {
			throw new SraException("strategy_internal_study et studyCode passé en parametre "+ studyCode + "avec status incompatible avec soumission :" + study.state.code);	
		}

		// Recuperation de tous les Reaset avec un status à NONE (verif dans validateParamForSubmission)
		List <ReadSet> readSets = new ArrayList<>();
		for (String readSetCode : readSetCodes) {
			ReadSet readSet = readsetsAPI.get(readSetCode);
			readSets.add(readSet);
		}

		
		// Renvoie un objet submission avec state.code='N'
		// et submission.studyCode renseigné (study à envoyer à l'EBI) si config.strategyStudy=strategy_internal_study et studyCode.state.code="N"
		// submission.release = false si config.strategyStudy == strategy_external_study
		// submission.release = studyCode.release si config.strategyStudy == strategy_internal_study
		//String user = contextValidation.getUser();
		Submission submission = createSubmissionEntity(config, studyCode, acStudy, user);

		// Liste des sous-objets utilisés dans submission
		List <Experiment> listExperiments = new ArrayList<>(); // liste des experiments utilisés et crees dans soumission avec state.code='N' à sauver dans database
		List <AbstractSample> listAbstractSamples = new ArrayList<>();//liste des AbstractSample(Sample ou ExternalSample) avec state.code='F-SUB' ou 'N' utilises dans soumission à sauver ou non dans database		
		List <AbstractStudy> listAbstractStudies = new ArrayList<>();//liste des AbstractStudy(Study ou ExternalStudy) avec state.code='F-SUB' ou 'N' utilises dans soumission à sauver ou non dans database		

		int countError = 0;
		String errorMessage = "";

		for (ReadSet readSet : readSets) {
			logger.debug("readSet : {}", readSet.code);
			// Verifier que c'est une premiere soumission pour ce readSet
			// Verifiez qu'il n'existe pas d'objet experiment referencant deja ce readSet


			// Recuperer scientificName via NCBI pour ce readSet. Le scientificName est utilisé dans la construction
			// des samples et des experiments 
			String laboratorySampleCode = readSet.sampleCode;
			models.laboratory.sample.instance.Sample laboratorySample = laboSampleAPI.get(laboratorySampleCode);
			String scientificName = laboratorySample.ncbiScientificName;
			if (StringUtils.isBlank(scientificName)){
				//scientificName=updateLaboratorySampleForNcbiScientificName(taxonId, contextValidation);
				throw new SraException("Pas de recuperation du nom scientifique pour le sample "+ laboratorySampleCode);
			} else {
				logger.debug("Nom du scientific Name = "+ laboratorySample.ncbiScientificName);
			}

			// Creer les objets avec leurs alias ou code, les instancier completement et les sauver.
			Experiment experiment = createExperimentEntity(readSet, scientificName, user);
			experiment.librarySelection = config.librarySelection;
			experiment.librarySource = config.librarySource;
			experiment.libraryStrategy = config.libraryStrategy;
			experiment.libraryConstructionProtocol = config.libraryConstructionProtocol;
			//logger.debug("scientificName =" + scientificName);
			//logger.debug("librarySelection = " + config.librarySelection);
			//logger.debug("librarySource = " + config.librarySource);
			//logger.debug("libraryStrategy = "+config.libraryStrategy);
			//logger.debug("libraryConstructionProtocol = "+ config.libraryConstructionProtocol);

			String clone = laboratorySample.referenceCollab;
			/*for (Iterator<Entry<String, UserCloneType>> iterator = mapUserClones.entrySet().iterator(); iterator.hasNext();) {
				  Entry<String, UserCloneType> entry = iterator.next();
				  if (entry.getKey().equals(clone)) {
					  logger.debug("dans initPrimarySubmission 1 : cle du userClone = '" + entry.getKey() + "'");
					  logger.debug("       study_ac : '" + entry.getValue().getStudyAc()+  "'");
					  logger.debug("       sample_ac : '" + entry.getValue().getSampleAc()+  "'");
				  }	
			}*/
			//logger.debug("name = '" + laboratorySample.name +"'");
			//logger.debug("clone = '" + clone +"'");
			
			// Creer le sample si besoin et ajouter dans submission.mapUserClone
			if (config.strategySample.equalsIgnoreCase("strategy_external_sample")){
				//logger.debug("Dans init, cas strategy_external_sample");
				ExternalSample externalSample = null;
				if (mapUserClones != null && !mapUserClones.isEmpty()) {
					if (StringUtils.isBlank(clone)) {
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque strategy_external_sample avec mapUserClone et pas de nom de clone dans ngl \n";
						continue;					
					}
					if (! mapUserClones.containsKey(clone)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque strategy_external_sample avec mapUserClone qui ne contient pas le clone '" + clone +"' \n";
						continue;
					}
					String sampleAc = mapUserClones.get(clone).getSampleAc();
					//logger.debug("Pour le clone "+ clone + " sample = " + sampleAc);

					if (StringUtils.isBlank(sampleAc)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque mapUserClone pour clone '" + clone + "' ne contient pas de sampleAc";
						continue;					
					}
					// recuperation dans base ou création du sample avec status F-SUB (si AC alors F-sub)
					externalSample = fetchExternalSample(sampleAc, user);
					//logger.debug("recuperation dans base du sample " +externalSample.accession);
					//logger.debug("recuperation dans base du sample " +externalSample.code);
					
					// Mise a jour de l'objet submission pour mapUserClone :
					UserCloneType submission_userClone = new UserCloneType();
					//logger.debug("alias="+mapUserClones.get(clone).getAlias());
					//logger.debug("sampleAC="+mapUserClones.get(clone).getSampleAc());
					//logger.debug("studyAC="+mapUserClones.get(clone).getStudyAc());
					submission_userClone.setAlias(mapUserClones.get(clone).getAlias());
					submission_userClone.setSampleAc(mapUserClones.get(clone).getSampleAc());
					submission_userClone.setStudyAc(mapUserClones.get(clone).getStudyAc());
					submission.mapUserClone.put(submission_userClone.getAlias(), submission_userClone);
				} else if (StringUtils.isNotBlank(acSample)) {
					externalSample = fetchExternalSample(acSample, user);
				} else {
					logger.error("empty mapUserClone", new Exception());
					continue;
				}

				// Mise a jour de l'objet submission pour les samples references
				//logger.debug("Mise a jour de l'objet submission pour les samples references");
				if (!submission.refSampleCodes.contains(externalSample.code)) {
					submission.refSampleCodes.add(externalSample.code);
				}
				
				// Pas de mise a jour de l'objet submission pour les samples à soumettre.

				//logger.debug("Mise a jour de listAbstractSamples pour les samples references");

				if (!listAbstractSamples.contains(externalSample)) {
					listAbstractSamples.add(externalSample);
				}
				//logger.debug("Mise a jour de experiment pour les samples references");

				// mettre à jour l'experiment pour la reference sample :
				experiment.sampleCode = externalSample.code;
				experiment.sampleAccession = externalSample.accession;

			} else { // strategie internal_sample
				logger.debug("Dans init, cas strategy_internal_sample");
				Sample sample = null;
				if (StringUtils.isNotBlank(acSample)) {
					sample = sampleAPI.dao_getObject(acSample);
					if (sample == null) 
						throw new RuntimeException("impossible de recuperer le sample "  + acSample + " dans la base avec strategie internal sample");
				} else {
					// Recuperer le sample existant avec son state.code ou bien en creer un nouveau avec state.code='N'
					sample = fetchSample(readSet, config.strategySample, scientificName, user);
					// Renseigner l'objet submission :
					// Verifier que l'objet sample n'a jamais ete soumis et n'est pas en cours de soumission
					logger.debug("sample = {} et state = {}" , sample, sample.state.code);
					if (! F_SUB.equalsIgnoreCase(sample.state.code) && ! N.equalsIgnoreCase(sample.state.code)) {
						throw new SraException("Tentative d'utilisation dans la soumission du sample "+ sample.code +" en cours de soumission avec state.code==" + sample.state.code);
					}
				}
				// Mise a jour de l'objet submission pour les samples references
				if (!submission.refSampleCodes.contains(sample.code)) {
					submission.refSampleCodes.add(sample.code);
				}
				// Mise a jour de l'objet submission pour les samples à soumettre :
				//------------------------------------------------------------------
				//SGAS
				if (N.equalsIgnoreCase(sample.state.code)){	
					List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("sampleCodes", sample.code)).toList();
					if (submissionList.size() > 0) {
						String message = "sampleCode=" + sample.code + " avec state=" + sample.state.code + "\n";
						for (Submission sub: submissionList) {
							message += "       - deja utilise par objet Submission " + sub.code + "\n";
						}
						logger.debug(message);
						throw new SraException(message);
					} else {
						if (! submission.sampleCodes.contains(sample.code)){
							submission.sampleCodes.add(sample.code);
						}
					}
				}
				if(!listAbstractSamples.contains(sample)){
					listAbstractSamples.add(sample);
				}
				// mettre à jour l'experiment pour la reference sample :
				experiment.sampleCode = sample.code;
				experiment.sampleAccession = sample.accession;
			}

			if (config.strategyStudy.equalsIgnoreCase("strategy_external_study")){
				logger.debug("Dans init, cas strategy_external_study");

				ExternalStudy externalStudy = null;

				if (mapUserClones != null && !mapUserClones.isEmpty()) {

					if (! mapUserClones.containsKey(clone)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque strategy_external_study et mapUserClone ne contient pas le clone '" + clone +"' \n";
						continue;
					} else {

						//logger.debug("alias="+mapUserClones.get(clone).getAlias());
						//logger.debug("sampleAC="+mapUserClones.get(clone).getSampleAc());
						//logger.debug("studyAC="+mapUserClones.get(clone).getStudyAc());
						// mettre a jour submission.mapUserClone :
						UserCloneType submission_userClone = new UserCloneType();
						submission_userClone.setAlias(mapUserClones.get(clone).getAlias());
						submission_userClone.setSampleAc(mapUserClones.get(clone).getSampleAc());
						submission_userClone.setStudyAc(mapUserClones.get(clone).getStudyAc());
						submission.mapUserClone.put(submission_userClone.getAlias(), submission_userClone);
					}

					String studyAc = mapUserClones.get(clone).getStudyAc();
					if (StringUtils.isBlank(studyAc)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque strategy_external_study et mapUserClone.get(clone).getStudyAc() non renseigne pour le clone '" + clone + "'\n";
						continue;					
					}

					// creation ou recuperation dans base de externalStudy avec state.code = F-SUB
					externalStudy = fetchExternalStudy(studyAc, user);
				} else if (StringUtils.isNotBlank(acStudy)) {
					externalStudy = fetchExternalStudy(acStudy, user);
				} else {
					throw new RuntimeException("impossible sans mapUserClone et sans acStudy");
				}
				// Mise à jour de l'objet submission pour les study references :
				//logger.debug("Mise à jour de l'objet submission pour les study references :");
				if (!submission.refStudyCodes.contains(externalStudy.code)) {
					submission.refStudyCodes.add(externalStudy.code);
				}
				//logger.debug("Mise à jour de listAbstractStudies pour les study references :");
				//				if(!listAbstractStudies.contains(externalStudy.code)){
				final String externalStudyCode = externalStudy.code;
				if(! Iterables.contains(listAbstractStudies, s->s.code.equals(externalStudyCode))) {
					listAbstractStudies.add(externalStudy);
				}	
				//				if(! Iterables.contains(listAbstractStudies, new Predicate <AbstractStudy> () {
				//					     @Override public boolean test(AbstractStudy s) { return s.code.equals(externalStudyCode); }	})) {
				//					listAbstractStudies.add(externalStudy);
				//				}	
				// mettre à jour l'experiment pour la reference study :
				//logger.debug("Mise à jour de experiment pour les study references :");
				experiment.studyCode = externalStudy.code;	
				experiment.studyAccession = externalStudy.accession;	

			} else { // strategie internal_study
				logger.debug("**********************   Strategie internal study          ************************");	

				if (StringUtils.isNotBlank(acStudy)) {
					//					study = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME,
					//							Study.class, DBQuery.and(DBQuery.is("accession", acStudy)));
					study = studyAPI.dao_findOne(DBQuery.is("accession", acStudy));
				} else if (StringUtils.isNotBlank(studyCode)) {
					//					study = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME,
					//							Study.class, DBQuery.and(DBQuery.is("code", studyCode)));
					study = studyAPI.dao_findOne(DBQuery.is("code", studyCode));
				} else {
					throw new RuntimeException("impossible de recuperer le study dans la base avec strategie 'internal study'");
				}
				// Mise à jour de l'objet submission pour les study references :
				if (!submission.refStudyCodes.contains(study.code)) {
					submission.refStudyCodes.add(study.code);
				}
				if (!listAbstractStudies.contains(study)) {
					listAbstractStudies.add(study);
				}	
				// mettre à jour l'experiment pour la reference study :
				experiment.studyCode = study.code;
				experiment.studyAccession = study.accession;

				// Mise a jour de l'objet submission pour le study à soumettre :// normalement deja fait dans createSubmissionEntity
				//--------------------------------------------------------------
				if (StringUtils.isBlank(acStudy) && (SRAStudyStateNames.NONE.equalsIgnoreCase(study.state.code))) {
					List <Submission> submissionList = submissionAPI.dao_find(DBQuery.is("studyCode", studyCode).is("release", false)).toList();
					if (submissionList.size() != 0) {
						String message = studyCode + " utilise par plusieurs objets submissions, studyState = " + study.state.code + "\n";
						for (Submission sub: submissionList) {
							message += studyCode + " utilise par objet Submission " + sub.code +"avec submissionState = " + submission.state.code + "\n";
						}
						logger.debug(message);
						throw new SraException(message);
					} else {
						logger.debug(studyCode + " utilise par aucune soumission autre que " + submission.code);	
					}	
					submission.studyCode = study.code; 
				}	
			}	

			// Ajouter l'experiment avec le statut forcement à 'N' à l'objet submission :
			// Mise à jour de l'objet submission avec les experiments à soumettre :
			//---------------------------------------------------------------------
			if (experiment.state.code.equalsIgnoreCase(N)) { 
				if(!submission.experimentCodes.contains(experiment.code)){
					listExperiments.add(experiment);
					submission.experimentCodes.add(experiment.code);
					//logger.debug("Ajout dans submission du expCode : " + experiment.code);
					if(experiment.run != null) {
						logger.debug("Ajout dans submission du runCode TOROTOTO: " + experiment.run.code);
						submission.runCodes.add(experiment.run.code);
					}// end if
				}// end if
			}// end if
		} // end for readset

		if (countError != 0){
			throw new SraException("Problemes pour creer la soumission\n" + errorMessage);
		}

		contextValidation.setCreationMode();

		// On ne sauvent que les study qui n'existent pas dans la base donc des ExternalStudy avec state.code='F-SUB'	
		for (AbstractStudy absStudyElt: listAbstractStudies) {
			//			if (! MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class, "code", absStudyElt.code)){	
			//				absStudyElt.validate(contextValidation);
			//				MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, absStudyElt);
			//				logger.debug("ok pour sauvegarde dans la base du study " + absStudyElt.code);
			//			}
			// On ne sauvent que les study qui n'existent pas dans la base donc des ExternalStudy avec state.code='F-SUB'	
			if (! abstractStudyAPI.dao_checkObjectExist("code", absStudyElt.code)) {	
				if (absStudyElt instanceof ExternalStudy) {
					ExternalStudy eStudy = (ExternalStudy) absStudyElt;
					eStudy.validate(contextValidation);
					externalStudyAPI.dao_saveObject(eStudy);
					logger.debug("ok pour sauvegarde dans la base de l'external study " + absStudyElt.code);
				}
			}
		}	

		// On ne sauvent que les samples qui n'existent pas dans la base donc des Samples avec state.code='N'
		// ou bien des ExternalSample avec state.code='F-SUB'
		for (AbstractSample sampleElt: listAbstractSamples) {
			//			if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class, "code", sampleElt.code)){	
			//				sampleElt.validate(contextValidation);
			//				MongoDBDAO.save(InstanceConstants.SRA_SAMPLE_COLL_NAME, sampleElt);
			//				logger.debug("ok pour sauvegarde dans la base du sample " + sampleElt.code);
			//			}
			// On ne sauvent que les samples qui n'existent pas dans la base donc des Samples avec state.code='N'
			// ou bien des ExternalSample avec state.code='F-SUB'
			if (!abstractSampleAPI.dao_checkObjectExist("code", sampleElt.code)){	
				sampleElt.validate(contextValidation);
				abstractSampleAPI.dao_saveObject(sampleElt);
				logger.debug("ok pour sauvegarde dans la base du sample " + sampleElt.code);
			}
		}
		// On ne sauvent que les experiment qui n'existent pas dans la base donc des experiment avec state.code='N'	
		for (Experiment expElt: listExperiments) {
			expElt.validate(contextValidation);
			contextValidation.removeKeyFromRootKeyName(expElt.code);

			//			if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "code", expElt.code)){	
			//				MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, expElt);
			//				logger.debug("sauvegarde dans la base de l'experiment " + expElt.code);
			//			}
			
			if (!experimentAPI.dao_checkObjectExist("code", expElt.code)) {	
				experimentAPI.dao_saveObject(expElt);
				logger.debug("sauvegarde dans la base de l'experiment " + expElt.code);
			}
		}

		
		//---------------------------------------------//
		// update des données et sauvegarde dans base: //
		//---------------------------------------------//

		contextValidation.setUpdateMode();

		// updater les readsets dans collection readSet de ngl-seq pour le status et sauver dans base :
		updateReadSetAndSaveIndb(readSets, N, user);
		// ajouter les readsets dans collection readset de sra :
		addReadsetIndb(listExperiments);
		
		// updater le study interne à soumettre pour son status et sauver dans base, si study à soumettre
		if (study != null && study.state != null && StringUtils.isNotBlank(study.state.code) && NONE.equals(study.state.code)) {
			updateStudyAndSaveIndb(study, N, user);
		}
		
		// updater la config pour le statut et sauver dans base :
		if (config.state != null && StringUtils.isNotBlank(config.state.code) && NONE.equals(config.state.code)) {
			updateConfigurationAndSaveIndb(config, N, user);
		}

		// valider submission une fois les experiments et samples sauves, et sauver submission
		contextValidation.setCreationMode();
//		contextValidation.putObject("type", "sra");
		submission.validate(contextValidation);
		if (!submissionAPI.dao_checkObjectExist("code",submission.code)){			
			submission.validate(contextValidation);
			submissionAPI.dao_saveObject(submission);
			logger.debug("sauvegarde dans la base du submission " + submission.code);
		}

		if (contextValidation.hasErrors()) {
			logger.debug("submission.validate produit des erreurs");
			// rallBack avec clean sur exp et sample et mise à jour study
			logger.debug("\ndisplayErrors dans SubmissionServices::initPrimarySubmission :");
			contextValidation.displayErrors(logger);
			logger.debug("\n end displayErrors dans SubmissionServices::initPrimarySubmission :");

			// enlever les samples, experiments et submission qui ont ete crées par le service et remettre
			// readSet.submissionState à NONE, et si studyCode utilisé par cette seule soumission remettre studyCode.state.code=NONE
			// et si config utilisé par cette seule soumission remettre configCode.state.code=NONE
			//cleanDataBase(submission.code, contextValidation);		
			//throw new SraException("SubmissionServices::initPrimarySubmission::probleme validation  voir log: ");
			submissionWorkflowsHelper.rollbackSubmission(submission, contextValidation);	
			contextValidation.displayErrors(logger);
			throw new SraException("SubmissionServices::initPrimarySubmission::probleme validation : "+ contextValidation.getErrors());			
		} 
		logger.debug("Creation de la soumission " + submission.code);
		return submission.code;
	}

	
	
	
	
	
	public ExternalStudy fetchExternalStudy(String studyAc, String user) throws SraException {
		ExternalStudy externalStudy;
//		if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class, "accession", studyAc)){
		if (abstractStudyAPI.dao_checkObjectExist("accession", studyAc)){
			// verifier que si objet existe dans base avec cet AC c'est bien un type externalStudy et non un type study
			AbstractStudy absStudy;
//			absStudy = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME,
//				AbstractStudy.class, DBQuery.and(DBQuery.is("accession", studyAc)));
			absStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", studyAc));
			
			//if (! (absStudy instanceof ExternalStudy)) {
//			if ( ExternalStudy.class.isInstance(absStudy)) {
			if (absStudy instanceof ExternalStudy) {
				//logger.debug("Recuperation dans base du study avec Ac = " + studyAc +" qui est du type externalStudy ");
			} else {
				throw new SraException("Recuperation dans base du study avec Ac = " + studyAc +" qui n'est pas du type externalStudy ");
			}
//			externalStudy = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME,
//					ExternalStudy.class, DBQuery.and(DBQuery.is("accession", studyAc)));
			externalStudy = externalStudyAPI.dao_findOne(DBQuery.is("accession", studyAc));		
		} else {
			// Creer objet externalStudy
			String externalStudyCode = sraCodeHelper.generateExternalStudyCode(studyAc);
			externalStudy = new ExternalStudy(); // objet avec state.code = submitted
			externalStudy.accession = studyAc;
			externalStudy.code = externalStudyCode;
			externalStudy.traceInformation.setTraceInformation(user);	
			externalStudy.state = new State(F_SUB, user);
		}
		return externalStudy;
	}
	
	public File createDirSubmission(Submission submission) throws SraException{
		// Determiner le repertoire de soumission:
//		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");	
//		Date courantDate = new java.util.Date();
//		String st_my_date = dateFormat.format(courantDate);
//					
//		String syntProjectCode = submission.code;
		/*
		for (String projectCode: submission.projectCodes) {
			if (StringUtils.isNotBlank(projectCode)) {
				syntProjectCode += "_" + projectCode;
			}
		}
		if (StringUtils.isNotBlank(syntProjectCode)){
			syntProjectCode = syntProjectCode.replaceFirst("_", "");
		}
		*/			
		//submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + syntProjectCode + File.separator + st_my_date;
		//submission.submissionTmpDirectory = VariableSRA.submissionRootDirectory + File.separator + syntProjectCode + File.separator + "tmp_" + st_my_date;
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code; 
		if (submission.release) {
			submission.submissionDirectory = submission.submissionDirectory + "_release"; 
		}
		File dataRep = new File(submission.submissionDirectory);
		logger.debug("Creation du repertoire de soumission : " + submission.submissionDirectory);
		logger.info("Creation du repertoire de soumission" + submission.submissionDirectory);
		if (dataRep.exists()){
			throw new SraException("Le repertoire " + dataRep + " existe deja !!! (soumission concurrente ?)");
		} else {
			if(!dataRep.mkdirs()){	
				throw new SraException("Impossible de creer le repertoire " + dataRep + " ");
			}
		}
		return (dataRep);
	}

	
	public void activatePrimarySubmission(ContextValidation contextValidation, String submissionCode) throws SraException {
		// creer repertoire de soumission sur disque et faire liens sur données brutes
		System.out.println("XXXX Dans services.SubmissionsServices.activatePrimarySubmissiion :");
		logger.debug("XXXX logger Dans services.SubmissionsServices.activatePrimarySubmissiion :");

//		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, submissionCode);
		Submission submission = submissionAPI.dao_getObject(submissionCode);
		if (submission == null){
			throw new SraException("aucun objet submission dans la base pour  : " + submissionCode);
		}
		String user = contextValidation.getUser();
		
		try {
			// creation repertoire de soumission :
//			File dataRep = 
					createDirSubmission(submission);
			// creation liens donnees brutes vers repertoire de soumission
			for (String experimentCode: submission.experimentCodes) {
				logger.debug("expCode = " + experimentCode);

//				Experiment expElt =  MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, experimentCode);
				Experiment expElt =  experimentAPI.dao_getObject(experimentCode);
//				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, expElt.readSetCode);
				ReadSet readSet = readsetsAPI.get(expElt.readSetCode);
//				Project p = MongoDBDAO.findByCode(InstanceConstants.PROJECT_COLL_NAME, Project.class, readSet.projectCode);
				Project p = projectAPI.get(readSet.projectCode);
	
				logger.debug("exp = "+ expElt.code);
				
				for (RawData rawData :expElt.run.listRawData) {
					// Partie de code deportée dans activate : on met la variable location à CNS
					// si les données sont physiquement au CNS meme si elles sont aussi au CCRT
					// et on change le chemin pour remplacer /ccc/genostore/.../rawdata par /env/cns/proj/ 
					
					String cns_directory = rawData.directory;
					if (rawData.directory.startsWith("/ccc/")) {
						int index = rawData.directory.indexOf("/rawdata/");
						String lotseq_dir = rawData.directory.substring(index + 9);
						cns_directory="/env/cns/proj/" + lotseq_dir;
					}
					logger.debug("rawData = " + rawData.relatifName);
					File fileCible = new File(cns_directory + File.separator + rawData.relatifName);
					if(fileCible.exists()){
						logger.debug("XXXXXXXXXXXXXXXXXXXX le fichier "+ fileCible +"existe bien");
						rawData.location = "CNS";
						rawData.directory = cns_directory;
					} else {
						logger.debug("XXXXXXXXXXXXXXXXXXXX le fichier "+ fileCible +"n'existe pas au CNS");
						if ("CNS".equalsIgnoreCase(readSet.location)) {
							if (p.archive){
								throw new SraException(rawData.relatifName + " n'existe pas sur les disques CNS, et Projet " + p.code + " avec archive=true, et readSet= " + readSet.code + " avec location = CNS");
							} else {
								throw new SraException(rawData.relatifName + " n'existe pas sur les disques CNS, et Projet " + p.code + " avec archive=false, et readSet " + readSet.code  + " localisée au CNS");
							}
						} else if ("CCRT".equalsIgnoreCase(readSet.location)) {
							rawData.location = readSet.location;
						} else {
							throw new SraException(rawData.relatifName + " avec location inconnue => " + readSet.location);
						}
					}
					if (rawData.extention.equalsIgnoreCase("fastq")) {
						rawData.gzipForSubmission = true;
					} else {
						rawData.gzipForSubmission = false;
						if (StringUtils.isBlank(rawData.md5)){
							contextValidation.addError("md5", " valeur à null alors que donnée deja zippée pour "+ rawData.relatifName);
						}
					}
					// On ne cree les liens dans repertoire de soumission vers rep des projets que si la 
					// donnée est au CNS et si elle n'est pas à zipper
					if ("CNS".equalsIgnoreCase(rawData.location) && ! rawData.gzipForSubmission) {
						logger.debug("run = "+ expElt.run.code);
						File fileLien = new File(submission.submissionDirectory + File.separator + rawData.relatifName);
						if(fileLien.exists()){
							fileLien.delete();
						}
						
						logger.debug("fileCible = " + fileCible);
						logger.debug("fileLien = " + fileLien);
						Path lien = Paths.get(fileLien.getPath());
						Path cible = Paths.get(fileCible.getPath());
						Files.createSymbolicLink(lien, cible);
						logger.debug("Lien symbolique avec :  lien= "+lien+" et  cible="+cible);
						//String cmd = "ln -s -f " + rawData.directory + File.separator + rawData.relatifName
						//+ " " + submission.submissionDirectory + File.separator + rawData.relatifName;
						//logger.debug("cmd = " + cmd);
					} else {
						logger.debug("Donnée "+ rawData.relatifName + " localisée au " + rawData.location);
					}
				}
				// sauver dans base la liste des rawData avec bonne location et bon directory:
//				MongoDBDAO.update(InstanceConstants.SRA_EXPERIMENT_COLL_NAME,  Experiment.class, 
//					DBQuery.is("code", experimentCode),
//					DBUpdate.set("run.listRawData", expElt.run.listRawData));
				
				//verifier autorisation champs dans experimentAPI
				experimentAPI.dao_update(DBQuery.is("code", experimentCode),
						DBUpdate.set("run.listRawData", expElt.run.listRawData));
				}
		} catch (SraException e) {
			throw new SraException(" Dans activatePrimarySubmission" + e);			
		} catch (SecurityException e) {
			throw new SraException(" Dans activatePrimarySubmission pb SecurityException: " + e);
		} catch (UnsupportedOperationException e) {
			throw new SraException(" Dans activatePrimarySubmission pb UnsupportedOperationException: " + e);
		} catch (FileAlreadyExistsException e) {
			throw new SraException(" Dans activatePrimarySubmission pb FileAlreadyExistsException: " + e);
		} catch (IOException e) {
			throw new SraException(" Dans activatePrimarySubmission pb IOException: " + e);		
		}
		
		
		// mettre à jour le champs submission.studyCode si besoin, si study à soumettre, si study avec state=uservalidate
		State state = new State(IW_SUB, user);
		submissionWorkflows.setState(contextValidation, submission, state);
		if (! contextValidation.hasErrors()) {
		// updater la soumission dans la base pour le repertoire de soumission (la date de soumission sera mise à la reception des AC)
//		MongoDBDAO.update(InstanceConstants.SRA_SUBMISSION_COLL_NAME,  Submission.class, 
//				DBQuery.is("code", submission.code),
//				DBUpdate.set("submissionDirectory", submission.submissionDirectory));
		submissionAPI.dao_update(DBQuery.is("code", submission.code),
				DBUpdate.set("submissionDirectory", submission.submissionDirectory));
		} else {
			logger.debug("Probleme pour passer la soumission avec le status "+ IW_SUB);
			contextValidation.displayErrors(logger);
			throw new SraException(" Dans activatePrimarySubmission Erreurs : " + contextValidation.getErrors().toString());		
		}
		
	}
		
	
	//private Submission createSubmissionEntity(Configuration config, String studyCode, String user, Map<String, UserCloneType> mapUserClones) throws SraException{
	private Submission createSubmissionEntity(Configuration config, String studyCode, String studyAc, String user) throws SraException{
		if (config==null) {
			throw new SraException("SubmissionServices::createSubmissionEntity::configuration : config null ???");
		}
		Submission submission = null;
//		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");	
		Date courantDate = new java.util.Date();
//		String st_my_date = dateFormat.format(courantDate);	
		submission = new Submission(user, config.projectCodes);
		submission.code = sraCodeHelper.generateSubmissionCode(config.projectCodes);
		submission.creationDate = courantDate;
		logger.debug("submissionCode="+ submission.code);
		submission.state = new State(N, user);
		submission.release = false; // soumission toujours en confidentielle et levee de confidentialite du
		                            // study par l'utilisateur via interface.
		submission.configCode = config.code;
		// Creation de la map deportéé dans methode initPrimarySubmission pour ne mettre dans la map
		// que les noms de clones utilisee par la soumission

		
		if (config.strategyStudy.equalsIgnoreCase("strategy_external_study")) {
			// pas de study à soumettre et par defaut pas de release.
			// ok submission.release == false et submission sans studyCode.
		} else if (config.strategyStudy.equalsIgnoreCase("strategy_internal_study")) {
		
			Study study = null;
			
			if (StringUtils.isNotBlank(studyAc)) {
//				study = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME,
//						Study.class, DBQuery.and(DBQuery.is("accession", studyAc)));
				study = studyAPI.dao_findOne(DBQuery.is("accession", studyAc));
				
			} else if (StringUtils.isNotBlank(studyCode)) {
//				study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, studyCode);
				study = studyAPI.dao_getObject(studyCode);
			} else {
				// rien : cela peut etre une soumission avec mapCloneToAc
			}
			if (study!=null){
				if (study.state == null) {
					throw new SraException("study.state== null incompatible avec soumission. =>  study.state.code in ('"+N+"','"+F_SUB+"')");
				}
			
				// mettre à jour l'objet submission pour le study  :
				if (! submission.refStudyCodes.contains("study.code")){
					submission.refStudyCodes.add(study.code);
				}
				
				// liste des soumission avec ce studyCode à soumettre:
				
				if (study.state.code.equals(NONE)) {
					
					submission.studyCode = study.code; // studyCode à soumettre
				} else if (study.state.code.equals(F_SUB)) {
				    // pas de soumission du study
				} else {
					throw new SraException("study.state.code" + study.state.code + " not in ('"+NONE+"', '"+F_SUB+"') => utilisation pour cette soumission d'un study en cours de soumission ?");
				}
			} 
		
		}
		return submission;
	}
	
	

	public String getNcbiScientificName(Integer taxonId) 
			throws IOException, 
			XPathExpressionException, 
			InterruptedException,
			ExecutionException,
			TimeoutException {
		//Promise<WSResponse> homePage = WS.url("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id="+taxonId+"&retmote=xml").get();
		CompletionStage<WSResponse> homePage = ws.url("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=taxonomy&id="+taxonId+"&retmote=xml").get();
		// Promise<Document> xml = homePage.map(response -> {
		CompletionStage<Document> xml = homePage.thenApplyAsync(response -> {
			try {
				logger.debug("response "+response.getBody());
				//Document d = XML.fromString(response.getBody());
				//Node n = scala.xml.XML.loadString(response.getBody());
				//logger.debug("J'ai une reponse ?"+ n.toString());
				DocumentBuilderFactory dbf =
						DocumentBuilderFactory.newInstance();
				//dbf.setValidating(false);
				//dbf.setSchema(null);
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document doc = db.parse(new InputSource(new StringReader(response.getBody())));
				return doc;
			} catch (SAXException e) {
				throw new RuntimeException("xml parsing failed",e);
			} catch (IOException e) {
				throw new RuntimeException("io error while parsing xml",e);
			} catch (ParserConfigurationException e) {
				throw new RuntimeException("xml parser configuration error",e);
			}

		});
		
//		Document doc = xml.toCompletableFuture().get(1000,TimeUnit.MILLISECONDS);
		Document doc = xml.toCompletableFuture().get();
		XPath xPath =  XPathFactory.newInstance().newXPath();
		String expression = "/TaxaSet/Taxon/ScientificName";

		//read a string value
		String scientificName = xPath.compile(expression).evaluate(doc);
		return scientificName;	
	}
		
		


	private Sample fetchSample(ReadSet readSet, String strategySample, String scientificName, String user) throws SraException {
		// Recuperer pour chaque readSet les objets de laboratory qui existent forcemment dans mongoDB, 
		// et qui permettront de renseigner nos objets SRA :
		
		String laboratorySampleCode = readSet.sampleCode;
//		models.laboratory.sample.instance.Sample laboratorySample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, models.laboratory.sample.instance.Sample.class, laboratorySampleCode);
		models.laboratory.sample.instance.Sample laboratorySample = laboSampleAPI.get(laboratorySampleCode);
//		String laboratorySampleName = laboratorySample.name;

//		String clone = laboratorySample.referenceCollab;
		String taxonId = laboratorySample.taxonCode;

//		String laboratoryRunCode = readSet.runCode;
//		models.laboratory.run.instance.Run  laboratoryRun = 
//				MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, models.laboratory.run.instance.Run.class, laboratoryRunCode);
//		models.laboratory.run.instance.Run  laboratoryRun = laboRunAPI.dao_getObject(laboratoryRunCode);
		logger.debug("!!!!!!!!!!!!!!!!!!!!!!! strategy_sample = " + strategySample);
		logger.debug("!!!!!!!!!!!!!!!!!!!!!!! readsetProjectCode = " + readSet.projectCode);
		logger.debug("!!!!!!!!!!!!!!!!!!!!!!! readsetCode = " + readSet.code);
		String codeSample = sraCodeHelper.generateSampleCode(readSet, readSet.projectCode, strategySample);
		Sample sample = null;
		// Si sample existe, prendre l'existant, sinon en creer un nouveau
		//if (services.SraDbServices.checkCodeSampleExistInSampleCollection(codeSample)) {
//		if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, Sample.class, "code", codeSample)){
//			logger.debug("Recuperation du sample "+ codeSample);
//			sample = MongoDBDAO.findByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, models.sra.submit.common.instance.Sample.class, codeSample);			
//			logger.debug(sample.clone);
//			logger.debug(sample.taxonId);
//			logger.debug(sample.title);
		if (sampleAPI.dao_checkObjectExist("code", codeSample)){
			sample = sampleAPI.dao_getObject(codeSample);			
		} else {
			//logger.debug("Creation du sample '"+ codeSample + "'");
			// creation du sample :
			sample = new Sample();
			sample.code = codeSample;
			sample.taxonId = new Integer(taxonId);
			// enrichir le sample avec scientific_name:
			sample.scientificName = scientificName;
			sample.clone = laboratorySample.referenceCollab;
			sample.projectCode = readSet.projectCode;
			sample.state = new State(N, user);
			sample.traceInformation.setTraceInformation(user);			
		}
//		logger.debug("readSetCode = {}", readSet.code);
//		logger.debug("laboratorySampleCode = {}" , laboratorySampleCode);
//		logger.debug("laboratorySampleName = {}" , laboratorySampleName);
//		logger.debug("taxonId = {}" , taxonId);
//		logger.debug("clone = {}" , clone);
//		logger.debug("typeBanque = {}", libProcessTypeCodeVal);
		return sample;
	}
	
	
	
	/*public ExternalSample fetchExternalSample(String sampleAc, String user) throws SraException {
		String externalSampleCode = SraCodeHelper.getInstance().generateExternalSampleCode(sampleAc);
		ExternalSample externalSample;
		if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, ExternalSample.class, "code", externalSampleCode)){
			//logger.debug("Recuperation du sample "+ externalSampleCode);
			externalSample = MongoDBDAO.findByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, models.sra.submit.common.instance.ExternalSample.class, externalSampleCode);
		} else {
			externalSample = new ExternalSample(); // objet avec state.code = submitted
			externalSample.accession = sampleAc;
			externalSample.code = externalSampleCode;
			externalSample.state = new State("F-SUB", user);			
			externalSample.traceInformation.setTraceInformation(user);		
		}
		return externalSample;
	}
	*/
	
	public ExternalSample fetchExternalSample(String sampleAc, String user) throws SraException {
		logger.debug("Entree dans fetchExternalSample");

		ExternalSample externalSample;

//		if (MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class, "accession", sampleAc)){
//			AbstractSample absSample;
//			absSample = MongoDBDAO.findOne(InstanceConstants.SRA_SAMPLE_COLL_NAME,
//				AbstractSample.class, DBQuery.and(DBQuery.is("accession", sampleAc)));
		if (abstractSampleAPI.dao_getObject(sampleAc) != null) {				
			AbstractSample absSample;
			absSample = abstractSampleAPI.dao_getObject(sampleAc);
					
			if (absSample instanceof ExternalSample) {
				//logger.debug("Recuperation dans base du sample avec Ac = " + sampleAc +" qui est du type externalSample ");
			} else {
				throw new SraException("Recuperation dans base du sample avec Ac = " + sampleAc +" qui n'est pas du type externalSample ");
			}
//			externalSample = MongoDBDAO.findOne(InstanceConstants.SRA_SAMPLE_COLL_NAME,
//					ExternalSample.class, DBQuery.and(DBQuery.is("accession", sampleAc)));
			externalSample = externalSampleAPI.dao_getObject(sampleAc);
			

		} else {
			logger.debug("Creation dans base du sample avec Ac" + sampleAc);
			String externalSampleCode = sraCodeHelper.generateExternalSampleCode(sampleAc);
			externalSample = new ExternalSample(); // objet avec state.code = submitted
			externalSample.accession = sampleAc;
			externalSample.code = externalSampleCode;
			externalSample.traceInformation.setTraceInformation(user);	
			externalSample.state = new State(F_SUB, user);
		}
		return externalSample;
	}
	
	
	

	/**
	 * createExperimentEntity.
	 * @param readSet         objet readSet
	 * @param scientificName  scientific name
	 * @param user            utilisateur
	 * @return                experiment present dans la base ou bien l'experiment crée dans la methode (sans sauvegarde dans la base)
	 * @throws SraException   erreur
	 */
	public Experiment createExperimentEntity(ReadSet readSet, String scientificName, String user) throws SraException {
		// On cree l'experiment pour le readSet demandé.
		// La validite du readSet doit avoir été testé avant.

		Experiment experiment = new Experiment(); 
		//SraParameter sraParam = new SraParameter();
		Map<String, String> mapLibProcessTypeCodeVal_orientation = VariableSRA.mapLibProcessTypeCodeVal_orientation();

		experiment.code = sraCodeHelper.generateExperimentCode(readSet.code);
		experiment.readSetCode = readSet.code;
		experiment.projectCode = readSet.projectCode;
		experiment.traceInformation.setTraceInformation(user);
		logger.debug("expCode =" + experiment.code);
		String laboratoryRunCode = readSet.runCode;
		logger.debug("laboratoryRunCode =" + laboratoryRunCode);

		models.laboratory.run.instance.Run  laboratoryRun = laboRunAPI.get(laboratoryRunCode);
		Map<String, PropertyValue> sampleOnContainerProperties = readSet.sampleOnContainer.properties;
//		Set <String> listKeysSampleOnContainerProperties = null;

//		if (sampleOnContainerProperties != null) {
//			listKeysSampleOnContainerProperties = sampleOnContainerProperties.keySet();  // Obtenir la liste des clés
//			for(String k: listKeysSampleOnContainerProperties) {
//				//logger.debug("lulu cle = '" + k+"'  => ");
//				PropertyValue propertyValue = sampleOnContainerProperties.get(k);
//			}
//			if (sampleOnContainerProperties.containsKey("libProcessTypeCode")) {
//				String libProcessTypeCode = (String) sampleOnContainerProperties.get("libProcessTypeCode").getValue();
//				//logger.debug(" !!! libProcessTypeCode="+ libProcessTypeCode);
//			}
//		}
		String libProcessTypeCodeVal = (String) sampleOnContainerProperties.get("libProcessTypeCode").getValue();
		String typeCode = readSet.typeCode;
		//logger.debug("libProcessTypeCodeVal = "+libProcessTypeCodeVal);
		//logger.debug("typeCode = "+typeCode);

		if (readSet.typeCode.equalsIgnoreCase("rsillumina")){
			typeCode = "Illumina";
		} else if (readSet.typeCode.equalsIgnoreCase("rsnanopore")){
			typeCode = "oxford_nanopore";
		} else if (readSet.typeCode.equalsIgnoreCase("default-readset")){
			// rien condition à eliminer au plus tard, lors de la mise en prod.
		} else {
			throw new SraException("readset.typeCode inconnu " + typeCode);
		}
		experiment.typePlatform = typeCode.toLowerCase();
		experiment.title = scientificName + "_" + typeCode + "_" + libProcessTypeCodeVal;
		experiment.libraryName = readSet.sampleCode + "_" +libProcessTypeCodeVal;			

		//logger.debug("typePlatform="+ experiment.typePlatform);
		//logger.debug("title="+ experiment.title);
		//logger.debug("libraryName="+ experiment.libraryName);
		
		if(laboratoryRun==null){
			throw new SraException("Pas de laboratoryRun pour " + readSet.code);
		}
		if (laboratoryRun.instrumentUsed == null ){
			throw new SraException("Pas de champs instrumentUsed pour le laboratoryRun " + laboratoryRun.code);
		} 
		//logger.debug("Recuperation de instrumentUsed = "+ laboratoryRun.instrumentUsed);
		InstrumentUsed instrumentUsed = laboratoryRun.instrumentUsed;
		
		//logger.debug(" !!!!!!!!! instrumentUsed.code = " + instrumentUsed.code);
		//logger.debug("!!!!!!!!!!!! instrumentUsed.typeCode = '" + instrumentUsed.typeCode+"'");
		//logger.debug("!!!!!!!!! instrumentUsed.typeCodeMin = '" + instrumentUsed.typeCode.toLowerCase()+"'");
		experiment.instrumentModel = VariableSRA.mapInstrumentModel().get(instrumentUsed.typeCode.toLowerCase());
		if (StringUtils.isBlank(experiment.instrumentModel)) {
			System.err.println("Pas de correspondance existante pour instrumentUsed.typeCodeMin = '" + instrumentUsed.typeCode.toLowerCase()+"'");	
		}
		
		experiment.libraryLayoutNominalLength = null;		
		if( ! "rsnanopore".equalsIgnoreCase(readSet.typeCode)){
			// Rechercher libraryLayoutNominalLength pour les single illumina (paired)
			// mettre la valeur calculée de libraryLayoutNominalLength
			models.laboratory.run.instance.Treatment treatmentMapping = readSet.treatments.get("mapping");
			if (treatmentMapping != null) {
				logger.debug("treatmentMapping pour " + experiment.code);
				Map <String, Map<String, PropertyValue>> resultsMapping = treatmentMapping.results();
				if ( resultsMapping != null && (resultsMapping.containsKey("pairs"))){
					Map<String, PropertyValue> pairs = resultsMapping.get("pairs");
					if (pairs != null) {
//						Set <String> listKeysMapping = pairs.keySet();  // Obtenir la liste des clés
//						for(String k: listKeysMapping) {
//							//System.out.print("coucou cle = '" + k+"'  => ");
//							PropertyValue propertyValue = pairs.get(k);
//							//logger.debug(propertyValue.value);
//						}
						if (pairs.containsKey("estimatedPEInsertSize")) {
							PropertyValue estimatedInsertSize = pairs.get("estimatedPEInsertSize");
							experiment.libraryLayoutNominalLength = (Integer) estimatedInsertSize.value;
							logger.debug("valeur calculee libraryLayoutNominalLength  => "  + experiment.libraryLayoutNominalLength);
						} 
						if (pairs.containsKey("estimatedMPInsertSize")) {
							PropertyValue estimatedInsertSize = pairs.get("estimatedMPInsertSize");
							experiment.libraryLayoutNominalLength = (Integer) estimatedInsertSize.value;
							logger.debug("valeur calculee libraryLayoutNominalLength  => "  + experiment.libraryLayoutNominalLength);
						}	
					}
				}
			}
			
			if (experiment.libraryLayoutNominalLength == null) {
				// mettre valeur theorique de libraryLayoutNominalLength (valeur a prendre dans readSet.sampleOnContainer.properties.nominalLength) 
				// voir recup un peu plus bas:
				//Map<String, PropertyValue> sampleOnContainerProperties = readSet.sampleOnContainer.properties;
//				if (sampleOnContainerProperties != null) {
					//Set <String> listKeysSampleOnContainerProperties = sampleOnContainerProperties.keySet();  // Obtenir la liste des clés

//					for(String k: listKeysSampleOnContainerProperties){
//						//System.out.print("MA cle = '" + k +"'");
//						PropertyValue propertyValue = sampleOnContainerProperties.get(k);
//						//System.out.print(propertyValue.toString());
//						//logger.debug(", MA value  => "+propertyValue.value);
//					} 
					if (sampleOnContainerProperties.containsKey("libLayoutNominalLength")) {	
						logger.debug("************** recherche valeur theorique possible pour " + experiment.code );
						PropertyValue nominalLengthTypeCode = sampleOnContainerProperties.get("libLayoutNominalLength");
						Integer nominalLengthCodeValue = (Integer) nominalLengthTypeCode.value;
						if ((nominalLengthCodeValue != null) && (nominalLengthCodeValue!= -1)){
							experiment.libraryLayoutNominalLength = nominalLengthCodeValue;
							logger.debug("************ valeur theorique libraryLayoutNominalLength  => "  + experiment.libraryLayoutNominalLength);
						}
					}
//				}
			}
		}
		//logger.debug("valeur de experiment.libLayoutExpLength"+ experiment.libraryLayoutNominalLength);			
		experiment.state = new State(N, user); 
		String laboratorySampleCode = readSet.sampleCode;
		//models.laboratory.sample.instance.Sample laboratorySample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, models.laboratory.sample.instance.Sample.class, laboratorySampleCode);
//		models.laboratory.sample.instance.Sample laboratorySample = 
				laboSampleAPI.get(laboratorySampleCode);
//		String taxonId = laboratorySample.taxonCode;
		//logger.debug("sampleCode=" +laboratorySampleCode); 
		//logger.debug("taxonId=" +taxonId); 
		//String laboratoryRunCode = readSet.runCode;
		//models.laboratory.run.instance.Run  laboratoryRun = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, models.laboratory.run.instance.Run.class, laboratoryRunCode);
//		String technology = laboratoryRun.instrumentUsed.typeCode;
		
		if( ! "rsnanopore".equalsIgnoreCase(readSet.typeCode)){
			// Recuperer l'information spotLength pour les illumina
			models.laboratory.run.instance.Treatment treatmentNgsrg = (laboratoryRun.treatments.get("ngsrg"));
			if (treatmentNgsrg != null) {
				Map <String, Map<String, PropertyValue>> resultsNgsrg = treatmentNgsrg.results();
				if (resultsNgsrg != null && resultsNgsrg.containsKey("default")) {
					Map<String, PropertyValue> ngsrg = resultsNgsrg.get("default");
//					Set <String> listKeys = ngsrg.keySet();  // Obtenir la liste des clés
					/*for(String k: listKeys){
					System.out.print("cle = " + k);
					PropertyValue propertyValue = ngsrg.get(k);
					//System.out.print(propertyValue.toString());
					logger.debug(", value  => "+propertyValue.value);
				} */
					PropertyValue propertyNbCycle = ngsrg.get("nbCycle");
					if (ngsrg.get("nbCycle") != null){
						experiment.spotLength = (Long) propertyNbCycle.value;
					}
				}
			}
		}
		
		if ("rsnanopore".equalsIgnoreCase(readSet.typeCode)) {
			// Pas de spot_descriptor
			experiment.libraryLayout = "SINGLE";
			experiment.libraryLayoutOrientation = VariableSRA.mapLibraryLayoutOrientation().get("forward");
		} else {
			
			// Ajouter les read_spec (dans SPOT_DESCRIPTOR ) en fonction de l'information SINGLE ou PAIRED et forward-reverse et last_base_coord :
			// les rsnanopore sont normalement des single forward.
			experiment.libraryLayout = null;
			experiment.libraryLayoutOrientation = null;

			if (laboratoryRun.properties.containsKey("sequencingProgramType")){	
				String libraryLayout =  (String) laboratoryRun.properties.get("sequencingProgramType").value;

				if (StringUtils.isNotBlank(libraryLayout)) { 
					if (libraryLayout.equalsIgnoreCase("SR")){
						experiment.libraryLayout = "SINGLE";
						experiment.libraryLayoutOrientation = VariableSRA.mapLibraryLayoutOrientation().get("forward");
					} else if( libraryLayout.equalsIgnoreCase("PE") || libraryLayout.equalsIgnoreCase("MP")) {
						experiment.libraryLayout = "PAIRED";
						//Map<String, PropertyValue> sampleOnContainerProperties = readSet.sampleOnContainer.properties;

//						if (sampleOnContainerProperties != null) {
//							//Set <String> listKeysSampleOnContainerProperties = sampleOnContainerProperties.keySet();  // Obtenir la liste des clés
//
//							/*for(String k: listKeysSampleOnContainerProperties){
//							System.out.print("cle = " + k);
//							PropertyValue propertyValue = sampleOnContainerProperties.get(k);
//							System.out.print(propertyValue.toString());
//							logger.debug(", value  => "+propertyValue.value);
//						} 
							
						if (sampleOnContainerProperties.containsKey("libProcessTypeCode")) {					
							PropertyValue libProcessTypeCode = sampleOnContainerProperties.get("libProcessTypeCode");
							String libProcessTypeCodeValue = (String) libProcessTypeCode.value;
//							if(libProcessTypeCodeValue.equalsIgnoreCase("A")||libProcessTypeCodeValue.equalsIgnoreCase("C")||libProcessTypeCodeValue.equalsIgnoreCase("N")){
//									experiment.libraryLayoutOrientation = "reverse-forward";
//								} else if (libProcessTypeCodeValue.equalsIgnoreCase("W")||libProcessTypeCodeValue.equalsIgnoreCase("F")
//										||libProcessTypeCodeValue.equalsIgnoreCase("H")||libProcessTypeCodeValue.equalsIgnoreCase("L")
//										||libProcessTypeCodeValue.equalsIgnoreCase("Z")||libProcessTypeCodeValue.equalsIgnoreCase("MI")
//										||libProcessTypeCodeValue.equalsIgnoreCase("K")||libProcessTypeCodeValue.equalsIgnoreCase("DA") 
//										||libProcessTypeCodeValue.equalsIgnoreCase("U")||libProcessTypeCodeValue.equalsIgnoreCase("DB")
//										||libProcessTypeCodeValue.equalsIgnoreCase("DC")||libProcessTypeCodeValue.equalsIgnoreCase("DD")
//										||libProcessTypeCodeValue.equalsIgnoreCase("DE")||libProcessTypeCodeValue.equalsIgnoreCase("RA")
//										||libProcessTypeCodeValue.equalsIgnoreCase("RB")||libProcessTypeCodeValue.equalsIgnoreCase("TA")
//										||libProcessTypeCodeValue.equalsIgnoreCase("TB")){
//									experiment.libraryLayoutOrientation = "forward-reverse";
//								} else {
//									throw new SraException("Pour le readSet " + readSet.code +  ", valeur de libProcessTypeCodeValue differente A,C,N, W, F, H, L ,Z, M, I, K => " + libProcessTypeCodeValue);
//								}
							if (mapLibProcessTypeCodeVal_orientation.get(libProcessTypeCodeValue)==null){
								throw new SraException("Pour le readSet " + readSet.code +  ", valeur de libProcessTypeCodeValue inconnue :" + libProcessTypeCodeValue);
							} else {
								experiment.libraryLayoutOrientation = mapLibProcessTypeCodeVal_orientation.get(libProcessTypeCodeValue);
							}
						}
					} else {
						logger.debug("Pour le laboratoryRun " + laboratoryRun.code + " valeur de properties.sequencingProgramType differente de SR ou PE => " + libraryLayout);
						throw new SraException("Pour le laboratoryRun " + laboratoryRun.code + " valeur de properties.sequencingProgramType differente de SR ou PE => " + libraryLayout);
					}
				}
				logger.debug("libraryLayout======"+libraryLayout);
			}
		}
		experiment.libraryConstructionProtocol = VariableSRA.defaultLibraryConstructionProtocol;
		experiment.run = createRunEntity(readSet);
		experiment.run.expCode=experiment.code;

		// Renseigner l'objet experiment pour lastBaseCoord : Recuperer les lanes associées au
		// run associé au readSet et recuperer le lane contenant le readSet.code. C'est dans les
		// traitement de cette lane que se trouve l'information:
		// Un readSet est sur une unique lane, mais une lane peut contenir plusieurs readSet
		List<Lane> laboratoryLanes = laboratoryRun.lanes;
		logger.debug("'"+readSet.code+"'");
		if (laboratoryLanes != null) {
			for (Lane ll : laboratoryLanes) {
				List<String> readSetCodes = ll.readSetCodes;
				if (readSetCodes != null) {
					for (String rsc : readSetCodes){
						if (rsc.equalsIgnoreCase(readSet.code)){
							// bonne lane = lane correspondant au run associé au readSet
							models.laboratory.run.instance.Treatment laneTreatment = (ll.treatments.get("ngsrg"));
							Map<String, Map<String, PropertyValue>> laneResults = laneTreatment.results();
							Map<String, PropertyValue> lanengsrg = laneResults.get("default");
//							Set<String> laneListKeys = lanengsrg.keySet();  // Obtenir la liste des clés
//							for(String k: laneListKeys) {
//								logger.debug("attention cle = " + k);
//								PropertyValue propertyValue = lanengsrg.get(k);
//								logger.debug(propertyValue.toString());
//								logger.debug(propertyValue.value);
//							}
							experiment.lastBaseCoord =  (Integer) lanengsrg.get("nbCycleRead1").value + 1;
							break;
						}
					}
				} else {
					logger.debug("Aucun readSetCodes dans NGL pour le readset " + readSet.code);
				}
			}
		}
		experiment.readSpecs = new ArrayList<>();
		
		if( ! "rsnanopore".equalsIgnoreCase(readSet.typeCode)){
			// IF ILLUMINA ET SINGLE  Attention != si nanopore et SINGLE
			if (StringUtils.isNotBlank(experiment.libraryLayout) && experiment.libraryLayout.equalsIgnoreCase("SINGLE") ) {
				ReadSpec readSpec_1 = new ReadSpec();
				readSpec_1.readIndex = 0; 
				readSpec_1.readLabel = "F";
				readSpec_1.readClass = "Application Read";
				readSpec_1.readType = "Forward";
//				readSpec_1.baseCoord = (Integer) 1;
				readSpec_1.baseCoord = 1;
				experiment.readSpecs.add(readSpec_1);
			}

			// IF ILLUMINA ET PAIRED ET "forward-reverse"
			if (StringUtils.isNotBlank(experiment.libraryLayout) && experiment.libraryLayout.equalsIgnoreCase("PAIRED") 
					&& StringUtils.isNotBlank(experiment.libraryLayoutOrientation) && experiment.libraryLayoutOrientation.equalsIgnoreCase("forward-reverse") ) {
				ReadSpec readSpec_1 = new ReadSpec();
				readSpec_1.readIndex = 0;
				readSpec_1.readLabel = "F";
				readSpec_1.readClass = "Application Read";
				readSpec_1.readType = "Forward";
//				readSpec_1.baseCoord = (Integer) 1;
				readSpec_1.baseCoord = 1;
				experiment.readSpecs.add(readSpec_1);

				ReadSpec readSpec_2 = new ReadSpec();
				readSpec_2.readIndex = 1;
				readSpec_2.readLabel = "R";
				readSpec_2.readClass = "Application Read";
				readSpec_2.readType = "Reverse";
				readSpec_2.baseCoord = experiment.lastBaseCoord;
				experiment.readSpecs.add(readSpec_2);

			}
			// IF ILLUMINA ET PAIRED ET "reverse-forward"
			if (StringUtils.isNotBlank(experiment.libraryLayout) && experiment.libraryLayout.equalsIgnoreCase("PAIRED") 
					&& StringUtils.isNotBlank(experiment.libraryLayoutOrientation) && experiment.libraryLayoutOrientation.equalsIgnoreCase("reverse-forward") ) {
				ReadSpec readSpec_1 = new ReadSpec();
				readSpec_1.readIndex = 0;
				readSpec_1.readLabel = "R";
				readSpec_1.readClass = "Application Read";
				readSpec_1.readType = "Reverse";
//				readSpec_1.baseCoord = (Integer) 1;
				readSpec_1.baseCoord = 1;
				experiment.readSpecs.add(readSpec_1);

				ReadSpec readSpec_2 = new ReadSpec();
				readSpec_2.readIndex = 1;
				readSpec_2.readLabel ="F";
				readSpec_2.readClass = "Application Read";
				readSpec_2.readType = "Forward";
				readSpec_2.baseCoord = experiment.lastBaseCoord;
				experiment.readSpecs.add(readSpec_2);
			}
		} else {
			//on ne cree pas de readSpec dans le cas de nanopore car pas de spot_descriptor
		}
		return experiment;
	}

	


	public Run createRunEntity(ReadSet readSet) {
		// On cree le run pour le readSet demandé.
		// La validite du readSet doit avoir été testé avant.

		// Recuperer pour le readSet la liste des fichiers associés:

//		String laboratoryRunCode = readSet.runCode;
//		models.laboratory.run.instance.Run  laboratoryRun = 
//				MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, models.laboratory.run.instance.Run.class, laboratoryRunCode);
//		InstrumentUsed instrumentUsed = laboratoryRun.instrumentUsed;
//		models.laboratory.run.instance.Run  laboratoryRun = laboRunAPI.dao_getObject(laboratoryRunCode);
		
		List <models.laboratory.run.instance.File> list_files =  readSet.files;
		if (list_files == null) {
//			logger.debug("Aucun fichier pour le readSet " + readSet.code +"???");
			RuntimeException e = new RuntimeException("Aucun fichier pour le readSet " + readSet.code +"???");
			logger.error("error", e);
			throw e;
		} else {
			//logger.debug("nbre de fichiers = " + list_files.size());
		}
		// Pour chaque readSet, creer un objet run 
		Date runDate = readSet.runSequencingStartDate;
		Run run = new Run();
		run.code = sraCodeHelper.generateRunCode(readSet.code);
		run.runDate = runDate;

		//run.projectCode = projectCode;
		run.runCenter = VariableSRA.centerName;
		// Renseigner le run pour ces fichiers sur la base des fichiers associes au readSet :
		// chemin des fichiers pour ce readset :
		String dataDir = readSet.path;

		for (models.laboratory.run.instance.File runInstanceFile: list_files) {
			String runInstanceExtentionFileName = runInstanceFile.extension;
			// conditions qui doivent etre suffisantes puisque verification préalable que le readSet
			// est bien valide pour la bioinformatique.
			if (runInstanceFile.usable 
					//&& ! runInstanceExtentionFileName.equalsIgnoreCase("fna") && ! runInstanceExtentionFileName.equalsIgnoreCase("qual")
					//&& ! runInstanceExtentionFileName.equalsIgnoreCase("fna.gz") && ! runInstanceExtentionFileName.equalsIgnoreCase("qual.gz")) {
					&&  (runInstanceExtentionFileName.equalsIgnoreCase("fastq.gz") || runInstanceExtentionFileName.equalsIgnoreCase("fastq"))) {
					RawData rawData = new RawData();
					//logger.debug("fichier " + runInstanceFile.fullname);
					rawData.extention = runInstanceFile.extension;
					logger.debug("dataDir "+dataDir);
					rawData.directory = dataDir.replaceFirst("\\/$", ""); // oter / terminal si besoin
					logger.debug("raw data directory"+rawData.directory);
					rawData.relatifName = runInstanceFile.fullname;
					rawData.location = readSet.location;
					if (runInstanceFile.properties != null && runInstanceFile.properties.containsKey("md5")) {
						rawData.md5 = (String) runInstanceFile.properties.get("md5").value;
						logger.debug("Recuperation du md5 pour" + rawData.relatifName +"= " + rawData.md5);
					}
					run.listRawData.add(rawData);
//					Set<String> listKeys = runInstanceFile.properties.keySet();
//					
//					for(String k: listKeys) {
//						//logger.debug("attention cle = " + k);
//						PropertyValue propertyValue = runInstanceFile.properties.get(k);
//						//logger.debug(propertyValue.toString());
//						//logger.debug(propertyValue.value);
//					}
			}
		}
		return run;
	}
	
	// delete la soumission et ses experiments et samples s'ils ne sont pas references par une autre soumission
	// Ne delete pas le study et la config associée car saisis par l'utilisateur qui peut vouloir les utiliser pour une prochaine soumission
	public void cleanDataBase(String submissionCode, ContextValidation contextValidation) throws SraException {
		logger.debug("Recherche objet submission dans la base pour "+ submissionCode);

//		Submission submission = MongoDBDAO.findByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submit.common.instance.Submission.class, submissionCode);
		Submission submission = submissionAPI.dao_getObject(submissionCode);
		if (submission==null){
			logger.debug("Aucun objet submission dans la base pour "+ submissionCode);
			return;
		} else {
			logger.debug("objet submission dans database pour "+ submissionCode);
			logger.debug("submission.accession dans cleanDataBase "+ submission.accession);
		}

		// Si la soumission est connu de l'EBI on ne pourra pas l'enlever de la base :
		if (StringUtils.isNotBlank(submission.accession)){
			logger.debug("objet submission avec AC : submissionCode = "+ submissionCode + " et submissionAC = "+ submission.accession);
			return;
		} 
		// Si la soumission concerne une release avec status "N-R" ou IW-SUB-R:
		if (submission.release && (submission.state.code.equalsIgnoreCase(N_R)||(submission.state.code.equalsIgnoreCase(IW_SUB_R)))) {
			// detruire la soumission :
//			MongoDBDAO.deleteByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, submission.code);
			submissionAPI.dao_deleteByCode(submission.code);
			// remettre le status du study avec un status F-SUB
			// La date de release du study est modifié seulement si retour posifit de l'EBI pour release donc si status F-SUB-R
//			MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class,
//					DBQuery.is("code", submission.studyCode),
//					DBUpdate.set("state.code", "F-SUB").set("traceInformation.modifyUser", contextValidation.getUser()).set("traceInformation.modifyDate", new Date()));

			studyAPI.dao_update(DBQuery.is("code", submission.studyCode),
			        		DBUpdate.set("state.code", F_SUB)
			                		.set("traceInformation.modifyUser", contextValidation.getUser())
			                		.set("traceInformation.modifyDate", new Date()));				
			
			
			//logger.debug("state.code remis à 'N' pour le readSet "+experiment.readSetCode);
			return;
		} 


		if (! submission.experimentCodes.isEmpty()) {
			for (String experimentCode : submission.experimentCodes) {
				// verifier que l'experiment n'est pas utilisé par autre objet submission avant destruction
//				Experiment experiment = MongoDBDAO.findByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, experimentCode);
				Experiment experiment = experimentAPI.dao_getObject(experimentCode);
				// mettre le status pour la soumission des readSet à NONE si possible: 
				if (experiment != null){
					//logger.debug(" !!!! experimentCode = "+ experimentCode);
					//logger.debug(" !!!! experiment.code = "+ experiment.code);

					//logger.debug(" !!!! submissionState.code remis à 'N' pour "+experiment.readSetCode);

					// remettre les readSet dans la base avec submissionState à "NONE":
//					MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
//							DBQuery.is("code", experiment.readSetCode),
//							DBUpdate.set("submissionState.code", initialStateCode).set("traceInformation.modifyUser", contextValidation.getUser()).set("traceInformation.modifyDate", new Date()));
					//logger.debug("submissionState.code remis à 'N' pour le readSet "+experiment.readSetCode);
					readsetsAPI.dao_update(DBQuery.is("code", experiment.readSetCode), 
										  DBUpdate.set("submissionState.code", NONE)
										  .set("traceInformation.modifyUser", contextValidation.getUser())
										  .set("traceInformation.modifyDate", new Date()));
//					List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("experimentCodes", experimentCode)).toList();
//SGAS 
					
					List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("experimentCodes", experimentCode)).toList();
					logger.debug("experimentCode=" + experiment.code + " avec state=" + experiment.state.code);
					if (submissionList.size() > 1) {
						logger.debug("experimentCode=" + experiment.code + " avec state=" + experiment.state.code);
						for (Submission sub: submissionList) {
							logger.debug("     - utilise par objet Submission " + sub.code);
						}
						throw new SraException(experimentCode + " utilise par plusieurs objets submissions");	
					} else {
						// todo : verifier qu'on ne detruit que des experiments en new ou uservalidate
						if (N.equals(experiment.state.code) || V_SUB.equals(experiment.state.code)){
//							MongoDBDAO.deleteByCode(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, models.sra.submit.sra.instance.Experiment.class, experimentCode);
							try {
                                experimentAPI.delete(experimentCode);
                            } catch (APIException e) {
                                throw new SraException(e.getMessage(), e);
                            }
							//logger.debug("deletion dans base pour experiment "+experimentCode);
						} else {
							logger.debug(experimentCode + " non delété dans base car status = " + experiment.state.code);
						}
					}
				}
			}
		}

		if (! submission.refSampleCodes.isEmpty()) {	
			for (String sampleCode : submission.refSampleCodes){
				// verifier que sample n'est pas utilisé par autre objet submission avant destruction
				// normalement sample crees dans init de type external avec state=F-SUB ou sample avec state='N'
//				List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("refSampleCodes", sampleCode)).toList();
				List <Submission> submissionList = (submissionAPI.dao_find(DBQuery.in("refSampleCodes", sampleCode))).toList();
				if (submissionList.size() > 1) {
					for (Submission sub: submissionList) {
						logger.debug(sampleCode + " utilise par objet Submission " + sub.code);
					}
				} else {
//					MongoDBDAO.deleteByCode(InstanceConstants.SRA_SAMPLE_COLL_NAME, models.sra.submit.common.instance.Sample.class, sampleCode);		
					sampleAPI.dao_deleteByCode(sampleCode);		
					logger.debug("deletion dans base pour sample "+sampleCode);
				}
			}
		}

		// verifier que la config à l'etat used n'est pas utilisé par une autre soumission avant de remettre son etat à 'N'
		// update la configuration pour le statut en remettant le statut new si pas utilisé par ailleurs :
//		List <Submission> submissionList = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("configCode", submission.configCode)).toList();
		List <Submission> submissionList = (submissionAPI.dao_find(DBQuery.in("configCode", submission.configCode))).toList();
		if (submissionList.size() <= 1) {
//			MongoDBDAO.update(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, 
//					DBQuery.is("code", submission.configCode),
//					DBUpdate.set("state.code", startStateCode).set("traceInformation.modifyUser", contextValidation.getUser()).set("traceInformation.modifyDate", new Date()));	
//			logger.debug("state.code remis à 'N' pour configuration "+submission.configCode);
			configurationAPI.dao_update(DBQuery.is("code", submission.configCode),DBUpdate.set("state.code", N).set("traceInformation.modifyUser", contextValidation.getUser()).set("traceInformation.modifyDate", new Date()));	
			logger.debug("state.code remis à '"+ NONE +"' pour configuration "+submission.configCode);
		}

		// verifier que le study à l'etat userValidate n'est pas utilisé par une autre soumission avant de remettre son etat à 'N'
		if (StringUtils.isNotBlank(submission.studyCode)){
//			List <Submission> submissionList2 = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("studyCode", submission.studyCode)).toList();
			List <Submission> submissionList2 = (submissionAPI.dao_find(DBQuery.in("studyCode", submission.studyCode))).toList();
			if (submissionList2.size() == 1) {
//				MongoDBDAO.update(InstanceConstants.SRA_STUDY_COLL_NAME, Study.class, 
//						DBQuery.is("code", submission.studyCode),
//						DBUpdate.set("state.code", startStateCode).set("traceInformation.modifyUser", contextValidation.getUser()).set("traceInformation.modifyDate", new Date()));	
				studyAPI.dao_update(DBQuery.is("code", submission.studyCode),
						DBUpdate.set("state.code", NONE).set("traceInformation.modifyUser", contextValidation.getUser()).set("traceInformation.modifyDate", new Date()));					logger.debug("state.code remis à N pour study "+submission.studyCode);
			}	
		}

		if (! submission.refStudyCodes.isEmpty()) {	
			// On ne peut detruire que des ExternalStudy crées et utilisés seulement par la soumission courante.
			for (String studyCode : submission.refStudyCodes){
				// verifier que study n'est pas utilisé par autre objet submission avant destruction
				// normalement study crees dans init de type external avec state=F-SUB ou study avec state='N'
//				List <Submission> submissionList2 = MongoDBDAO.find(InstanceConstants.SRA_SUBMISSION_COLL_NAME, Submission.class, DBQuery.in("refStudyCodes", studyCode)).toList();
				List <Submission> submissionList2 = (submissionAPI.dao_find(DBQuery.in("refStudyCodes", studyCode))).toList();
				if (submissionList2.size() > 1) {
					for (Submission sub: submissionList2) {
						logger.debug(studyCode + " utilise par objet Submission " + sub.code);
					}
				} else {
//					AbstractStudy study = MongoDBDAO.findByCode(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.AbstractStudy.class, studyCode);
					AbstractStudy study = abstractStudyAPI.dao_getObject(studyCode);
					if (study != null){
//						if ( ExternalStudy.class.isInstance(study)) {
						if (study instanceof ExternalStudy) {
//							ExternalStudy eStudy = (ExternalStudy) study;
							// on ne veut enlever que les external_study cree par cette soumission, si internalStudy cree, on veut juste le remettre avec bon state.
							//logger.debug("Recuperation dans base du study avec Ac = " + studyAc +" qui est du type externalStudy ");
							if(F_SUB.equalsIgnoreCase(study.state.code) ){
//								MongoDBDAO.deleteByCode(InstanceConstants.SRA_STUDY_COLL_NAME, models.sra.submit.common.instance.Study.class, studyCode);		
								externalStudyAPI.dao_deleteByCode(studyCode);		
								logger.debug("deletion dans base pour study "+studyCode);
							}
						}
					}
				}
			}
		}

		logger.debug("deletion dans base pour submission "+submissionCode);
//		MongoDBDAO.deleteByCode(InstanceConstants.SRA_SUBMISSION_COLL_NAME, models.sra.submit.common.instance.Submission.class, submissionCode);
		submissionAPI.dao_deleteByCode(submissionCode);
	}
	
	
	
	
	public String initPrimarySubmission_ori(List<String> readSetCodes, String studyCode, String configCode, String acStudy, String acSample, Map<String, UserCloneType>mapUserClones, ContextValidation contextValidation) throws SraException, IOException {
		
	//public String initPrimarySubmission(List<String> readSetCodes, String studyCode, String configCode, String acStudy, String acSample, Map<String, UserCloneType>mapUserClones, Map<String, UserExperimentType> mapUserExperiments, Map< String, UserSampleType> mapUserSamples, ContextValidation contextValidation) throws SraException, IOException {
		//public String initNewSubmission(List<String> readSetCodes, String studyCode, String configCode, Map<String, UserCloneType>mapUserClones, Map<String, UserExperimentType> mapUserExperiments, ContextValidation contextValidation) throws SraException, IOException {
		// Cree en base un objet submission avec state.code=N, met dans la base la configuration avec state.code='N'
		// met les readSet avec state.code = 'N', les experiments avec state.code='N', les samples à soumettre 
		// avec state.code='N' sinon laisse les samples dans leurs state, et met le study à soumettre (study avec state.code='N') avec state.code=V-SUB 

		// Pour simplifier workflow on n'autorise pas a la creation d'une soumission, l'utilisation d'un sample crée par une autre soumission
		// mais toujours dans le processus de soumission. Idem pour les study. Si la creation d'une soumission utilise un study ou un sample deja existant
		// dans la base, alors le state.code doit etre à F-SUB.
		// Pour une premiere soumission d'un readSet, on peut devoir utiliser un study ou un sample existant, deja soumis à l'EBI.
		// En revanche on ne doit pas utiliser un experiment ou un run existant
		
		// Verifier config et initialiser objet submission avec release a false(private) state.code='N'et 
		// pas de validation integrale de config qui a ete stocke dans la base donc valide mais verification
		// de quelques contraintes en lien avec soumission.
		

		String user = contextValidation.getUser();
		if (readSetCodes == null || readSetCodes.size() < 1 ) {
			throw new SraException("Aucun readset à soumettre n'a été indiqué");
		}
		
		logger.debug("Dans init : acStudy = " + acStudy + " et acSample= " + acSample);
		//logger.debug("Dans init : taille map clone = " + mapUserClones.size());
		logger.debug("Taille readSetCodes = " + readSetCodes.size());
		
		if (StringUtils.isBlank(configCode)) {
			throw new SraException("la configuration a un code à null");
		}
//		Configuration config = MongoDBDAO.findByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, configCode);

		
		//Configuration config = validateConfigurationForSubmission(configCode);
		
		Configuration config = configurationAPI.dao_getObject(configCode);
		
		Study study = null;

		
		
		
		if (config == null) {
			throw new SraException("Configuration " + configCode + " n'existe pas dans database");
		} 
		if (StringUtils.isBlank(config.state.code)) {
			throw new SraException("Configuration " + config.code + " avec champs state.code non renseigne");
		}
		logger.debug("dans init, config.state.code='"+config.state.code+"'");
		

		if (config.strategyStudy.equalsIgnoreCase("strategy_internal_study")) {
			if (StringUtils.isNotBlank(acStudy)) {
				// Recuperer le study si acStudy renseigné et strategy_internal_study :
				// si ac renseigné alors study dans l'etat F-SUB, ou en cours release
				if (studyAPI.dao_checkObjectExist("accession", acStudy)) {
					study = studyAPI.dao_findOne(DBQuery.is("accession", acStudy));
				} else {
					throw new SraException("strategy_internal_study et acStudy passé en parametre "+ acStudy + "n'existe pas dans database");	
				}	
			} else {
				// Si studyCode renseigné et strategy_internal_study :
				// study en cours de premiere soumission ou bien à NONE ou bien en cours de release
				if (StringUtils.isNotBlank(studyCode)) {
					if (studyAPI.dao_checkObjectExist("code", studyCode)) {
						study = studyAPI.dao_getObject(studyCode);	
					} else {
						throw new SraException("strategy_internal_study et studyCode passé en parametre "+ studyCode + "n'existe pas dans database");	
					}
					// si study jamais soumis (pas d'ac), alors seul etat autorise = NONE
					if (StringUtils.isBlank(study.accession)) {
						throw new SraException("strategy_internal_study et studyCode passé en parametre "+ studyCode + "avec status incompatible avec soumission :" + study.state.code);	
					}
				}
			}
		} else if (config.strategyStudy.equalsIgnoreCase("strategy_external_study")) {
			if (StringUtils.isNotBlank(acStudy)) {
//				ExternalStudy uniqExternalStudy = fetchExternalStudy(acStudy, user);
			} else {
				if (StringUtils.isNotBlank(studyCode)){
					throw new SraException("Configuration " + config.code + " avec strategy_external_study incompatible avec studyCode renseigne : '" + studyCode +"'");
				}
				if (mapUserClones == null || mapUserClones.isEmpty()){
					throw new SraException("Configuration " + config.code + " avec strategy_study = 'strategy_external_study' incompatible avec acStudy et mapUserClone non renseignés");
				}
			}
		} else {
			throw new SraException("Configuration " + config.code + " avec strategy_study = '"+ config.strategyStudy + "' (valeur non attendue)");
		}

		
		logger.debug("STRATEGY SAMPLE = '" + config.strategySample + "'");
		if (config.strategySample.equalsIgnoreCase("strategy_external_sample")) {
			logger.debug("Dans init, cas strategy_external_sample");
//			if (StringUtils.isNotBlank(acSample)) {
//				uniqExternalSample = fetchExternalSample(acSample, user);
//			} else {
				if (mapUserClones == null || mapUserClones.isEmpty()){
					throw new SraException("Configuration " + config.code + "avec configuration.strategy_sample='strategy_external_sample' incompatible avec acSample nul et mapUserClone non renseigné");
				}
//			}
		} 
		
		if (config.strategySample.equalsIgnoreCase("strategy_internal_sample")) {
			if (StringUtils.isNotBlank(acSample)) {
				//logger.debug("Dans init, cas strategy_internal_sample");
//				uniqSample = sampleAPI.dao_findOne(DBQuery.is("accession", acSample));
			} /*else {
				if (mapUserClones== null || mapUserClones.isEmpty()){
					throw new SraException("Configuration " + config.code + "avec configuration.strategy_sample='strategy_external_sample' incompatible avec mapUserClone non renseigné");
				}
			}*/
		} 
		

		// Verifier que tous les readSetCode passes en parametres correspondent bien a des objets en base avec
		// submissionState='NONE' cad des readSets qui n'interviennent dans aucune soumission.
		List <ReadSet> readSets = new ArrayList<>();
		for (String readSetCode : readSetCodes) {
			if (StringUtils.isNotBlank(readSetCode)) {
				//logger.debug("!!!!!!!!!!!!!         readSetCode = " + readSetCode);
//				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
				ReadSet readSet = readsetsAPI.get(readSetCode);
				if (readSet == null) {
					throw new SraException("Le readSet " + readSetCode + " n'existe pas dans database");
				} else if ((readSet.submissionState == null)||(StringUtils.isBlank(readSet.submissionState.code))) {
					throw new SraException("Le readSet " + readSet.code + " n'a pas de submissionState.code");
				} else if(!readSet.submissionState.code.equalsIgnoreCase(NONE)){
					throw new SraException("Le readSet " + readSet.code + " a un submissionState.code à la valeur " + readSet.submissionState.code);
				} else {
					readSets.add(readSet);
				}
			}
		}	
		
		// Renvoie un objet submission avec state.code='N'
		// et submission.studyCode renseigné (study à envoyer à l'EBI) si config.strategyStudy=strategy_internal_study et studyCode.state.code="N"
		// submission.release = false si config.strategyStudy == strategy_external_study
		// submission.release = studyCode.release si config.strategyStudy == strategy_internal_study
		//String user = contextValidation.getUser();
		Submission submission = createSubmissionEntity(config, studyCode, acStudy, user);
				
		// Liste des sous-objets utilisés dans submission
		List <Experiment> listExperiments = new ArrayList<>(); // liste des experiments utilisés et crees dans soumission avec state.code='N' à sauver dans database
		List <AbstractSample> listAbstractSamples = new ArrayList<>();//liste des AbstractSample(Sample ou ExternalSample) avec state.code='F-SUB' ou 'N' utilises dans soumission à sauver ou non dans database		
		List <AbstractStudy> listAbstractStudies = new ArrayList<>();//liste des AbstractStudy(Study ou ExternalStudy) avec state.code='F-SUB' ou 'N' utilises dans soumission à sauver ou non dans database		

		int countError = 0;
		String errorMessage = "";
	
		for (ReadSet readSet : readSets) {
			logger.debug("readSet : {}", readSet.code);
			// Verifier que c'est une premiere soumission pour ce readSet
			// Verifiez qu'il n'existe pas d'objet experiment referencant deja ce readSet
			Boolean alreadySubmit = experimentAPI.dao_checkObjectExist("readSetCode", readSet.code);		
			if ( alreadySubmit ) {
				// signaler erreur et passer au readSet suivant
				countError++;
				errorMessage = errorMessage + "  - Soumission deja existante dans base pour : '" + readSet.code + "' \n";
				// Recuperer exp dans mongo
				continue;
			} 					
			// Verifier que ce readSet est bien valide avant soumission :
			if (! readSet.bioinformaticValuation.valid.equals(TBoolean.TRUE)) {
				countError++;
				errorMessage = errorMessage + "  - Soumission impossible pour le readset '" + readSet.code + "' parceque non valide pour la bioinformatique \n";
				continue;
			} 
			
			// Recuperer scientificName via NCBI pour ce readSet. Le scientificName est utilisé dans la construction
			// des samples et des experiments 
			String laboratorySampleCode = readSet.sampleCode;
			models.laboratory.sample.instance.Sample laboratorySample = laboSampleAPI.get(laboratorySampleCode);
			String scientificName = laboratorySample.ncbiScientificName;
			if (StringUtils.isBlank(scientificName)){
				//scientificName=updateLaboratorySampleForNcbiScientificName(taxonId, contextValidation);
				throw new SraException("Pas de recuperation du nom scientifique pour le sample "+ laboratorySampleCode);
			} else {
				logger.debug("Nom du scientific Name = "+ laboratorySample.ncbiScientificName);
			}
			// Creer les objets avec leurs alias ou code, les instancier completement et les sauver.
			
			// Creer l'experiment avec un state.code = 'N'
			//logger.debug("scientificName =" + scientificName);
			//logger.debug("librarySelection = " + config.librarySelection);
			//logger.debug("librarySource = " + config.librarySource);
			//logger.debug("libraryStrategy = "+config.libraryStrategy);
			//logger.debug("libraryConstructionProtocol = "+ config.libraryConstructionProtocol);
			
			Experiment experiment = createExperimentEntity(readSet, scientificName, user);
			experiment.librarySelection = config.librarySelection;
			experiment.librarySource = config.librarySource;
			experiment.libraryStrategy = config.libraryStrategy;
			experiment.libraryConstructionProtocol = config.libraryConstructionProtocol;
			
			//logger.debug("scientificName =" + scientificName);
			//logger.debug("librarySelection = " + config.librarySelection);
			//logger.debug("librarySource = " + config.librarySource);
			//logger.debug("libraryStrategy = "+config.libraryStrategy);
			//logger.debug("libraryConstructionProtocol = "+ config.libraryConstructionProtocol);
			
			//String laboratorySampleName = laboratorySample.name;
			String clone = laboratorySample.referenceCollab;
			/*for (Iterator<Entry<String, UserCloneType>> iterator = mapUserClones.entrySet().iterator(); iterator.hasNext();) {
				  Entry<String, UserCloneType> entry = iterator.next();
				  if (entry.getKey().equals(clone)) {
					  logger.debug("dans initPrimarySubmission 1 : cle du userClone = '" + entry.getKey() + "'");
					  logger.debug("       study_ac : '" + entry.getValue().getStudyAc()+  "'");
					  logger.debug("       sample_ac : '" + entry.getValue().getSampleAc()+  "'");
				  }	
			}*/
			//logger.debug("name = '" + laboratorySample.name +"'");
			//Slogger.debug("clone = '" + clone +"'");
			// Creer le sample si besoin et ajouter dans submission.mapUserClone
			if (config.strategySample.equalsIgnoreCase("strategy_external_sample")){
				//logger.debug("Dans init, cas strategy_external_sample");

				ExternalSample externalSample = null;
				if (mapUserClones != null && !mapUserClones.isEmpty()) {
					if (StringUtils.isBlank(clone)) {
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque strategy_external_sample avec mapUserClone et pas de nom de clone dans ngl \n";
						continue;					
					}
					if (! mapUserClones.containsKey(clone)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque strategy_external_sample avec mapUserClone qui ne contient pas le clone '" + clone +"' \n";
						continue;
					}
					String sampleAc = mapUserClones.get(clone).getSampleAc();
					//logger.debug("Pour le clone "+ clone + " sample = " + sampleAc);
					
					if (StringUtils.isBlank(sampleAc)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque mapUserClone pour clone '" + clone + "' ne contient pas de sampleAc \n";
						continue;					
					}
					// recuperation dans base ou création du sample avec status F-SUB (si AC alors F-sub)
					externalSample = fetchExternalSample(sampleAc, user);
					//logger.debug("recuperation dans base du sample " +externalSample.accession);
					//logger.debug("recuperation dans base du sample " +externalSample.code);
					// Mise a jour de l'objet submission pour mapUserClone :
					UserCloneType submission_userClone = new UserCloneType();
					//logger.debug("alias="+mapUserClones.get(clone).getAlias());
					//logger.debug("sampleAC="+mapUserClones.get(clone).getSampleAc());
					//logger.debug("studyAC="+mapUserClones.get(clone).getStudyAc());
					submission_userClone.setAlias(mapUserClones.get(clone).getAlias());
					submission_userClone.setSampleAc(mapUserClones.get(clone).getSampleAc());
				    submission_userClone.setStudyAc(mapUserClones.get(clone).getStudyAc());
				    submission.mapUserClone.put(submission_userClone.getAlias(), submission_userClone);
				    
				} else if (StringUtils.isNotBlank(acSample)) {
					externalSample = fetchExternalSample(acSample, user);
				} else {
					logger.error("empty mapUserClone", new Exception());
					continue;
				}
				
				// Mise a jour de l'objet submission pour les samples references
				//logger.debug("Mise a jour de l'objet submission pour les samples references");
				if (!submission.refSampleCodes.contains(externalSample.code)) {
					submission.refSampleCodes.add(externalSample.code);
				}
				
				// Pas de mise a jour de l'objet submission pour les samples à soumettre.
				
				//logger.debug("Mise a jour de listAbstractSamples pour les samples references");

				if (!listAbstractSamples.contains(externalSample)) {
					listAbstractSamples.add(externalSample);
				}
				//logger.debug("Mise a jour de experiment pour les samples references");

				// mettre à jour l'experiment pour la reference sample :
				experiment.sampleCode = externalSample.code;
				experiment.sampleAccession = externalSample.accession;
				
			} else { // strategie internal_sample
				logger.debug("Dans init, cas strategy_internal_sample");

				Sample sample = null;

				if (StringUtils.isNotBlank(acSample)) {
					sample = sampleAPI.dao_getObject(acSample);
					if (sample == null) 
						throw new RuntimeException("impossible de recuperer le sample "  + acSample + " dans la base avec strategie internal sample");
					
				} else {
				
					// Recuperer le sample existant avec son state.code ou bien en creer un nouveau avec state.code='N'
					sample = fetchSample(readSet, config.strategySample, scientificName, user);
					// Renseigner l'objet submission :
					// Verifier que l'objet sample n'a jamais ete soumis et n'est pas en cours de soumission
					logger.debug("sample = {} et state = {}" , sample, sample.state.code);
					if (! F_SUB.equalsIgnoreCase(sample.state.code) && ! N.equalsIgnoreCase(sample.state.code)) {
						throw new SraException("Tentative d'utilisation dans la soumission du sample "+ sample.code +" en cours de soumission avec state.code==" + sample.state.code);
					}
				}
				// Mise a jour de l'objet submission pour les samples references
				if (!submission.refSampleCodes.contains(sample.code)) {
					submission.refSampleCodes.add(sample.code);
				}
				// Mise a jour de l'objet submission pour les samples à soumettre :
				//------------------------------------------------------------------
//SGAS
				if (N.equalsIgnoreCase(sample.state.code)){	
					List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("sampleCodes", sample.code)).toList();
					//if (submissionList.size() > 0) {
						String message = "sampleCode=" + sample.code + " avec state=" + sample.state.code + "\n";
						for (Submission sub: submissionList) {
							message += "       - utilise par objet Submission " + sub.code + "\n";
						}
						logger.debug(message);
					//	throw new SraException(message);
					//} else {
						if (! submission.sampleCodes.contains(sample.code)){
							submission.sampleCodes.add(sample.code);
						}
					//}
				}
				if(!listAbstractSamples.contains(sample)){
					listAbstractSamples.add(sample);
				}
				// mettre à jour l'experiment pour la reference sample :
				experiment.sampleCode = sample.code;
				experiment.sampleAccession = sample.accession;
			}
			
			if (config.strategyStudy.equalsIgnoreCase("strategy_external_study")){
				logger.debug("Dans init, cas strategy_external_study");

				ExternalStudy externalStudy = null;
				
				if (mapUserClones != null && !mapUserClones.isEmpty()) {
					
					if (! mapUserClones.containsKey(clone)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque strategy_external_study et mapUserClone ne contient pas le clone '" + clone +"' \n";
						continue;
					} else {
						
						//logger.debug("alias="+mapUserClones.get(clone).getAlias());
						//logger.debug("sampleAC="+mapUserClones.get(clone).getSampleAc());
						//logger.debug("studyAC="+mapUserClones.get(clone).getStudyAc());
						// mettre a jour submission.mapUserClone :
						UserCloneType submission_userClone = new UserCloneType();
						submission_userClone.setAlias(mapUserClones.get(clone).getAlias());
						submission_userClone.setSampleAc(mapUserClones.get(clone).getSampleAc());
						submission_userClone.setStudyAc(mapUserClones.get(clone).getStudyAc());
						submission.mapUserClone.put(submission_userClone.getAlias(), submission_userClone);
					}
					
					String studyAc = mapUserClones.get(clone).getStudyAc();
					if (StringUtils.isBlank(studyAc)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque strategy_external_study et mapUserClone.get(clone).getStudyAc() non renseigne pour le clone '" + clone + "'\n";
						continue;					
					}
					
					// creation ou recuperation dans base de externalStudy avec state.code = F-SUB
					externalStudy = fetchExternalStudy(studyAc, user);
				} else if (StringUtils.isNotBlank(acStudy)) {
					externalStudy = fetchExternalStudy(acStudy, user);
				} else {
					throw new RuntimeException("impossible sans mapUserClone et sans acStudy");
				}
				// Mise à jour de l'objet submission pour les study references :
				//logger.debug("Mise à jour de l'objet submission pour les study references :");
				if (!submission.refStudyCodes.contains(externalStudy.code)) {
					submission.refStudyCodes.add(externalStudy.code);
				}
				//logger.debug("Mise à jour de listAbstractStudies pour les study references :");
//				if(!listAbstractStudies.contains(externalStudy.code)){
				final String externalStudyCode = externalStudy.code;
				if(! Iterables.contains(listAbstractStudies, s->s.code.equals(externalStudyCode))) {
					listAbstractStudies.add(externalStudy);
				}	
//				if(! Iterables.contains(listAbstractStudies, new Predicate <AbstractStudy> () {
//					     @Override public boolean test(AbstractStudy s) { return s.code.equals(externalStudyCode); }	})) {
//					listAbstractStudies.add(externalStudy);
//				}	
				// mettre à jour l'experiment pour la reference study :
				//logger.debug("Mise à jour de experiment pour les study references :");
				experiment.studyCode = externalStudy.code;	
				experiment.studyAccession = externalStudy.accession;	
					
			} else { // strategie internal_study
				logger.debug("**********************   Strategie internal study          ************************");	

				if (StringUtils.isNotBlank(acStudy)) {
//					study = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME,
//							Study.class, DBQuery.and(DBQuery.is("accession", acStudy)));
					study = studyAPI.dao_findOne(DBQuery.is("accession", acStudy));
				} else if (StringUtils.isNotBlank(studyCode)) {
//					study = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME,
//							Study.class, DBQuery.and(DBQuery.is("code", studyCode)));
					study = studyAPI.dao_findOne(DBQuery.is("code", studyCode));
				} else {
					throw new RuntimeException("impossible de recuperer le study dans la base avec strategie 'internal study'");
				}
				// Mise à jour de l'objet submission pour les study references :
				if (!submission.refStudyCodes.contains(study.code)) {
					submission.refStudyCodes.add(study.code);
				}
				if (!listAbstractStudies.contains(study)) {
					listAbstractStudies.add(study);
				}	
				// mettre à jour l'experiment pour la reference study :
				experiment.studyCode = study.code;
				experiment.studyAccession = study.accession;
				
				// Mise a jour de l'objet submission pour le study à soumettre :// normalement deja fait dans createSubmissionEntity
				//--------------------------------------------------------------
				if (StringUtils.isBlank(acStudy) && (SRAStudyStateNames.NONE.equalsIgnoreCase(study.state.code))) {
					List <Submission> submissionList = submissionAPI.dao_find(DBQuery.is("studyCode", studyCode).is("release", false)).toList();
					if (submissionList.size() != 0) {
						String message = studyCode + " utilise par plusieurs objets submissions, studyState = "+ study.state.code;
						for (Submission sub: submissionList) {
							message += studyCode + " utilise par objet Submission " + sub.code +"avec submissionState = " + submission.state.code;
						}
						logger.debug(message);
						throw new SraException(message);
					} else {
						logger.debug(studyCode + " utilise par aucune soumission autre que " + submission.code);	
					}	
					submission.studyCode = study.code; 
				}	
			}	
			
			// Ajouter l'experiment avec le statut forcement à 'N' à l'objet submission :
			// Mise à jour de l'objet submission avec les experiments à soumettre :
			//---------------------------------------------------------------------
			if (experiment.state.code.equalsIgnoreCase(N)) { 
				if(!submission.experimentCodes.contains(experiment.code)){
					listExperiments.add(experiment);
					submission.experimentCodes.add(experiment.code);
					//logger.debug("Ajout dans submission du expCode : " + experiment.code);
					if(experiment.run != null) {
						logger.debug("Ajout dans submission du runCode TOROTOTO: " + experiment.run.code);
						submission.runCodes.add(experiment.run.code);
					}// end if
				}// end if
			}// end if
		} // end for readset
		
		if (countError != 0){
			throw new SraException("Problemes pour creer la soumission \n" + errorMessage);
		}

		contextValidation.setCreationMode();
			
		// On ne sauvent que les study qui n'existent pas dans la base donc des ExternalStudy avec state.code='F-SUB'	
		for (AbstractStudy absStudyElt: listAbstractStudies) {
//			if (! MongoDBDAO.checkObjectExist(InstanceConstants.SRA_STUDY_COLL_NAME, AbstractStudy.class, "code", absStudyElt.code)){	
//				absStudyElt.validate(contextValidation);
//				MongoDBDAO.save(InstanceConstants.SRA_STUDY_COLL_NAME, absStudyElt);
//				logger.debug("ok pour sauvegarde dans la base du study " + absStudyElt.code);
//			}
			// On ne sauvent que les study qui n'existent pas dans la base donc des ExternalStudy avec state.code='F-SUB'	
			if (! abstractStudyAPI.dao_checkObjectExist("code", absStudyElt.code)) {	
				if (absStudyElt instanceof ExternalStudy) {
					ExternalStudy eStudy = (ExternalStudy) absStudyElt;
					eStudy.validate(contextValidation);
					externalStudyAPI.dao_saveObject(eStudy);
					logger.debug("ok pour sauvegarde dans la base du study " + absStudyElt.code);
				}
			}
		}	

		// On ne sauvent que les samples qui n'existent pas dans la base donc des Samples avec state.code='N'
		// ou bien des ExternalSample avec state.code='F-SUB'
		for (AbstractSample sampleElt: listAbstractSamples) {
//			if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_SAMPLE_COLL_NAME, AbstractSample.class, "code", sampleElt.code)){	
//				sampleElt.validate(contextValidation);
//				MongoDBDAO.save(InstanceConstants.SRA_SAMPLE_COLL_NAME, sampleElt);
//				logger.debug("ok pour sauvegarde dans la base du sample " + sampleElt.code);
//			}
			// On ne sauvent que les samples qui n'existent pas dans la base donc des Samples avec state.code='N'
			// ou bien des ExternalSample avec state.code='F-SUB'
			if (!abstractSampleAPI.dao_checkObjectExist("code", sampleElt.code)){	
				sampleElt.validate(contextValidation);
				abstractSampleAPI.dao_saveObject(sampleElt);
				logger.debug("ok pour sauvegarde dans la base du sample " + sampleElt.code);
			}
		}
		// On ne sauvent que les experiment qui n'existent pas dans la base donc des experiment avec state.code='N'	
		for (Experiment expElt: listExperiments) {
			expElt.validate(contextValidation);
//			if (!MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "code", expElt.code)){	
//				MongoDBDAO.save(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, expElt);
//				logger.debug("sauvegarde dans la base de l'experiment " + expElt.code);
//			}
			if (!experimentAPI.dao_checkObjectExist("code", expElt.code)) {	
				experimentAPI.dao_saveObject(expElt);
				logger.debug("sauvegarde dans la base de l'experiment " + expElt.code);
			}
		}

		//---------------------//
		// update des données: //
		//---------------------//
		
		contextValidation.setUpdateMode();
		// Updater les readSets pour le status dans la base: 
		for (ReadSet readSet : readSets) {
			if (readSet == null){
//				throw new SraException("readSet " + readSet.code + " n'existe pas dans database");
				throw new SraException("null readSet in read sets (should not happen)");
			} else {
				readSet.submissionState.code = N;
//				MongoDBDAO.update(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class,
//				DBQuery.is("code", readSet.code),
//				DBUpdate.set("submissionState.code", startStateCode).set("traceInformation.modifyUser", user).set("traceInformation.modifyDate", new Date()));
				readsetsAPI.dao_update(DBQuery.is("code", readSet.code), 
						DBUpdate.set("submissionState.code", N)
							    .set("traceInformation.modifyUser", user)
							    .set("traceInformation.modifyDate", new Date()));
			}
		}		
	
		// updater le study interne à soumettre pour son status
		if (study != null && study.state != null && StringUtils.isNotBlank(study.state.code) && NONE.equals(study.state.code)) {
			study.state.code = N;
			study.traceInformation.modifyDate = new Date();
			study.traceInformation.modifyUser = user;
			studyAPI.dao_saveObject(study);
		}
		
		// updater la config pour le statut
//		MongoDBDAO.save(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, config);
		if (config.state != null && StringUtils.isNotBlank(config.state.code) && NONE.equals(config.state.code)) {
			config.state.code = N;
			config.traceInformation.modifyDate = new Date();
			config.traceInformation.modifyUser = user;
			configurationAPI.dao_saveObject(config);
		}	
		
		
		// valider submission une fois les experiments et samples sauves, et sauver submission
		contextValidation.setCreationMode();
//		contextValidation.putObject("type", "sra");
		submission.validate(contextValidation);
		if (!submissionAPI.dao_checkObjectExist("code",submission.code)){			
			submission.validate(contextValidation);
			submissionAPI.dao_saveObject(submission);
			logger.debug("sauvegarde dans la base du submission " + submission.code);
		}
		
		if (contextValidation.hasErrors()) {
			logger.debug("submission.validate produit des erreurs");
			// rallBack avec clean sur exp et sample et mise à jour study
			logger.debug("\ndisplayErrors dans SubmissionServices::initPrimarySubmission :");
			contextValidation.displayErrors(logger);
			logger.debug("\n end displayErrors dans SubmissionServices::initPrimarySubmission :");
			
			// enlever les samples, experiments et submission qui ont ete crées par le service et remettre
			// readSet.submissionState à NONE, et si studyCode utilisé par cette seule soumission remettre studyCode.state.code=NONE
			// et si config utilisé par cette seule soumission remettre configCode.state.code=NONE
			//cleanDataBase(submission.code, contextValidation);		
			//throw new SraException("SubmissionServices::initPrimarySubmission::probleme validation  voir log: ");
			submissionWorkflowsHelper.rollbackSubmission(submission, contextValidation);	
			contextValidation.displayErrors(logger);
			throw new SraException("SubmissionServices::initReleaseSubmission::probleme validation  voir log: ");			
		} 
		logger.debug("Creation de la soumission " + submission.code);
		return submission.code;
	}




}

	
