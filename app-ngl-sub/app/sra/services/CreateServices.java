package sra.services;
import static ngl.refactoring.state.SRASubmissionStateNames.NONE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_N;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_V;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

import com.mongodb.BasicDBObject;

import controllers.sra.api.AbstractSampleAPI;
import controllers.sra.api.AbstractStudyAPI;
import controllers.sra.api.AnalysisAPI;
import controllers.sra.api.AtlaseaAPI;
import controllers.sra.api.ConfigurationAPI;
import controllers.sra.api.ExperimentAPI;
import controllers.sra.api.ExternalSampleAPI;
import controllers.sra.api.ExternalStudyAPI;
import controllers.sra.api.ProjectAPI;
import controllers.sra.api.ReadsetAPI;
import controllers.sra.api.SampleAPI;
import controllers.sra.api.StudyAPI;
import controllers.sra.api.SubmissionAPI;
import fr.cea.ig.ngl.dao.api.APIException;
import fr.cea.ig.ngl.dao.api.APIValidationException;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsDAO;
import fr.cea.ig.ngl.dao.runs.RunsAPI;
//import fr.cea.ig.ngl.dao.sra.SampleDAO;
//import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.common.instance.TraceInformation;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.AbstractSample;
import models.sra.submit.sra.instance.AbstractStudy;
import models.sra.submit.sra.instance.Analysis;
import models.sra.submit.sra.instance.Atlasea;
import models.sra.submit.sra.instance.Attribute;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Configuration.StrategySample;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.ExternalSample;
import models.sra.submit.sra.instance.ExternalStudy;
import models.sra.submit.sra.instance.IStateReference;
import models.sra.submit.sra.instance.Project;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.ReadSpec;
import models.sra.submit.sra.instance.Readset;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.sra.instance.Sample;
import models.sra.submit.sra.instance.Study;
import models.sra.submit.sra.instance.Submission;
import models.sra.submit.sra.instance.Submission.TypeRawDataSubmitted;
import models.sra.submit.sra.instance.UserRefCollabType;
//import ngl.refactoring.state.SRAStudyStateNames;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import sra.services.SraEbiAPI;
import sra.services.Tools;
import services.sra.SraCodeHelper;
import services.sra.SraException;
import services.sra.SraValidationException;
import services.sra.SraValidationHelper;
import services.sra.VariableSRA;
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;
import sra.api.SubmissionNewAPI;
import sra.api.SubmissionNewAPITools;
import validation.ContextValidation;


public class CreateServices {
	private static final play.Logger.ALogger logger = play.Logger.of(CreateServices.class);

	private final ConfigurationAPI       configurationAPI;
	private final SubmissionAPI 	     submissionAPI;
	private final AnalysisAPI            analysisAPI;
	private final StudyAPI               studyAPI;
	private final AbstractStudyAPI       abstractStudyAPI;
	private final ExternalStudyAPI       externalStudyAPI;
	private final SampleAPI              sampleAPI;
	private final AbstractSampleAPI      abstractSampleAPI;
	private final ExternalSampleAPI      externalSampleAPI;
	private final ExperimentAPI          experimentAPI;
	//private final SamplesAPI           laboSampleAPI;
	private final ProjectAPI             projectAPI;
	private final ReadSetsAPI            laboReadsetsAPI;  // collection readset de ngl-seq
	private final ReadsetAPI             readsetAPI;       // collection readset de sra
	private final RunsAPI                laboRunAPI;
	private final SraCodeHelper          sraCodeHelper;
	private final SubmissionNewAPITools  submissionNewAPITools;
	private final String                 patternSampleAc = "(E|S)RS\\d+";
	private final String                 patternSampleExternalId = "SAM";

	private final String                 patternStudyAc = "ERP\\d+";
	private final String                 patternStudyExternalId  = "PRJ";
	private final AtlaseaAPI             atlaseaAPI;
	private final fr.cea.ig.ngl.dao.api.AnalysisAPI            biAnalysisAPI;
	private final SraEbiAPI              ebiAPI;
	private final TaxonomyServices       taxonomyServices;
	private final static int             maxReadsetInSubmission = 1000;
	
	@Inject
	public CreateServices(
			ConfigurationAPI  configurationAPI,
			SubmissionAPI     submissionAPI,
			AnalysisAPI       analysisAPI,
			StudyAPI          studyAPI,
			AbstractStudyAPI  abstractStudyAPI,
			ExternalStudyAPI  externalStudyAPI,
			SampleAPI         sampleAPI,
			AbstractSampleAPI abstractSampleAPI,
			ExternalSampleAPI externalSampleAPI,
			ExperimentAPI     experimentAPI,
			ReadsetAPI        readsetAPI,
			ReadSetsAPI       readsetsAPI,
			//SamplesAPI        laboSampleAPI,
			RunsAPI           laboRunAPI,
			//ProjectsAPI       projectAPI,
			ProjectAPI        projectAPI,
			AtlaseaAPI        atlaseaAPI,
			fr.cea.ig.ngl.dao.api.AnalysisAPI biAnalysisAPI,       
			WSClient          ws,
			SraCodeHelper     sraCodeHelper,
			SraEbiAPI         ebiAPI,
			TaxonomyServices  taxonomyServices,
			SubmissionNewAPITools   submissionNewAPITools) {

		//this.ctx = ctx;
		this.configurationAPI  = configurationAPI;
		this.submissionAPI     = submissionAPI;
		this.analysisAPI       = analysisAPI;          
		this.studyAPI          = studyAPI;
		this.abstractStudyAPI  = abstractStudyAPI;
		this.externalStudyAPI  = externalStudyAPI;
		this.sampleAPI         = sampleAPI;
		this.abstractSampleAPI = abstractSampleAPI;
		this.externalSampleAPI = externalSampleAPI;
		this.experimentAPI     = experimentAPI;
		this.readsetAPI        = readsetAPI;
		this.laboReadsetsAPI       = readsetsAPI;
		//this.laboSampleAPI     = laboSampleAPI;
		this.laboRunAPI        = laboRunAPI;
		this.projectAPI        = projectAPI;
		this.atlaseaAPI        = atlaseaAPI;
		this.biAnalysisAPI     = biAnalysisAPI;
		//		this.ws                = ws;
		this.sraCodeHelper     = sraCodeHelper;
		this.submissionNewAPITools   = submissionNewAPITools;
		this.ebiAPI            = ebiAPI;
		this.taxonomyServices  = taxonomyServices;

	}

	//	public void isSubmittable(Sample sample, ContextValidation contextValidation) {
	//		contextValidation.addKeyToRootKeyName("sample");
	//		if( ! ebiAPI.submittable(sample.taxonId)) {
	//			contextValidation.addError("taxonId", sample.taxonId + " n'est pas soumettable à l'EBI");
	//		}
	//		contextValidation.removeKeyFromRootKeyName("sample");
	//	}

	// ok marche bien
	public void isSubmittable(int taxonId, String sampleCodes, ContextValidation contextValidation) {
		contextValidation.addKeyToRootKeyName("sample");
		if( ! ebiAPI.submittable(taxonId)) {
			//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXX 3333333333333       taxonId=" + taxonId + " n'est pas soumettable à l'EBI");
			contextValidation.addError("taxonId", taxonId + " n'est pas soumettable à l'EBI dans les samples " + sampleCodes);
		} else {
			//logger.debug("XXXXXXXXXXXXXXXXXXXXXXXX 3333333333333       taxonId=" + taxonId + " est soumettable à l'EBI");
		}
		contextValidation.removeKeyFromRootKeyName("sample");
	}

	public Project createUmbrellaEntity (String title, String description,  String dropBox, String strTaxonId, String attributes,List<String>idsPubmed, List<String>childrenProjectAccessions, List<String>parentProjectAccessions, ContextValidation ctxVal ) {
		Project umbrellaProject = new Project();
		umbrellaProject.submissionProjectType = "UMBRELLA_PROJECT";
		umbrellaProject.title = title;
		umbrellaProject.description = description;
		// NGL-4708		
		if (attributes != null && !attributes.isEmpty()) {
			umbrellaProject.attributes = attributes;
		}

		umbrellaProject.dropBox = dropBox;

		if (childrenProjectAccessions != null && !childrenProjectAccessions.isEmpty()) {
			umbrellaProject.childrenProjectAccessions = childrenProjectAccessions;
		}
		// NGL-4013 : Pas de controle unicité parent car plusieurs projects peuvent avoir le meme parent .
		if (parentProjectAccessions != null && !parentProjectAccessions.isEmpty()) {
			umbrellaProject.parentProjectAccessions = parentProjectAccessions;
		}

		if (idsPubmed != null && !idsPubmed.isEmpty()) {
			umbrellaProject.idsPubmed = idsPubmed;
		}
		// on verifie que le taxonId existe bien à l'EBI ou au NCBI avec scientificName 
		// (si non soumettable pas important car taxon au niveau du study et non du sample)
		if (StringUtils.isNotBlank(strTaxonId)) {
			umbrellaProject.scientificName = taxonomyServices.getNcbiEbiScientificName(strTaxonId, ctxVal);
			umbrellaProject.taxonId = strTaxonId;
		}	
		String user = ctxVal.getUser();
		umbrellaProject.code = sraCodeHelper.generateUmbrellaCode();
		while (projectAPI.dao_checkObjectExist("code", umbrellaProject.code)) {
			umbrellaProject.code = sraCodeHelper.generateUmbrellaCode();
		}
		umbrellaProject.traceInformation.setTraceInformation(user);
		umbrellaProject.state = new State(SUB_N, user);

		//logger.debug("XXXXXXX  umbrellaProject.code = " + umbrellaProject.code);
		return umbrellaProject;
	}

	// cree une soumission de type creation pour un project umbrella, la met dans status SUB_N et sauve dans database
	public Submission initPrimarySubmissionForUmbrella(ContextValidation contextValidation, String title, String description, List<String> parentProjectAccessions, List<String> childrenProjectAccessions, List<String>idsPubmed, String strTaxonId, String attributes, String dropBox) 
			throws SraException, SraValidationException, IOException, APIValidationException, APIException {

		if (StringUtils.isBlank(title)) {
			throw new SraException("initPrimarySubmissionForUmbrella", " titre du project umbrella a null");
		}
		if (StringUtils.isBlank(description)) {
			throw new SraException("initPrimarySubmissionForUmbrella", " description du project umbrella a null");
		}
		if (StringUtils.isBlank(dropBox)) {
			throw new SraException("initPrimarySubmissionForUmbrella", " dropBox du project umbrella a null");
		}
		// NGL-3585
		//		if (childrenProjectAccessions == null || childrenProjectAccessions.isEmpty()) {
		//			throw new SraException("initPrimarySubmissionForUmbrella", " aucun project enfant ?");
		//		}

		contextValidation.setCreationMode();
		String user = contextValidation.getUser();	
		Project umbrellaProject = createUmbrellaEntity(title, description,  dropBox, strTaxonId, attributes, idsPubmed, childrenProjectAccessions, parentProjectAccessions, contextValidation);
		umbrellaProject.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			//logger.debug("erreur validation sur project umbrella :");
			contextValidation.displayErrors(logger, "debug");
			throw new SraValidationException(contextValidation);
		}
		Date courantDate = new java.util.Date();
		Submission submission = new Submission(user);
		submission.traceInformation.creationDate = courantDate;
		submission.state = new State(SUB_N, user);
		submission.type = Submission.Type.CREATION;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		submission.code = sraCodeHelper.generateSubmissionCodeForUmbrella();		
		// Determiner le submissionDirectory et submission.ebiResult une fois submission.code defini :
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + "/" + submission.code; 	
		//logger.debug("AAAAAAAAAAAAAAA            submission.submissionDirectory=" + submission.submissionDirectory);
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";

		submission.umbrellaCode = umbrellaProject.code;
		submission.dropBox = dropBox;
		// verifier que le project  a soumettre n'est pas deja à soumettre dans une autre soumission:
		List <Submission> submissionList = submissionAPI.dao_find(DBQuery.is("umbrellaCode", umbrellaProject.code)).toList();
		if (submissionList.size() != 0) {
			String message = umbrellaProject.code + " utilise par plusieurs objets submissions, projectUmbrellaStateCode = " + umbrellaProject.state.code + "\n";
			for (Submission sub: submissionList) {
				message += umbrellaProject.code + " utilise par objet Submission " + sub.code +" avec submissionStateCode = " + sub.state.code + "\n";
			}
			//logger.debug(message);
			throw new SraException(message);
		} else {
			//logger.debug(umbrellaCode + " utilise par aucune soumission autre que " + submission.code);
		}	

		contextValidation.setCreationMode();
		submission.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			//logger.debug("submission.validate produit des erreurs");
			//logger.debug("\ndisplayErrors dans CreateServices::initPrimarySubmissionForUmbrella :");
			contextValidation.displayErrors(logger, "debug");
			//logger.debug("\n end displayErrors dans SubmissionServices::initPrimarySubmissionForUmbrella :");
			// destruction dans base des objets lies a cette soumission, si possible:
			submissionNewAPITools.rollbackSubmission(submission, contextValidation.getUser());	
			throw new SraValidationException(contextValidation);			
		} 
		projectAPI.dao_saveObject(umbrellaProject); 
		submission = submissionAPI.dao_saveObject(submission);

		//logger.debug("sauvegarde dans la base du submission " + submission.code);
		return submissionAPI.get(submission.code);	
	}
	

	public void validateParametersInitPrimarySubmissionBionano(			
			List<String> readSetCodes, 
			List<String> cmap_names, // nom complet des fichiers cmap
			String analysisCode, 
			ContextValidation contextValidation) throws SraException, SraValidationException, IOException {
		// verification des parametres :
		if(StringUtils.isBlank(analysisCode)) {
			throw new SraException("initPrimarySubmissionBionano", "l'argument analysisCode n'est pas defini");			
		}
		if( ! analysisAPI.dao_checkObjectExist("code", analysisCode)) {
			throw new SraException("initPrimarySubmissionBionano", "l'analyse avec le code "+ analysisCode + " n'existe pas dans la base");
		}			
		Analysis analysis = analysisAPI.get(analysisCode);
		// verifier qu'il n'existe pas de soumission avec analysisCode :
		List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("analysisCode", analysisCode)).toList();

		if (submissionList.size() >= 1) {
			String mess = "";
			for (Submission submission: submissionList) {
				mess += submission.code + ", ";
			}
			mess = mess.replaceFirst(", $", ""); // oter , terminals si besoin

			throw new SraException("initPrimarySubmissionBionano", "l'analyse avec le code "+ analysisCode + " existe deja dans une soumission : " + mess);
		}
		if(StringUtils.isBlank(analysis.dropBox)) {
			throw new SraException("validateParametersInitPrimarySubmissionBionano", "l'analyse avec le code "+ analysisCode + " ne contient pas de champs dropBox");
		}			
		if(StringUtils.isBlank(analysis.sampleIdentifier)) {
			throw new SraException("validateParametersInitPrimarySubmissionBionano", "l'analyse avec le code "+ analysisCode + " ne contient pas de sampleIdentifier");
		}
		if( ! (analysis.sampleIdentifier.matches(patternSampleAc)||analysis.sampleIdentifier.startsWith(patternSampleExternalId)) ) {
			throw new SraException("l'analyse avec le code " + analysis.code + " contient un sampleIdentifier "+ analysis.sampleIdentifier + "qui n'est pas de la forme attendu (ERS ou SRS ou SAM)");
		}
		if(StringUtils.isBlank(analysis.studyIdentifier)) {
			throw new SraException("initPrimarySubmissionBionano", "l'analyse avec le code "+ analysisCode + " ne contient pas de studyIdentifier");
		}
		if( ! (analysis.studyIdentifier.matches(patternStudyAc)||analysis.studyIdentifier.startsWith(patternStudyExternalId)) ) {
			throw new SraException("l'analyse avec le code " + analysis.code + " contient un studyIdentifier "+ analysis.studyIdentifier + "qui n'est pas de la forme attendu (ERP ou PRJ)");
		}

		if (readSetCodes == null || readSetCodes.size() < 1 ) {
			//logger.debug("Aucun readset à soumettre n'a été indiqué");
			throw new SraException("initPrimarySubmissionBionano", "Aucun readset à soumettre ???");
		}

		if (analysis.listRawData != null) {
			throw new SraException("initPrimarySubmissionBionano", "appel de la methode avec analysis contenant deja des rawData  ???");			
		}

		if(cmap_names == null || cmap_names.size() < 1) {
			throw new SraException("initPrimarySubmissionBionano", "Aucun fichier cmap pour cette soumission bionano ???");			
		}

		// verifier existence du fichier sur disque dur ici ou deporter à activation(mieux ici car saisie utilisateur)	
		//		for(String cmap_name : cmap_names) {
		//			File fileCible      = new File(cmap_name);
		//			if (! fileCible.exists()) {
		//				throw new SraException("initPrimarySubmissionBionano", "le fichier cmap indique " + cmap_name + " n'existe pas au CNS???");						
		//			}
		//		}
		// verifier que tous les readsets sont de type bionano et soumettables : 
		Boolean bionano = true;
		validateReadSetCodesForSubmission(readSetCodes, contextValidation, bionano);
	}

	public List<RawData> buildRawData(ReadSet readSet, String analysisCode) {
		List<RawData> listRawData = new ArrayList<RawData>();
		String dataDir = readSet.path;
		List <models.laboratory.run.instance.File> list_files =  readSet.files;
		for (models.laboratory.run.instance.File runInstanceFile: list_files) {
			String runInstanceExtentionFileName = runInstanceFile.extension;
			// conditions qui doivent etre suffisantes puisque verification préalable que le readSet
			// est bien valide pour la bioinformatique.
			if (runInstanceFile.usable &&  
					(runInstanceExtentionFileName.equalsIgnoreCase("bnx.gz") || runInstanceExtentionFileName.equalsIgnoreCase("bnx"))) {
				RawData rawData = new RawData();
				rawData.readsetCode = readSet.code;
				if(StringUtils.isNotBlank(analysisCode)) {
					rawData.analysisCode = analysisCode;
				}
				//rawData.instrumentUsedTypeCode = instrumentUsedTypeCode;
				//logger.debug("fichier " + runInstanceFile.fullname);
				rawData.extention = runInstanceFile.extension;
				//logger.debug("dataDir "+dataDir);
				rawData.directory = dataDir.replaceFirst("\\/$", ""); // oter / terminal si besoin
				//logger.debug("raw data directory"+rawData.directory);
				rawData.relatifName = runInstanceFile.fullname;

				// mettre dans rawData.collabFileName le relatifName par defaut, ou collabFileName si existe : 
				rawData.collabFileName = rawData.relatifName;	// nom du fichier 				
				if(runInstanceFile.properties.containsKey("collabFileName")) {
					PropertyValue propertyValue = runInstanceFile.properties.get("collabFileName");
					String collabFileName = (String) propertyValue.value;
					if(StringUtils.isNotBlank(collabFileName)) {
						rawData.collabFileName = collabFileName;
					}
				}

				if (rawData.collabFileName.endsWith("bnx")) {
					rawData.collabFileName = rawData.collabFileName.concat(".gz");
				} 
				rawData.location = readSet.location;
				if (runInstanceFile.properties != null && runInstanceFile.properties.containsKey("md5")) {
					rawData.md5 = (String) runInstanceFile.properties.get("md5").value;
				} else {
					logger.error("NGL-SUB_TICKET_JOE A CREER : Attention, dans readset.bionano." +  readSet.code + " : aucun md5 pour le fichier à soumettre " + rawData.relatifName );
				}
				listRawData.add(rawData);
			}
		}
		return listRawData;
	}


	public RawData buildRawDataFromAbsoluteFileCmap(String absoluteFileName, String analysisCode) {
		RawData rawData = new RawData();
		if (StringUtil.isBlank(absoluteFileName)){
			throw (new SraException("appel de buildRawDataFromAbsoluteFileName avec argument sans caracteres visibles"));
		}
		if (! absoluteFileName.contains("/")) {
			throw (new SraException("appel de buildRawDataFromAbsoluteFileName avec argument qui ne correspond pas a un nom de fichier complet "+ absoluteFileName));
		}
		int endIndex   = absoluteFileName.lastIndexOf("/");
		String dataDir = absoluteFileName.substring(0, endIndex);
		String relatifName = absoluteFileName.replaceAll(".*/", "");
		if(! relatifName.endsWith(".cmap")) {
			throw (new SraException("appel de buildRawDataFromAbsoluteFileName avec argument qui ne correspond pas a un nom de fichier .cmap "+ absoluteFileName));	
		}
		File file = new File(absoluteFileName);
		if (! file.canRead()) {
			throw (new SraException("fichier "+ absoluteFileName + " qui n'est pas accessible en lecture"));	
		}
		if(StringUtils.isNotBlank(analysisCode)) {
			rawData.analysisCode = analysisCode;
		}
		rawData.extention = "cmap";
		rawData.directory = dataDir.replaceFirst("\\/$", ""); // oter / terminal si besoin
		//logger.debug("raw data directory"+rawData.directory);
		rawData.relatifName = relatifName;
		rawData.collabFileName = rawData.relatifName;	// nom du fichier 
		rawData.location = "CNS";
		rawData.gzipForSubmission = false;
		rawData.md5sumForSubmission = true;
		return rawData;
	}

	public Submission initPrimarySubmissionBionano (
			List<String> readSetCodes, 
			List<String> cmap_names, // nom complet des fichiers cmap
			String analysisCode, 
			ContextValidation contextValidation) throws SraException, SraValidationException, IOException, APIValidationException, APIException {
		//logger.debug("dans initPrimarySubmissionBionano");
		validateParametersInitPrimarySubmissionBionano(readSetCodes, cmap_names, analysisCode, contextValidation);
		if(contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		// Pas de test ici sur analysisCode ou analysis car testé dans validateParametersInitPrimarySubmissionBionano 
		Analysis analysis = analysisAPI.get(analysisCode);
		// contruction de la soumission avec analysisCode et sans configCode
		String user = contextValidation.getUser();
		Submission submission = new Submission(user);
		Date courantDate = new java.util.Date();
		submission.traceInformation.creationDate = courantDate;
		submission.state = new State(SUB_N, user);
		submission.type = Submission.Type.CREATION;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.bionanoRawData;
		submission.analysisCode = analysis.code;
		// NGL-4593
		submission.dropBox = analysis.dropBox;
		// construction si besoin d'un externalStudy
		String acStudy = analysis.studyIdentifier;
		String study_code = null;
		String study_accession = null;
		ExternalStudy externalStudy = null;
		boolean study_in_base = false;
		int countError = 0;
		String errorMessage = "";
		if (acStudy.matches(patternStudyAc)) {
			if(abstractStudyAPI.dao_checkObjectExist("accession", acStudy)) {
				AbstractStudy abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", acStudy));
				if (abstStudy != null) {
					study_code = abstStudy.code;
					study_accession = acStudy;
					study_in_base = true;
					//logger.debug("study existant dans base avec study_code = " + study_code);
					// NGL-4593
					if(!isDropBoxStudyCompatible(abstStudy, submission, contextValidation, courantDate)) {
						countError++;
					}
				}
			} 
		} else if (acStudy.startsWith(patternStudyExternalId)) {
			if(abstractStudyAPI.dao_checkObjectExist("externalId", acStudy)) {
				AbstractStudy abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("externalId", acStudy));
				if (abstStudy != null) {
					study_code = abstStudy.code;
					if(StringUtils.isNotBlank(abstStudy.accession)) {
						study_accession = abstStudy.accession;
					} else {
						study_accession = acStudy;
					}
					study_in_base = true;
					//logger.debug("study existant dans base avec study_code = " + study_code);
					// NGL-4593
					if(!isDropBoxStudyCompatible(abstStudy, submission, contextValidation, courantDate)) {
						countError++;
					}
				}
			}
		} else {
			throw new SraException("pattern de l'identifiant du study " + acStudy + " non reconnu");
		}

		if(! study_in_base) {
			externalStudy = buildExternalStudy(acStudy, user);
			study_code = externalStudy.code;
			study_accession = acStudy;
		}
		// Renseigner la soumission pour le study :
		submission.refStudyCodes.add(study_code);
		// corriger si besoin l'analyse pour le study_accession (cas ou l'utilisateur a mis PRJ => remplacer par ERP si possible)
		analysis.studyIdentifier = study_accession;
		// renseigner l'analyse pour le study_code 		
		analysis.studyCode = study_code;

		// construction si besoin d'un externalSample
		ExternalSample externalSample = null;
		String sampleAc = analysis.sampleIdentifier;
		String sample_code = null;
		String sample_accession = null;
		boolean sample_in_base = false;
		String sample_taxonId = null;
		if(sampleAc.matches(patternSampleAc)) {
			if(abstractSampleAPI.dao_checkObjectExist("accession", sampleAc)) {
				AbstractSample abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("accession", sampleAc));
				if (abstSample != null) {
					sample_code = abstSample.code;
					sample_accession = sampleAc;
					sample_in_base = true;
					if (abstSample instanceof Sample) {
						sample_taxonId = "" + ((Sample) abstSample).taxonId;
					}
					//logger.debug("sample existant dans base avec sample_code = {}, ligne 560" , sample_code);
					// NGL-4593
					if(!isDropBoxSampleCompatible(abstSample, submission, contextValidation, courantDate)) {
						countError++;
						logger.error("ERROR MESSAGE={}", errorMessage);
					}
				}
			} 
		} else if(sampleAc.startsWith(patternSampleExternalId)) {
			if(abstractSampleAPI.dao_checkObjectExist("externalId", sampleAc)) {
				AbstractSample abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("externalId", sampleAc));
				if (abstSample != null) {
					sample_code = abstSample.code;
					if(StringUtils.isNotBlank(abstSample.accession)) {
						sample_accession = abstSample.accession;
					} else {
						sample_accession = sampleAc;
					}
					sample_in_base = true;
					//logger.debug("sample existant dans base avec sample_code = " + sample_code);
					// NGL-4593
					if(!isDropBoxSampleCompatible(abstSample, submission, contextValidation, courantDate)) {
						countError++;
						logger.error("ERROR MESSAGE={}", errorMessage);
					}
				}
			}
		} else {
			throw new SraException("pattern de l'identifiant du sample " + sampleAc + " non reconnu");
		}
		if(! sample_in_base) {
			externalSample = buildExternalSample(sampleAc, user);
			sample_code = externalSample.code;
			sample_accession = sampleAc;
		}

		if(countError != 0) {
			throw new SraException("Problemes pour creer la soumission\n" + errorMessage);
		}

		// Renseigner la soumission pour le sample :
		submission.refSampleCodes.add(sample_code);
		// corriger si besoin l'analyse pour le sample_accession (cas ou l'utilisateur a mis SAM => remplacer par ERS si possible)
		analysis.sampleIdentifier = sample_accession;
		// renseigner l'analyse pour le sample_code 		
		analysis.sampleCode = sample_code;

		// construction des rawData correspondant au readset : 

		List <ReadSet> readSets        = new ArrayList<>();
		List <String>  subProjectCodes = new ArrayList<>();
		List <RawData> listRawData     = new ArrayList<RawData>();
		for (String readSetCode : readSetCodes) {	
			ReadSet readSet = laboReadsetsAPI.get(readSetCode);
			models.laboratory.run.instance.Run  laboratoryRun = laboRunAPI.get(readSet.runCode);
			if(laboratoryRun==null){
				throw new SraException("Pas de laboratoryRun pour " + readSet.code);
			}
//			if (laboratoryRun.instrumentUsed == null ){
//				throw new SraException("Pas de champs instrumentUsed pour le laboratoryRun " + laboratoryRun.code);
//			} 
//			String instrumentUsedTypeCode = laboratoryRun.instrumentUsed.typeCode;
//			if(StringUtils.isBlank(instrumentUsedTypeCode)) {
//				throw new SraException("Pas de champs instrumentUsedTypeCode pour le laboratoryRun " + laboratoryRun.code);
//			}
			readSets.add(readSet);
			if (StringUtils.isNotBlank(sample_taxonId) && ! sample_taxonId.equalsIgnoreCase(readSet.sampleOnContainer.taxonCode)) {
				contextValidation.addError("readsetCode: "+ readSet.code, " a un taxonId " + readSet.sampleOnContainer.taxonCode + " != du taxon dans le sample indique dans l'analyse :" + sample_taxonId);
			}
			if( ! subProjectCodes.contains(readSet.projectCode)) {
				subProjectCodes.add(readSet.projectCode);
			}
			// construire le rawData correspondant au readset et ajouter analysisCode
			List<RawData> listRawDatatmp = buildRawData(readSet, analysisCode); 
			for (RawData rawData : listRawDatatmp) {
				if (!listRawData.contains(rawData)) {
					listRawData.add(rawData);
				}
			}
		}
		// construction des rawData correspondant aux fichiers cmap de l'analyse :
		List <RawData> listRawDataCmap     = new ArrayList<RawData>();
		for(String cmap_name :cmap_names) {
			RawData rawData = buildRawDataFromAbsoluteFileCmap(cmap_name, analysisCode);
			if(!listRawDataCmap.contains(rawData)) {
				listRawDataCmap.add(rawData);
			}
		}
		//Renseigner la soumission pour ses projects_Codes:
		// Determiner le code de la soumission, une fois la liste des projectCodes definie :
		submission.projectCodes = subProjectCodes;
		submission.code = sraCodeHelper.generateSubmissionCode(subProjectCodes);
		//logger.debug("xxxxxxxxxxxxxxxxxxxx initPrimarySubmissionBionano, submissionCode = "+ submission.code);
		// Determiner le submissionDirectory et submission.ebiResult une fois submission.code defini :
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + "/" + submission.code; 	
		//logger.debug("AAAAAAAAAAAAAAA            submission.submissionDirectory=" + submission.submissionDirectory);
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";

		//---------------------------------------------//
		// update des données et sauvegarde dans base: //
		//---------------------------------------------//
		contextValidation.setCreationMode();
		// On ne sauve  le study reference dans analysis que s'il n'existe pas dans base donc avec state.code='F-SUB'	
		if(externalStudy != null) {
			externalStudy.validate(contextValidation);
			externalStudyAPI.dao_saveObject(externalStudy);
			//logger.debug("ok pour sauvegarde dans la base de l'external study " + externalStudy.code);
		}



		// On ne sauve  le sample reference dans analysis que s'il n'existe pas dans base donc avec state.code='F-SUB'	
		if(externalSample != null) {
			externalSample.validate(contextValidation);
			externalSampleAPI.dao_saveObject(externalSample);
			//externalSampleAPI.create(externalSample, user); // à revoir car cree le code et embetant de deporter partie metier sur API
			//logger.debug("ok pour sauvegarde dans la base de l'external sample " + externalSample.code);
		}


		contextValidation.setUpdateMode();

		// updater les readsets dans collection labo_readSet de ngl-seq pour le status et sauver dans base :
		updateReadSetAndSaveIndbForCreation(readSets, SUB_N, user);

		// ajouter les readsets dans collection readset de sra :
		addReadsetBionanoIndb(listRawData, analysis.code);
		// ajouter les rawData correspondant aux readsets à l'analyse
		analysis.listRawData = listRawData;		
		// ajouter les rawData correspondant aux cmap_names à l'analyse
		analysis.listRawData.addAll(listRawDataCmap);
		//logger.debug("analysis avec " + analysis.listRawData.size() +" rawData");
		// updater l'analyse pour le statut et sauver toute l'analyse:
		if (NONE.equals(analysis.state.code)) {
			updateAnalysisAndSaveIndb(analysis, SUB_N, user);
		}


		// valider submission une fois tous les autres objets validés et sauvés.
		contextValidation.setCreationMode();
		submission.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			//logger.debug("\ndisplayErrors dans CreateServices::initPrimarySubmissionBionano :");
			contextValidation.displayErrors(logger, "debug");
			//logger.debug("\n end displayErrors dans SubmissionServices::initPrimarySubmissionBionano :");
			// enlever les samples, experiments et submission qui ont ete crées par le service et remettre
			// readSet.submissionState à NONE, et si studyCode utilisé par cette seule soumission remettre studyCode.state.code=NONE
			// et si config utilisé par cette seule soumission remettre configCode.state.code=NONE
			//logger.debug("appel de submissionNewAPITools.rollbackSubmission");
			submissionNewAPITools.rollbackSubmission(submission, contextValidation.getUser());	
			//			throw new SraException("initPrimarySubmission::probleme validation : "+ contextValidation.getErrors());			
			throw new SraValidationException(contextValidation);			
		} 

		submission = submissionAPI.dao_saveObject(submission);
		// retourner l'objet de la base avec id :
		return submissionAPI.get(submission.code);		
	}

	public String  validatePrimarySubmissionWithoutRawData(
			String studyCode, 
			List<String>sampleCodes) throws SraException, SraValidationException, IOException {


		if(StringUtils.isBlank(studyCode) && (sampleCodes==null || sampleCodes.size()==0)) {
			throw new SraException("Aucun studyCode ou sampleCodes à soumettre?");
		}	
		String dropBoxStudy = "";
		if(StringUtils.isNotBlank(studyCode)) {
			if( ! studyAPI.dao_checkObjectExist("code", studyCode)) {
				throw new SraException("le studyCode indique " + studyCode + " n'existe pas dans la base");
			}
			Study study = studyAPI.dao_findOne(DBQuery.is("code", studyCode));	
			if(! NONE.equals(study.state.code)) {
				throw new SraException("le studyCode indique " + studyCode + " existe dans la base avec etat different de NONE "+ study.state.code);
			}
			if(StringUtils.isBlank(study.dropBox)) {
				throw new SraException("le study " + studyCode + " existe dans la base sans dropBox");
			} else {
				dropBoxStudy = study.dropBox;
			}
			// verifier que le study a soumettre n'est pas deja à soumettre dans une autre soumission:
			List <Submission> submissionList = submissionAPI.dao_find(DBQuery.is("studyCode", studyCode)).toList();
			if (submissionList.size() != 0) {
				String message = studyCode + " utilise par plusieurs objets submissions, studyState = " + study.state.code + "\n";
				for (Submission sub: submissionList) {
					message += studyCode + " utilise par objet Submission " + sub.code + " avec submissionState = " + sub.state.code + "\n";
				}
				//logger.debug(message);
				throw new SraException(message);
			} 
		}
		String dropBoxSample = "";
		String messError = "";
		int countError = 0;
		if(sampleCodes != null && sampleCodes.size() != 0) {
			for(String sampleCode:sampleCodes) {
				if( ! sampleAPI.dao_checkObjectExist("code", sampleCode)) {
					throw new SraException("le sampleCode indique " + sampleCode + " n'existe pas dans la base");
				}
				Sample sample = sampleAPI.dao_findOne(DBQuery.is("code", sampleCode));	
				if(!NONE.equals(sample.state.code)) {
					throw new SraException("le sampleCode indique " + sampleCode + " existe dans la base avec etat different de NONE "+ sample.state.code);
				}
				// verifier que le sample a soumettre n'est pas deja à soumettre dans une autre soumission:
				List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("sampleCodes", sampleCode)).toList();
				if (submissionList.size() != 0) {
					String message = sampleCode + " utilise par plusieurs objets submissions, sampleState = " + sample.state.code + "\n";
					for (Submission sub: submissionList) {
						message += sampleCode + " utilise par objet Submission " + sub.code + " avec submissionState = " + sub.state.code + "\n";
					}
					//logger.debug(message);
					throw new SraException(message);
				} 
				// verifier que tous les samples ont meme dropBox
				if(StringUtils.isBlank(sample.dropBox)) {
					countError++;
					messError += "sample " + sample.code + " sans dropBox\n";	
				}
				if (StringUtils.isBlank(dropBoxSample)) {
					dropBoxSample = sample.dropBox;
				}
				if (! sample.dropBox.equals(dropBoxSample)) {
					countError++;
					messError += "Les samples a soumettre ont des dropBox differentes \n";					
				}
			}
		}
		if (countError != 0 ) {
			throw new SraException("Problemes pour creer la soumission\n" + messError);
		}
		if(StringUtils.isBlank(dropBoxStudy)&& StringUtils.isBlank(dropBoxSample)) {
			throw new SraException("Problemes pour creer la soumission\n" + "aucune dropBox definie\n");
		} 
		if(StringUtils.isNotBlank(dropBoxStudy)&& StringUtils.isNotBlank(dropBoxSample)) {
			if(! dropBoxStudy.equals(dropBoxSample)) {
				throw new SraException("Problemes pour creer la soumission\n" + "dropBox du study et dropBox des samples differentes \n");
			}
		} 

		if(StringUtils.isNotBlank(dropBoxStudy)) {
			return dropBoxStudy;
		} else {
			return dropBoxSample;
		}
	}

	
	public Submission buildSubmissionWithStudyAndValidate(Study study, ContextValidation contextValidation) throws SraException, SraValidationException, IOException, APIValidationException, APIException {
		String user = contextValidation.getUser();	
		//logger.debug("Dans CreateServices::initPrimarySubmissionWithoutRawData studyCode = " + studyCode);
		Submission submission = new Submission(user);
		contextValidation.setCreationMode();
		if(study == null) {
			contextValidation.addError("study", "null");
			return null;
		}
		
		study.validate(contextValidation);
		if(contextValidation.hasErrors()) {
			return null;	
		}
		
		// NGL-4593
		if(StringUtils.isBlank(study.dropBox)) {
			throw new SraException("buildSubmissionWithStudyAndValidate", " dropBox du study "+ study.code + " a null");
		}
		
		//studyAPI.create(study, user); create qui cree le code 
		//alors que validate du study en mode creation qui attend code => incoherence
		//studyAPI.dao_saveObject(study); 
		
		// updater le study pour son status et le sauver dans la base 
		updateStudyAndSaveIndb(study, SUB_V, user);		

		logger.debug("ok pour la sauvegarde du study " + study.code);
		submission.dropBox = study.dropBox;
		// Mise à jour de l'objet submission pour les study references :
		if (!submission.refStudyCodes.contains(study.code)) {
			submission.refStudyCodes.add(study.code);
		}
		// Mise a jour de l'objet submission pour le study à soumettre :
		submission.studyCode = study.code;
		// Ajout des projectCodes du study dans subProjectCodes seulement si study à soumettre:
		for(String projCode : study.projectCodes) {
			if ( ! submission.projectCodes.contains(projCode)) {
				submission.projectCodes.add(projCode);
			}
		}
		
		Date courantDate = new java.util.Date();
		submission.traceInformation.creationDate = courantDate;
		submission.traceInformation.createUser = user;
		submission.state = new State(SUB_V, user);
		submission.type = Submission.Type.CREATION;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		// Determiner le code de la soumission, une fois la liste des projectCodes definie :
		submission.code = sraCodeHelper.generateSubmissionCode(submission.projectCodes);
		// Determiner le submissionDirectory et submission.ebiResult une fois submission.code defini :
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + "/" + submission.code; 	
		//logger.debug("AAAAAAAAAAAAAAA            submission.submissionDirectory=" + submission.submissionDirectory);
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		submission.dropBox = study.dropBox;
		submission.validate(contextValidation);
		submission = submissionAPI.dao_saveObject(submission);

		//logger.debug("sauvegarde dans la base du submission " + submission.code);
		logger.debug("appel du validate de submission");
		
		if (contextValidation.hasErrors()) {
			//logger.debug("submission.validate produit des erreurs");
			logger.debug("\ndisplayErrors dans CreateServices::initPrimarySubmission :");
			contextValidation.displayErrors(logger, "debug");
			logger.debug("\n end displayErrors dans SubmissionServices::initPrimarySubmission :");
			// enlever les samples, experiments et submission qui ont ete crées par le service et remettre
			// readSet.submissionState à NONE, et si studyCode utilisé par cette seule soumission remettre studyCode.state.code=NONE
			// et si config utilisé par cette seule soumission remettre configCode.state.code=NONE

			submissionNewAPITools.rollbackSubmission(submission, contextValidation.getUser());	
			//			throw new SraException("initPrimarySubmission::probleme validation : "+ contextValidation.getErrors());			
			throw new SraValidationException(contextValidation);			
		} 

		// retourner l'objet de la base avec id :
		logger.debug("end de la methode buildSubmissionWithStudyAndValidate");
		return submissionAPI.get(submission.code);
	}

	public Submission buildSubmissionWithSampleAndValidate(Sample sample, ContextValidation contextValidation) throws SraException, SraValidationException, IOException, APIValidationException, APIException {
		String user = contextValidation.getUser();	
		//logger.debug("Dans CreateServices::initPrimarySubmissionWithoutRawData studyCode = " + studyCode);
		Submission submission = new Submission(user);
		contextValidation.setCreationMode();
		if(sample == null) {
			contextValidation.addError("sample", "null");
			return null;
		}
		// verifier que le taxonId est bien submitable :
		isSubmittable(sample.taxonId, sample.code, contextValidation);
		if(StringUtils.isBlank(sample.attributes)) {
			contextValidation.addError("sample.code", "absence du champs attributes");
		}
		sample.validate(contextValidation);
		if(contextValidation.hasErrors()) {
			return null;	
		}
		
		// NGL-4593
		if(StringUtils.isBlank(sample.dropBox)) {
			throw new SraException("buildSubmissionWithSampleAndValidate", " dropBox du sample "+ sample.code + " a null");
		}
		
		// updater le sample pour son status et le sauver dans la base 
		updateSampleAndSaveIndb(sample, SUB_V, user);		

		logger.debug("ok pour la sauvegarde du sample " + sample.code);
		submission.dropBox = sample.dropBox;
		// Mise à jour de l'objet submission pour les samples references :

		if (!submission.refSampleCodes.contains(sample.code)) {
			submission.refSampleCodes.add(sample.code);
		}
		// Mise a jour de l'objet submission pour le sample à soumettre :
		if (!submission.sampleCodes.contains(sample.code)) {
			submission.sampleCodes.add(sample.code);
		}		
		// Ajout des projectCodes du sample dans subProjectCodes seulement si study à soumettre:
		
		if ( ! submission.projectCodes.contains(sample.projectCode)) {
			submission.projectCodes.add(sample.projectCode);
		}
		
		
		Date courantDate = new java.util.Date();
		submission.traceInformation.creationDate = courantDate;
		submission.traceInformation.createUser = user;
		submission.state = new State(SUB_V, user);
		submission.type = Submission.Type.CREATION;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		// Determiner le code de la soumission, une fois la liste des projectCodes definie :
		submission.code = sraCodeHelper.generateSubmissionCode(submission.projectCodes);
		// Determiner le submissionDirectory et submission.ebiResult une fois submission.code defini :
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + "/" + submission.code; 	
		//logger.debug("AAAAAAAAAAAAAAA            submission.submissionDirectory=" + submission.submissionDirectory);
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		submission.dropBox = sample.dropBox;
		submission.validate(contextValidation);
		submission = submissionAPI.dao_saveObject(submission);

		//logger.debug("sauvegarde dans la base du submission " + submission.code);
		logger.debug("appel du validate de submission");
		
		if (contextValidation.hasErrors()) {
			//logger.debug("submission.validate produit des erreurs");
			logger.debug("\ndisplayErrors dans CreateServices::buildSubmissionWithSampleAndValidate");
			contextValidation.displayErrors(logger, "debug");
			logger.debug("\n end displayErrors dans dans CreateServices::buildSubmissionWithSampleAndValidate");
			// enlever les samples, experiments et submission qui ont ete crées par le service et remettre
			// readSet.submissionState à NONE, et si studyCode utilisé par cette seule soumission remettre studyCode.state.code=NONE
			// et si config utilisé par cette seule soumission remettre configCode.state.code=NONE

			submissionNewAPITools.rollbackSubmission(submission, contextValidation.getUser());	
			//			throw new SraException("buildSubmissionWithSampleAndValidate::probleme validation : "+ contextValidation.getErrors());			
			throw new SraValidationException(contextValidation);			
		} 

		// retourner l'objet de la base avec id :
		logger.debug("buildSubmissionWithSampleAndValidate:: retour de la soumission " + submission.code);
		logger.debug("end de la methode buildSubmissionWithSampleAndValidate");
		return submissionAPI.get(submission.code);
	}

	public Submission initPrimarySubmissionWithoutRawData(
			String studyCode, 
			List<String>sampleCodes,
			ContextValidation contextValidation) throws SraException, SraValidationException, IOException, APIValidationException, APIException {
		String user = contextValidation.getUser();	
		//logger.debug("Dans CreateServices::initPrimarySubmissionWithoutRawData studyCode = " + studyCode);
		String dropBox = validatePrimarySubmissionWithoutRawData(studyCode, sampleCodes);
		Submission submission = new Submission(user);
		if(StringUtils.isNotBlank(studyCode)) {
			Study study = studyAPI.dao_findOne(DBQuery.is("code", studyCode));	
			// NGL-4593
			if(StringUtils.isBlank(study.dropBox)) {
				throw new SraException("initPrimarySubmissionWithoutRawData", " dropBox du study "+ studyCode + " a null");
			}
			submission.dropBox = dropBox;
			// Mise à jour de l'objet submission pour les study references :
			if (!submission.refStudyCodes.contains(study.code)) {
				submission.refStudyCodes.add(study.code);
			}
			// Mise a jour de l'objet submission pour le study à soumettre :
			submission.studyCode = study.code;
			// Ajout des projectCodes du study dans subProjectCodes seulement si study à soumettre:
			for(String projCode : study.projectCodes) {
				if ( ! submission.projectCodes.contains(projCode)) {
					submission.projectCodes.add(projCode);
				}
			}
		}

		if(sampleCodes != null) {
			for (String sampleCode:sampleCodes) {
				Sample sample = sampleAPI.dao_findOne(DBQuery.is("code", sampleCode));	
				if (! submission.refSampleCodes.contains(sample.code)) {
					submission.refSampleCodes.add(sample.code);
				}
				if (! submission.sampleCodes.contains(sample.code)) {
					submission.sampleCodes.add(sample.code);
				}	
				if ( ! submission.projectCodes.contains(sample.projectCode)) {
					submission.projectCodes.add(sample.projectCode);
				}
			}
		}

		Date courantDate = new java.util.Date();
		submission.traceInformation.creationDate = courantDate;
		submission.traceInformation.createUser = user;
		submission.state = new State(SUB_N, user);
		submission.type = Submission.Type.CREATION;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		// Determiner le code de la soumission, une fois la liste des projectCodes definie :
		submission.code = sraCodeHelper.generateSubmissionCode(submission.projectCodes);
		// Determiner le submissionDirectory et submission.ebiResult une fois submission.code defini :
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + "/" + submission.code; 	
		//logger.debug("AAAAAAAAAAAAAAA            submission.submissionDirectory=" + submission.submissionDirectory);
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		submission.dropBox = dropBox;

		// updater dans la base le study pour son status si study à soumettre
		if (StringUtils.isNotBlank(submission.studyCode)) {
			Study study = studyAPI.dao_findOne(DBQuery.is("code", studyCode));	
			updateStudyAndSaveIndb(study, SUB_V, user);
		}
		// updater dans la base les samples à soumettre pour status
		for(String sampleCode : submission.sampleCodes) {
			Sample sample = sampleAPI.dao_getObject(sampleCode);
			updateSampleAndSaveIndb(sample, SUB_V, user);
		}
		// valider submission une fois les experiments et samples sauves, et sauver submission
		contextValidation.setCreationMode();
		submission.validate(contextValidation);
		submission = submissionAPI.dao_saveObject(submission);
		if (contextValidation.hasErrors()) {
			//logger.debug("submission.validate produit des erreurs");
			//logger.debug("\ndisplayErrors dans CreateServices::initPrimarySubmission :");
			contextValidation.displayErrors(logger, "debug");
			//logger.debug("\n end displayErrors dans SubmissionServices::initPrimarySubmission :");
			// enlever les samples, experiments et submission qui ont ete crées par le service et remettre
			// readSet.submissionState à NONE, et si studyCode utilisé par cette seule soumission remettre studyCode.state.code=NONE
			// et si config utilisé par cette seule soumission remettre configCode.state.code=NONE

			submissionNewAPITools.rollbackSubmission(submission, contextValidation.getUser());	
			//			throw new SraException("initPrimarySubmission::probleme validation : "+ contextValidation.getErrors());			
			throw new SraValidationException(contextValidation);			
		} 

		//logger.debug("sauvegarde dans la base du submission " + submission.code);
		// retourner l'objet de la base avec id :
		return submissionAPI.get(submission.code);
	}
	
	

	// NGL-4593
	private boolean isDropBoxSampleCompatible(AbstractSample abstSample, Submission submission, ContextValidation ctxVal, Date courantDate) {
		if(StringUtils.isBlank(submission.dropBox)) {
			ctxVal.addError("Error", "submission.dropBox non defini");
			return false;
		}
		if( abstSample._type.equals("ExternalSample")) {	
			return true;
		}
		Sample sample = (Sample) abstSample;
		//logger.debug("sample {} existe dans base et de type Sample ", sample.code);
		//logger.debug("isDropBoxSampleCompatible::dropBox in submission={}", submission.dropBox );
		// si sample prive sur autre dropBox alors error.
		if(StringUtils.isNotBlank(sample.dropBox) 
				&& !sample.dropBox.equals(submission.dropBox) 
				&& sample.releaseDate.after(courantDate)) {

			//logger.debug("sample existant dans base de type Sample  et prive et avec dropBox={}, different de submission.dropBox = {}" , sample.dropBox, submission.dropBox);
			ctxVal.addError("Error", "Soumission dans dropBox " + submission.dropBox + " impossible avec le sample=" + sample.code + " privé (sample.releaseDate=" + sample.releaseDate +") et soumis dans dropBox " + sample.dropBox);	
			return false;
		} else {
			return true;
		}
	}

	// NGL-4593
	private boolean isDropBoxStudyCompatible(AbstractStudy abstStudy, Submission submission,  ContextValidation ctxVal, Date courantDate) {
		// NGL-4593
		if(StringUtils.isBlank(submission.dropBox)) {
			ctxVal.addError("Error", "submission.dropBox non defini");
			return false;
		}
		if( abstStudy._type.equals("ExternalStudy")) {	
			return true;
		}
		Study study = (Study) abstStudy;
		//logger.debug("study {} existe dans base et de type Study = ", study.code);

		// si study prive sur autre dropBox alors error.
		if(StringUtils.isNotBlank(study.dropBox) 
				&& !study.dropBox.equals(submission.dropBox) 
				&& study.releaseDate.after(courantDate)) {	
			//logger.debug("study existant dans base de type Study  et prive et avec dropBox={}, different de submission = " ,study.dropBox);
			ctxVal.addError("Error", "Soumission dans dropBox " + submission.dropBox + " impossible avec le study=" + study.code + " privé (study.releaseDate=" + study.releaseDate +") et soumis dans dropBox " + study.dropBox);	
			return false;
		} else {
			return true;
		}
	}

	private void traitementSubmissionForReadsetCaseAcSample(
			ReadSet readSet, 
			Experiment experiment, 
			String refCollab, 
			String identifierSample, 
			Map<String, UserRefCollabType> mapUserRefCollab,
			List <AbstractSample> listAbstractSamples,  
			Submission submission, 
			ContextValidation contextValidation) {
		//logger.debug("ZZZZZZZZZZZZZZZZZZ   Dans traitementSubmissionForReadsetCaseAcSample, cas STRATEGY_AC_SAMPLE, avec acSample={}", identifierSample);

		// Dans ce cas on ne sait pas si on va recuperer un Sample s'il a ete construit par
		// le genoscope, ou un ExternalSample s'il a ete contruit par un collaborateur.
		ExternalSample externalSample = null;
		AbstractSample abstSample = null;
		String sample_code = "";
		String sample_identifier = "";
		String user = contextValidation.getUser();
		Date courantDate = new Date();
		
		if (StringUtils.isBlank(identifierSample) && mapUserRefCollab != null && !mapUserRefCollab.isEmpty()) {
			//logger.debug("ZZZZZZZZZZZZZZZZZZ     cas STRATEGY_AC_SAMPLE sans acSample via interface et avec mapUserRefCollab");
			if (StringUtils.isBlank(refCollab)) {
				contextValidation.addError("Soumission impossible pour readset " + readSet.code, "absence de refCollab(sub) dans ngl-bi et UserRefCollab renseigne");
				return;					
			}
			if (! mapUserRefCollab.containsKey(refCollab)){
				contextValidation.addError("Soumission impossible pour readset " + readSet.code, "absence de la refCollab(sub) " + refCollab + " dans UserRefCollab ");
				return;
			}
			String sampleIdentifier = mapUserRefCollab.get(refCollab).getSampleAc();
			//logger.debug("Pour la refCollab "+ refCollab + " sample = " + sampleAc);
			//deja verifié dans validateParamForSubmission
			//		if (StringUtils.isBlank(sampleAc)){
			//			//countError++;
			//			//errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque mapUserRefCollab pour clone '" + refCollab + "' ne contient pas de sampleAc";
			//			contextValidation.addError("Soumission impossible pour readset " + readSet.code, " mapUserRefCollab pour clone '" + refCollab + "' ne contient pas de sampleAc");
			//			continue;					
			//		}
			//NGL-3666
			//					if( ! sampleAc.matches(patternSampleAc)) {
			if( ! (sampleIdentifier.matches(patternSampleAc) || sampleIdentifier.startsWith(patternSampleExternalId)) ) {
				contextValidation.addError("Soumission impossible pour readset " + readSet.code, " mapUserRefCollab pour clone '" + refCollab + "' contient un sampleAc " + sampleIdentifier + " qui n'est pas de la forme attendu (ERS..., SRS... ou SAM...)");
				return;	
			}
			// recuperation dans base ou création du sample avec status SUB-F (si AC alors SUB-F)
			boolean sample_in_base = false;
			//logger.debug("xxxxxxxxxxx  ligne 1040, avec sampleAc={} et patternSampleAc={}", sampleAc, patternSampleAc);
			if(sampleIdentifier.matches(patternSampleAc)) {
				//logger.debug("sample match avec patternSampleAc = {}, ligne 1042" , sampleAc);
				if(abstractSampleAPI.dao_checkObjectExist("accession", sampleIdentifier)) {
					abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("accession", sampleIdentifier));
					if (abstSample != null) {
						sample_code = abstSample.code;
						sample_identifier = sampleIdentifier;
						sample_in_base = true;
						//logger.debug("sample existant dans base avec sample_code = {}, ligne 1053", sample_code);
						// NGL-4593
						//					if(! isDropBoxSampleCompatible(abstSample, submission, contextValidation, courantDate)) {
						//						countError++;
						//						continue;
						//					}

					}
				} 
			} else if(sampleIdentifier.startsWith(patternSampleExternalId)) {
				//logger.debug("sample match avec patternSampleExternalId = " + sampleIdentifier);

				if(abstractSampleAPI.dao_checkObjectExist("externalId", sampleIdentifier)) {
					abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("externalId", sampleIdentifier));
					if (abstSample != null) {
						sample_code = abstSample.code;
						if(StringUtils.isNotBlank(abstSample.accession)) {
							sample_identifier = abstSample.accession;
						} else {
							sample_identifier = sampleIdentifier;
						}
						sample_in_base = true;
						//logger.debug("sample existant dans base avec sample_code = {}, ligne 1075" + sample_code);
						// NGL-4593
						//if(!isDropBoxSampleCompatible(abstSample, submission, contextValidation, courantDate)) {
						//	countError++;
						//	continue;
						//}
					}
				}
			} else {
				throw new SraException("pattern de l'identifiant du sample " + sampleIdentifier + " non reconnu");
			}

			if(! sample_in_base) {
				externalSample = buildExternalSample(sampleIdentifier, user);
				sample_code = externalSample.code;
				sample_identifier = sampleIdentifier;
				abstSample = (AbstractSample) externalSample;
			}
			// NGL-4593
			if(!isDropBoxSampleCompatible(abstSample, submission, contextValidation, courantDate)) {
				return;
			}
			// Mise a jour de l'objet submission pour mapUserRefCollab :
			UserRefCollabType submission_userRefCollab = new UserRefCollabType();
			//					logger.debug("alias=" + mapUserRefCollab.get(refCollab).getAlias());
			//					logger.debug("sampleAC=" + mapUserRefCollab.get(refCollab).getSampleAc());
			//					logger.debug("studyAC=" + mapUserRefCollab.get(refCollab).getStudyAc());
			submission_userRefCollab.setRefCollabName(mapUserRefCollab.get(refCollab).getRefCollabName());
			submission_userRefCollab.setSampleAc(mapUserRefCollab.get(refCollab).getSampleAc());
			submission_userRefCollab.setStudyAc(mapUserRefCollab.get(refCollab).getStudyAc());
			//					logger.debug("submission_userRefCollab.alias=" + submission_userRefCollab.getAlias());
			//					logger.debug("submission_userRefCollab.sampleAC=" + submission_userRefCollab.getSampleAc());
			//					logger.debug("submission_userRefCollab.studyAC=" + submission_userRefCollab.getStudyAc());
//			if(submission.mapUserRefCollab == null || submission.mapUserRefCollab.isEmpty()) {
//				//logger.debug("submission.mapUserRefCollab vide= ");
//				submission.mapUserRefCollab = new HashMap<String, UserRefCollabType>();
//			}
//			//logger.debug("submission.mapUserRefCollab = " + submission.mapUserRefCollab.size());
//			submission.mapUserRefCollab.put(submission_userRefCollab.getAlias(), submission_userRefCollab);
//			//logger.debug("submission mis a jour pour userRefCollab");
			//NGL-4952
			if(submission.userRefCollabs == null) {
				submission.userRefCollabs = new ArrayList<UserRefCollabType>();
			}
			if(! submission.userRefCollabs.contains(submission_userRefCollab)) {
				submission.userRefCollabs.add(submission_userRefCollab);
			}
		} else if (StringUtils.isNotBlank(identifierSample)) {
			//logger.debug("ZZZZZZZZZZZZZZZZZZ    cas avec acSample={} indique dans interface", acSample);

			//NGL-3666
			//if( ! acSample.matches(patternSampleAc)) {

			boolean sample_in_base = false;
			if(identifierSample.matches(patternSampleAc)) {
				//logger.debug("sample match avec patternSampleAc = {}, ligne 1124" , acSample);
				if(abstractSampleAPI.dao_checkObjectExist("accession", identifierSample)) {
					abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("accession", identifierSample));
					//logger.debug("abstSample.ac={}",abstSample.accession );
					//logger.debug("abstSample.code={}",abstSample.code );
					//logger.debug("abstSample._type={}",abstSample._type );

					if (abstSample != null) {
						sample_code = abstSample.code;
						sample_identifier = identifierSample;
						sample_in_base = true;
						//logger.debug("sample existant dans base avec sample_code = {}, ligne 1135", sample_code);
						// NGL-4593
						//					if(!isDropBoxSampleCompatible(abstSample, submission, contextValidation, courantDate)) {
						//						countError++;
						//						continue;
						//					}
					}
				} 
			}else if(identifierSample.startsWith(patternSampleExternalId)) {
				//logger.debug("sample match avec patternSampleExternalId = {}" , acSample);

				if(abstractSampleAPI.dao_checkObjectExist("externalId", identifierSample)) {
					abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("externalId", identifierSample));
					if (abstSample != null) {
						sample_code = abstSample.code;
						if(StringUtils.isNotBlank(abstSample.accession)) {
							sample_identifier = abstSample.accession;
						} else {
							sample_identifier = identifierSample;
						}
						sample_in_base = true;
						//					// NGL-4593
						//					if(!isDropBoxSampleCompatible(abstSample, submission, contextValidation, courantDate)) {
						//						countError++;
						//						continue;
						//					}
					}
				}
			} else {
				throw new SraException("pattern de l'identifiant du sample " + identifierSample + " non reconnu");
			}

			if(! sample_in_base) {
				externalSample = buildExternalSample(identifierSample, user);
				sample_code = externalSample.code;
				sample_identifier = identifierSample;
				abstSample = (AbstractSample) externalSample;
			}
			// NGL-4593
			if(!isDropBoxSampleCompatible(abstSample, submission, contextValidation, courantDate)) {
				return;
			}
		} else {
			throw new SraException("mapUserRefCollab et acSample vides et strategy STRATEGY_AC_SAMPLE");
		}

		// Mise a jour de l'objet submission pour les samples references
		//logger.debug("Mise a jour de l'objet submission pour les samples references");	
		if (!submission.refSampleCodes.contains(sample_code)) {
			//logger.debug("Ajout dans submission du sample_code " + sample_code);
			submission.refSampleCodes.add(sample_code);
		}
		// Liste des samples qui seront a sauver s'ils n'existent pas dans base :
		if (abstSample != null) {
			if (!listAbstractSamples.contains(abstSample)) {
				listAbstractSamples.add(abstSample);
			}
		} else {
			if (!listAbstractSamples.contains(externalSample)) {
				listAbstractSamples.add(externalSample);
			}
		}
		// mettre à jour l'experiment pour la reference sample :
		//logger.debug("ajout dans experiment du sample_code "+ sample_code);
		experiment.sampleCode = sample_code;
		experiment.sampleIdentifier = sample_identifier;
	}


	private void traitementSubmissionForReadsetCaseNoAcSample(ReadSet readSet, Configuration config, Experiment experiment, 
			List<AbstractSample> listAbstractSamples, Submission submission, ContextValidation contextValidation) {

		//logger.debug("Dans init, cas strategy_internal_sample");
		Sample sample = null;
		String user = contextValidation.getUser();
		String scientificName = readSet.sampleOnContainer.ncbiScientificName;
		Date courantDate = new Date();
		// Recuperer le sample existant avec son state.code ou bien en creer un nouveau avec state.code='N'
		sample = fetchSample(readSet, config.strategySample, scientificName, user, config.dropBox);
		
		// NGL-4593
		if(! isDropBoxSampleCompatible((AbstractSample) sample, submission, contextValidation, courantDate)) {
			return;
		}
		// Renseigner l'objet submission :
		// Verifier que l'objet sample  n'est pas en cours de soumission
		//logger.debug("2025 XXXXXXXXXXXXXXXXXXXX  traitementSubmissionForReadsetCaseNoAcSample,   sample = {} et state = {}" , sample, sample.state.code);
		if ( !( SUB_F.equalsIgnoreCase(sample.state.code) || SUB_N.equalsIgnoreCase(sample.state.code) ) ) {
			throw new SraException("Tentative d'utilisation dans la soumission du sample "+ sample.code +" en cours de soumission avec state.code==" + sample.state.code);
		}
		// Mise a jour de l'objet submission pour les samples references
		if (!submission.refSampleCodes.contains(sample.code)) {
			submission.refSampleCodes.add(sample.code);
		}
		// Mise a jour de l'objet submission pour les samples à soumettre :
		//------------------------------------------------------------------
		if ( ! (SUB_N.equalsIgnoreCase(sample.state.code) || SUB_F.equalsIgnoreCase(sample.state.code) ) ) {
			throw new SraException("Soumission qui utilise le sample "+ sample.code + " avec un statut" + sample.state.code);
		}

		if (SUB_N.equalsIgnoreCase(sample.state.code)) {
			List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("sampleCodes", sample.code)).toList();
			if (submissionList.size() > 0) {
				String message = "sampleCode=" + sample.code + " avec state=" + sample.state.code + "\n";
				for (Submission sub: submissionList) {
					message += "       - deja utilise par objet Submission " + sub.code + "\n";
				}
				//logger.debug(message);
				throw new SraException(message);
			} else {
				// les samples à soumettre à l'EBI ont un status à SUB-N (ceux à SUB-F ont deja un AC)
				if (! submission.sampleCodes.contains(sample.code)) {
					submission.sampleCodes.add(sample.code);
				}		
			}
		}

		// liste des samples et externalSample à sauver en base si n'existent pas deja:
		if(!listAbstractSamples.contains(sample)){
			listAbstractSamples.add(sample);
		}
		// mettre à jour l'experiment pour la reference sample :
		experiment.sampleCode = sample.code;
		experiment.sampleIdentifier = sample.accession;
	}


	private void traitementSubmissionForReadsetCaseAcStudy(
			ReadSet readSet, 
			Experiment experiment,  
			String refCollab, 
			String identifierStudy, 
			Map<String, UserRefCollabType> mapUserRefCollab, 
			List <AbstractStudy> listAbstractStudies, 
			Submission submission, 
			ContextValidation contextValidation) {
		AbstractStudy abstStudy = null;
		ExternalStudy externalStudy = null;
		String user = contextValidation.getUser();
		Date courantDate = new Date();
		String study_identifier = "";
		String study_code = "";

		if (StringUtils.isBlank(identifierStudy) && mapUserRefCollab != null && !mapUserRefCollab.isEmpty()) {
			//logger.debug("mapUserRefCollab NOT NULL");
			if (StringUtils.isBlank(refCollab)) {
				contextValidation.addError("Soumission impossible pour readset " + readSet.code, "absence de refCollab(sub) dans ngl-bi et UserRefCollab renseigne");
				return;					
			}
			if (! mapUserRefCollab.containsKey(refCollab)){
				contextValidation.addError("Soumission impossible pour readset " + readSet.code, "absence de la refCollab(sub) " + refCollab + " dans UserRefCollab ");
				return;
			}

			//logger.debug("mapUserRefCollab contient la refCollab={}", refCollab);

			// mettre a jour submission.mapUserRefCollab :
			UserRefCollabType submission_userRefCollab = new UserRefCollabType();
			submission_userRefCollab.setRefCollabName(mapUserRefCollab.get(refCollab).getRefCollabName());
			submission_userRefCollab.setSampleAc(mapUserRefCollab.get(refCollab).getSampleAc());
			submission_userRefCollab.setStudyAc(mapUserRefCollab.get(refCollab).getStudyAc());
			//				if(submission.mapUserRefCollab == null || submission.mapUserRefCollab.isEmpty()) {
			//					//logger.debug("submission.mapUserRefCollab vide= ");
			//					submission.mapUserRefCollab = new HashMap<String, UserRefCollabType>();
			//				}
			//				submission.mapUserRefCollab.put(submission_userRefCollab.getAlias(), submission_userRefCollab);
			//logger.debug("mise a jour mapRefCollab de la soumission");
			// NGL-4952
			if(submission.userRefCollabs == null) {
				submission.userRefCollabs = new ArrayList<UserRefCollabType>();
			}
			if(! submission.userRefCollabs.contains(submission_userRefCollab)) {
				submission.userRefCollabs.add(submission_userRefCollab);
			}

			String studyIdentifier = mapUserRefCollab.get(refCollab).getStudyAc();
			//logger.debug("studyAcInMap={}", studyAc);
			// deja verifié dans validateParamForSubmission
			//		if (StringUtils.isBlank(studyAc)){
			//			countError++;
			//			errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque STRATEGY_AC_STUDY et mapUserRefCollab.get(clone).getStudyAc() non renseigne pour le clone '" + refCollab + "'\n";
			//			contextValidation.addError("Soumission impossible pour readset " + readSet.code, "absence de nom de clone dans mapUserRefCollab et mapUserRefCollab renseigne");
			//
			//			continue;					
			//		}
			//
			//		if ( ! (studyAc.matches(patternStudyAc) || studyAc.startsWith(patternStudyExternalId)) ) {
			//			//logger.debug("Erreur detectee pour studyAc dans mapRefCollab");
			//			countError++;
			//			errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque mapUserRefCollab pour clone '" + refCollab + "' contient un AC de study " + studyAc + " qui n'est pas de la forme attendu\n";
			//			continue;					
			//		}
			// recuperation dans base ou création du study avec status SUB-F (si AC alors SUB-F)
			boolean study_in_base = false;
			if(studyIdentifier.matches(patternStudyAc)) {
				//logger.debug("studyAcInMap match bien avec patternStudyAc");

				if(abstractStudyAPI.dao_checkObjectExist("accession", studyIdentifier)) {
					abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", studyIdentifier));
					if (abstStudy != null) {
						study_code = abstStudy.code;
						study_identifier = studyIdentifier;
						study_in_base = true;
						//logger.debug("study existant dans base avec study_code = " + study_code);
					}
					//				// NGL-4593
					//				if(!isDropBoxStudyCompatible(abstStudy, submission, contextValidation, courantDate)) {
					//					countError++;
					//					continue;
					//				}
				} 

			} else if(studyIdentifier.startsWith(patternStudyExternalId)) {
				//logger.debug("studyAcInMap match bien avec patternStudyExternalId");

				if(abstractStudyAPI.dao_checkObjectExist("externalId", studyIdentifier)) {
					//logger.debug("studyAcInMap n'existe pas dans base");

					abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("externalId", studyIdentifier));
					if (abstStudy != null) {
						study_code = abstStudy.code;
						if(StringUtils.isNotBlank(abstStudy.accession)) {
							study_identifier = abstStudy.accession;
						} else {
							study_identifier = studyIdentifier;
						}
						study_in_base = true;
						//logger.debug("study existant dans base avec study_code = " + study_code);
					}
					//				// NGL-4593
					//				if(!isDropBoxStudyCompatible(abstStudy, submission, contextValidation, courantDate)) {
					//					countError++;
					//					continue;
					//				}
				}
			} else {
				throw new SraException("pattern de l'identifiant du study " + studyIdentifier + " non reconnu");
			}

			if(! study_in_base) {
				//logger.debug("study = {} doit etre construit", studyAc);

				externalStudy = buildExternalStudy(studyIdentifier, user);
				study_code = externalStudy.code;
				study_identifier = studyIdentifier;
				abstStudy = (AbstractStudy) externalStudy;
			}
			// NGL-4593
			if(!isDropBoxStudyCompatible(abstStudy, submission, contextValidation, courantDate)) {
				return;
			}

		} else if (StringUtils.isNotBlank(identifierStudy)) {
			// NGL-3666
			//if( ! acStudy.matches(patternStudyAc)) {
			// deja verifié dans validateParamForSubmission
			//		if( ! (acStudy.matches(patternStudyAc)||acStudy.startsWith(patternStudyExternalId)) ) {
			//			errorMessage = errorMessage + "STRATEGY_AC_STUDY et acStudy transmis " + acStudy + " qui n'est pas de la forme attendue\n";
			//			continue;
			//		}

			boolean study_in_base = false;
			if (identifierStudy.matches(patternStudyAc)) {
				if(abstractStudyAPI.dao_checkObjectExist("accession", identifierStudy)) {
					abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", identifierStudy));
					if (abstStudy != null) {
						study_code = abstStudy.code;
						study_identifier = identifierStudy;
						study_in_base = true;
						//logger.debug("study existant dans base avec study_code = " + study_code);
					}
					//				// NGL-4593
					//				if(!isDropBoxStudyCompatible(abstStudy, submission, contextValidation, courantDate)) {
					//					countError++;
					//					continue;
					//				}
				} 
			} else if (identifierStudy.startsWith(patternStudyExternalId)) {
				if(abstractStudyAPI.dao_checkObjectExist("externalId", identifierStudy)) {
					abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("externalId", identifierStudy));
					if (abstStudy != null) {
						study_code = abstStudy.code;
						if(StringUtils.isNotBlank(abstStudy.accession)) {
							study_identifier = abstStudy.accession;
						} else {
							study_identifier = identifierStudy;
						}
						study_in_base = true;
						//logger.debug("study existant dans base avec study_code = " + study_code);
					}
					//				// NGL-4593
					//				if(!isDropBoxStudyCompatible(abstStudy, submission, contextValidation, courantDate)) {
					//					countError++;
					//					continue;
					//				}
				}
			} else {
				throw new SraException("pattern de l'identifiant du study " + identifierStudy + " non reconnu");
			}
			if(! study_in_base) {
				externalStudy = buildExternalStudy(identifierStudy, user);
				study_code = externalStudy.code;
				study_identifier = identifierStudy;
				abstStudy = (AbstractStudy) externalStudy;
			}
			// NGL-4593
			if(!isDropBoxStudyCompatible(abstStudy, submission, contextValidation, courantDate)) {
				return;
			}
		} else {
			throw new SraException("Soumission impossible pour le readset '" + readSet.code + "' parceque STRATEGY_AC_STUDY et pas de mapUserRefCollab ni d'acStudy");
		}
		// Mise à jour de l'objet submission pour les study references :
		if (!submission.refStudyCodes.contains(study_code)) {
			submission.refStudyCodes.add(study_code);
		}				
		// Renseigner la liste des studies et externalStudies qui seront sauvés en base si absent de la base :
		if (abstStudy != null) {
			if (!listAbstractStudies.contains(abstStudy)) {
				listAbstractStudies.add(abstStudy);
			}
		} else {
			if (!listAbstractStudies.contains(externalStudy)) {
				listAbstractStudies.add(externalStudy);
			}
		}

		// mettre à jour l'experiment pour la reference study :
		//logger.debug("Mise à jour de experiment pour les study references :");
		experiment.studyCode = study_code;	
		experiment.studyIdentifier = study_identifier;
	}

	private void traitementSubmissionForReadsetCaseNoAcStudy(ReadSet readSet, Configuration config, String studyCode, Experiment experiment, 
			List<AbstractStudy> listAbstractStudies, Submission submission, ContextValidation contextValidation) {


		Study study = null;
		//logger.debug("**********************   STRATEGY_CODE_STUDY      ************************");	
		// on recupere depuis la base un study non null avec bon status (validateParamForSubmission)
		study = studyAPI.dao_findOne(DBQuery.is("code", studyCode));
		Date courantDate = new Date();
		//	// NGL-4593
		//	if(StringUtils.isNotBlank(study.dropBox) 
		//			&& ! study.dropBox.equals(submission.dropBox) 
		//			&& study.releaseDate.after(courantDate)) {
		//		countError++;
		//		errorMessage = errorMessage + "Soumission impossible dans dropBox " + submission.dropBox + " avec studyCode=" + study.code + " et releaseDate=" + study.releaseDate +  " et dropBox=" + study.dropBox + "\n";	
		//		logger.error("ERROR MESSAGE={}", errorMessage);
		//	} 
		// NGL-4593
		if(!isDropBoxStudyCompatible((AbstractStudy) study, submission, contextValidation, courantDate)) {
			return;
		}
		// Mise à jour de l'objet submission pour les study references :
		if (!submission.refStudyCodes.contains(study.code)) {
			submission.refStudyCodes.add(study.code);
		}
		// Mise a jour de l'objet submission pour le study à soumettre :
		if(NONE.equals(study.state.code)) {
			submission.studyCode = study.code;
			// NGL-4593
			if(StringUtils.isBlank(study.dropBox)) {
				study.dropBox = submission.dropBox;
			}
			// verifier que le study a soumettre n'est pas deja à soumettre dans une autre soumission:
			List <Submission> submissionList = submissionAPI.dao_find(DBQuery.is("studyCode", studyCode)).toList();
			if (submissionList.size() != 0) {
				String message = studyCode + " utilise par plusieurs objets submissions, studyState = " + study.state.code + "\n";
				for (Submission sub: submissionList) {
					message += studyCode + " utilise par objet Submission " + sub.code +" avec submissionState = " + sub.state.code + "\n";
				}
				//logger.debug(message);
				throw new SraException(message);
			} else {
				//logger.debug(studyCode + " utilise par aucune soumission autre que " + submission.code);
			}	
			// Ajout des projectCodes du study dans subProjectCodes seulement si study a soumettre :
			for(String projCode : study.projectCodes) {
				if ( ! submission.projectCodes.contains(projCode)) {
					submission.projectCodes.add(projCode);
				}
			}
		}
		if (!listAbstractStudies.contains(study)) {
			listAbstractStudies.add(study);
		}	
		// mettre à jour l'experiment pour la reference study :
		experiment.studyCode = study.code;
		experiment.studyIdentifier = study.accession;
	}
	private void traitementSubmissionForReadset(
			String readSetCode, 
			Configuration config, 
			String studyCode,
			String acStudy,
			String acSample,
			Map<String, UserRefCollabType> mapUserRefCollab,
			ContextValidation contextValidation,
			List <Experiment> listExperiments,
			List <AbstractStudy> listAbstractStudies,
			List <AbstractSample> listAbstractSamples,
			Submission submission ) {

		String user = contextValidation.getUser();
		String dropBox = config.dropBox;
	
		ReadSet readSet = laboReadsetsAPI.get(readSetCode);
		//readSets.add(readSet);
		if( ! submission.projectCodes.contains(readSet.projectCode)) {
			submission.projectCodes.add(readSet.projectCode);
		}
		
		//logger.debug("2025 XXXXXXXXXXXXXXXX  readSet : {}", readSet.code);
		// Verifier que c'est une premiere soumission pour ce readSet
		// Verifiez qu'il n'existe pas d'objet experiment referencant deja ce readSet

		// Recuperer scientificName via NCBI pour ce readSet. Le scientificName est utilisé dans la construction
		// des samples et des experiments 
		String scientificName = readSet.sampleOnContainer.ncbiScientificName;
		// Creer les objets avec leurs alias ou code, les instancier completement et les sauver.
		Experiment experiment = createExperimentEntity(readSet, scientificName, user);
		experiment.dropBox = dropBox;
		experiment.librarySelection = config.librarySelection;
		experiment.librarySource = config.librarySource;
		experiment.libraryStrategy = config.libraryStrategy;
		experiment.libraryConstructionProtocol = config.libraryConstructionProtocol;
		String refCollab = readSet.sampleOnContainer.referenceCollab;
		// NGL-3413
		if(readSet.properties.containsKey("refCollabSub")) {
			PropertyValue propertyValue = readSet.properties.get("refCollabSub");
			String refCollabSub = (String) propertyValue.value;
			if(StringUtils.isNotBlank(refCollabSub)) {
				refCollab = refCollabSub;
			}
		}

		//			for (Iterator<Entry<String, UserRefCollabType>> iterator = mapUserRefCollab.entrySet().iterator(); iterator.hasNext();) {
		//				  Entry<String, UserRefCollabType> entry = iterator.next();
		//				  if (entry.getKey().equals(refCollab)) {
		//					  logger.debug("dans initPrimarySubmission 1 : cle du userRefCollab = '" + entry.getKey() + "'");
		//					  logger.debug("       study_ac : '" + entry.getValue().getStudyAc()+  "'");
		//					  logger.debug("       sample_ac : '" + entry.getValue().getSampleAc()+  "'");
		//				  }	
		//			}
		//logger.debug("2025 XXXXXXXXXXXXXXXX  avant switch   readSet : {}", readSet.code);

		switch(config.strategySample) {
		case STRATEGY_AC_SAMPLE : {
			//logger.debug("2025 XXXXXXXXXXXXXXXX  dans switch AC_SAMPLE  readSet : {}", readSet.code);

			//logger.debug("YYYYYYYYYYY   avant traitementSubmissionForReadsetCaseAcSample");
			traitementSubmissionForReadsetCaseAcSample(readSet, experiment, refCollab, acSample, mapUserRefCollab, 
					listAbstractSamples, submission, contextValidation);
			//logger.debug("YYYYYYYYYYY   apres traitementSubmissionForReadsetCaseAcSample");

			break;
		}
		case STRATEGY_CODE_SAMPLE_TAXON : case STRATEGY_CODE_SAMPLE_REFCOLLAB : case STRATEGY_CODE_SAMPLE_BANK : {
			//logger.debug("2025 XXXXXXXXXXXXXXXX  dans switch CODE_SAMPLE  readSet : {}", readSet.code);

			traitementSubmissionForReadsetCaseNoAcSample(readSet, config, experiment, 
					listAbstractSamples, submission, contextValidation);
			break;
		} 

		default :
			throw new SraException("strategySample non attendue : " + config.strategySample);
		}

		switch (config.strategyStudy) {
		case STRATEGY_AC_STUDY : {
			//logger.debug("2025 XXXXXXXXXXXXXXXX  dans switch AC_STUDY  readSet : {}", readSet.code);

			//logger.debug("YYYYYYYYYYY   avant traitementSubmissionForReadsetCaseAcStudy");
			traitementSubmissionForReadsetCaseAcStudy(readSet, experiment, refCollab, acStudy, mapUserRefCollab, 
					listAbstractStudies, submission, contextValidation);
			//logger.debug("YYYYYYYYYYY   apres traitementSubmissionForReadsetCaseAcStudy");
			break;
		}
		case STRATEGY_CODE_STUDY : {
			//logger.debug("2025 XXXXXXXXXXXXXXXX  dans switch CODE_STUDY  readSet : {}", readSet.code);

			traitementSubmissionForReadsetCaseNoAcStudy(readSet, config, studyCode, experiment, 
					listAbstractStudies, submission, contextValidation);

			break;
		} // end case
		default:
			throw new SraException("strategyStudy non attendue : " + config.strategyStudy);
		} // end switch	


		// Ajouter l'experiment avec le statut forcement à 'N' à l'objet submission :
		// Mise à jour de l'objet submission avec les experiments à soumettre :
		//---------------------------------------------------------------------
		if (experiment.state.code.equalsIgnoreCase(SUB_N)) { 
			List <Submission> submissionList = submissionAPI.dao_find(DBQuery.in("experimentCodes", experiment.code)).toList();
			if (submissionList.size() > 0) {
				String message = "experimentCode=" + experiment.code + " avec state=" + experiment.state.code + "\n";
				for (Submission sub: submissionList) {
					message += "       - deja utilise par objet Submission " + sub.code + "\n";
				}
				//logger.debug(message);
				throw new SraException(message);
			} 
			// les experiments à soumettre à l'EBI ont un status à SUB-N (ceux à SUB-F ont deja un AC)	
			if(!submission.experimentCodes.contains(experiment.code)){
				listExperiments.add(experiment);
				submission.experimentCodes.add(experiment.code);
				//logger.debug("Ajout dans submission du expCode : " + experiment.code);
				if(experiment.run != null) {
					//logger.debug("Ajout dans submission du runCode : " + experiment.run.code);
					submission.runCodes.add(experiment.run.code);
				}// end if
			}// end if
		}// end if
	}


	public ArrayList<ReadSet> selectSubmittableReadsetInAnalyse(models.laboratory.run.instance.Analysis analysis, ContextValidation ctxVal) {
		//$http.get(jsRoutes.controllers.readsets.api.ReadSets.list().url,{params:{submissionStateCode:"NONE", productionValidCode:"TRUE", includes:"code", bioinformaticValidCode:"TRUE", typeCodes:tab_typeCodes}}).success(function(data) {

		ArrayList<String> typeCodes = new ArrayList<String>(Arrays.asList("rsillumina", "rsnanopore", "rsmgi", "rspacbio")); 
		BasicDBObject keys = new BasicDBObject();

		keys.put("code",  1);
		keys.put("projectCode",  1);
		keys.put("submissionState.code", 1);
		keys.put("productionValuation.valid",  1);
		keys.put("bioinformaticValuation.valid",  1);
		keys.put("properties", 1);
		keys.put("typeCode",  1);
		keys.put("sampleOnContainer", 1);
		
		ArrayList<ReadSet> readsets = new ArrayList<ReadSet>();
		for (String code : analysis.readSetCodes) {
			ReadSet rs = laboReadsetsAPI.getObject(code, keys);
			//logger.debug("XXXXXXXXXXXXX  11111");
			if( ! typeCodes.contains(rs.typeCode) ) {
				logger.debug("dans analysis '{}', le readset '{}' est non soumettable car typeCode='{}'", 
						analysis.code, rs.code, rs.typeCode);
				ctxVal.addError("analysis." + analysis.code, "avec readset " + rs.code + " non soumettable car typeCode=" + rs.typeCode);
				continue;
			}
			//logger.debug("XXXXXXXXXXXXX 22222222222");

			if(	! rs.productionValuation.valid.equals((TBoolean.valueOf(true))) ) {
				logger.debug("dans analysis '{}', le readset '{}' est non soumettable car rs.productionValuation.valid='{}'", 
						analysis.code, rs.code, rs.productionValuation.valid);
				//ctxVal.addError("analysis." + analysis.code, "avec readset " + rs.code + " non soumettable car productionValuation.valid=" + rs.productionValuation.valid);
				continue;
			}
			//logger.debug("XXXXXXXXXXXXX  3333333333");

			if(! rs.bioinformaticValuation.valid.equals((TBoolean.valueOf(true)))) {
				logger.debug("dans analysis '{}', le readset '{}' est non soumettable car rs.productionValuation.valid='{}'", 
						analysis.code, rs.code, rs.bioinformaticValuation.valid);
				//ctxVal.addError("analysis." + analysis.code, "avec readset " + rs.code + " non soumettable car bioinformaticValuation.valid=" + rs.bioinformaticValuation.valid);
				continue;
			}
			//logger.debug("XXXXXXXXXXXXX  444444444444");
			if(! rs.properties.containsKey("refCollabSub") || StringUtil.isBlank((String) rs.properties.get("refCollabSub").getValue())) {
				logger.debug("dans analysis '{}', le readset '{}' est non soumettable car sans refCollabSub", 
						analysis.code, rs.code);
				//ctxVal.addError("analysis." + analysis.code, "avec readset " + rs.code + " sans refCollabSub");
				//logger.debug("XXXXXXXXXXXXX  555555555555");
				continue;
			} else {	
				String refCollabSub = (String) rs.properties.get("refCollabSub").getValue();
				if( ! (refCollabSub.matches(patternSampleAc)||refCollabSub.startsWith(patternSampleExternalId)) ) {
					logger.debug("dans analysis '{}', le readset '{}' est non soumettable car refCollabSub pas de la forme ERS ou SAM", 
							analysis.code, rs.code);
					//ctxVal.addError("analysis." + analysis.code, "avec readset " + rs.code + " avec refCollabSub qui ne matche pas avec ERS... ou SAM...");
					continue;
				}
				//logger.debug("XXXXXXXXXXXXX  666666666666666666");
			}
			if (! rs.submissionState.code.equals("NONE") ) {	
				logger.debug("dans analysis '{}', le readset '{}' est non soumettable", analysis.code, rs.code);
				continue;
			} 
			logger.debug("XXXXXXXXXXXXX  77777777777777");

			
			if( ! ctxVal.hasErrors() && rs.submissionState.code.equals("NONE")) {
				logger.debug("dans analysis '{}', le readset '{}' est soumettable", analysis.code, rs.code);
				readsets.add(rs);
				continue;
			}
			logger.debug("XXXXXXXXXXXXX  888888888888");
		} 
		logger.debug("En sortie de checkCoherenceTaxonInSampleAndReadsetForAtlasea, displayError");
		ctxVal.displayErrors(logger, "debug");
		return readsets;
	}	

	private void checkCoherenceTaxonInSampleAndReadsetForAtlasea(List<ReadSet> listReadsets, ContextValidation ctx) {
		Map<String, Sample> map = new HashMap<String, Sample>();

		for (ReadSet rs : listReadsets) {
			//logger.debug("AAAAAAAAAAAA     rs.='{}'", rs.code);

			String sampleIdentifier = (String)rs.properties.get("refCollabSub").getValue();
			Sample sample = null;

			if(map.containsKey(sampleIdentifier)) {
				sample = map.get(sampleIdentifier);
			} else {
				try {
					logger.debug("KKKKKKKKKKKKKKKKKKKKKKKKK dans try     rs.='{}'", rs.code);
					sample = ebiAPI.ebiBrowserFetchSample(sampleIdentifier);
					//logger.debug("fin try     rs.='{}'", rs.code);
				} catch (SraException | IOException | ParserConfigurationException | ParseException  e) {
					//logger.debug("dans catch AAAA     rs.{}", rs.code);
					ctx.addError(rs.code, "Error lors de la recuperation a l'EBI du sample " + sampleIdentifier);
					ctx.displayErrors(logger, "debug");
					continue;
				} catch (Exception e) {
					//logger.debug("dans catch BBBBB      rs.{} et displayError", rs.code);
					ctx.addError(rs.code, "Error lors de la recuperation a l'EBI du sample " + sampleIdentifier);
					ctx.displayErrors(logger, "debug");
					continue;
				}
				//logger.debug("apres try catch, pour le rs "+ rs.code + ", sample recupere à l'EBI :" + sample.code);
				map.put(sampleIdentifier, sample);
			}
			
			//logger.debug("pour le rs "+ rs.code + ", sample recupere à l'EBI :" + sample.code);
			int    sampleTaxonId    = sample.taxonId;
			String sampleTaxonCode  = Integer.toString(sampleTaxonId);
			//logger.debug("dans ebi_sample, taxonCode={}", sampleTaxonCode);
			String rsTaxonCode = rs.sampleOnContainer.taxonCode;
			//logger.debug("rsTaxonCode='{}'", rsTaxonCode);
			if(StringUtils.isBlank(rsTaxonCode)) {
				ctx.addError(rs.code, "avec taxonId non defini");
				continue;
			}
			if( ! ebiAPI.submittable(sampleTaxonId)) {
				ctx.addError(rs.code, "associe au sampleCode " + sample.code + " avec taxon=" + sample.scientificName + " et taxonId=" + sampleTaxonCode + " non submittable à l'EBI");
			} 
			if( ! rsTaxonCode.equalsIgnoreCase(sampleTaxonCode) ) {
				ctx.addError(rs.code, "avec informations de taxon='" + rs.sampleOnContainer.ncbiScientificName + "' et taxonId=" + rs.sampleOnContainer.taxonCode 
						+ " differentes des information du sample " + sampleIdentifier + " avec sampleTaxon='" + sample.scientificName + "' et sampleTaxonId=" + sampleTaxonCode);
			}
		}
	}	
	
	public ArrayList<Submission> initPrimarySubmissionAtlaseaForOneTolid(
			String tolid, 
			ContextValidation ctxVal, 
			ContextValidation ctxSuccess) {

		ArrayList<Submission> submissions = new ArrayList<Submission>();
		//logger.debug("XXXXXXXXXXXXXXX          Dans CreateServices.initPrimarySubmissionAtlaseaForOneTolid");
		if(StringUtils.isBlank(tolid)) {
			ctxVal.addError("tolid", "sans caracteres visibles, aucune soumission réalisée");
			return null;
		}
		String messError = " aucune soumission de données brutes possible";
		// Recuperer enr Atlasea ayant le tolid indiqué
		if( ! atlaseaAPI.dao_checkObjectExist("tolId", tolid)) {
			ctxVal.addError("tolid." + tolid, " absent de la table ngl-sub.Atlasea (aucun projet Atlasea declarés)  => " + messError);
			return submissions;
		}
		Atlasea atlasea = atlaseaAPI.dao_findOne(DBQuery.in("tolId", tolid));
		//logger.debug("XXXXXXXXXXX    atlasea.code='{}'", atlasea.code);
		if (! atlasea.endSubmitProjects) {
			ctxVal.addError("tolid." + tolid, " qui n'a pas tous ses projects declares à l'EBI => " + messError);
			return submissions;
		}
		
		// Recuperer biAnalysis correspondant au tolid
		String analysisCode = atlasea.code;
		if(StringUtils.isBlank(analysisCode)) {
			ctxVal.addError("tolid." + tolid, " qui n'ai pas lié à une analyse => " + messError);
			return submissions;
		}
		//logger.debug("analysisCode='{}'", analysisCode);
		
		if( ! biAnalysisAPI.isObjectExist(analysisCode) ) {
			ctxVal.addError("analysis." + analysisCode , " absent de la table ngl-bi.Analysis => " + messError);
			return submissions;
		}
		models.laboratory.run.instance.Analysis analysis = biAnalysisAPI.getObject(atlasea.code);
		//logger.debug("analysis.code='{}'", analysis.code);

		
		// Dans une analyse, on va avoir des readsets à soumettre avec des AC differents : readsets venant d'un meme individu
		// mais de tissus differents par ex. => meme taxonId, geographicLocation mais pas meme sample
		// car distinction entre sample pour la tete et sample pour le corps d'un organisme.
		ArrayList<ReadSet> readsets = selectSubmittableReadsetInAnalyse(analysis, ctxVal);
		//logger.debug("XXXXXXXXX   apres select, nbre de readset pour soumission={}", readsets.size());
		if(ctxVal.hasErrors()) {
			ctxVal.displayErrors(logger, "error");
			return submissions;
		}
		
		if(readsets.size() == 0) {
			//logger.debug("ZZZZZZZZZ Aucun readsets à soumettre pour le tolid '{}', (voir analyse '{}')", tolid, analysis.code);
			ctxSuccess.addError("tolid." + tolid,  "Aucun readsets à soumettre pour l'analyse " + analysis.code);
			return submissions;
		}
		
		// verifier que les taxonCodes des readsets sont coherents avec les taxonCodes des samples à l'EBI (des refCollabSub) :
		checkCoherenceTaxonInSampleAndReadsetForAtlasea(readsets, ctxVal);
		if(ctxVal.hasErrors()) {
			ctxVal.displayErrors(logger, "error");
			return submissions;
		}
		
		ArrayList<String> hiC_readsetCodes = new ArrayList<String>();
		ArrayList<String> dna_readsetCodes = new ArrayList<String>();
		ArrayList<String> rna_readsetCodes = new ArrayList<String>();
		
		for(ReadSet rs : readsets) {
			if(rs.projectCode.equalsIgnoreCase("DKC")) {
				dna_readsetCodes.add(rs.code);
			} else if (rs.projectCode.equalsIgnoreCase("DKF")) {
				hiC_readsetCodes.add(rs.code);
			} else if (rs.projectCode.equalsIgnoreCase("DKE")) {
				rna_readsetCodes.add(rs.code);
			} else {
				ctxVal.addError("error.analysis.code" , analysisCode + " avec readset " + rs.code + " qui a un projectCode " + rs.projectCode + " non prévu");
			}
		}

		String studyCode  = null;
		String acStudy    = atlasea.sequencingProjectAc;
		String acSample   = null;
		Map<String, UserRefCollabType> mapUserRefCollab = new HashMap<String, UserRefCollabType>();
		for (ReadSet rs : readsets) {
			// pas besoin de controler ici si propriete existe car fait dans le select des readsets
			String refCollabSub = (String)rs.properties.get("refCollabSub").getValue();
			mapUserRefCollab.put(refCollabSub, new UserRefCollabType(rs.code, null, refCollabSub) );
		}
		boolean noCheckTaxonSampleCollab = true;  // controle de coherence des taxonCodes deja faite precedemment
		try {
			if(dna_readsetCodes.size() > 0) {
				//logger.debug("XXXXXXXXX   apres select, nbre de readset DNA pour soumission={}", dna_readsetCodes.size());
				String submissionCode = sraCodeHelper.generateAtlaseaSubmissionCode(tolid, "DKC");
				//logger.debug("XXXXXXXXX   submissionCode={}", submissionCode);
				String configCode = "CONF_atlasea_DNA";
				// controle de coherence des taxonCodes deja faite precedemment
				Submission submission = initPrimarySubmission(submissionCode, dna_readsetCodes, studyCode, configCode, acStudy, acSample, mapUserRefCollab, noCheckTaxonSampleCollab, ctxVal);
				if(! ctxVal.hasErrors()) {
					submissions.add(submission);
				} else {
					//logger.debug("debut des error dans ctxVal");
					ctxVal.displayErrors(logger, "debug");
					//logger.debug("fin des error dans ctxVal");
				}
			}
		} catch (Exception e) {
			// supprimer les soumissions crées et 
			//ctxVal.addError("error", "soumission des sequences DKC du tolid "  + tolid + " (analyse " + analysis.code + ") en erreur " + e.getMessage());
			//mess += "soumission des sequences DKC du tolid "  + tolid + " (analyse " + analysis.code + ") en erreur " + e.getMessage();
			//logger.debug("YYYY  Exception interceptée dans CreateServices.initPrimarySubmissionAtlaseaForOneTolid() lors de la soumission des rawData DKC(DNA) avec message = " + e.getMessage());
			ctxVal.addError("tolid." + tolid, "soumission DKC (DNA) en erreur " + e.getMessage() + " (aucune soumission cree pour DKC)");
		}
		try {	
			if(hiC_readsetCodes.size() > 0) {
				//logger.debug("XXXXXXXXX   apres select, nbre de readset HiC pour soumission={}", hiC_readsetCodes.size());
				String submissionCode = sraCodeHelper.generateAtlaseaSubmissionCode(tolid, "DKF");
				//logger.debug("XXXXXXXXX   submissionCode={}", submissionCode);
				String configCode = "CONF_atlasea_HiC";			
				Submission submission = initPrimarySubmission(submissionCode, hiC_readsetCodes, studyCode, configCode, acStudy, acSample, mapUserRefCollab, noCheckTaxonSampleCollab, ctxVal);
				if(! ctxVal.hasErrors()) {
					submissions.add(submission);
				}
			}
		} catch (Exception e) {
			// supprimer les soumissions crées et 
			//ctxVal.addError("error", "soumission des sequences DKC du tolid "  + tolid + " (analyse " + analysis.code + ") en erreur " + e.getMessage());
			//mess += "soumission des sequences DKC du tolid "  + tolid + " (analyse " + analysis.code + ") en erreur " + e.getMessage();
			logger.debug("YYYY  Exception interceptée dans CreateServices.initPrimarySubmissionAtlaseaForOneTolid() lors de la soumission des rawData DKF(HiC) avec message = " + e.getMessage());
			ctxVal.addError("tolid." + tolid, "soumission DKF (HiC) en erreur " + e.getMessage() + " (aucune soumission cree pour DKF)");
		}
		try {
			if(rna_readsetCodes.size() > 0) {
				logger.debug("XXXXXXXXX   apres select, nbre de readset RNA pour soumission={}", rna_readsetCodes.size());
				String submissionCode = sraCodeHelper.generateAtlaseaSubmissionCode(tolid, "DKE");
				logger.debug("XXXXXXXXX   submissionCode={}", submissionCode);
				String configCode = "CONF_atlasea_RNA";
				Submission submission = initPrimarySubmission(submissionCode, rna_readsetCodes, studyCode, configCode, acStudy, acSample, mapUserRefCollab, noCheckTaxonSampleCollab, ctxVal);
				if(! ctxVal.hasErrors()) {
					submissions.add(submission);
				}
			}
		} catch (Exception e) {
			// supprimer les soumissions crées et 
			//ctxVal.addError("error", "soumission des sequences DKC du tolid "  + tolid + " (analyse " + analysis.code + ") en erreur " + e.getMessage());
			//mess += "soumission des sequences DKC du tolid "  + tolid + " (analyse " + analysis.code + ") en erreur " + e.getMessage();
			logger.debug("YYYY  Exception interceptée dans CreateServices.initPrimarySubmissionAtlaseaForOneTolid() lors de la soumission des rawData DKE(RNA) avec message = " + e.getMessage());
			ctxVal.addError("tolid." + tolid, "soumission DKE (RNA) en erreur " + e.getMessage() + " (aucune soumission cree pour DKE)");
		}
		
		if (ctxVal.hasErrors()) {
			// detruire les soumissions crées pour lesquelles il n'y a pas eu d'erreur :
			for (Submission submission : submissions) {
				String user  = "ngsrg";
				submissionNewAPITools.rollbackSubmission(submission, user);
			}
			return null;
		} 
		for (Submission submission : submissions) {
			ctxSuccess.addError("submission." + submission.code, " bien crée");
		}
//		logger.debug("ZZZZZZZZZZ  Dans Create, message = " + sb);
		return submissions;	
	}
	
	
	// NGL-3853 : pour avoir une soumission avec code atlasea généré à l'exterieur de NGL-SUB (il faut le tolId qu'on a pas ici) 
	public Submission initPrimarySubmission(
			List<String> readSetCodes, 
			String studyCode, 
			String configCode, 
			String acStudy, 
			String acSample, 
			Map<String, UserRefCollabType>mapUserRefCollab, 
			boolean noCheckTaxonSampleCollab,
			ContextValidation contextValidation) throws SraException, SraValidationException, IOException, APIValidationException, APIException {
		return initPrimarySubmission(null, readSetCodes, studyCode, configCode, acStudy, acSample, mapUserRefCollab, noCheckTaxonSampleCollab, contextValidation);
	}
	
	// normalement appelé avec submissionCode != null uniquement pour les soumissions automatiques de atlasea :
	public Submission initPrimarySubmission(
			String submissionCode,
			List<String> readSetCodes, 
			String studyCode, 
			String configCode, 
			String acStudy, 
			String acSample, 
			Map<String, UserRefCollabType>mapUserRefCollab, 
			boolean noCheckTaxonSampleCollab,
			ContextValidation contextValidation) throws SraException, SraValidationException, IOException, APIValidationException, APIException {
		// Pour simplifier workflow on n'autorise pas a la creation d'une soumission, l'utilisation d'un sample crée par une autre soumission
		// mais toujours dans le processus de soumission. Idem pour les study. Si la creation d'une soumission utilise un study ou un sample deja existant
		// dans la base, alors le state.code doit etre à F-SUB.
		// Pour une premiere soumission d'un readSet, on peut devoir utiliser un study ou un sample existant, deja soumis à l'EBI.
		// En revanche on ne doit pas utiliser un experiment ou un run existant
		//logger.debug("999999999999999999  Dans initPrimarySubmission acSample={}", acSample);
		//logger.debug("999999999999999999  Dans initPrimarySubmission acStudy={}", acStudy);
		String user = contextValidation.getUser();
		
		//logger.debug("acSample={}", acSample);
		validateParamForSubmission(readSetCodes, studyCode, configCode, acStudy, acSample, mapUserRefCollab, contextValidation);
		logger.debug("AAAAAAAAAAAAA   sortie de validateParamForSubmission, avec contextValidation:");

		contextValidation.displayErrors(logger, "debug");
		logger.debug("end contextValidation");

		if(contextValidation.hasErrors()) {
			contextValidation.displayErrors(logger, "debug");
			throw new SraValidationException(contextValidation);
		}
		//logger.debug("2025 XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX                 sortie de validateParamForSubmission sans erreur");
		// Liste des projectCodes de la soumission, qui doit contenir les 
		// projectCodes des objets soumis : readsets, samples, experiments et study
		// le projectCode de l'experiment et du sample est le meme que celui du readset.
		Configuration config = configurationAPI.get(configCode);
		String dropBox = config.dropBox;

		//logger.debug("dropBox in config={}", dropBox);
		// Recuperation de tous les Readset avec un status à NONE 
		//(verif etat des readsets dans validateParamForSubmission, dans validateReadSetCodesForSubmission)
		//List <ReadSet> readSets = new ArrayList<>();
		// Renvoie un objet submission avec state.code='SUB-N'
		Submission submission = createSubmissionEntityForPrimarySubmission(config, studyCode, acStudy, user, dropBox);
		//logger.debug("dropBox in submission={}", submission.dropBox);
		// Liste des sous-objets utilisés dans submission
		List <Experiment> listExperiments = new ArrayList<>();         // liste des experiments utilisés et crees dans soumission 
		// avec state.code='N' à sauver dans database
		List <AbstractSample> listAbstractSamples = new ArrayList<>(); // liste des AbstractSample(Sample ou ExternalSample) 
		// avec state.code='F-SUB' ou 'N' utilises dans soumission à sauver ou non dans database		
		List <AbstractStudy> listAbstractStudies = new ArrayList<>();  // liste des AbstractStudy(Study ou ExternalStudy) avec state.code='F-SUB' ou 'N' 
		// utilises dans soumission à sauver ou non dans database	

		//		int countError = 0;
		//		String errorMessage = "";

		//logger.debug("2025 XXXXXXXXXXXXX    avant traitementSubmissionForReadset");
		for (String readSetCode : readSetCodes) {
			traitementSubmissionForReadset(
					readSetCode, config, 
					studyCode, acStudy, acSample,
					mapUserRefCollab, contextValidation, 
					listExperiments, listAbstractStudies, listAbstractSamples, 
					submission);
		} // end for readset
		
		
		if(contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		// NGL-4952
		// Après traitementSubmissionForReadset(), les experiments sont renseignés pour sampleCode et sampleIdentifier
		// avec sampleIdentifier à null si sample jamais soumis
		// On peut verifier ici pour chaque experiment construit et avant sauvegarde de tout objet en base,
		// que chaque experiment pointe bien vers un sample ayant meme taxonId que le readset :
		ContextValidation ctxVal = ContextValidation.createUndefinedContext(user);
		logger.debug("Dans CreateServices.initPrimarySubmission, noCheckTaxonSampleCollab={}", noCheckTaxonSampleCollab);
			
		submissionNewAPITools.checkVisibiliteSamplesAndStudy(listExperiments, contextValidation);
		if(contextValidation.hasErrors()) {
		    contextValidation.displayUniqMessageError(logger, "debug");
		    throw new SraValidationException(contextValidation);
		}  
		if( ! noCheckTaxonSampleCollab ) {
			long startTime = System.currentTimeMillis();
			submissionNewAPITools.checkCoherenceTaxonInSampleAndReadset(listExperiments, contextValidation);
		    long endTime = System.currentTimeMillis();
		    logger.debug("WWWWWWW   Total elapsed time in execution of method checkCoherenceTaxonInSampleAndReadset() is :"+ (endTime-startTime));
		    if(contextValidation.hasErrors()) {
		    	contextValidation.displayUniqMessageError(logger, "debug");
		    	throw new SraValidationException(contextValidation);
		    } 
		}  
		
		//logger.debug("2025 XXXXXXXXXXXXX    apres traitementSubmissionForReadset sans erreur");

		// Determiner le code de la soumission, une fois la liste des projectCodes definie :
		if(StringUtils.isNotBlank(submissionCode) && submissionCode.contains("atlasea")) {
			submission.code = submissionCode;
		} else {
			submission.code = sraCodeHelper.generateSubmissionCode(submission.projectCodes);
		}

		// Determiner le submissionDirectory et submission.ebiResult une fois submission.code defini :
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + "/" + submission.code; 	
		//logger.debug("AAAAAAAAAAAAAAA            submission.submissionDirectory=" + submission.submissionDirectory);
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		

		
		//---------------------------------------------//
		// update des données et sauvegarde dans base: //
		//---------------------------------------------//
		contextValidation.setCreationMode();
		// On ne sauvent que les study qui n'existent pas dans la base donc des ExternalStudy avec state.code='F-SUB'	
		for (AbstractStudy absStudyElt: listAbstractStudies) {
			// On sauve les study qui n'existent pas dans la base (forcement externalStudy)
			// sans tester validation qui sera tester a la sauvagarde de la soumission
			if (! abstractStudyAPI.dao_checkObjectExist("code", absStudyElt.code)) {	
				if (absStudyElt instanceof ExternalStudy) {
					ExternalStudy eStudy = (ExternalStudy) absStudyElt;
					eStudy.validate(contextValidation);
					externalStudyAPI.dao_saveObject(eStudy);
					//logger.debug("ok pour sauvegarde dans la base de l'external study " + absStudyElt.code);
				}
			}
		}	

		// On sauve les sample qui n'existent pas dans la base (externalSample à SUB-F ou Sample à SUB_N)
		// sans tester validation qui sera tester a la sauvegarde de la soumission
		//Map <Integer, String> hTaxonId = new HashMap<Integer, String>();
		HashMap<String, String>   hTaxonId = new HashMap<String, String>();

		for (AbstractSample sampleElt: listAbstractSamples) {

			if (!abstractSampleAPI.dao_checkObjectExist("code", sampleElt.code)){	
				// validation qui depend du type de l'objet.
				//logger.debug("xxxxx   validation du sample " + sampleElt.code);
				if (sampleElt instanceof ExternalSample) {
					ExternalSample eSample = (ExternalSample) sampleElt;
					eSample.validate(contextValidation);
				} else {
					Sample sample = (Sample) sampleElt;
					//logger.debug("PPPPPPPPPPPPPPPPPPPPPP   pour " + sample.code + " taxonId=" + sample.taxonId);
					if(! hTaxonId.containsKey(Integer.toString(sample.taxonId))) {
						hTaxonId.put(Integer.toString(sample.taxonId), sample.code);
					} else {
						String oldValue = hTaxonId.get(Integer.toString(sample.taxonId));
						String newValue = oldValue + ", " + sample.code;
						hTaxonId.put(Integer.toString(sample.taxonId), newValue);
					}
					//logger.debug("xxxxx demande "+ sample.code + ".isSubmittable("+sample.taxonId+")");
					//isSubmittable(sample, contextValidation); deporte plus bas pour limiter requetes EBI
					sample.validate(contextValidation);
				}
				abstractSampleAPI.dao_saveObject(sampleElt);
				//abstractSampleAPI.create(sampleElt, user); // faudrait deporter generation du code dans create et pas ici
				//logger.debug("ok pour sauvegarde dans la base du sample " + sampleElt.code);
			}
		}

		// verification des taxonId : ok marche bien
		for (Entry<String, String> setTaxon : hTaxonId.entrySet()) {
			//logger.debug("ZZZZZZZZZZZZZZZZ22222222222    test isSubmittable pour " + setTaxon.getKey());
			isSubmittable(Integer.parseInt(setTaxon.getKey()), setTaxon.getValue(), contextValidation);
		}	

		// On sauve les experiments qui n'existent pas dans la base
		// sans tester validation qui sera testee a la sauvegarde de la soumission
		for (Experiment expElt: listExperiments) {
			expElt.validate(contextValidation);
			contextValidation.removeKeyFromRootKeyName(expElt.code);		
			if (!experimentAPI.dao_checkObjectExist("code", expElt.code)) {	
				experimentAPI.dao_saveObject(expElt);
				//logger.debug("sauvegarde dans la base de l'experiment " + expElt.code);
			}
		}

		contextValidation.setUpdateMode();
		// updater les readsets dans collection readSet de ngl-seq pour le status et sauver dans base :
		//updateReadSetAndSaveIndbForCreation(readSets, SUB_N, user);
		//int cp = 0;
		for (String readSetCode : readSetCodes) {
			//logger.debug("updateStatus readset {}, {}", cp, readSetCode);
			ReadSet readSet = laboReadsetsAPI.get(readSetCode);
			updateReadSetAndSaveIndbForCreation_1(readSet, SUB_N, user);
		}
		// ajouter les readsets dans collection readset de sra :
		addReadsetIndb(listExperiments);


		// updater dans la base le study pour son status si study à soumettre
		if (StringUtils.isNotBlank(submission.studyCode)) {
			Study study = studyAPI.dao_getObject(submission.studyCode);
			if (study != null && NONE.equals(study.state.code)) {
				updateStudyAndSaveIndb(study, SUB_N, user);
				if(StringUtils.isBlank(study.dropBox)) {
					study.dropBox = submission.dropBox;
					studyAPI.update(study,  user);
				} 
			}
		}
		// updater la config pour le statut et sauver dans base :
		if (NONE.equals(config.state.code)) {
			updateConfigurationAndSaveIndb(config, SUB_N, user);
		}


		// valider submission une fois les experiments et samples sauves, et sauver submission
		contextValidation.setCreationMode();
		submission.validate(contextValidation);
		if (contextValidation.hasErrors()) {
			//logger.debug("submission.validate produit des erreurs");
			//logger.debug("\ndisplayErrors dans CreateServices::initPrimarySubmission :");
			contextValidation.displayErrors(logger, "debug");
			//logger.debug("\n end displayErrors dans SubmissionServices::initPrimarySubmission :");
			// enlever les samples, experiments et submission qui ont ete crées par le service et remettre
			// readSet.submissionState à NONE, et si studyCode utilisé par cette seule soumission remettre studyCode.state.code=NONE
			// et si config utilisé par cette seule soumission remettre configCode.state.code=NONE

			submissionNewAPITools.rollbackSubmission(submission, contextValidation.getUser());	
			//			throw new SraException("initPrimarySubmission::probleme validation : "+ contextValidation.getErrors());			
			throw new SraValidationException(contextValidation);			
		} 

		submission = submissionAPI.dao_saveObject(submission);
		//logger.debug("sauvegarde dans la base du submission " + submission.code);
		// retourner l'objet de la base avec id :
		return submissionAPI.get(submission.code);
	}


	public void validateForStrategyCodeStudy(String configCode, String acStudy, String studyCode) {
		logger.debug("dans validateForStrategyCodeStudy");
		if (StringUtils.isNotBlank(acStudy)) {
			throw new SraException("validateParamForSubmission", "Configuration " + configCode + " avec STRATEGY_CODE_STUDY incompatible avec acStudy renseigne : '" + acStudy +"'");
			//contextValidation.addError("validateParamForSubmission", "Configuration " + config.code + " avec STRATEGY_CODE_STUDY incompatible avec acStudy renseigne : '" + acStudy +"'");
		}
		if (StringUtils.isBlank(studyCode)) {
			throw new SraException("validateParamForSubmission", "STRATEGY_CODE_STUDY et studyCode passé en parametre null");	
			//contextValidation.addError("validateParamForSubmission", "STRATEGY_CODE_STUDY et studyCode passé en parametre null");	

		}
		if (! studyAPI.dao_checkObjectExist("code", studyCode)) {
			throw new SraException("validateParamForSubmission", "STRATEGY_CODE_STUDY et studyCode passé en parametre "+ studyCode + " n'existe pas dans database");	
			//contextValidation.addError("validateParamForSubmission", "STRATEGY_CODE_STUDY et studyCode passé en parametre "+ studyCode + " n'existe pas dans database");	
		}
		Study study = studyAPI.dao_getObject(studyCode);	
		if (study.state == null) {
			throw new SraException("validateParamForSubmission", " STRATEGY_CODE_STUDY et studyCode passé en parametre "+ studyCode + " sans champs state renseigné");	
			//contextValidation.addError("validateParamForSubmission", " STRATEGY_CODE_STUDY et studyCode passé en parametre "+ studyCode + " sans champs state renseigné");	

		}
		if (study.state.code == null) {
			throw new SraException("validateParamForSubmission", " STRATEGY_CODE_STUDY et studyCode passé en parametre "+ studyCode + " sans champs state.code renseigné");	
			//contextValidation.addError("validateParamForSubmission", " STRATEGY_CODE_STUDY et studyCode passé en parametre "+ studyCode + " sans champs state.code renseigné");	
		}			
		if (!NONE.equals(study.state.code) 
				&& !SUB_F.equals(study.state.code)) {
			throw new SraException("validateParamForSubmission", " STRATEGY_CODE_STUDY et studyCode passé en parametre "+ studyCode + " avec un status non attendu :" + study.state.code);	
		}
		logger.debug("end validateForStrategyCodeStudy");
	}


	public void validateForStrategyAcStudy (String configCode, String acStudy, String studyCode, List<String> readSetCodes, Map<String, UserRefCollabType>mapUserRefCollab, ContextValidation contextValidation) {
		logger.debug("dans validateForStrategyAcStudy");	
		if( StringUtils.isNotBlank(acStudy) && ! ( acStudy.matches(patternStudyAc) || acStudy.startsWith(patternStudyExternalId) )) {
			contextValidation.addError("STRATEGY_AC_STUDY", "acStudy transmis '" + acStudy + "' qui n'est pas de la forme attendue (ERP.. ou PRJ..)");
			return;
		}
		if (StringUtils.isNotBlank(acStudy)) {
			// pas de controle de mapUserRefCollab car c'est cet acStudy qui sera utilisé pour tous les readsets
			return;
		}
		// on s'attend à ce que acStudy soit fournit par mapUserRefCollab
		if (mapUserRefCollab == null || mapUserRefCollab.isEmpty())  {
			contextValidation.addError("STRATEGY_AC_STUDY", "incompatible avec acStudy et mapUserRefCollab non renseignés");
			return;
		} 
		// NGL-4813
		for (String rsCode : readSetCodes) {
			ReadSet readSet = laboReadsetsAPI.get(rsCode);
			if(readSet == null) {
				contextValidation.addError(rsCode, " n'existe pas dans la la base");
				continue;
			}
			if(readSet.sampleOnContainer == null) {
				contextValidation.addError(rsCode, " sans sampleOnContainer");
				continue;
				//throw new SraException("validateParamForSubmission", "readSet " + rsCode + " sans sampleOnContainer");
			}
			String refCollab = readSet.sampleOnContainer.referenceCollab;
			// NGL-3413
			if(readSet.properties.containsKey("refCollabSub")) {
				PropertyValue propertyValue = readSet.properties.get("refCollabSub");
				String refCollabSub = (String) propertyValue.value;
				if(StringUtils.isNotBlank(refCollabSub)) {
					refCollab = refCollabSub;
				}
			}
			if (StringUtils.isBlank(refCollab)) {
				contextValidation.addError(rsCode, "absence de refCollab(sub) dans ngl-bi et UserRefCollab renseigne");
				continue;					
			}
			if(! mapUserRefCollab.containsKey(refCollab)) {
				contextValidation.addError(rsCode, "absence de la refCollab(sub) " + refCollab + " dans UserRefCollab ");
				continue;
			}
			String acStudyInMap = mapUserRefCollab.get(refCollab).getStudyAc();
			if (StringUtils.isBlank(acStudyInMap)) {
				contextValidation.addError(rsCode, " avec refCollab(sub)=" + refCollab + " sans studyAc dans UserRefCollab et aucun identifiant de study indique par utilisateur dans interface de soumission => incompatible avec STRATEGY_AC_STUDY");
				continue;
			} 
			if (! (acStudyInMap.matches(patternStudyAc) || acStudyInMap.startsWith(patternStudyExternalId)) ) {
				contextValidation.addError(rsCode, " avec refCollab(sub)=" + refCollab + " avec, dans userFileRefCollab, un identifiant de study=" + acStudyInMap + " qui n'est pas de la forme attendue (ERP.. ou PRJ..)");
				continue;
			}
		}// end for
	}


	
	private void oldcheckCoherenceTaxonInSampleAndReadset(List<Experiment> listExperiments, ContextValidation ctx){
		for (Experiment expElt: listExperiments) {
			String sampleCode       = expElt.sampleCode;
			String sampleIdentifier = expElt.sampleIdentifier;
			logger.debug("XXXXXXXXXXXXXX      exp='{}'", expElt.code);
			AbstractSample sample = null;
			
			if(StringUtils.isBlank(sampleCode)) {
				ctx.addError(expElt.code, "experiment sans sampleCode dans checkCoherenceTaxonInSampleAndReadset?");
				continue;
			}
			if(abstractSampleAPI.dao_checkObjectExist("code", sampleCode)) {
				sample = abstractSampleAPI.get(sampleCode);
				if( ! sample._type.equals("Sample")) {
					sample = null;
				}
			}
			if(sample == null) {
				if(StringUtils.isBlank(sampleIdentifier)) {
					ctx.addError(expElt.code, "experiment avec sampleCode='" + sampleCode +"' sans sampleIdentifier dans checkCoherenceTaxonInSampleAndReadset?");	
					continue;
				} else {
					try {
						sample = ebiAPI.ebiBrowserFetchSample(sampleIdentifier);
					} catch(Exception e) {
						ctx.addError(expElt.code, "Error lors de la recuperation a l'EBI du sample " + sampleIdentifier + " " + e);
						continue;
					}
				}
			}
			if( sample == null ) {
				logger.debug("Impossible de verifier le sample '{}' pour le readset '{}'", sampleCode, expElt.readSetCode);
				ctx.addError(expElt.code, "impossible de verifier le sample " + sampleCode +"(sampleIdentifier : "+ sampleIdentifier +")");
				continue;
			} 
			int    sampleTaxonId    = ((Sample) sample).taxonId;
			String sampleTaxonCode  = Integer.toString(sampleTaxonId);

			if( ! ebiAPI.submittable(sampleTaxonId)) {
				//logger.debug("exp.{} avec sampleCollabTaxonId='{}' et sampleCollabCode='{}'", expElt.code, sampleTaxonId, sampleTaxonCode);
				ctx.addError(expElt.code, "associe au sampleCode " + sampleCode + " avec taxonCode " + sampleTaxonCode + " non submittable à l'EBI");
			} 
			ReadSet readset = laboReadsetsAPI.get(expElt.readSetCode);
			if( ! readset.sampleOnContainer.taxonCode.equals(sampleTaxonCode) ) {
				//logger.debug("KKKKKKKKKK    le sample '{}' (taxonId='{}') et le readset '{}'(taxonId='{}' ont des taxonCodes differents", sampleCode, sampleTaxonCode, readset.code, readset.sampleOnContainer.taxonCode );
				ctx.addError(expElt.code, "avec taxonCode=" + readset.sampleOnContainer.taxonCode + " different du taxonCode= " + sampleTaxonCode + " dans le sample " +  sampleCode);
				continue;
			}
			//logger.debug("le sample '{}' (taxonId='{}') et le readset '{}'(taxonId='{}' ont meme taxonCode", sampleCode, sampleTaxonCode, readset.code, readset.sampleOnContainer.taxonCode );
		}
	}

	public void validateForStrategyAcSample (String configCode, String acSample, List<String> readSetCodes, Map<String, UserRefCollabType>mapUserRefCollab, ContextValidation contextValidation) {	
		//logger.debug("YYYYYYYYYYYYYYYYY dans validateForStrategyAcSample");
		if( StringUtils.isNotBlank(acSample) && ! ( acSample.matches(patternSampleAc) || acSample.startsWith(patternSampleExternalId) )) {
			contextValidation.addError("STRATEGY_AC_SAMPLE", "et acSample transmis '" + acSample + "' qui n'est pas de la forme attendue (ERS.. ou SAM..)");
			return;
		}
		//logger.debug("YYYYYYYYYYYYYYYYY 00 dans validateForStrategyAcSample");
		if (StringUtils.isNotBlank(acSample)) {
			// pas de controle de mapUserRefCollab car c'est cet acSample qui sera utilisé pour tous les readsets
			return;
		}
		//logger.debug("YYYYYYYYYYYYYYYYY 11 dans validateForStrategyAcSample");
		// on s'attend à ce que acSample soit fournit par mapUserRefCollab
		if (mapUserRefCollab == null || mapUserRefCollab.isEmpty())  {
			contextValidation.addError("STRATEGY_AC_SAMPLE", "incompatible avec acSample et mapUserRefCollab non renseignés");
			return;
		} 
		//logger.debug("YYYYYYYYYYYYYYYYY 22 dans validateForStrategyAcSample");	
		// NGL-4813
		for (String rsCode : readSetCodes) {
			ReadSet readSet = laboReadsetsAPI.get(rsCode);
			if(readSet == null) {
				contextValidation.addError(rsCode, " n'existe pas dans la la base");
				continue;
			}
			//logger.debug("YYYYYYYYYYYYYYYYY A");
			if(readSet.sampleOnContainer == null) {
				contextValidation.addError(rsCode, " sans sampleOnContainer");
				continue;
			}
			//logger.debug("YYYYYYYYYYYYYYYYY B");
			String refCollab = readSet.sampleOnContainer.referenceCollab;
			// NGL-3413
			if(readSet.properties.containsKey("refCollabSub")) {
				PropertyValue propertyValue = readSet.properties.get("refCollabSub");
				String refCollabSub = (String) propertyValue.value;
				if(StringUtils.isNotBlank(refCollabSub)) {
					refCollab = refCollabSub;
				}
			}
			if (StringUtils.isBlank(refCollab)) {
				contextValidation.addError(rsCode, "absence de refCollab(sub) dans ngl-bi et UserRefCollab renseigne");
				continue;					
			}
			//logger.debug("YYYYYYYYYYYYYYYYY C");
			if(! mapUserRefCollab.containsKey(refCollab)) {
				contextValidation.addError(rsCode, "absence de la refCollab(sub) " + refCollab + " dans UserRefCollab ");
				continue;
			}
			//logger.debug("YYYYYYYYYYYYYYYYY D");
			String sampleIdentifier = mapUserRefCollab.get(refCollab).getSampleAc();
			if (StringUtils.isBlank(sampleIdentifier)) {
				contextValidation.addError(rsCode, " avec refCollab(sub)=" + refCollab + " sans sampleAc dans UserRefCollab et aucun identifiant de sample indique par utilisateur dans interface de soumission => incompatible avec STRATEGY_AC_SAMPLE");
				continue;
			} 
			//logger.debug("YYYYYYYYYYYYYYYYY E");
			if( ! (sampleIdentifier.matches(patternSampleAc) || sampleIdentifier.startsWith(patternSampleExternalId)) ) {
				logger.debug("YYYYYYYYYYYYYYYYY F");
				contextValidation.addError(rsCode, " avec refCollab(sub)=" + refCollab + " avec, dans userFileRefCollab, un identifiant de sample=" + sampleIdentifier + " qui n'est pas de la forme attendue (ERS.. SRS.. ou SAM..)");
				continue;	
			}
		} // end for
	}	

	
	//	valide la configuration et les differents parametres de la soumission.
	// ne pas utiliser SraException pour message formaté sur plusieurs lignes,
	// car retour chariots mal geré au niveau du javascript 
	// => preferer utilisation de contextValidation pour messages multiples 
	public void validateParamForSubmission(List<String> readSetCodes, 
			String studyCode, 
			String configCode, 
			String acStudy, 
			String acSample, 
			Map<String, UserRefCollabType>mapUserRefCollab, ContextValidation contextValidation) throws SraException {
		// verifier si utilisation d'un study ou d'un sample, qu'ils sont bien visibles à l'EBI :
	
		if (readSetCodes == null || readSetCodes.isEmpty() ) {
			throw new SraException("validateParamForSubmission", "Aucun readset a soumettre ! ");
		}
		if(readSetCodes.size()> maxReadsetInSubmission) {
			throw new SraException("validateParamForSubmission", "Le nombre de readsets " + readSetCodes.size() + " depasse le maximum autorise de " + maxReadsetInSubmission); 
		}
		// configuration obligatoire :
		if (StringUtils.isBlank(configCode) ) {
			throw new SraException("validateParamForSubmission", "La configuration a un code à null");
		}
		// validation des readsets de type different de bionano :
		validateReadSetCodesForSubmission(readSetCodes, contextValidation, false);
		contextValidation.displayErrors(logger, "debug");
		if(contextValidation.hasErrors()) {
			return;
		}
		
		Configuration config = configurationAPI.get(configCode);	
		if (config == null) {
			throw new SraException("validateParamForSubmission", "Configuration " + configCode + " n'existe pas dans database");
		} 
		if (StringUtils.isBlank(config.state.code)) {
			throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec champs state.code non renseigne");
		}
		if(StringUtils.isBlank(config.dropBox)) {
			throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec champs dropBox non renseigne");
		}
		switch (config.strategyStudy) {
			case STRATEGY_CODE_STUDY :
				validateForStrategyCodeStudy(configCode, acStudy, studyCode);
				break;
			case STRATEGY_AC_STUDY :
				validateForStrategyAcStudy(configCode, acStudy, studyCode, readSetCodes, mapUserRefCollab, contextValidation);
				break;
			default :
				throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec strategy_study = '"+ config.strategyStudy + "' (valeur non attendue)");
		}
		switch (config.strategySample) {
			case STRATEGY_CODE_SAMPLE_TAXON : case STRATEGY_CODE_SAMPLE_REFCOLLAB : case STRATEGY_CODE_SAMPLE_BANK :
				break;
			case STRATEGY_AC_SAMPLE :
				logger.debug("appel de validateForStrategyAcSample");
				validateForStrategyAcSample(configCode, acSample, readSetCodes, mapUserRefCollab, contextValidation);
				break;
			default :
				throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec strategy_sample = '"+ config.strategySample + "' (valeur non attendue)");
		}
		return;
	}

	private void validateReadSetCodesForSubmission (List<String> readSetCodes, ContextValidation contextValidation, boolean bionano) {
		// Verifier que tous les readSetCode passes en parametres correspondent bien a des objets en base avec
		// submissionState='NONE' cad des readSets qui n'interviennent dans aucune soumission et readset valides pour soumission (readset valides pour la bioinformatique)
		if(readSetCodes == null) {
			return;
		}

		for (String readSetCode : readSetCodes) {
			//logger.debug("YYYYYYYYYYYY          readsetCode={}",readSetCode );
			if (StringUtils.isNotBlank(readSetCode)) {
				//				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
				ReadSet readSet = laboReadsetsAPI.get(readSetCode);

				if (readSet == null) {
					contextValidation.addError("Le readset." + readSetCode, " n'existe pas dans la base de donnees");
					continue;
//					// verifier que ce readset est bien disponible. 
//					// controle enleve => NGL-4694
//				} else if (readSet.state == null || readSet.state.code == null || StringUtils.isBlank(readSet.state.code) || !readSet.state.code.equals("A")) {
//					contextValidation.addError("Le readset " + readSetCode, " n'est pas disponible");
//					continue;
					
				// Verifier que ce readSet est bien valide pour la bioinfo avant soumission :
				} else if (readSet.bioinformaticValuation == null || ! readSet.bioinformaticValuation.valid.equals(TBoolean.TRUE)) {
					contextValidation.addError("Le readSet " + readSet.code," est non valide pour la bioinformatique");
					continue; 
				// Verifier que ce readset est bien valide QC (pour la production)
				// NGL-4694
				} else if (readSet.productionValuation == null || ! readSet.productionValuation.valid.equals(TBoolean.TRUE)) {
					contextValidation.addError("Le readSet " + readSet.code," est non valide pour la production (QC) ");
					continue; 
				} else if(!readSet.submissionState.code.equalsIgnoreCase(NONE)){
					contextValidation.addError("Le readSet " + readSet.code,  " a un submissionState.code à la valeur " + readSet.submissionState.code);
					continue;
				} else {
					// NGL-4166 : notifier une erreur si collabFileName contient des espaces internes :
					// NGL-4810 : empecher collabFileName avec autre extention que fastq.gz si file avec extention fastq
					List <models.laboratory.run.instance.File> list_files =  readSet.files;
					for (models.laboratory.run.instance.File runInstanceFile: list_files) {
						if (runInstanceFile.usable ) { 		
							if(runInstanceFile.properties.containsKey("collabFileName")) {
								PropertyValue propertyValue = runInstanceFile.properties.get("collabFileName");
								String collabFileName = (String) propertyValue.value;
								if(StringUtils.isBlank(runInstanceFile.extension)) {
									contextValidation.addError("Le readset." + readSetCode, " a un fichier " + runInstanceFile.fullname + " sans extention");
								}
								if(StringUtils.isNotBlank(collabFileName) && StringUtils.isNotBlank(runInstanceFile.extension)) {
									collabFileName = Tools.clean(collabFileName);
									//logger.debug("collabFileName = " + collabFileName);
									if (collabFileName.matches("^\\S*\\s+.*$")) {
										contextValidation.addError("Le readset." + readSetCode, " a un fichier " + runInstanceFile.fullname + " avec un collabFileName " + collabFileName + " contenant des espaces");
									}
									if(! collabFileName.endsWith(".fastq.gz") && runInstanceFile.extension.contains("fastq")) {
										contextValidation.addError("Le readset." + readSetCode, " a un fichier " + runInstanceFile.fullname + " avec un collabFileName " + collabFileName + " qui ne contient pas l'extention attendue '.fastq.gz'");
									}
								}
							}
						}
					}
					Boolean alreadySubmit = experimentAPI.dao_checkObjectExist("readSetCode", readSet.code);		
					if ( alreadySubmit ) {
						contextValidation.addError("Le readSet " + readSet.code, "  - a deja un experiment dans la base de donnees");
						continue;
					} 					

					// on verifie que la soumission ne contient que des readsets bionano ou aucun readset bionano
					if (bionano) {
						if(! "rsbionano".equalsIgnoreCase(readSet.typeCode)) {
							contextValidation.addError("Le readSet " + readSet.code," est du type " + readSet.typeCode + " au lieu du typeCode attendu rsbionano");
							continue;
						}
					} else {
						if("rsbionano".equalsIgnoreCase(readSet.typeCode)) {
							contextValidation.addError("Le readSet " + readSet.code," est du type " + readSet.typeCode + " au lieu du typeCode attendu different de rsbionano");
							continue;
						}
					}

					if (readSet.sampleOnContainer == null) {
						contextValidation.addError("Le readSet " + readSet.code," n'a pas de sampleOnContainer");
						continue;
					}
					String scientificName = readSet.sampleOnContainer.ncbiScientificName;
					if (StringUtils.isBlank(scientificName)) {
						contextValidation.addError("Le readSet " + readSet.code," n'a pas de scientificName dans la base ");
						continue;
					}
					//NGL-3833
					if(StringUtils.isBlank(readSet.sampleOnContainer.sampleTypeCode)) {
						contextValidation.addError("Le readSet " + readSet.code," n'a pas de sampleTypeCode dans la base ");
						continue;
					}

					String sampleTypeCode = readSet.sampleOnContainer.sampleTypeCode;
					String targetedRegion;
					String amplificationPrimers;
					if (sampleTypeCode.equalsIgnoreCase("amplicon")) {
						if ( ! readSet.sampleOnContainer.properties.containsKey("targetedRegion") ) {
							contextValidation.addError("Le readSet " + readSet.code," avec un sampleTypeCode = amplicon n'a pas de property targetedRegion ");
							continue;
						}
						targetedRegion = (String) readSet.sampleOnContainer.properties.get("targetedRegion").getValue();
						if(StringUtils.isBlank(targetedRegion)) {
							contextValidation.addError("Le readSet " + readSet.code," avec un sampleTypeCode = amplicon a une property targetedRegion sans valeur ");
							continue;
						}
						if ( ! readSet.sampleOnContainer.properties.containsKey("amplificationPrimers") ) {
							contextValidation.addError("Le readSet " + readSet.code," avec un sampleTypeCode = amplicon n'a pas de property amplificationPrimers ");
							continue;
						}
						amplificationPrimers = (String) readSet.sampleOnContainer.properties.get("amplificationPrimers").getValue();
						if(StringUtils.isBlank(amplificationPrimers)) {
							contextValidation.addError("Le readSet " + readSet.code," avec un sampleTypeCode = amplicon a une property amplificationPrimers sans valeur ");
							continue;
						}
						if(readSet.typeCode.equalsIgnoreCase("rsillumina")) {
							//String title = "Illumina sequencing of the " + targetedRegion + " region (primers : " + amplificationPrimers + ")";
						} else if(readSet.typeCode.equalsIgnoreCase("rsnanopore")) {
							//String title = "Nanopore sequencing of the " + targetedRegion + " region (primers : " + amplificationPrimers + ")";
						} else {
							contextValidation.addError("Le readSet " + readSet.code," avec un avec sample de type amplicon et typeCode=" +readSet.typeCode + " non gere");
							continue;
						}
					}
				}
			}
		}	
		//logger.debug("XXXXXXXXXXX sortie de validateReadSetCodesForSubmission pour les readsets avec contextValidation:");
		
	}


	private Submission createSubmissionEntityForPrimarySubmission(Configuration config, 
			String studyCode, 
			String studyAc, 
			String user,
			String dropBox) throws SraException{
		//logger.debug("Dans createSubmissionEntityForPrimarySubmission");
		if (config == null) {
			throw new SraException("SubmissionServices::createSubmissionEntity::configuration : config null ???");
		}
		Submission submission = null;
		//		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");	
		Date courantDate = new java.util.Date();
		//		String st_my_date = dateFormat.format(courantDate);	
		submission = new Submission(user);
		submission.traceInformation.creationDate = courantDate;
		//logger.debug("submissionCode="+ submission.code);
		submission.state = new State(SUB_N, user);
		submission.type = Submission.Type.CREATION;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.defaultRawData;
		submission.configCode = config.code;
		submission.dropBox = dropBox;

		//      Ne pas determiner submissionDirectory et ebiResult car submission.code non definit à ce stade.
		//		submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code; 	
		//		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		// Creation de la map deportéé dans methode initPrimarySubmission pour ne mettre dans la map
		// que les noms de clones utilisee par la soumission

		switch (config.strategyStudy) {
		case STRATEGY_AC_STUDY :
			break;
		case STRATEGY_CODE_STUDY :		
			Study study = studyAPI.dao_getObject(studyCode);
			if (study != null){
				if (study.state == null) {
					throw new SraException("study.state == null incompatible avec soumission. =>  study.state.code in ('" + SUB_N + "','" + SUB_F + "')");
				}
			}
			break;
		default:
			throw new SraException("strategyStudy non attendue : " + config.strategyStudy);
		}
		return submission;
	}



	public String getAmpliconExperimentTitle(ReadSet readSet) {

		String sampleTypeCode = readSet.sampleOnContainer.sampleTypeCode;
		String title = "";


		if (sampleTypeCode.equalsIgnoreCase("amplicon")) {
			String targetedRegion;
			if ( ! readSet.sampleOnContainer.properties.containsKey("targetedRegion") ) {
				throw new SraException("Absence du champs sampleOnContainer.properties.targetedRegion pour le readset " + readSet.code);
			}
			targetedRegion = (String) readSet.sampleOnContainer.properties.get("targetedRegion").getValue();
			if(StringUtils.isBlank(targetedRegion)) {
				throw new SraException("Absence de valeur pour le champs sampleOnContainer.properties.targetedRegion pour le readset " + readSet.code);	
			}
			String amplificationPrimers;
			if ( ! readSet.sampleOnContainer.properties.containsKey("amplificationPrimers") ) {
				throw new SraException("Absence du champs sampleOnContainer.properties.amplificationPrimers pour le readset " + readSet.code);
			}
			amplificationPrimers = (String) readSet.sampleOnContainer.properties.get("amplificationPrimers").getValue();
			if(StringUtils.isBlank(amplificationPrimers)) {
				throw new SraException("Absence de valeur pour le champs sampleOnContainer.properties.amplificationPrimers pour le readset " + readSet.code);	
			}
			String ncbiScientificName = readSet.sampleOnContainer.ncbiScientificName;
			if(StringUtils.isBlank(ncbiScientificName)) {
				throw new SraException("Absence de valeur pour le champs sampleOnContainer.ncbiScientificName pour le readset " + readSet.code);	
			}
			//		if(readSet.typeCode.equalsIgnoreCase("rsillumina")) {
			//			title = "Illumina sequencing of the " + targetedRegion + " region (primers : " + amplificationPrimers + ")";
			//		} else if(readSet.typeCode.equalsIgnoreCase("rsnanopore")) {
			//			title = "Nanopore sequencing of the " + targetedRegion + " region (primers : " + amplificationPrimers + ")";
			//		} else {
			//			throw new SraException("readset " + readSet.code + " avec sample de type amplicon et typeCode=" +readSet.typeCode);		
			//		}
			amplificationPrimers = amplificationPrimers.replaceAll("\\s+$", "");
			amplificationPrimers = amplificationPrimers.replaceAll("^\\s+$", "");
			if(amplificationPrimers.endsWith("primers")) {
				title = targetedRegion + " amplification for " + ncbiScientificName +  " (" + amplificationPrimers + ")";
			} else {
				title = targetedRegion + " amplification for " + ncbiScientificName +  " (" + amplificationPrimers + " primers)";
			}
		} 
		return title;
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
		experiment.code = sraCodeHelper.generateExperimentCode(readSet.code);
		experiment.readSetCode = readSet.code;
		experiment.projectCode = readSet.projectCode;
		experiment.traceInformation.setTraceInformation(user);
		//logger.debug("expCode =" + experiment.code);
		String laboratoryRunCode = readSet.runCode;
		//logger.debug("laboratoryRunCode =" + laboratoryRunCode);

		models.laboratory.run.instance.Run  laboratoryRun = laboRunAPI.get(laboratoryRunCode);
		Map<String, PropertyValue> sampleOnContainerProperties = readSet.sampleOnContainer.properties;

		String libProcessTypeCodeVal = (String) sampleOnContainerProperties.get("libProcessTypeCode").getValue();
		String categoryCode = "";
		//logger.debug("libProcessTypeCodeVal = "+libProcessTypeCodeVal);
		
		if (readSet.typeCode.equalsIgnoreCase("rsillumina")){
			categoryCode = "Illumina";
		} else if (readSet.typeCode.equalsIgnoreCase("rsnanopore")){
			categoryCode = "oxford_nanopore";
		} else if (readSet.typeCode.equalsIgnoreCase("rspacbio")){
			categoryCode = "pacbio_smrt";
		} else if (readSet.typeCode.equalsIgnoreCase("rsmgi")){
			categoryCode = "dnbseq";  // pour mgi
		} else {
			throw new SraException("readset.typeCode inconnu " + readSet.typeCode);
		}
		if(StringUtils.isBlank(libProcessTypeCodeVal)) {
			throw new SraException("Pas de champs libProcessTypeCodeVal pour le readset " + readSet.code);
		}
		if(StringUtils.isBlank(readSet.sampleCode)) {
			throw new SraException("Pas de champs sampleCode pour le readset " + readSet.code);
		}
		experiment.typePlatform = categoryCode.toLowerCase();
		// modification construction titre voir NGL-4770 plus bas
		//experiment.title = scientificName + "_" + categoryCode + "_" + libProcessTypeCodeVal;
		experiment.libraryName = readSet.sampleCode + "_" + libProcessTypeCodeVal;			

		//logger.debug("typePlatform="+ experiment.typePlatform);
		//logger.debug("title="+ experiment.title);
		//logger.debug("libraryName="+ experiment.libraryName);

		if(laboratoryRun==null){
			throw new SraException("Pas de laboratoryRun pour " + readSet.code);
		}
		if (laboratoryRun.instrumentUsed == null ){
			throw new SraException("Pas de champs instrumentUsed pour le laboratoryRun " + laboratoryRun.code);
		} 
//		//logger.debug("Recuperation de instrumentUsed = "+ laboratoryRun.instrumentUsed);
		InstrumentUsed instrumentUsed = laboratoryRun.instrumentUsed;
		//String laboratoryRunTypeCode = laboratoryRun.typeCode;
		
		//logger.debug(" !!!!!!!!! instrumentUsed.code = " + instrumentUsed.code);
		//logger.debug("!!!!!!!!!!!! instrumentUsed.typeCode = '" + instrumentUsed.typeCode+"'");
		//logger.debug("!!!!!!!!! instrumentUsed.typeCodeMin = '" + instrumentUsed.typeCode.toLowerCase()+"'");
		experiment.instrumentModel = instrumentUsed.typeCode.toLowerCase();
		
		// mieux de prendre run.typeCode plutot que run.instrumentUsed.typeCode car run.typeCode apparait dans description 
		// et plus facile pour mettre collection sraParameter à jour.
		experiment.instrumentModel = VariableSRA.mapInstrumentModel().get(instrumentUsed.typeCode.toLowerCase());
		
		if (StringUtils.isBlank(experiment.instrumentModel)) {
			throw new SraException("Pas de correspondance existante pour instrumentUsed.typeCodeMin = '" + instrumentUsed.typeCode.toLowerCase()+"'");	
		}
		if(readSet.sampleOnContainer.sampleTypeCode!=null && readSet.sampleOnContainer.sampleTypeCode.equalsIgnoreCase("amplicon")) {
			// Definir un titre dans le cas des readset de type amplicon :
			experiment.title = getAmpliconExperimentTitle(readSet);
		} else {	
			// NGL-4770 : construction automatique du titre en fonction de typeCode et instrumentModel, 
			// en mettant dans le titre le nom de l'instrument au CNS et non celui connu à l'EBI
			// Rq : on met dans l'experiment le nom de l'instrument de l'EBI.
			String experimentTitleKey = categoryCode + "_" + libProcessTypeCodeVal;
			experiment.title = VariableSRA.mapExperimentTitle().get(experimentTitleKey.toLowerCase());
			if (StringUtils.isBlank(experiment.title)) {
				throw new SraException("Pas de construction automatique du titre possible pour experiment = " + experiment.code + " avec categoryCode = " + categoryCode + " et libProcessTypeCodeVal = " + libProcessTypeCodeVal);	
			}
			String cnsNameInstrumentModel = VariableSRA.mapCnsNameInstrumentModel().get(instrumentUsed.typeCode.toLowerCase());
			if (StringUtils.isBlank(cnsNameInstrumentModel)) {
				throw new SraException("Pas d'entree dans sraParameters.cnsNameInstrumentModel pour instrumentUsed.typeCode = " + instrumentUsed.typeCode + " pour experiment = " + experiment.code);	
			}
			experiment.title = experiment.title.replaceFirst("INSTRUMENT", cnsNameInstrumentModel);
		}
		if(categoryCode.equals("Illumina")) {
			experiment.libraryLayoutNominalLength = getIlluminaLibraryLayoutNominalLength(readSet);
			experiment.spotLength = getIlluminaSpotLength(readSet);
			experiment.lastBaseCoord = getIlluminaLastBaseCoord(readSet);
			informsLibraryLayoutAndOrientation(readSet, experiment);
			informsIlluminaReadSpec(readSet, experiment);
		} else {
			// Pas de spot_descriptor si oxford_nanopore (ONT) ou pacbio_smrt ou mgi
			// renseigner libraryLayout et orientation
			informsLibraryLayoutAndOrientation(readSet, experiment);
		}
		experiment.libraryConstructionProtocol = VariableSRA.defaultLibraryConstructionProtocol;
		experiment.run = createRunEntity(readSet);
		experiment.run.expCode=experiment.code;
		experiment.state = new State(SUB_N, user); 
		return experiment;
	}
	
	// transforme string en liste de String en prenant ',' comme separateur.
	private List<String> getListString(String str) {
		List<String> list = null;
		if(StringUtils.isNotBlank(str)) {
			// oter tous les espaces
			str = str.replaceAll("\\s+", "");
			// oter , terminale si besoin :
			str = str.replaceAll(",$", "");
			list = Arrays.asList(str.split(","));
//			for (String elt : list) {
//				//logger.debug("ZZZZZZZZZZZZZ   elt={}", elt);
//			}
			//si chaine ne contient pas de , alors ok
		}	
		return list;
	}
	//obsolete ?
	// crée le study et le sauve en base avec son code
	public Study createAndSaveStudyEntity(List<String> projectCodes, String existingStudyType, String centerProjectName, 
			                              String title, String description, String dropBox, 
			                              String strTaxonId, String strIdsPubmeds, String strLocusTagPrefixs, String strParentProjectAccessions,
			                              String strAttributes, ContextValidation contextValidation) 
					                      throws SraException, SraValidationException, IOException, APIValidationException, APIException {
		
		Study study = createStudyEntity(projectCodes, existingStudyType, centerProjectName, title, description, dropBox, 
					 strTaxonId,  strIdsPubmeds,  strLocusTagPrefixs,  strParentProjectAccessions,
					 strAttributes,  contextValidation);
		study.validate(contextValidation);
		if (! contextValidation.hasErrors()) {
			//logger.debug("Aucune erreur dans study !!!!!!!!");
			while (! studyAPI.dao_checkObjectExist("code", study.code)) {
				study.code = sraCodeHelper.generateStudyCode();
			}
			studyAPI.dao_saveObject(study);
		} else {
			contextValidation.displayErrors(logger, "debug");
			//logger.debug("Erreur dans study");
		}
		return studyAPI.get(study.code);	

	}

	
	// retourne le study avec code, sans le sauver dans base.
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
		//logger.debug("YYYYYYYYYYYYYYYYYYYYYYYY dans CreateServices.createAndSaveStudyEntity");
		contextValidation.setCreationMode();
		String user = contextValidation.getUser();	
		Study study = new Study();
		study.projectCodes = new ArrayList<String>();
		if(projectCodes != null) {
			study.projectCodes.addAll(projectCodes);
		}
		study.existingStudyType = existingStudyType;
		study.centerProjectName = centerProjectName;
		study.title = title;
		study.description = description;
		study.dropBox = dropBox;
		// NGL-4593
		if(StringUtils.isBlank(study.dropBox)) {
			throw new SraException("createStudyEntity", " dropBox du study "+ study.code + " a null");
		}
		study.taxonId = strTaxonId;
		// synchroniser description et abstract et taxonId et scientificName
		submissionNewAPITools.synchronyseValuesInStudy(study, contextValidation);
		//logger.debug("Apres synchronyseValuesInStudy");
		if(StringUtils.isNotBlank(strIdsPubmeds)) {
			//logger.debug("strIdsPubmeds={}", strIdsPubmeds);
			List<String> list = getListString(strIdsPubmeds);
			if(list != null) {
				study.idsPubmed = new ArrayList<String>();
				study.idsPubmed.addAll(list);
//				for(String idPubmed : study.idsPubmed) {
//					//logger.debug("dans liste         idPubmed={}", idPubmed);
//				}
			}
		}
		if(StringUtils.isNotBlank(strLocusTagPrefixs)) {
			//logger.debug("strLocusTagPrefixs={}", strLocusTagPrefixs);
			List<String> list = getListString(strLocusTagPrefixs);
			if(list != null) {
				study.locusTagPrefixs = new ArrayList<String>();
				study.locusTagPrefixs.addAll(list);
			}
		}
		if(StringUtils.isNotBlank(strParentProjectAccessions)) {
			//logger.debug("strParentProjectAccessions={}", strParentProjectAccessions);
			List<String> list = getListString(strParentProjectAccessions);
			if(list != null) {
				study.parentProjectAccessions = new ArrayList<String>();
				study.parentProjectAccessions.addAll(list);
			}
		}
		if(StringUtils.isNotBlank(strAttributes)) {
			//logger.debug("strAttributes=SraValidationHelper{}", strAttributes);
			study.attributes = strAttributes;
		}
		study.state = new State(NONE, user);
		study.traceInformation = new TraceInformation(user);
		study.traceInformation.createUser = user;
		study.traceInformation.creationDate = new java.util.Date();
		//logger.debug("study.traceInformation.creationDate={}", study.traceInformation.creationDate);
		study.code = sraCodeHelper.generateStudyCode();
		while (studyAPI.dao_checkObjectExist("code", study.code)) {
			study.code = sraCodeHelper.generateStudyCode();
		}
		return study;
	}

	public Sample createSampleEntity(String projectCode, 
									 int    taxonId, 
									 String attributes, 
									 String dropbox, 
									 String title, 
									 String anonymizedName, 
									 ContextValidation contextValidation) {
		contextValidation.setCreationMode();
		String user = contextValidation.getUser();	
		Sample sample = new Sample();
		sample.projectCode = projectCode;
		sample.taxonId = taxonId;
		sample.attributes = attributes;
		sample.dropBox = dropbox;
		sample.title = title;
		sample.anonymizedName = anonymizedName;
		logger.debug("projectCode={}, dropBox={}, title={}, anonymizedName={}, taxonId={}", sample.projectCode, sample.dropBox, sample.title, sample.anonymizedName, sample.taxonId);
		// NGL-4593
		if(StringUtils.isBlank(sample.dropBox)) {
			throw new SraException("createSampleEntity", " dropBox du sample "+ sample.code + " a null");
		}
	
		sample.state = new State(NONE, user);
		sample.traceInformation = new TraceInformation(user);
		sample.traceInformation.createUser = user;
		sample.traceInformation.creationDate = new java.util.Date();
		sample.code = sraCodeHelper.generateSampleCode(sample.projectCode, sample.taxonId, null);
		
		if(sampleAPI.dao_checkObjectExist("code", sample.code)) {
			throw new SraException("createSampleEntity", "Il existe déjà un sample " + sample.code + " dans la base");
		}
		return sample;
	}
	
	
	public void informsLibraryLayoutAndOrientation(ReadSet readSet, Experiment experiment) {

		// cas ou type_code=Illumina :			
		// Ajouter les read_spec (dans SPOT_DESCRIPTOR ) en fonction de l'information SINGLE ou PAIRED et forward-reverse et last_base_coord :
		// les rsnanopore sont normalement des single forward.
		experiment.libraryLayout = null;
		experiment.libraryLayoutOrientation = null;
		models.laboratory.run.instance.Run  laboratoryRun = laboRunAPI.get(readSet.runCode);
		Map<String, PropertyValue> sampleOnContainerProperties = readSet.sampleOnContainer.properties;
		Map<String, String> mapLibProcessTypeCodeVal_orientation = VariableSRA.mapLibProcessTypeCodeVal_orientation();

		if( readSet.typeCode.equalsIgnoreCase("rsnanopore") || readSet.typeCode.equalsIgnoreCase("rspacbio")) {
			experiment.libraryLayout = "SINGLE";
			experiment.libraryLayoutOrientation = VariableSRA.mapLibraryLayoutOrientation().get("forward");
			return;
		}
		if ( ! readSet.typeCode.equalsIgnoreCase("rsillumina") && ! readSet.typeCode.equalsIgnoreCase("rsmgi")) {
			throw new SraException("readset.typeCode inconnu " + readSet.typeCode);
		}
		if (laboratoryRun.properties.containsKey("sequencingProgramType")){	
			String libraryLayout =  (String) laboratoryRun.properties.get("sequencingProgramType").value;

			if (StringUtils.isNotBlank(libraryLayout)) { 
				if (libraryLayout.equalsIgnoreCase("SR")){
					experiment.libraryLayout = "SINGLE";
					experiment.libraryLayoutOrientation = VariableSRA.mapLibraryLayoutOrientation().get("forward");
				} else if( libraryLayout.equalsIgnoreCase("PE") || libraryLayout.equalsIgnoreCase("MP")) {
					experiment.libraryLayout = "PAIRED";
					//Map<String, PropertyValue> sampleOnContainerProperties = readSet.sampleOnContainer.properties;

					//					if (sampleOnContainerProperties != null) {
					//						//Set <String> listKeysSampleOnContainerProperties = sampleOnContainerProperties.keySet();  // Obtenir la liste des clés
					//						/*for(String k: listKeysSampleOnContainerProperties){
					//						logger.debug("cle = " + k);
					//						PropertyValue propertyValue = sampleOnContainerProperties.get(k);
					//						logger.debug(propertyValue.toString());
					//						logger.debug(", value  => "+propertyValue.value);
					//					} 

					if (sampleOnContainerProperties.containsKey("libProcessTypeCode")) {					
						PropertyValue libProcessTypeCode = sampleOnContainerProperties.get("libProcessTypeCode");
						String libProcessTypeCodeValue = (String) libProcessTypeCode.value;
						if (mapLibProcessTypeCodeVal_orientation.get(libProcessTypeCodeValue)==null){
							throw new SraException("Pour le readSet " + readSet.code +  ", valeur de libProcessTypeCodeValue inconnue :" + libProcessTypeCodeValue);
						} else {
							experiment.libraryLayoutOrientation = mapLibProcessTypeCodeVal_orientation.get(libProcessTypeCodeValue);
						}
					}
				} else {
					//logger.debug("Pour le laboratoryRun " + laboratoryRun.code + " valeur de properties.sequencingProgramType differente de SR ou PE => " + libraryLayout);
					throw new SraException("Pour le laboratoryRun " + laboratoryRun.code + " valeur de properties.sequencingProgramType differente de SR ou PE => " + libraryLayout);
				}
			}
			//logger.debug("libraryLayout======"+libraryLayout);
		}
	}


	public void informsIlluminaReadSpec(ReadSet readSet, Experiment experiment){

		//List<ReadSpec> readSpecs = new ArrayList<>();
		if( ! readSet.typeCode.equalsIgnoreCase("rsillumina")) {
			return;
		}
		if(StringUtils.isBlank(experiment.libraryLayout)) {
			throw new SraException("experiment " +  experiment.code + " sans libraryLayout");
		}
		if (! experiment.libraryLayout.equalsIgnoreCase("SINGLE") && ! experiment.libraryLayout.equalsIgnoreCase("PAIRED")) { 
			throw new SraException("experiment " +  experiment.code + " avec libraryLayout non attendu " + experiment.libraryLayout);
		}
		if(StringUtils.isBlank(experiment.libraryLayoutOrientation)) {
			throw new SraException("experiment " +  experiment.code + " sans libraryLayoutOrientation");
		}
		if (! experiment.libraryLayoutOrientation.equalsIgnoreCase("forward-reverse") && ! experiment.libraryLayoutOrientation.equalsIgnoreCase("reverse-forward")) { 
			throw new SraException("experiment " +  experiment.code + " avec libraryLayoutOrientation non attendu " + experiment.libraryLayoutOrientation);
		}		
		// IF ILLUMINA ET SINGLE  Attention != si nanopore et SINGLE
		if (StringUtils.isNotBlank(experiment.libraryLayout) && experiment.libraryLayout.equalsIgnoreCase("SINGLE") ) {
			ReadSpec readSpec_1 = new ReadSpec();
			readSpec_1.readIndex = 0; 
			readSpec_1.readLabel = "F";
			readSpec_1.readClass = "Application Read";
			readSpec_1.readType = "Forward";
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
			//			readSpec_1.baseCoord = (Integer) 1;
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
	}

	public Integer getIlluminaLibraryLayoutNominalLength(ReadSet readSet) {

		Integer libraryLayoutNominalLength = null;
		if( ! readSet.typeCode.equalsIgnoreCase("rsillumina")) {
			return libraryLayoutNominalLength;
		}
		// Rechercher libraryLayoutNominalLength pour les single illumina (paired)
		// mettre la valeur calculée de libraryLayoutNominalLength
		models.laboratory.run.instance.Treatment treatmentMapping = readSet.treatments.get("mapping");
		if (treatmentMapping != null) {
			//logger.debug("treatmentMapping pour " + experiment.code);
			Map <String, Map<String, PropertyValue>> resultsMapping = treatmentMapping.results();
			if ( resultsMapping != null && (resultsMapping.containsKey("pairs"))){
				Map<String, PropertyValue> pairs = resultsMapping.get("pairs");
				if (pairs != null) {
					//						Set <String> listKeysMapping = pairs.keySet();  // Obtenir la liste des clés
					//						for(String k: listKeysMapping) {
					//							//logger.debug("coucou cle = '" + k+"'  => ");
					//							PropertyValue propertyValue = pairs.get(k);
					//							//logger.debug(propertyValue.value);
					//						}
					if (pairs.containsKey("estimatedPEInsertSize")) {
						PropertyValue estimatedInsertSize = pairs.get("estimatedPEInsertSize");
						libraryLayoutNominalLength = (Integer) estimatedInsertSize.value;
						//logger.debug("valeur calculee libraryLayoutNominalLength  => "  + experiment.libraryLayoutNominalLength);
					} 
					if (pairs.containsKey("estimatedMPInsertSize")) {
						PropertyValue estimatedInsertSize = pairs.get("estimatedMPInsertSize");
						libraryLayoutNominalLength = (Integer) estimatedInsertSize.value;
						//logger.debug("valeur calculee libraryLayoutNominalLength  => "  + experiment.libraryLayoutNominalLength);
					}	
				}
			} 
		}

		if (libraryLayoutNominalLength == null) {
			// mettre valeur theorique de libraryLayoutNominalLength (valeur a prendre dans readSet.sampleOnContainer.properties.nominalLength) 
			// voir recup un peu plus bas:
			Map<String, PropertyValue> sampleOnContainerProperties = readSet.sampleOnContainer.properties;
			if (sampleOnContainerProperties.containsKey("libLayoutNominalLength")) {	
				//logger.debug("************** recherche valeur theorique possible pour " + experiment.code );
				PropertyValue nominalLengthTypeCode = sampleOnContainerProperties.get("libLayoutNominalLength");
				Integer nominalLengthCodeValue = (Integer) nominalLengthTypeCode.value;
				if ((nominalLengthCodeValue != null) && (nominalLengthCodeValue!= -1)){
					libraryLayoutNominalLength = nominalLengthCodeValue;
					//logger.debug("************ valeur theorique libraryLayoutNominalLength  => "  + experiment.libraryLayoutNominalLength);
				}
			}
		}

		return libraryLayoutNominalLength;
	}

	public Integer getIlluminaLastBaseCoord (ReadSet readSet) {
		Integer lastBaseCoord = null;
		if( ! readSet.typeCode.equalsIgnoreCase("rsillumina")) {
			return lastBaseCoord;
		}
		models.laboratory.run.instance.Run  laboratoryRun = laboRunAPI.get(readSet.runCode);
		models.laboratory.run.instance.Treatment treatmentNgsrg = (laboratoryRun.treatments.get("ngsrg"));
		if (treatmentNgsrg != null) {
			Map <String, Map<String, PropertyValue>> resultsNgsrg = treatmentNgsrg.results();
			if (resultsNgsrg != null && resultsNgsrg.containsKey("default")) {
				Map<String, PropertyValue> ngsrg = resultsNgsrg.get("default");
				//					Set <String> listKeys = ngsrg.keySet();  // Obtenir la liste des clés
				/*for(String k: listKeys){
				logger.debug("cle = " + k);
				PropertyValue propertyValue = ngsrg.get(k);
				//logger.debug(propertyValue.toString());
				logger.debug(", value  => "+propertyValue.value);
			} */

				if (ngsrg.get("nbCycleRead1") != null && ngsrg.get("nbCycleRead1").value != null) {
					//	logger.debug(("lastBaseCoord " + experiment.lastBaseCoord));
					lastBaseCoord =  (Integer) ngsrg.get("nbCycleRead1").value + 1;
				}
			}
		}

		// Si lastBaseCoord non trouvé au niveau du run alors recuperer les lanes associées au
		// run associé au readSet et recuperer le lane contenant le readSet.code. C'est dans les
		// traitement de cette lane que se trouve l'information:
		// lastBaseCoord trouvé au niveau du run si recent ou run exterieur, et trouvé au niveau des lanes si runs plus anciens.
		if (lastBaseCoord == null ) {
			List<Lane> laboratoryLanes = laboratoryRun.lanes;
			if (laboratoryLanes != null) {
				//logger.debug("c'est la '"+readSet.code+"'");
				boucle_1 :
					for (Lane ll : laboratoryLanes) {
						if (ll != null) {
							List<String> readSetCodes = ll.readSetCodes;
							for (String rsc : readSetCodes) {
								//logger.debug("rsc =  "+ rsc);
								if (rsc != null && rsc.equalsIgnoreCase(readSet.code)){
									// bonne lane = lane correspondant au run associé au readSet
									//logger.debug("lane correspondant au run associé au readset");
									if (ll.treatments==null) {
										//logger.debug("lane sans traitement ?");
									}
									Map<String, Map<String, PropertyValue>> laneResults = null;
									Map<String, PropertyValue> lanengsrg = null;
									models.laboratory.run.instance.Treatment laneTreatment = null;
									if (ll.treatments != null) {
										laneTreatment = (ll.treatments.get("ngsrg"));
									}
									if (laneTreatment != null) {
										laneResults = laneTreatment.results();
									} 
									if (laneResults != null) {
										lanengsrg = laneResults.get("default");
										//								Set<String> laneListKeys = lanengsrg.keySet();  // Obtenir la liste des clés
										//								for(String k: laneListKeys) {
										//									logger.debug("attention cle = " + k);
										//									PropertyValue propertyValue = lanengsrg.get(k);
										//									logger.debug(propertyValue.toString());
										//									logger.debug(propertyValue.value);
										//								}
										if (lanengsrg != null) {
											//logger.debug("lanengsrg != null");
											if (lanengsrg.get("nbCycleRead1") != null) {
												if (lanengsrg.get("nbCycleRead1").value != null) {
													//	logger.debug(("lastBaseCoord " + experiment.lastBaseCoord));
													lastBaseCoord =  (Integer) lanengsrg.get("nbCycleRead1").value + 1;
												}
											}
										}
									}
									break boucle_1;
								}
							}
							//logger.debug("lane non null");
						} else {
							//logger.debug("Run avec une lane null ?");
						}
						//				logger.debug("c'est la sortie la  plus interne '" + readSet.code + "'");
					}
			//			logger.debug("c'est la sortie 2 '" + readSet.code + "'");
			}
		}
		return lastBaseCoord;
	}


	public Long getIlluminaSpotLength(ReadSet readSet) {
		Long spotLength = null;
		if( ! readSet.typeCode.equalsIgnoreCase("rsillumina")) {
			return spotLength;
		}
		models.laboratory.run.instance.Run  laboratoryRun = laboRunAPI.get(readSet.runCode);
		models.laboratory.run.instance.Treatment treatmentNgsrg = (laboratoryRun.treatments.get("ngsrg"));
		if (treatmentNgsrg != null) {
			Map <String, Map<String, PropertyValue>> resultsNgsrg = treatmentNgsrg.results();
			if (resultsNgsrg != null && resultsNgsrg.containsKey("default")) {
				Map<String, PropertyValue> ngsrg = resultsNgsrg.get("default");
				//Set <String> listKeys = ngsrg.keySet();  // Obtenir la liste des clés
				/*for(String k: listKeys){
					logger.debug("cle = " + k);
					PropertyValue propertyValue = ngsrg.get(k);
					//logger.debug(propertyValue.toString());
					logger.debug(", value  => "+propertyValue.value);
				} */
				PropertyValue propertyNbCycle = ngsrg.get("nbCycle");
				if (ngsrg.get("nbCycle") != null){
					spotLength = (Long) propertyNbCycle.value;
				}
			}
		}
		return spotLength;
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
			//			RuntimeException e = new RuntimeException("Aucun fichier pour le readSet " + readSet.code +"???");
			//			logger.debug("error", e);
			//logger.debug("Aucun fichier pour le readSet " + readSet.code +"???");		
			throw new SraException("Aucun fichier pour le readSet " + readSet.code +"???");
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
				//logger.debug("dataDir "+dataDir);
				rawData.directory = dataDir.replaceFirst("\\/$", ""); // oter / terminal si besoin
				//logger.debug("raw data directory"+rawData.directory);
				rawData.relatifName = runInstanceFile.fullname;

				// mettre dans rawData.collabFileName le relatifName par defaut, ou collabFileName si existe : 
				rawData.collabFileName = rawData.relatifName;	// nom du fichier 				
				if(runInstanceFile.properties.containsKey("collabFileName")) {
					PropertyValue propertyValue = runInstanceFile.properties.get("collabFileName");
					String collabFileName = (String) propertyValue.value;
					if(StringUtils.isNotBlank(collabFileName)) {
						rawData.collabFileName = collabFileName;
					}
				}

				if (rawData.collabFileName.endsWith("fastq")) {
					rawData.collabFileName = rawData.collabFileName.concat(".gz");
				} 
				rawData.location = readSet.location;
				if (runInstanceFile.properties != null && runInstanceFile.properties.containsKey("md5")) {
					rawData.md5 = (String) runInstanceFile.properties.get("md5").value;
					//logger.debug("Recuperation du md5 pour" + rawData.relatifName +"= " + rawData.md5);
				} else {
					logger.error("NGL-SUB_TICKET_JOE A CREER : Attention, dans readset.Illumina." +  readSet.code + " : aucun md5 pour le fichier à soumettre " + rawData.relatifName );
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



	public ExternalSample buildExternalSample(String sampleAc, String user) throws SraException {
		//logger.debug("Entree dans buildExternalSample");
		ExternalSample externalSample = new ExternalSample(); // objet avec state.code = submitted
		//logger.debug("Creation dans base du sample avec Ac " + sampleAc);
		String externalSampleCode = sraCodeHelper.generateExternalSampleCode(sampleAc);
		if(sampleAc.matches(patternSampleAc)) {
			externalSample.accession = sampleAc;
		} else if (sampleAc.startsWith(patternSampleExternalId)) {
			externalSample.externalId = sampleAc;
		} else {
			throw new SraException("identifiant du sample "+ sampleAc +" non reconnu");
		}
		externalSample.code = externalSampleCode;
		externalSample.traceInformation.setTraceInformation(user);	
		externalSample.state = new State(SUB_F, user);
		externalSample.consolidatedPublicStatus = true;
		//logger.debug("externalSample.accession = " + externalSample.accession);
		//logger.debug("externalSample.code = " + externalSample.code);
		//logger.debug("externalSample.state.code = " + externalSample.state.code);
		return externalSample;
	}

	//NGL-4266 :
	private String buildAttributes(ReadSet readSet) throws SraException {

		String sampleAttributes = "";

		if ( readSet.sampleOnContainer.properties.containsKey("collectDate") ) {
			//logger.debug("2025 collectDate ok");

			Date valueCollectionDate = (Date) readSet.sampleOnContainer.properties.get("collectDate").getValue();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = formatter.format(valueCollectionDate);
			//logger.debug("2025 strDate " + strDate);

			if(StringUtils.isNotBlank(strDate)) {
				sampleAttributes = sampleAttributes.concat("<SAMPLE_ATTRIBUTE><TAG>collection date</TAG><VALUE>" + strDate + "</VALUE></SAMPLE_ATTRIBUTE>\n");
			}
		}
		if ( readSet.sampleOnContainer.properties.containsKey("geographicLocation") ) {
			String valueGeographicLocation = (String) readSet.sampleOnContainer.properties.get("geographicLocation").getValue();
			if(StringUtils.isNotBlank(valueGeographicLocation)) {
				sampleAttributes = sampleAttributes.concat("<SAMPLE_ATTRIBUTE><TAG>geographic location (country and/or sea)</TAG><VALUE>" + valueGeographicLocation + "</VALUE></SAMPLE_ATTRIBUTE>\n");
			}
		}	
		if ( readSet.sampleOnContainer.properties.containsKey("geographicRegion") ) {
			String valueGeographicRegion = (String) readSet.sampleOnContainer.properties.get("geographicRegion").getValue();
			if(StringUtils.isNotBlank(valueGeographicRegion)) {
				sampleAttributes = sampleAttributes.concat("<SAMPLE_ATTRIBUTE><TAG>geographic location (region and locality)</TAG><VALUE>" + valueGeographicRegion + "</VALUE></SAMPLE_ATTRIBUTE>\n");
			}
		}	
		//logger.debug("2025 sampleAttributes=" + sampleAttributes);
		return sampleAttributes;
	}


	private Sample fetchSample(ReadSet readSet, StrategySample strategySample, String scientificName, String user, String dropBox) throws SraException {
		// Recuperer pour chaque readSet les objets de laboratory qui existent forcemment dans mongoDB, 
		// et qui permettront de renseigner nos objets SRA :

		//logger.debug(" 2025  XXXXXXXXXXXXXXXXXXXX          dans fetchSample readsetCode = " + readSet.code);
		String codeSample = sraCodeHelper.generateSampleCode(readSet, readSet.projectCode, strategySample);
		//logger.debug(" 2025 2 XXXXXXXXXXXXXXXXXXXX          dans fetchSample readsetCode = " + readSet.code);

		if (readSet.sampleOnContainer == null) {
			throw new SraException("Absence du champs sampleOnContainer pour le readset " + readSet.code);
		}
		if(readSet.sampleOnContainer.sampleTypeCode == null) {
			throw new SraException("Absence du champs sampleOnContainer.sampleTypeCode pour le readset " + readSet.code);
		}

		
		String taxonId = readSet.sampleOnContainer.taxonCode;
		String sampleTypeCode = readSet.sampleOnContainer.sampleTypeCode;
		String targetedRegion;
		String amplificationPrimers;
		String title = "";
		//logger.debug(" 2025  3  XXXXXXXXXXXXXXXXXXXX          dans fetchSample readsetCode = " + readSet.code);

		if (sampleTypeCode.equalsIgnoreCase("amplicon")) {
			if ( ! readSet.sampleOnContainer.properties.containsKey("targetedRegion") ) {
				throw new SraException("Absence du champs sampleOnContainer.properties.targetedRegion pour le readset " + readSet.code);
			}
			targetedRegion = (String) readSet.sampleOnContainer.properties.get("targetedRegion").getValue();
			if(StringUtils.isBlank(targetedRegion)) {
				throw new SraException("Absence de valeur pour le champs sampleOnContainer.properties.targetedRegion pour le readset " + readSet.code);	
			}
			if ( ! readSet.sampleOnContainer.properties.containsKey("amplificationPrimers") ) {
				throw new SraException("Absence du champs sampleOnContainer.properties.amplificationPrimers pour le readset " + readSet.code);
			}
			amplificationPrimers = (String) readSet.sampleOnContainer.properties.get("amplificationPrimers").getValue();
			if(StringUtils.isBlank(amplificationPrimers)) {
				throw new SraException("Absence de valeur pour le champs sampleOnContainer.properties.amplificationPrimers pour le readset " + readSet.code);	
			}
			if(readSet.typeCode.equalsIgnoreCase("rsillumina")) {
				title = "Illumina sequencing of the " + targetedRegion + " region (primers : " + amplificationPrimers + ")";
			} else if(readSet.typeCode.equalsIgnoreCase("rsnanopore")) {
				title = "Nanopore sequencing of the " + targetedRegion + " region (primers : " + amplificationPrimers + ")";
			} else {
				throw new SraException("readset " + readSet.code + " avec sample de type amplicon et typeCode=" +readSet.typeCode);		
			}
		}

		//logger.debug(" 2025  4  XXXXXXXXXXXXXXXXXXXX          dans fetchSample readsetCode = " + readSet.code);

		// Si sample existe, prendre l'existant, sinon en creer un nouveau
		Sample sample = null;
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
			sample.dropBox = dropBox;
			//logger.debug(" 2025  5  XXXXXXXXXXXXXXXXXXXX          dans fetchSample readsetCode = " + readSet.code);

			//NGL-4266 :
			String sampleAttributes = buildAttributes(readSet);
			if(StringUtils.isNotBlank(sampleAttributes)) {
				sample.attributes = sampleAttributes;
			}
			//logger.debug(" 2025  6  XXXXXXXXXXXXXXXXXXXX          dans fetchSample readsetCode = " + readSet.code);

			sample.projectCode = readSet.projectCode;
			sample.state = new State(SUB_N, user);
			sample.traceInformation.setTraceInformation(user);	

			// indiquez un titre pour les samples dans le cas des readsets avec sampleTypeCode=amplicon
			if(StringUtil.isNotBlank(title)){
				sample.title = title;
			}
		}
		//		logger.debug("readSetCode = {}", readSet.code);
		//		logger.debug("taxonId = {}" , taxonId);
		//		logger.debug("clone = {}" , clone);
		//		logger.debug("typeBanque = {}", libProcessTypeCodeVal);
		return sample;
	}

	public ExternalStudy buildExternalStudy(String studyAc, String user) throws SraException {
		ExternalStudy externalStudy;
		String externalStudyCode = sraCodeHelper.generateExternalStudyCode(studyAc);
		externalStudy = new ExternalStudy(); // objet avec state.code = submitted
		if(studyAc.matches(patternStudyAc)) {
			externalStudy.accession = studyAc;
		} else if (studyAc.startsWith(patternStudyExternalId)) {
			externalStudy.externalId = studyAc;
		} else {
			throw new SraException("identifiant du study "+ studyAc +" non reconnu");
		}
		externalStudy.code = externalStudyCode;
		externalStudy.traceInformation.setTraceInformation(user);	
		externalStudy.state = new State(SUB_F, user);
		externalStudy.consolidatedPublicStatus = true;
		return externalStudy;
	}

	//	public ExternalStudy fetchExternalStudy(String studyAc, String user) throws SraException {
	//		ExternalStudy externalStudy;
	//		if (abstractStudyAPI.dao_checkObjectExist("accession", studyAc)){
	//			// verifier que si objet existe dans base avec cet AC c'est bien un type externalStudy et non un type study
	//			AbstractStudy absStudy;
	////			absStudy = MongoDBDAO.findOne(InstanceConstants.SRA_STUDY_COLL_NAME,
	////				AbstractStudy.class, DBQuery.and(DBQuery.is("accession", studyAc)));
	//			absStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", studyAc));
	//			
	////			if ( ExternalStudy.class.isInstance(absStudy)) {
	//			if (absStudy instanceof ExternalStudy) {
	//				//logger.debug("Recuperation dans base du study avec Ac = " + studyAc +" qui est du type externalStudy ");
	//			} else {
	//				throw new SraException("Recuperation dans base du study avec Ac = " + studyAc +" qui n'est pas du type externalStudy ");
	//			}
	//			externalStudy = externalStudyAPI.dao_findOne(DBQuery.is("accession", studyAc));		
	//		} else {
	//			// Creer objet externalStudy
	//			String externalStudyCode = sraCodeHelper.generateExternalStudyCode(studyAc);
	//			externalStudy = new ExternalStudy(); // objet avec state.code = submitted
	//			externalStudy.accession = studyAc;
	//			externalStudy.code = externalStudyCode;
	//			externalStudy.traceInformation.setTraceInformation(user);	
	//			externalStudy.state = new State(SUB_F, user);
	//		}
	//		return externalStudy;
	//	}

	public void updateConfigurationAndSaveIndb (Configuration config, String submissionStateCode, String user) throws APIValidationException, APIException {
		if (config.state != null && StringUtils.isNotBlank(config.state.code) && NONE.equals(config.state.code)) {
			config.state.code = submissionStateCode;
			config.traceInformation.modifyDate = new Date();
			config.traceInformation.modifyUser = user;
			//configurationAPI.dao_saveObject(config);
			configurationAPI.update(config, user);
		}	
	}


	public void updateStudyAndSaveIndb(Study study, String nextStateCode, String user) throws APIValidationException, APIException {
		if (study != null && study.state != null && StringUtils.isNotBlank(study.state.code) && NONE.equals(study.state.code)) {
			study.setTraceUpdateStamp(user);
			study.setStateHistorique(nextStateCode, user);
			studyAPI.dao_saveObject(study);
			//studyAPI.update(study,  user);
		}
	}

	public void updateSampleAndSaveIndb(Sample sample, String nextStateCode, String user) throws APIValidationException, APIException {
		if (sample != null && sample.state != null && StringUtils.isNotBlank(sample.state.code) && NONE.equals(sample.state.code)) {
			sample.setTraceUpdateStamp(user);
			sample.setStateHistorique(nextStateCode, user);
			sampleAPI.dao_saveObject(sample);
			//sampleAPI.update(sample, user);
		}
	}

	public void updateProjectAndSaveIndb(Project project, String nextStateCode, String user) throws APIValidationException, APIException {
		if (project != null && project.state != null && StringUtils.isNotBlank(project.state.code) && NONE.equals(project.state.code)) {
			project.setTraceUpdateStamp(user);
			project.setStateHistorique(nextStateCode, user);
			projectAPI.dao_saveObject(project);
			//projectAPI.update(project, user);
		}
	}

	public void updateAnalysisAndSaveIndb(Analysis analysis, String nextStateCode, String user) throws APIValidationException, APIException {
		if (analysis != null && analysis.state != null && StringUtils.isNotBlank(analysis.state.code) && NONE.equals(analysis.state.code)) {
			analysis.setTraceUpdateStamp(user);
			analysis.setStateHistorique(nextStateCode, user);
			analysisAPI.dao_saveObject(analysis);
			//analysisAPI.update(analysis, user);
		}
	}

	//	@Deprecated
	//	public void updateSubmissionState(Submission submission, State nextState) {
	//		submission.getTraceInformation().forceModificationStamp(nextState.user, nextState.date);
	//		submission.setState(nextState.createHistory(submission.getState()));
	//	}
	//	

	public void addReadsetIndb(List<Experiment> listExperiments) {
		for (Experiment experiment : listExperiments) {
			Readset readset = new Readset();
			readset.code = experiment.readSetCode;
			readset.type = experiment.typePlatform.toLowerCase();
			readset.experimentCode = experiment.code;
			readset.runCode = experiment.run.code;
			readsetAPI.dao_saveObject(readset);
		}
	}
	public void addReadsetBionanoIndb(List<RawData> listRawData, String analysisCode) {
		for (RawData rawData : listRawData) {
			if(StringUtils.isBlank(rawData.readsetCode)) {
				// cas d'un rawData fichier cmap d'une analyse
				continue;
			}
			Readset readset = new Readset();
			readset.code = rawData.readsetCode;
			readset.type = "bionano";
			readset.analysisCode = analysisCode;
			readsetAPI.dao_saveObject(readset);
		}
	}	
	// methode à utiliser uniquement a la creation d'une soumission car met a jour 
	// le champs ReadSet.submissionUser qui ne doit pas etre ecrase par les differents
	// changement d'etat
	public void updateReadSetAndSaveIndbForCreation (List<ReadSet> readSets, String submissionStateCode, String user) throws SraException {
		// Updater les readSets pour le status dans la base: 
		for (ReadSet readSet : readSets) {
			updateReadSetAndSaveIndbForCreation_1(readSet, submissionStateCode, user);
		}
	}	

	public void updateReadSetAndSaveIndbForCreation_1 (ReadSet readSet, String submissionStateCode, String user) throws SraException {
		// Updater le readSet pour le status dans la base: 
		if (readSet == null) {
			throw new SraException("null readSet in read sets (should not happen)");
		} 
		readSet.traceInformation.modificationStamp(user);
		State newState = IStateReference.createStateHistorique(readSet.submissionState, submissionStateCode, user);
		readSet.submissionState = newState;	
		if(submissionStateCode.equals(SUB_N)) {
			laboReadsetsAPI.dao_update(DBQuery.is("code", readSet.code), 
					DBUpdate.set("submissionState", newState)
					.set("submissionUser", user)
					.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", new Date()));
		} else {
			laboReadsetsAPI.dao_update(DBQuery.is("code", readSet.code), 
					DBUpdate.set("submissionState", newState)
					.set("traceInformation.modifyUser", user)
					.set("traceInformation.modifyDate", new Date()));
		}

	}	


}
