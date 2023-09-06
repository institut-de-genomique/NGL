package services;
import static ngl.refactoring.state.SRASubmissionStateNames.NONE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_N;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
//import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import java.util.Map.Entry;

import javax.inject.Inject;
 
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

//import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.AnalysisAPI;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.ExternalSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.ExternalStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ProjectAPI;
import fr.cea.ig.ngl.dao.api.sra.ReadsetAPI;
import fr.cea.ig.ngl.dao.api.sra.SampleAPI;
import fr.cea.ig.ngl.dao.api.sra.StudyAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
//import fr.cea.ig.ngl.dao.projects.ProjectsAPI;
import fr.cea.ig.ngl.dao.readsets.ReadSetsAPI;
import fr.cea.ig.ngl.dao.runs.RunsAPI;
//import fr.cea.ig.ngl.dao.samples.SamplesAPI;
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
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import models.sra.submit.util.VariableSRA;
//import ngl.refactoring.state.SRAStudyStateNames;
import play.libs.ws.WSClient;
import services.taxonomy.Taxon;
import services.taxonomy.TaxonomyServices;
import sra.api.submission.SubmissionNewAPITools;
import validation.ContextValidation;



public class CreateServices {
	private static final play.Logger.ALogger logger = play.Logger.of(CreateServices.class);

	private final ConfigurationAPI  configurationAPI;
	private final SubmissionAPI 	submissionAPI;
	private final AnalysisAPI       analysisAPI;
	private final StudyAPI          studyAPI;
	private final AbstractStudyAPI  abstractStudyAPI;
	private final ExternalStudyAPI  externalStudyAPI;
	private final SampleAPI         sampleAPI;
	private final AbstractSampleAPI abstractSampleAPI;
	private final ExternalSampleAPI externalSampleAPI;
	private final ExperimentAPI     experimentAPI;
	//private final SamplesAPI        laboSampleAPI;
	private final ProjectAPI        projectAPI;
	private final ReadSetsAPI       readsetsAPI;  // collection readset de ngl-seq
	private final ReadsetAPI        readsetAPI;   // collection readset de sra
	private final RunsAPI           laboRunAPI;
    private final SraCodeHelper     sraCodeHelper;
	private final SubmissionNewAPITools  submissionNewAPITools;
	private final String            patternSampleAc = "ERS\\d+";
	private final String            patternSampleExternalId = "SAM";

	private final String            patternStudyAc = "ERP\\d+";
	private final String            patternStudyExternalId  = "PRJ";

	private final SraEbiAPI         ebiAPI;
	private final TaxonomyServices  taxonomyServices;

	@Inject
	public CreateServices(ConfigurationAPI  configurationAPI,
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
		this.readsetsAPI       = readsetsAPI;
		//this.laboSampleAPI     = laboSampleAPI;
		this.laboRunAPI        = laboRunAPI;
		this.projectAPI        = projectAPI;
//		this.ws                = ws;
		this.sraCodeHelper     = sraCodeHelper;
		this.submissionNewAPITools   = submissionNewAPITools;
		this.ebiAPI            = ebiAPI;
		this.taxonomyServices  = taxonomyServices;

	}
	
	public void isSubmittable(Sample sample, ContextValidation contextValidation) {
		contextValidation.addKeyToRootKeyName("sample");
		if( ! ebiAPI.submittable(sample.taxonId)) {
			contextValidation.addError("taxonId", sample.taxonId + " n'est pas soumettable à l'EBI");
		}
		contextValidation.removeKeyFromRootKeyName("sample");
	}
	
	public void isSubmittable(int taxonId, ContextValidation contextValidation) {
		contextValidation.addKeyToRootKeyName("sample");
		if( ! ebiAPI.submittable(taxonId)) {
			contextValidation.addError("taxonId", taxonId + " n'est pas soumettable à l'EBI");
		}
		contextValidation.removeKeyFromRootKeyName("sample");
	}
	
	// cree une soumission de type creation pour un project umbrella, la met dans status SUB_N et sauve dans database
	public Submission initPrimarySubmissionForUmbrella(String title, String description, List<String> childrenProjectAccessions, List<String>idsPubmed, String strTaxonId, ContextValidation contextValidation) 
			throws SraException, SraValidationException, IOException {
		
		if (StringUtils.isBlank(title)) {
			throw new SraException("initPrimarySubmissionForUmbrella", " titre du project umbrella a null");
		}
		if (StringUtils.isBlank(description)) {
			throw new SraException("initPrimarySubmissionForUmbrella", " description du project umbrella a null");
		}	
		// NGL-3585
//		if (childrenProjectAccessions == null || childrenProjectAccessions.isEmpty()) {
//			throw new SraException("initPrimarySubmissionForUmbrella", " aucun project enfant ?");
//		}
		
		contextValidation.setCreationMode();
		String user = contextValidation.getUser();	
		Project umbrellaProject = new Project();
		
		umbrellaProject.submissionProjectType = "UMBRELLA_PROJECT";
		umbrellaProject.title = title;
		umbrellaProject.description = description;
		
		if (childrenProjectAccessions != null && !childrenProjectAccessions.isEmpty()) {
			umbrellaProject.childrenProjectAccessions = childrenProjectAccessions;
			for (String projectAC : umbrellaProject.childrenProjectAccessions) {
				List <Project> umbrellaList = projectAPI.dao_find(DBQuery.in("childrenProjectAccessions", projectAC)).toList();
				if (umbrellaList.size() > 0) {
					String message = "projectAC =" + projectAC + "\n";
					for (Project p: umbrellaList) {
						message += "       - deja utilise par le project umbrella  " + p.code + "\n";
					}
					//logger.debug(message);
					throw new SraException(message);
				}	 
			}
		}
		if (idsPubmed != null && !idsPubmed.isEmpty()) {
			umbrellaProject.idsPubmed = idsPubmed;
		}
		if (StringUtils.isNotBlank(strTaxonId)) {
			Taxon taxon = taxonomyServices.getTaxon(strTaxonId);
			if (taxon == null) {
				throw new SraException("initPrimarySubmissionForUmbrella", " Pas de recuperation du taxonId " + strTaxonId + " au NCBI ou à l'EBI");
			}
			if(StringUtils.isNotBlank(taxon.errorMessage)) {
				throw new SraException("initPrimarySubmissionForUmbrella", " Probleme lors de la recuperation du taxonId " + strTaxonId + " au NCBI ou à l'EBI " + taxon.errorMessage);
			}
			if(StringUtils.isNotBlank(taxon.scientificName)) {
				umbrellaProject.scientificName = taxon.scientificName;
				umbrellaProject.taxonId = strTaxonId;
			}
		}	
		umbrellaProject.traceInformation = new TraceInformation(); 
		umbrellaProject.traceInformation.setTraceInformation(user);
		umbrellaProject.state = new State(SUB_N, user);
		umbrellaProject.code = sraCodeHelper.generateUmbrellaCode();
		
		//logger.debug("XXXXXXX  umbrellaProject.code = " + umbrellaProject.code);
		
		umbrellaProject.validate(contextValidation);

		if (contextValidation.hasErrors()) {
			//logger.debug("erreur validation dur project umbrella :");
			contextValidation.displayErrors(logger);
		} else {
			//logger.debug("project umbrella ok");
		}
		

//		// Pas de validation complete du project car ici on recoit le code d'un umbrella deja dans base donc deja validé
		Date courantDate = new java.util.Date();
		Submission submission = new Submission(user);
		submission.traceInformation.creationDate = courantDate;
		submission.state = new State(SUB_N, user);
		submission.type = Submission.Type.CREATION;
		submission.typeRawDataSubmitted = TypeRawDataSubmitted.withoutRawData;
		submission.code = sraCodeHelper.generateSubmissionCodeForUmbrella();		
		// Determiner le submissionDirectory et submission.ebiResult une fois submission.code defini :
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code; 	
		//logger.debug("AAAAAAAAAAAAAAA            submission.submissionDirectory=" + submission.submissionDirectory);
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";
		
		submission.umbrellaCode = umbrellaProject.code;
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

		if(StringUtils.isBlank(analysis.sampleAccession)) {
			throw new SraException("validateParametersInitPrimarySubmissionBionano", "l'analyse avec le code "+ analysisCode + " ne contient pas de sampleAccession");
		}
		if( ! (analysis.sampleAccession.matches(patternSampleAc)||analysis.sampleAccession.startsWith(patternSampleExternalId)) ) {
			throw new SraException("l'analyse avec le code " + analysis.code + " contient un sampleAccession "+ analysis.sampleAccession + "qui n'est pas de la forme attendu (ERS ou SAM)");
		}
		if(StringUtils.isBlank(analysis.studyAccession)) {
			throw new SraException("initPrimarySubmissionBionano", "l'analyse avec le code "+ analysisCode + " ne contient pas de studyAccession");
		}
		if( ! (analysis.studyAccession.matches(patternStudyAc)||analysis.studyAccession.startsWith(patternStudyExternalId)) ) {
			throw new SraException("l'analyse avec le code " + analysis.code + " contient un studyAccession "+ analysis.studyAccession + "qui n'est pas de la forme attendu (ERP ou PRJ)");
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

	public List<RawData> buildRawData(ReadSet readSet, String analysisCode, String instrumentUsedTypeCode) {
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
				rawData.instrumentUsedTypeCode = instrumentUsedTypeCode;
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
			throw (new SraException("appel de buildRawDataFromAbsoluteFileName avec argument sans caracteres"));
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
			ContextValidation contextValidation) throws SraException, SraValidationException, IOException {
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
		
		// construction si besoin d'un externalStudy
		String acStudy = analysis.studyAccession;
		String study_code = null;
		String study_accession = null;
		ExternalStudy externalStudy = null;
		boolean study_in_base = false;

		if (acStudy.matches(patternStudyAc)) {
			if(abstractStudyAPI.dao_checkObjectExist("accession", acStudy)) {
				AbstractStudy abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", acStudy));
				if (abstStudy != null) {
					study_code = abstStudy.code;
					study_accession = acStudy;
					study_in_base = true;
					//logger.debug("study existant dans base avec study_code = " + study_code);
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
				}
			}
		} else {
			throw new SraException("pattern de l'identifiant du study " + acStudy + "non reconnu");
		}
		if(! study_in_base) {
			externalStudy = buildExternalStudy(acStudy, user);
			study_code = externalStudy.code;
			study_accession = acStudy;
		}
		// Renseigner la soumission pour le study :
		submission.refStudyCodes.add(study_code);
		// corriger si besoin l'analyse pour le study_accession (cas ou l'utilisateur a mis PRJ => remplacer par ERP si possible)
		analysis.studyAccession = study_accession;
		
		// construction si besoin d'un externalSample
		ExternalSample externalSample = null;
		String sampleAc = analysis.sampleAccession;
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
					//logger.debug("sample existant dans base avec sample_code = " + sample_code);
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
				}
			}
		} else {
			throw new SraException("pattern de l'identifiant du sample " + sampleAc + "non reconnu");
		}
		if(! sample_in_base) {
			externalSample = buildExternalSample(sampleAc, user);
			sample_code = externalSample.code;
			sample_accession = sampleAc;
		}
		
		// Renseigner la soumission pour le sample :
		submission.refSampleCodes.add(sample_code);
		// corriger si besoin l'analyse pour le sample_accession (cas ou l'utilisateur a mis SAM => remplacer par ERS si possible)
		analysis.sampleAccession = sample_accession;
		
		// construction des rawData correspondant au readset : 
		
		List <ReadSet> readSets        = new ArrayList<>();
		List <String>  subProjectCodes = new ArrayList<>();
		List <RawData> listRawData     = new ArrayList<RawData>();
		for (String readSetCode : readSetCodes) {	
			ReadSet readSet = readsetsAPI.get(readSetCode);
			models.laboratory.run.instance.Run  laboratoryRun = laboRunAPI.get(readSet.runCode);
			if(laboratoryRun==null){
				throw new SraException("Pas de laboratoryRun pour " + readSet.code);
			}
			if (laboratoryRun.instrumentUsed == null ){
				throw new SraException("Pas de champs instrumentUsed pour le laboratoryRun " + laboratoryRun.code);
			} 
			String instrumentUsedTypeCode = laboratoryRun.instrumentUsed.typeCode;
			if(StringUtils.isBlank(instrumentUsedTypeCode)) {
				throw new SraException("Pas de champs instrumentUsedTypeCode pour le laboratoryRun " + laboratoryRun.code);
			}
			readSets.add(readSet);
			if (StringUtils.isNotBlank(sample_taxonId) && ! sample_taxonId.equalsIgnoreCase(readSet.sampleOnContainer.taxonCode)) {
				contextValidation.addError("readsetCode: "+ readSet.code, " a un taxonId " + readSet.sampleOnContainer.taxonCode + " != du taxon dans le sample indique dans l'analyse :" + sample_taxonId);
			}
			if( ! subProjectCodes.contains(readSet.projectCode)) {
				subProjectCodes.add(readSet.projectCode);
			}
			// construire le rawData correspondant au readset et ajouter analysisCode
			List<RawData> listRawDatatmp = buildRawData(readSet, analysisCode, instrumentUsedTypeCode); 
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
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code; 	
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
	
	public void validatePrimarySubmissionWithoutRawData(
			String studyCode, 
			List<String>sampleCodes,
			ContextValidation contextValidation) throws SraException, SraValidationException, IOException {

		if(StringUtils.isBlank(studyCode) && (sampleCodes==null || sampleCodes.size()==0)) {
			throw new SraException("Aucun studyCode ou sampleCodes à soumettre?");
		}	
		if(StringUtils.isNotBlank(studyCode)) {
			if( ! studyAPI.dao_checkObjectExist("code", studyCode)) {
				throw new SraException("le studyCode indique " + studyCode + " n'existe pas dans la base");
			}
			Study study = studyAPI.dao_findOne(DBQuery.is("code", studyCode));	
			if(! NONE.equals(study.state.code)) {
				throw new SraException("le studyCode indique " + studyCode + " existe dans la base avec etat different de NONE "+ study.state.code);
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
					logger.debug(message);
					throw new SraException(message);
				} 
			}
		}
	}

	
	
	public Submission initPrimarySubmissionWithoutRawData(
			String studyCode, 
			List<String>sampleCodes,
			ContextValidation contextValidation) throws SraException, SraValidationException, IOException {
		String user = contextValidation.getUser();	
		//logger.debug("Dans CreateServices::initPrimarySubmissionWithoutRawData studyCode = " + studyCode);
		validatePrimarySubmissionWithoutRawData(studyCode, sampleCodes, contextValidation);
		Submission submission = new Submission(user);

		if(StringUtils.isNotBlank(studyCode)) {
			Study study = studyAPI.dao_findOne(DBQuery.is("code", studyCode));	

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
				if (!submission.refSampleCodes.contains(sample.code)) {
					submission.refSampleCodes.add(sample.code);
				}
				if (!submission.sampleCodes.contains(sample.code)) {
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
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code; 	
		//logger.debug("AAAAAAAAAAAAAAA            submission.submissionDirectory=" + submission.submissionDirectory);
		submission.ebiResult = "ebi_AC_" + submission.code + ".txt";

		
		// updater dans la base le study pour son status si study à soumettre
		if (StringUtils.isNotBlank(submission.studyCode)) {
			Study study = studyAPI.dao_findOne(DBQuery.is("code", studyCode));	
			updateStudyAndSaveIndb(study, SUB_N, user);
		}
		// updater dans la base les samples à soumettre pour status
		for(String sampleCode : submission.sampleCodes) {
			Sample sample = sampleAPI.dao_getObject(sampleCode);
			updateSampleAndSaveIndb(sample, SUB_N, user);
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

	
	
	public Submission initPrimarySubmission(List<String> readSetCodes, 
											String studyCode, 
											String configCode, 
											String acStudy, 
											String acSample, 
											Map<String, UserRefCollabType>mapUserRefCollab, 
											ContextValidation contextValidation) throws SraException, SraValidationException, IOException {
		// Pour simplifier workflow on n'autorise pas a la creation d'une soumission, l'utilisation d'un sample crée par une autre soumission
		// mais toujours dans le processus de soumission. Idem pour les study. Si la creation d'une soumission utilise un study ou un sample deja existant
		// dans la base, alors le state.code doit etre à F-SUB.
		// Pour une premiere soumission d'un readSet, on peut devoir utiliser un study ou un sample existant, deja soumis à l'EBI.
		// En revanche on ne doit pas utiliser un experiment ou un run existant
		//logger.debug("initPrimarySubmission::acSample=" + acSample);
		String user = contextValidation.getUser();		
		validateParamForSubmission(readSetCodes, studyCode, configCode, acStudy, acSample, mapUserRefCollab, contextValidation);
		if(contextValidation.hasErrors()) {
			throw new SraValidationException(contextValidation);
		}
		
		// Liste des projectCodes de la soumission, qui doit contenir les 
		// projectCodes des objets soumis : readsets, samples, experiments et study
		// le projectCode de l'experiment et du sample est le meme que celui du readset.
		Configuration config = configurationAPI.get(configCode);
		
		// Recuperation de tous les Readset avec un status à NONE 
		//(verif etat des readsets dans validateParamForSubmission, dans validateReadSetCodesForSubmission)
		List <ReadSet> readSets = new ArrayList<>();
		// Renvoie un objet submission avec state.code='SUB-N'
		Submission submission = createSubmissionEntityForPrimarySubmission(config, studyCode, acStudy, user);
		// Liste des sous-objets utilisés dans submission
		List <Experiment> listExperiments = new ArrayList<>();         // liste des experiments utilisés et crees dans soumission 
		// avec state.code='N' à sauver dans database
		List <AbstractSample> listAbstractSamples = new ArrayList<>(); // liste des AbstractSample(Sample ou ExternalSample) 
		// avec state.code='F-SUB' ou 'N' utilises dans soumission à sauver ou non dans database		
		List <AbstractStudy> listAbstractStudies = new ArrayList<>();  // liste des AbstractStudy(Study ou ExternalStudy) avec state.code='F-SUB' ou 'N' 
		// utilises dans soumission à sauver ou non dans database	

		int countError = 0;
		String errorMessage = "";

		
		for (String readSetCode : readSetCodes) {
			ReadSet readSet = readsetsAPI.get(readSetCode);
			readSets.add(readSet);
			if( ! submission.projectCodes.contains(readSet.projectCode)) {
				submission.projectCodes.add(readSet.projectCode);
			}
		}
		
		
		for (ReadSet readSet : readSets) {
			//logger.debug("readSet : {}", readSet.code);
			// Verifier que c'est une premiere soumission pour ce readSet
			// Verifiez qu'il n'existe pas d'objet experiment referencant deja ce readSet

			// Recuperer scientificName via NCBI pour ce readSet. Le scientificName est utilisé dans la construction
			// des samples et des experiments 
			String scientificName = readSet.sampleOnContainer.ncbiScientificName;
			// Creer les objets avec leurs alias ou code, les instancier completement et les sauver.
			Experiment experiment = createExperimentEntity(readSet, scientificName, user);

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

			switch(config.strategySample) {
			case STRATEGY_AC_SAMPLE : {
				//logger.debug("Dans init, cas STRATEGY_AC_SAMPLE");
				// Dans ce cas on ne sait pas si on va recuperer un Sample s'il a ete construit par
				// le genoscope, ou un ExternalSample s'il a ete contruit par un collaborateur.
				ExternalSample externalSample = null;
				AbstractSample abstSample = null;
				String sample_code = "";
				String sample_accession = "";
				if (mapUserRefCollab != null && !mapUserRefCollab.isEmpty()) {
					//logger.debug("cas STRATEGY_AC_SAMPLE avec mapUserRefCollab");
					if (StringUtils.isBlank(refCollab)) {
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque STRATEGY_AC_SAMPLE et pas de nom de clone dans ngl \n";
						continue;					
					}
					if (! mapUserRefCollab.containsKey(refCollab)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque STRATEGY_AC_SAMPLE avec mapUserRefCollab qui ne contient pas le clone '" + refCollab +"' \n";
						continue;
					}
					String sampleAc = mapUserRefCollab.get(refCollab).getSampleAc();
					//logger.debug("Pour la refCollab "+ refCollab + " sample = " + sampleAc);

					if (StringUtils.isBlank(sampleAc)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque mapUserRefCollab pour clone '" + refCollab + "' ne contient pas de sampleAc";
						continue;					
					}
					//NGL-3666
//					if( ! sampleAc.matches(patternSampleAc)) {
					if( ! (sampleAc.matches(patternSampleAc) || sampleAc.startsWith(patternSampleExternalId) )) {
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque mapUserRefCollab pour clone '" + refCollab + "' contient un AC sample " + sampleAc + " qui n'est pas de la forme attendu\n";
						continue;	
					}
					// recuperation dans base ou création du sample avec status SUB-F (si AC alors SUB-F)
					boolean sample_in_base = false;
					if(sampleAc.matches(patternSampleAc)) {
						if(abstractSampleAPI.dao_checkObjectExist("accession", sampleAc)) {
							abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("accession", sampleAc));
							if (abstSample != null) {
								sample_code = abstSample.code;
								sample_accession = sampleAc;
								sample_in_base = true;
								//logger.debug("sample existant dans base avec sample_code = " + sample_code);
							}
						} 
					} else if(sampleAc.startsWith(patternSampleExternalId)) {
						if(abstractSampleAPI.dao_checkObjectExist("externalId", sampleAc)) {
							abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("externalId", sampleAc));
							if (abstSample != null) {
								sample_code = abstSample.code;
								if(StringUtils.isNotBlank(abstSample.accession)) {
									sample_accession = abstSample.accession;
								} else {
									sample_accession = sampleAc;
								}
								sample_in_base = true;
								//logger.debug("sample existant dans base avec sample_code = " + sample_code);
							}
						}
					} else {
						throw new SraException("pattern de l'identifiant du sample " + sampleAc + "non reconnu");
					}
					
					if(! sample_in_base) {
						externalSample = buildExternalSample(sampleAc, user);
						sample_code = externalSample.code;
						sample_accession = sampleAc;
					}
					// Mise a jour de l'objet submission pour mapUserRefCollab :
					UserRefCollabType submission_userRefCollab = new UserRefCollabType();
//					logger.debug("alias=" + mapUserRefCollab.get(refCollab).getAlias());
//					logger.debug("sampleAC=" + mapUserRefCollab.get(refCollab).getSampleAc());
//					logger.debug("studyAC=" + mapUserRefCollab.get(refCollab).getStudyAc());
					submission_userRefCollab.setAlias(mapUserRefCollab.get(refCollab).getAlias());
					submission_userRefCollab.setSampleAc(mapUserRefCollab.get(refCollab).getSampleAc());
					submission_userRefCollab.setStudyAc(mapUserRefCollab.get(refCollab).getStudyAc());
//					logger.debug("submission_userRefCollab.alias=" + submission_userRefCollab.getAlias());
//					logger.debug("submission_userRefCollab.sampleAC=" + submission_userRefCollab.getSampleAc());
//					logger.debug("submission_userRefCollab.studyAC=" + submission_userRefCollab.getStudyAc());
					if(submission.mapUserRefCollab == null || submission.mapUserRefCollab.isEmpty()) {
						//logger.debug("submission.mapUserRefCollab vide= ");
						submission.mapUserRefCollab = new HashMap<String, UserRefCollabType>();
					}
					//logger.debug("submission.mapUserRefCollab = " + submission.mapUserRefCollab.size());
					submission.mapUserRefCollab.put(submission_userRefCollab.getAlias(), submission_userRefCollab);
					//logger.debug("submission mis a jour pour userRefCollab");
				} else if (StringUtils.isNotBlank(acSample)) {
					//logger.debug("cas sans mapUserRefCollab et avec acSample indique dans interface");
					
					//NGL-3666
					//if( ! acSample.matches(patternSampleAc)) {
					if( ! (acSample.matches(patternSampleAc) || acSample.startsWith(patternSampleExternalId) )) {
						countError++;
						errorMessage = errorMessage + "STRATEGY_AC_SAMPLE et acSample transmis " + acSample + " qui n'est pas de la forme attendue\n";
						continue;
					}
					
		
					boolean sample_in_base = false;
					if(acSample.matches(patternSampleAc)) {
						if(abstractSampleAPI.dao_checkObjectExist("accession", acSample)) {
							abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("accession", acSample));
							if (abstSample != null) {
								sample_code = abstSample.code;
								sample_accession = acSample;
								sample_in_base = true;
								//logger.debug("sample existant dans base avec sample_code = " + sample_code);
							}
						} 
					} else if(acSample.startsWith(patternSampleExternalId)) {
						if(abstractSampleAPI.dao_checkObjectExist("externalId", acSample)) {
							abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("externalId", acSample));
							if (abstSample != null) {
								sample_code = abstSample.code;
								if(StringUtils.isNotBlank(abstSample.accession)) {
									sample_accession = abstSample.accession;
								} else {
									sample_accession = acSample;
								}
								sample_in_base = true;
								//logger.debug("sample existant dans base avec sample_code = " + sample_code);
							}
						}
					} else {
						throw new SraException("pattern de l'identifiant du sample " + acSample + "non reconnu");
					}
					
					if(! sample_in_base) {
						externalSample = buildExternalSample(acSample, user);
						sample_code = externalSample.code;
						sample_accession = acSample;
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
				experiment.sampleAccession = sample_accession;
				break;
			}
			case STRATEGY_CODE_SAMPLE_TAXON : case STRATEGY_CODE_SAMPLE_REFCOLLAB : case STRATEGY_CODE_SAMPLE_BANK : {
				//logger.debug("Dans init, cas strategy_internal_sample");
				Sample sample = null;
				// Recuperer le sample existant avec son state.code ou bien en creer un nouveau avec state.code='N'
				sample = buildSample(readSet, config.strategySample, scientificName, user);
				// Renseigner l'objet submission :
				// Verifier que l'objet sample  n'est pas en cours de soumission
				//logger.debug("sample = {} et state = {}" , sample, sample.state.code);
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
				experiment.sampleAccession = sample.accession;
				break;
			} 

			default :
				throw new SraException("strategySample non attendue : " + config.strategySample);
			}
			String study_accession = "";
			String study_code = "";
			switch (config.strategyStudy) {
			case STRATEGY_AC_STUDY : {
				AbstractStudy abstStudy = null;
				ExternalStudy externalStudy = null;
				if (mapUserRefCollab != null && !mapUserRefCollab.isEmpty()) {
					if ( ! mapUserRefCollab.containsKey(refCollab)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque STRATEGY_AC_STUDY et mapUserRefCollab ne contient pas la refCollab '" + refCollab +"' \n";
						continue;
					} else {
						// mettre a jour submission.mapUserRefCollab :
						UserRefCollabType submission_userRefCollab = new UserRefCollabType();
						submission_userRefCollab.setAlias(mapUserRefCollab.get(refCollab).getAlias());
						submission_userRefCollab.setSampleAc(mapUserRefCollab.get(refCollab).getSampleAc());
						submission_userRefCollab.setStudyAc(mapUserRefCollab.get(refCollab).getStudyAc());
						if(submission.mapUserRefCollab == null || submission.mapUserRefCollab.isEmpty()) {
							//logger.debug("submission.mapUserRefCollab vide= ");
							submission.mapUserRefCollab = new HashMap<String, UserRefCollabType>();
						}
						submission.mapUserRefCollab.put(submission_userRefCollab.getAlias(), submission_userRefCollab);
					}
					String studyAc = mapUserRefCollab.get(refCollab).getStudyAc();
					if (StringUtils.isBlank(studyAc)){
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque STRATEGY_AC_STUDY et mapUserRefCollab.get(clone).getStudyAc() non renseigne pour le clone '" + refCollab + "'\n";
						continue;					
					}
					//logger.debug("studyAc dans mapRefCollab = " + studyAc);
					//NGL-3666 :
					//if ( ! studyAc.matches(patternStudyAc)){
					if ( ! (studyAc.matches(patternStudyAc) || studyAc.startsWith(patternStudyExternalId)) ) {
						//logger.debug("Erreur detectee pour studyAc dans mapRefCollab");
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque mapUserRefCollab pour clone '" + refCollab + "' contient un AC de study " + studyAc + " qui n'est pas de la forme attendu\n";
						continue;					
					}
					// recuperation dans base ou création du sample avec status SUB-F (si AC alors SUB-F)
					boolean study_in_base = false;
					if(studyAc.matches(patternStudyAc)) {
						if(abstractStudyAPI.dao_checkObjectExist("accession", studyAc)) {
							abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", studyAc));
							if (abstStudy != null) {
								study_code = abstStudy.code;
								study_accession = studyAc;
								study_in_base = true;
								//logger.debug("study existant dans base avec study_code = " + study_code);
							}
						} 
					} else if(studyAc.startsWith(patternStudyExternalId)) {
						if(abstractStudyAPI.dao_checkObjectExist("externalId", studyAc)) {
							abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("externalId", studyAc));
							if (abstStudy != null) {
								study_code = abstStudy.code;
								if(StringUtils.isNotBlank(abstStudy.accession)) {
									study_accession = abstStudy.accession;
								} else {
									study_accession = studyAc;
								}
								study_in_base = true;
								//logger.debug("study existant dans base avec study_code = " + study_code);
							}
						}
					} else {
						throw new SraException("pattern de l'identifiant du study " + studyAc + "non reconnu");
					}
					
					if(! study_in_base) {
						externalStudy = buildExternalStudy(studyAc, user);
						study_code = externalStudy.code;
						study_accession = studyAc;
					}
					
				} else if (StringUtils.isNotBlank(acStudy)) {
					// NGL-3666
					//if( ! acStudy.matches(patternStudyAc)) {
					if( ! (acStudy.matches(patternStudyAc)||acStudy.startsWith(patternStudyExternalId)) ) {
						countError++;
						errorMessage = errorMessage + "STRATEGY_AC_STUDY et acStudy transmis " + acStudy + " qui n'est pas de la forme attendue\n";
						continue;
					}
					
					boolean study_in_base = false;
					if (acStudy.matches(patternStudyAc)) {
						if(abstractStudyAPI.dao_checkObjectExist("accession", acStudy)) {
							abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", acStudy));
							if (abstStudy != null) {
								study_code = abstStudy.code;
								study_accession = acStudy;
								study_in_base = true;
								//logger.debug("study existant dans base avec study_code = " + study_code);
							}
						} 
					} else if (acStudy.startsWith(patternStudyExternalId)) {
						if(abstractStudyAPI.dao_checkObjectExist("externalId", acStudy)) {
							abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("externalId", acStudy));
							if (abstStudy != null) {
								study_code = abstStudy.code;
								if(StringUtils.isNotBlank(abstStudy.accession)) {
									study_accession = abstStudy.accession;
								} else {
									study_accession = acStudy;
								}
								study_in_base = true;
								//logger.debug("study existant dans base avec study_code = " + study_code);
							}
						}
					} else {
						throw new SraException("pattern de l'identifiant du study " + acStudy + "non reconnu");
					}
					if(! study_in_base) {
						externalStudy = buildExternalStudy(acStudy, user);
						study_code = externalStudy.code;
						study_accession = acStudy;
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
				experiment.studyAccession = study_accession;	
				break;
			}
			case STRATEGY_CODE_STUDY : {
				Study study = null;
				//logger.debug("**********************   STRATEGY_CODE_STUDY      ************************");	
				// on recupere depuis la base un study non null avec bon status (validateParamForSubmission)
				study = studyAPI.dao_findOne(DBQuery.is("code", studyCode));
				
				// Mise à jour de l'objet submission pour les study references :
				if (!submission.refStudyCodes.contains(study.code)) {
					submission.refStudyCodes.add(study.code);
				}
				// Mise a jour de l'objet submission pour le study à soumettre :
				if(NONE.equals(study.state.code)) {
					submission.studyCode = study.code;
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
				experiment.studyAccession = study.accession;
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
		} // end for readset
		
		if (countError != 0){
			//logger.debug(errorMessage);
			throw new SraException("Problemes pour creer la soumission\n" + errorMessage);
		}
		
		// Determiner le code de la soumission, une fois la liste des projectCodes definie :
		submission.code = sraCodeHelper.generateSubmissionCode(submission.projectCodes);
		
		// Determiner le submissionDirectory et submission.ebiResult une fois submission.code defini :
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code; 	
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
		Map <Integer, String> hTaxonId = new HashMap<Integer, String>();
		for (AbstractSample sampleElt: listAbstractSamples) {
			if (!abstractSampleAPI.dao_checkObjectExist("code", sampleElt.code)){	
				// validation qui depend du type de l'objet.
				//logger.debug("xxxxx   validation du sample " + sampleElt.code);
				if (sampleElt instanceof ExternalSample) {
					ExternalSample eSample = (ExternalSample) sampleElt;
					eSample.validate(contextValidation);
				} else {
					Sample sample = (Sample) sampleElt;
					hTaxonId.put(sample.taxonId, null);
					//logger.debug("xxxxx demande "+ sample.code + ".isSubmittable("+sample.taxonId+")");
					//isSubmittable(sample, contextValidation); deporte plus bas pour limiter requetes EBI
					sample.validate(contextValidation);
				}
				abstractSampleAPI.dao_saveObject(sampleElt);
				//logger.debug("ok pour sauvegarde dans la base du sample " + sampleElt.code);
			}
		}
		// verification des taxonId :
        for (Map.Entry mapentry : hTaxonId.entrySet()) {
			isSubmittable((int)mapentry.getKey(), contextValidation);
        }
        
		// On sauve les experiments qui n'existent pas dans la base
		// sans tester validation qui sera tester a la sauvegarde de la soumission
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
		updateReadSetAndSaveIndbForCreation(readSets, SUB_N, user);

		// ajouter les readsets dans collection readset de sra :
		addReadsetIndb(listExperiments);

		// updater dans la base le study pour son status si study à soumettre
		if (StringUtils.isNotBlank(submission.studyCode)) {
			Study study = studyAPI.dao_getObject(submission.studyCode);
			if (study != null && NONE.equals(study.state.code)) {
				updateStudyAndSaveIndb(study, SUB_N, user);
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
		//logger.debug("dans validateParamForSubmission");
		
		// configuration obligatoire :
		if (StringUtils.isBlank(configCode) && (readSetCodes != null || readSetCodes.size() > 0) ) {
			throw new SraException("initPrimarySubmission", "La configuration a un code à null");
		}
		
		if(readSetCodes==null || readSetCodes.size()==0) {
			throw new SraException("validateParamForSubmission", " Aucun readset et aucun study pour construire la soumission ???");
		}
		// validation des readsets de type different de bionano :
		validateReadSetCodesForSubmission(readSetCodes, contextValidation, false);
		
		Configuration config = configurationAPI.get(configCode);	
		Study study = null;

		if (config == null) {
			throw new SraException("validateParamForSubmission", "Configuration " + configCode + " n'existe pas dans database");
			//contextValidation.addError("validateParamForSubmission", "la configuration " + configCode + " n'existe pas dans la base de donnees");
		} 
		if (StringUtils.isBlank(config.state.code)) {
			throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec champs state.code non renseigne");
			//contextValidation.addError("validateParamForSubmission", "Configuration " + config.code + " avec champs state.code non renseigne");
		}
		//logger.debug("validateParamForSubmission, config.state.code='" + config.state.code+"'");
		
		switch (config.strategyStudy) {
		case STRATEGY_CODE_STUDY :
			if (StringUtils.isNotBlank(acStudy)) {
				throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec STRATEGY_CODE_STUDY incompatible avec acStudy renseigne : '" + acStudy +"'");
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
			study = studyAPI.dao_getObject(studyCode);	
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
			break;
		case STRATEGY_AC_STUDY :	
			if (StringUtils.isNotBlank(studyCode)){
				throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec STRATEGY_AC_STUDY incompatible avec studyCode renseigne : '" + studyCode +"'");
			}
			if (StringUtils.isBlank(acStudy) && (mapUserRefCollab == null || mapUserRefCollab.isEmpty()) ) {
				throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec strategy_study = 'STRATEGY_AC_STUDY' incompatible avec acStudy et mapUserRefCollab non renseignés");
			}
			break;
		default :
			throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec strategy_study = '"+ config.strategyStudy + "' (valeur non attendue)");
		}
		switch (config.strategySample) {
		case STRATEGY_CODE_SAMPLE_TAXON : case STRATEGY_CODE_SAMPLE_REFCOLLAB : case STRATEGY_CODE_SAMPLE_BANK :
			break;
		case STRATEGY_AC_SAMPLE :
			//logger.debug("Dans validateParamForSubmission, cas STRATEGY_AC_SAMPLE");
			if  (StringUtils.isBlank(acSample) && (mapUserRefCollab == null || mapUserRefCollab.isEmpty()) ) {
				throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec configuration.strategy_sample='strategy_ac_sample' incompatible avec acSample null et mapUserRefCollab non renseigné");
			}
			
			break;
		default :
			throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec strategy_sample = '"+ config.strategySample + "' (valeur non attendue)");
		}		
	}
	
	
	private void validateReadSetCodesForSubmission (List<String> readSetCodes, ContextValidation contextValidation, boolean bionano) {
		// Verifier que tous les readSetCode passes en parametres correspondent bien a des objets en base avec
		// submissionState='NONE' cad des readSets qui n'interviennent dans aucune soumission et readset valides pour soumission (readset valides pour la bioinformatique)
		if(readSetCodes == null) {
			return;
		}

    	for (String readSetCode : readSetCodes) {
			if (StringUtils.isNotBlank(readSetCode)) {
				//				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
				ReadSet readSet = readsetsAPI.get(readSetCode);
				

				if (readSet == null) {
					contextValidation.addError("Le readset." + readSetCode, " n'existe pas dans la base de donnees");
					continue;
				} else if ((readSet.submissionState == null)||(StringUtils.isBlank(readSet.submissionState.code))) {
					contextValidation.addError("Le readset " + readSetCode, " n'a pas de submissionState.code");
					continue;
				} else if(!readSet.submissionState.code.equalsIgnoreCase(NONE)){
					contextValidation.addError("Le readSet " + readSet.code,  " a un submissionState.code à la valeur " + readSet.submissionState.code);
					continue;
				} else {
					//NGL-4166 :
					// notifier une erreur si collabFileName contient des espaces internes :
					List <models.laboratory.run.instance.File> list_files =  readSet.files;
					for (models.laboratory.run.instance.File runInstanceFile: list_files) {
						if (runInstanceFile.usable ) { 		
							if(runInstanceFile.properties.containsKey("collabFileName")) {
								PropertyValue propertyValue = runInstanceFile.properties.get("collabFileName");
								String collabFileName = (String) propertyValue.value;
								if(StringUtils.isNotBlank(collabFileName)) {
									collabFileName = Tools.clean(collabFileName);
									logger.debug("collabFileName = " + collabFileName);
									if (collabFileName.matches("^\\S*\\s+.*$")) {
										contextValidation.addError("Le readset." + readSetCode, " a un fichier " + runInstanceFile.fullname + " avec un collabFileName " + collabFileName + " contenant des espaces");
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
					// Verifier que ce readSet est bien valide avant soumission :
					if (! readSet.bioinformaticValuation.valid.equals(TBoolean.TRUE)) {
						contextValidation.addError("Le readSet " + readSet.code,"est non valide pour la bioinformatique");
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
					// NGL-4266
					valideAttributes(readSet, contextValidation);
					
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
	}

	
	private Submission createSubmissionEntityForPrimarySubmission(Configuration config, 
																  String studyCode, 
																  String studyAc, 
																  String user) throws SraException{
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
		//logger.debug("expCode =" + experiment.code);
		String laboratoryRunCode = readSet.runCode;
		//logger.debug("laboratoryRunCode =" + laboratoryRunCode);

		models.laboratory.run.instance.Run  laboratoryRun = laboRunAPI.get(laboratoryRunCode);
		Map<String, PropertyValue> sampleOnContainerProperties = readSet.sampleOnContainer.properties;

		String libProcessTypeCodeVal = (String) sampleOnContainerProperties.get("libProcessTypeCode").getValue();
		String typeCode = readSet.typeCode;
		//logger.debug("libProcessTypeCodeVal = "+libProcessTypeCodeVal);
		//logger.debug("typeCode = "+typeCode);

		if (readSet.typeCode.equalsIgnoreCase("rsillumina")){
			typeCode = "Illumina";
		} else if (readSet.typeCode.equalsIgnoreCase("rsnanopore")){
			typeCode = "oxford_nanopore";
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
			throw new SraException("Pas de correspondance existante pour instrumentUsed.typeCodeMin = '" + instrumentUsed.typeCode.toLowerCase()+"'");	
		}
		
		experiment.libraryLayoutNominalLength = null;		
		if( ! "rsnanopore".equalsIgnoreCase(readSet.typeCode)){
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
							experiment.libraryLayoutNominalLength = (Integer) estimatedInsertSize.value;
							//logger.debug("valeur calculee libraryLayoutNominalLength  => "  + experiment.libraryLayoutNominalLength);
						} 
						if (pairs.containsKey("estimatedMPInsertSize")) {
							PropertyValue estimatedInsertSize = pairs.get("estimatedMPInsertSize");
							experiment.libraryLayoutNominalLength = (Integer) estimatedInsertSize.value;
							//logger.debug("valeur calculee libraryLayoutNominalLength  => "  + experiment.libraryLayoutNominalLength);
						}	
					}
				} 
			}
			
			if (experiment.libraryLayoutNominalLength == null) {
				// mettre valeur theorique de libraryLayoutNominalLength (valeur a prendre dans readSet.sampleOnContainer.properties.nominalLength) 
				// voir recup un peu plus bas:
				if (sampleOnContainerProperties.containsKey("libLayoutNominalLength")) {	
					//logger.debug("************** recherche valeur theorique possible pour " + experiment.code );
					PropertyValue nominalLengthTypeCode = sampleOnContainerProperties.get("libLayoutNominalLength");
					Integer nominalLengthCodeValue = (Integer) nominalLengthTypeCode.value;
					if ((nominalLengthCodeValue != null) && (nominalLengthCodeValue!= -1)){
						experiment.libraryLayoutNominalLength = nominalLengthCodeValue;
						//logger.debug("************ valeur theorique libraryLayoutNominalLength  => "  + experiment.libraryLayoutNominalLength);
					}
				}
			}
		}
		//logger.debug("valeur de experiment.libLayoutExpLength"+ experiment.libraryLayoutNominalLength);			
		experiment.state = new State(SUB_N, user); 
	
		if( ! "rsnanopore".equalsIgnoreCase(readSet.typeCode)){
			// Recuperer l'information spotLength pour les illumina
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
//							/*for(String k: listKeysSampleOnContainerProperties){
//							logger.debug("cle = " + k);
//							PropertyValue propertyValue = sampleOnContainerProperties.get(k);
//							logger.debug(propertyValue.toString());
//							logger.debug(", value  => "+propertyValue.value);
//						} 
							
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
		experiment.libraryConstructionProtocol = VariableSRA.defaultLibraryConstructionProtocol;
		experiment.run = createRunEntity(readSet);
		experiment.run.expCode=experiment.code;
		// Renseigner lastBaseCoord au niveau du run
		
		
		// Recuperer l'information lastBaseCoord au niveau du run
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
					experiment.lastBaseCoord =  (Integer) ngsrg.get("nbCycleRead1").value + 1;
				}
			}
		}

		// Si lastBaseCoord non trouvé au niveau du run alors recuperer les lanes associées au
		// run associé au readSet et recuperer le lane contenant le readSet.code. C'est dans les
		// traitement de cette lane que se trouve l'information:
		// lastBaseCoord trouvé au niveau du run si recent ou run exterieur, et trouvé au niveau des lanes si runs plus anciens.
		if (experiment.lastBaseCoord == null ) {
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
													experiment.lastBaseCoord =  (Integer) lanengsrg.get("nbCycleRead1").value + 1;
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
		experiment.readSpecs = new ArrayList<>();
		
		if( ! "rsnanopore".equalsIgnoreCase(readSet.typeCode)){
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
//			RuntimeException e = new RuntimeException("Aucun fichier pour le readSet " + readSet.code +"???");
//			logger.debug("error", e);
			logger.debug("Aucun fichier pour le readSet " + readSet.code +"???");		
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
	
	
//	public ExternalSample fetchExternalSample(String sampleAc, String user) throws SraException {
//		//logger.debug("Entree dans fetchExternalSample");
//
//		ExternalSample externalSample;
//
//		if (abstractSampleAPI.dao_getObject(sampleAc) != null) {				
//			AbstractSample absSample;
//			absSample = abstractSampleAPI.dao_getObject(sampleAc);
//					
//			if (absSample instanceof ExternalSample) {
//				//logger.debug("Recuperation dans base du sample avec Ac = " + sampleAc +" qui est du type externalSample ");
//			} else {
//				throw new SraException("Recuperation dans base du sample avec Ac = " + sampleAc +" qui n'est pas du type externalSample ");
//			}
////			externalSample = MongoDBDAO.findOne(InstanceConstants.SRA_SAMPLE_COLL_NAME,
////					ExternalSample.class, DBQuery.and(DBQuery.is("accession", sampleAc)));
//			externalSample = externalSampleAPI.dao_getObject(sampleAc);
//		} else {
//			//logger.debug("Creation dans base du sample avec Ac " + sampleAc);
//			String externalSampleCode = sraCodeHelper.generateExternalSampleCode(sampleAc);
//			externalSample = new ExternalSample(); // objet avec state.code = submitted
//			externalSample.accession = sampleAc;
//			externalSample.code = externalSampleCode;
//			externalSample.traceInformation.setTraceInformation(user);	
//			externalSample.state = new State(SUB_F, user);
//		}
//		return externalSample;
//	}

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
		//logger.debug("externalSample.accession = " + externalSample.accession);
		//logger.debug("externalSample.code = " + externalSample.code);
		//logger.debug("externalSample.state.code = " + externalSample.state.code);
		return externalSample;
	}
	
	//NGL-4266 :
	private String buildAttributes(ReadSet readSet) throws SraException {

		String sampleAttributes = "";

		if ( readSet.sampleOnContainer.properties.containsKey("collectionDateException") ) {
			String valueCollectionDateException = (String) readSet.sampleOnContainer.properties.get("collectionDateException").getValue();
			if(StringUtils.isNotBlank(valueCollectionDateException)) {
				valueCollectionDateException = "missing: " + valueCollectionDateException;
				sampleAttributes.concat("<SAMPLE_ATTRIBUTE><TAG>collection date</TAG><VALUE>" + valueCollectionDateException + "</VALUE></SAMPLE_ATTRIBUTE>\n");
			}
		}
		if ( readSet.sampleOnContainer.properties.containsKey("collectionDate") ) {
			Date valueCollectionDate = (Date) readSet.sampleOnContainer.properties.get("collectionDate").getValue();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = formatter.format(valueCollectionDate);
			if(StringUtils.isNotBlank(strDate)) {
				sampleAttributes.concat("<SAMPLE_ATTRIBUTE><TAG>collection date</TAG><VALUE>" + strDate + "</VALUE></SAMPLE_ATTRIBUTE>\n");
			}
		}
		if ( readSet.sampleOnContainer.properties.containsKey("geographicLocationException") ) {
			String valueGeographicLocationException = (String) readSet.sampleOnContainer.properties.get("geographicLocationException").getValue();
			if(StringUtils.isNotBlank(valueGeographicLocationException)) {
				valueGeographicLocationException = "missing: " + valueGeographicLocationException;
				sampleAttributes.concat("<SAMPLE_ATTRIBUTE><TAG>geographic location (country and/or sea)</TAG><VALUE>" + valueGeographicLocationException + "</VALUE></SAMPLE_ATTRIBUTE>\n");
			}
		}
		if ( readSet.sampleOnContainer.properties.containsKey("geographicLocation") ) {
			String valueGeographicLocation = (String) readSet.sampleOnContainer.properties.get("geographicLocation").getValue();
			if(StringUtils.isNotBlank(valueGeographicLocation)) {
				sampleAttributes.concat("<SAMPLE_ATTRIBUTE><TAG>geographic location (country and/or sea)</TAG><VALUE>" + valueGeographicLocation + "</VALUE></SAMPLE_ATTRIBUTE>\n");
			}
		}	
		if ( readSet.sampleOnContainer.properties.containsKey("geographicRegion") ) {
			String valueGeographicRegion = (String) readSet.sampleOnContainer.properties.get("geographicRegion").getValue();
			if(StringUtils.isNotBlank(valueGeographicRegion)) {
				sampleAttributes.concat("<SAMPLE_ATTRIBUTE><TAG>geographic location (region and locality)</TAG><VALUE>" + valueGeographicRegion + "</VALUE></SAMPLE_ATTRIBUTE>\n");
			}
		}	
		
		return sampleAttributes;
	}
	
	//NGL-4266 :
	private void valideAttributes(ReadSet readSet, ContextValidation contextValidation) throws SraException {
		boolean collectionDateException = false;
		if ( readSet.sampleOnContainer.properties.containsKey("collectionDateException") ) {
			String valueCollectionDateException = (String) readSet.sampleOnContainer.properties.get("collectionDateException").getValue();
			if(StringUtils.isNotBlank(valueCollectionDateException)) {
				collectionDateException = true;
			}
		}
		if ( readSet.sampleOnContainer.properties.containsKey("collectionDate") ) {
			Date valueCollectionDate = (Date) readSet.sampleOnContainer.properties.get("collectionDate").getValue();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			String strDate = formatter.format(valueCollectionDate);
			if(StringUtils.isNotBlank(strDate)) {
				if(collectionDateException) {
					contextValidation.addError("Le readSet " + readSet.code,  " possede une 'collectionDateException' et une 'collectionDate'");
				}	
			}
		}
		boolean geographicLocationException = false;
		if ( readSet.sampleOnContainer.properties.containsKey("geographicLocationException") ) {
			String valueGeographicLocationException = (String) readSet.sampleOnContainer.properties.get("geographicLocationException").getValue();
			if(StringUtils.isNotBlank(valueGeographicLocationException)) {
				geographicLocationException = true;
			}
		}
		if ( readSet.sampleOnContainer.properties.containsKey("geographicLocation") ) {
			String valueGeographicLocation = (String) readSet.sampleOnContainer.properties.get("geographicLocation").getValue();
			if(StringUtils.isNotBlank(valueGeographicLocation)) {
				if(geographicLocationException) {
					contextValidation.addError("Le readSet " + readSet.code,  " possede une 'geographicLocationException' et une 'geographicLocation'");
				}
			}
		}	
		if ( readSet.sampleOnContainer.properties.containsKey("geographicRegion") ) {
			String valueGeographicRegion = (String) readSet.sampleOnContainer.properties.get("geographicRegion").getValue();
			if(StringUtils.isNotBlank(valueGeographicRegion)) {
				if(geographicLocationException) {
					contextValidation.addError("Le readSet " + readSet.code,  " possede une 'geographicLocationException' et une 'geographicRegion'");
				}
			}
		}		
	}
		
	private Sample buildSample(ReadSet readSet, StrategySample strategySample, String scientificName, String user) throws SraException {
		// Recuperer pour chaque readSet les objets de laboratory qui existent forcemment dans mongoDB, 
		// et qui permettront de renseigner nos objets SRA :
		
		//logger.debug("!!!!!!!!!!!!!!!!!!!!!!! readsetCode = " + readSet.code);
		String codeSample = sraCodeHelper.generateSampleCode(readSet, readSet.projectCode, strategySample);

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
			//NGL-4266 :
			String sampleAttributes = buildAttributes(readSet);
			if(StringUtils.isNotBlank(sampleAttributes)) {
				sample.attributes = sampleAttributes;
			}
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
	
	public void updateConfigurationAndSaveIndb (Configuration config, String submissionStateCode, String user) {
		if (config.state != null && StringUtils.isNotBlank(config.state.code) && NONE.equals(config.state.code)) {
			config.state.code = submissionStateCode;
			config.traceInformation.modifyDate = new Date();
			config.traceInformation.modifyUser = user;
			configurationAPI.dao_saveObject(config);
		}	
	}
					
	
	public void updateStudyAndSaveIndb(Study study, String nextStateCode, String user) {
		if (study != null && study.state != null && StringUtils.isNotBlank(study.state.code) && NONE.equals(study.state.code)) {
			study.setTraceUpdateStamp(user);
			study.setStateHistorique(nextStateCode, user);
			studyAPI.dao_saveObject(study);
		}
	}
	
	public void updateSampleAndSaveIndb(Sample sample, String nextStateCode, String user) {
		if (sample != null && sample.state != null && StringUtils.isNotBlank(sample.state.code) && NONE.equals(sample.state.code)) {
			sample.setTraceUpdateStamp(user);
			sample.setStateHistorique(nextStateCode, user);
			sampleAPI.dao_saveObject(sample);
		}
	}
		
	public void updateProjectAndSaveIndb(Project project, String nextStateCode, String user) {
		if (project != null && project.state != null && StringUtils.isNotBlank(project.state.code) && NONE.equals(project.state.code)) {
			project.setTraceUpdateStamp(user);
			project.setStateHistorique(nextStateCode, user);
			projectAPI.dao_saveObject(project);
		}
	}
	
	public void updateAnalysisAndSaveIndb(Analysis analysis, String nextStateCode, String user) {
		if (analysis != null && analysis.state != null && StringUtils.isNotBlank(analysis.state.code) && NONE.equals(analysis.state.code)) {
			analysis.setTraceUpdateStamp(user);
			analysis.setStateHistorique(nextStateCode, user);
			analysisAPI.dao_saveObject(analysis);
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
			if (readSet == null) {
				throw new SraException("null readSet in read sets (should not happen)");
			} 
			readSet.traceInformation.modificationStamp(user);
			State newState = IStateReference.createStateHistorique(readSet.submissionState, submissionStateCode, user);
			readSet.submissionState = newState;	
			if(submissionStateCode.equals(SUB_N)) {
				readsetsAPI.dao_update(DBQuery.is("code", readSet.code), 
						DBUpdate.set("submissionState", newState)
						.set("submissionUser", user)
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", new Date()));
			} else {
				readsetsAPI.dao_update(DBQuery.is("code", readSet.code), 
						DBUpdate.set("submissionState", newState)
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", new Date()));
			}
		}
	}		
}
