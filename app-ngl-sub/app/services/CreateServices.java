package services;
import static ngl.refactoring.state.SRASubmissionStateNames.NONE;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_F;
import static ngl.refactoring.state.SRASubmissionStateNames.SUB_N;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
//import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import java.util.Map.Entry;

import javax.inject.Inject;
 
import org.apache.commons.lang3.StringUtils;
import org.mongojack.DBQuery;
import org.mongojack.DBUpdate;

//import fr.cea.ig.lfw.utils.Iterables;
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
//import fr.cea.ig.ngl.dao.samples.SamplesAPI;
//import fr.cea.ig.ngl.dao.sra.SampleDAO;
//import fr.cea.ig.ngl.dao.sra.SubmissionDAO;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TBoolean;
import models.laboratory.run.instance.InstrumentUsed;
import models.laboratory.run.instance.Lane;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.common.instance.AbstractSample;
import models.sra.submit.common.instance.AbstractStudy;
import models.sra.submit.common.instance.ExternalSample;
import models.sra.submit.common.instance.ExternalStudy;
import models.sra.submit.common.instance.IStateReference;
import models.sra.submit.common.instance.Readset;
import models.sra.submit.common.instance.Sample;
import models.sra.submit.common.instance.Study;
import models.sra.submit.common.instance.Submission;
import models.sra.submit.common.instance.UserRefCollabType;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Configuration.StrategySample;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.sra.instance.RawData;
import models.sra.submit.sra.instance.ReadSpec;
import models.sra.submit.sra.instance.Run;
import models.sra.submit.util.SraCodeHelper;
import models.sra.submit.util.SraException;
import models.sra.submit.util.SraValidationException;
import models.sra.submit.util.VariableSRA;
//import ngl.refactoring.state.SRAStudyStateNames;
import play.libs.ws.WSClient;
import sra.api.submission.SubmissionNewAPITools;
import validation.ContextValidation;



public class CreateServices {
	private static final play.Logger.ALogger logger = play.Logger.of(CreateServices.class);

	private final ConfigurationAPI  configurationAPI;
	private final SubmissionAPI 	submissionAPI;
	private final StudyAPI          studyAPI;
	private final AbstractStudyAPI  abstractStudyAPI;
	private final ExternalStudyAPI  externalStudyAPI;
	private final SampleAPI         sampleAPI;
	private final AbstractSampleAPI abstractSampleAPI;
	private final ExternalSampleAPI externalSampleAPI;
	private final ExperimentAPI     experimentAPI;
	//private final SamplesAPI        laboSampleAPI;
	private final ReadSetsAPI       readsetsAPI;  // collection readset de ngl-seq
	private final ReadsetAPI        readsetAPI;   // collection readset de sra
	private final RunsAPI           laboRunAPI;
    private final SraCodeHelper     sraCodeHelper;
	private final SubmissionNewAPITools  submissionNewAPITools;
	private final String            patternSampleAC = "ERS\\d+";
	private final String            patternStudyAC  = "ERP\\d+";


	@Inject
	public CreateServices(ConfigurationAPI  configurationAPI,
			              SubmissionAPI     submissionAPI,
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
			              ProjectsAPI       projectAPI,
			              WSClient          ws,
			              SraCodeHelper     sraCodeHelper,
     		          	  SubmissionNewAPITools   submissionNewAPITools) {

		//this.ctx = ctx;
		this.configurationAPI  = configurationAPI;
		this.submissionAPI     = submissionAPI;
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
//		this.projectAPI        = projectAPI;
//		this.ws                = ws;
		this.sraCodeHelper     = sraCodeHelper;
		this.submissionNewAPITools   = submissionNewAPITools;

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

		String user = contextValidation.getUser();		
		if (readSetCodes == null || readSetCodes.size() < 1 ) {
			logger.debug("Aucun readset à soumettre n'a été indiqué");
			//throw new SraException("initPrimarySubmission", "Aucun readset à soumettre n'a été indiqué");
		}
		//logger.debug("Dans init : acStudy = " + acStudy + " et acSample= " + acSample);
		if(mapUserRefCollab != null) {
			logger.debug("initPrimarySubmission:: taille map clone = " + mapUserRefCollab.size());
		}
		if(readSetCodes != null) {
			logger.debug("initPrimarySubmission::Taille readSetCodes = " + readSetCodes.size());
		}
		if (StringUtils.isBlank(configCode)) {
			throw new SraException("initPrimarySubmission", "La configuration a un code à null");
		}
		validateParamForSubmission(readSetCodes, studyCode, configCode, acStudy, acSample, mapUserRefCollab, contextValidation);
		// Liste des projectCodes de la soumission, qui doit contenir les 
		// projectCodes des objets soumis : readsets, samples, experiments et study
		// le projectCode de l'experiment et du sample est le meme que celui du readset.
		List <String> subProjectCodes = new ArrayList<>();
		Configuration config = configurationAPI.dao_getObject(configCode);
		
		//logger.debug("apres le validateParamForSubmission");
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

		// Cas d'une soumission d'un study sans readset et donc sans samples ni experiments ni runs:
		if (readSetCodes == null) {
			switch (config.strategyStudy) {
			case STRATEGY_AC_STUDY : {
				throw new SraException("Aucun readset à soumettre et strategy AC_study ");
			}
			case STRATEGY_CODE_STUDY : {
				Study study = null;
				if (!studyAPI.dao_checkObjectExist("code", studyCode)) {
					throw new SraException("Strategy STRATEGY_CODE_STUDY et study " + studyCode + " inexistant dans base");
				}
				study = studyAPI.dao_findOne(DBQuery.is("code", studyCode));				
				// Mise à jour de l'objet submission pour les study references :
				if (!submission.refStudyCodes.contains(study.code)) {
					submission.refStudyCodes.add(study.code);
				}
				// Mise a jour de l'objet submission pour le study à soumettre :
				if(NONE.equals(study.state.code)) {
					submission.studyCode = study.code;
					// verifier que le study a soumettre n'est pas deja à soumettre dans une autre soumission:
					// List <Submission> submissionList = submissionAPI.dao_find(DBQuery.is("studyCode", studyCode).is("type", Submission.Type.CREATION)).toList();
					// verifier que le study a soumettre n'est pas deja impliqué dans une autre soumission:
					List <Submission> submissionList = submissionAPI.dao_find(DBQuery.is("studyCode", studyCode)).toList();
					if (submissionList.size() != 0) {
						String message = studyCode + " utilise par plusieurs objets submissions, studyState = " + study.state.code + "\n";
						for (Submission sub: submissionList) {
							message += studyCode + " utilise par objet Submission " + sub.code + " avec submissionState = " + sub.state.code + "\n";
						}
						logger.debug(message);
						throw new SraException(message);
					} else {
						//logger.debug(studyCode + " utilise par aucune soumission autre que " + submission.code);	
					}	
					// Ajout des projectCodes du study dans subProjectCodes seulement si study à soumettre:
					for(String projCode : study.projectCodes) {
						if ( ! subProjectCodes.contains(projCode)) {
							subProjectCodes.add(projCode);
						}
					}
				}
				
				if (!listAbstractStudies.contains(study)) {
					listAbstractStudies.add(study);
				}	
				break;
			}
			default :
				throw new SraException("Strategy pour study non attendue : " + config.strategyStudy);
			}
		// Cas d'une soumission  avec readset:
		} else {
			for (String readSetCode : readSetCodes) {
				ReadSet readSet = readsetsAPI.get(readSetCode);
				readSets.add(readSet);
				if( ! subProjectCodes.contains(readSet.projectCode)) {
					subProjectCodes.add(readSet.projectCode);
				}
			}
		}
		
		// Cas d'une soumission  avec readset:
		for (ReadSet readSet : readSets) {
			//logger.debug("readSet : {}", readSet.code);
			// Verifier que c'est une premiere soumission pour ce readSet
			// Verifiez qu'il n'existe pas d'objet experiment referencant deja ce readSet

			// Recuperer scientificName via NCBI pour ce readSet. Le scientificName est utilisé dans la construction
			// des samples et des experiments 
//			String laboratorySampleCode = readSet.sampleCode;
//			models.laboratory.sample.instance.Sample laboratorySample = laboSampleAPI.get(laboratorySampleCode);
//			String scientificName = laboratorySample.ncbiScientificName;
			if (readSet.sampleOnContainer == null) {
				throw new SraException("Absence du champs sampleOnContainer pour le readset " + readSet.code);
			}
			//logger.debug("readset.code = " + readSet.code);
			String scientificName = readSet.sampleOnContainer.ncbiScientificName;
			
			if (StringUtils.isBlank(scientificName)) {
				//scientificName=updateLaboratorySampleForNcbiScientificName(taxonId, contextValidation);
				throw new SraException("Pas de recuperation du nom scientifique pour le readSet "+ readSet.code);
			} else {
				//logger.debug("Nom du scientific Name = "+ scientificName);
			}

			// Creer les objets avec leurs alias ou code, les instancier completement et les sauver.
			Experiment experiment = createExperimentEntity(readSet, scientificName, user);

			experiment.librarySelection = config.librarySelection;
			experiment.librarySource = config.librarySource;
			experiment.libraryStrategy = config.libraryStrategy;
			experiment.libraryConstructionProtocol = config.libraryConstructionProtocol;
//			String refCollab = laboratorySample.referenceCollab;
			String refCollab = readSet.sampleOnContainer.referenceCollab;
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
					logger.debug("cas STRATEGY_AC_SAMPLE avec mapUserRefCollab");
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
					if( ! sampleAc.matches(patternSampleAC)) {
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque mapUserRefCollab pour clone '" + refCollab + "' contient un AC sample " + sampleAc + " qui n'est pas de la forme attendu\n";
						continue;	
					}
					// recuperation dans base ou création du sample avec status SUB-F (si AC alors SUB-F)

					if(abstractSampleAPI.dao_checkObjectExist("accession", sampleAc)) {
						abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("accession", sampleAc));
						if (abstSample != null) {
							sample_code = abstSample.code;
							sample_accession = abstSample.accession;
							//logger.debug("sample existant dans base avec sample_code = " + sample_code);
						}
					} else {
//						externalSample = fetchExternalSample(sampleAc, user);
						//logger.debug("construction d'un nouveau sample avec user = " + user);
						externalSample = buildExternalSample(sampleAc, user);
						sample_code = externalSample.code;
						sample_accession = externalSample.accession;
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
					if( ! acSample.matches(patternSampleAC)) {
						countError++;
						errorMessage = errorMessage + "STRATEGY_AC_SAMPLE et acSample transmis " + acSample + " qui n'est pas de la forme attendue\n";
						continue;
					}
					if(abstractSampleAPI.dao_checkObjectExist("accession", acSample)) {
						abstSample = abstractSampleAPI.dao_findOne(DBQuery.is("accession", acSample));
						if (abstSample != null) {
							sample_code = abstSample.code;
							sample_accession = abstSample.accession;
						}
					} else {
						externalSample = fetchExternalSample(acSample, user);
						sample_code = externalSample.code;
						sample_accession = externalSample.accession;
					}
				} else {
					throw new SraException("mapUserRefCollab vide et strategy STRATEGY_AC_SAMPLE");
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
				sample = fetchSample(readSet, config.strategySample, scientificName, user);
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
						logger.debug(message);
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
							logger.debug("submission.mapUserRefCollab vide= ");
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
					logger.debug("studyAc dans mapRefCollab = " + studyAc);
					if ( ! studyAc.matches(patternStudyAC)){
						logger.debug("Erreur detectee pour studyAc dans mapRefCollab");
						countError++;
						errorMessage = errorMessage + "Soumission impossible pour le readset '" + readSet.code + "' parceque mapUserRefCollab pour clone '" + refCollab + "' contient un AC de study " + studyAc + " qui n'est pas de la forme attendu\n";
						continue;					
					}
					if(abstractStudyAPI.dao_checkObjectExist("accession", studyAc)) {
						abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", studyAc));
						if (abstStudy != null) {
							study_accession = studyAc;
							study_code = abstStudy.code;
						}
					} else {
						externalStudy = buildExternalStudy(studyAc, user);
						study_accession = studyAc;
						study_code = externalStudy.code;
					}
				} else if (StringUtils.isNotBlank(acStudy)) {
					if( ! acStudy.matches(patternStudyAC)) {
						countError++;
						errorMessage = errorMessage + "STRATEGY_AC_STUDY et acStudy transmis " + acStudy + " qui n'est pas de la forme attendue\n";
						continue;
					}
					if(abstractStudyAPI.dao_checkObjectExist("accession", acStudy)) {
						abstStudy = abstractStudyAPI.dao_findOne(DBQuery.is("accession", acStudy));			
						if (abstStudy != null) {
							//logger.debug("study " + abstStudy.code + " qui est bien un study");
							study_accession = acStudy;
							study_code = abstStudy.code;
						}	
					} else {
						externalStudy = buildExternalStudy(acStudy, user);
						study_accession = acStudy;
						study_code = externalStudy.code;
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
						logger.debug(message);
						throw new SraException(message);
					} else {
						//logger.debug(studyCode + " utilise par aucune soumission autre que " + submission.code);
					}	
					// Ajout des projectCodes du study dans subProjectCodes seulement si study a soumettre :
					for(String projCode : study.projectCodes) {
						if ( ! subProjectCodes.contains(projCode)) {
							subProjectCodes.add(projCode);
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
			logger.debug(errorMessage);
			throw new SraException("Problemes pour creer la soumission\n" + errorMessage);
		}
		
		// Determiner le code de la soumission, une fois la liste des projectCodes definie :
		submission.projectCodes = subProjectCodes;
		submission.code = sraCodeHelper.generateSubmissionCode(subProjectCodes);
		
		// Determiner le submissionDirectory et submission.ebiResult une fois submission.code defini :
		submission.submissionDirectory = VariableSRA.submissionRootDirectory + File.separator + submission.code; 	
		logger.debug("AAAAAAAAAAAAAAA            submission.submissionDirectory=" + submission.submissionDirectory);
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
		for (AbstractSample sampleElt: listAbstractSamples) {
			if (!abstractSampleAPI.dao_checkObjectExist("code", sampleElt.code)){	
				// validation qui depend du type de l'objet.
				//logger.debug("validation du sample");
				if (sampleElt instanceof ExternalSample) {
					ExternalSample eSample = (ExternalSample) sampleElt;
					eSample.validate(contextValidation);
				} else {
					Sample sample = (Sample) sampleElt;
					sample.validate(contextValidation);
				}
				abstractSampleAPI.dao_saveObject(sampleElt);
				//logger.debug("ok pour sauvegarde dans la base du sample " + sampleElt.code);
			}
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
			logger.debug("submission.validate produit des erreurs");
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
		Configuration config = configurationAPI.dao_getObject(configCode);	
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
			if (mapUserRefCollab == null || mapUserRefCollab.isEmpty()){
				throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec configuration.strategy_sample='strategy_ac_sample' incompatible avec acSample null et mapUserRefCollab non renseigné");
			}
			break;
		default :
			throw new SraException("validateParamForSubmission", "Configuration " + config.code + " avec strategy_sample = '"+ config.strategySample + "' (valeur non attendue)");
		}
		
		validateReadSetCodesForSubmission(readSetCodes, contextValidation);
//		if (!errorMessages.equals("")) {
//			throw new SraException("validateParamForSubmission", "Probleme avec les readsetCodes : \n" + errorMessages);
//		}
		
	}
	
	
	public void validateReadSetCodesForSubmission (List<String> readSetCodes, ContextValidation contextValidation) {
		// Verifier que tous les readSetCode passes en parametres correspondent bien a des objets en base avec
		// submissionState='NONE' cad des readSets qui n'interviennent dans aucune soumission et readset valides pour soumission (readset valides pour la bioinformatique)
		if(readSetCodes == null) {
			return;
		}
//		String errorMessage = "";
//		int countError = 0;
		//contextValidation.addKeyToRootKeyName("validateReadSetCodesForSubmission");

		for (String readSetCode : readSetCodes) {

			if (StringUtils.isNotBlank(readSetCode)) {
				//logger.debug("!!!!!!!!!!!!!         readSetCode = " + readSetCode);
				//				ReadSet readSet = MongoDBDAO.findByCode(InstanceConstants.READSET_ILLUMINA_COLL_NAME, ReadSet.class, readSetCode);
				ReadSet readSet = readsetsAPI.get(readSetCode);
				if (readSet == null) {
//					errorMessage += "Le readset '" + readSetCode + "' n'existe pas dans la base\n";
//					countError++;
					contextValidation.addError("Le readset." + readSetCode, " n'existe pas dans la base de donnees");
					//throw new SraException("Le readSet " + readSetCode + " n'existe pas dans database");
				} else if ((readSet.submissionState == null)||(StringUtils.isBlank(readSet.submissionState.code))) {
//					errorMessage += "Le readset '" + readSetCode + "' n'a pas de submissionState.code\n";
//					countError++;
					contextValidation.addError("Le readset " + readSetCode, " n'a pas de submissionState.code");
					//throw new SraException("Le readSet " + readSet.code + " n'a pas de submissionState.code");
				} else if(!readSet.submissionState.code.equalsIgnoreCase(NONE)){
//					errorMessage += "Le readSet " + readSet.code + " a un submissionState.code à la valeur " + readSet.submissionState.code + "\n";
//					countError++;
					contextValidation.addError("Le readSet " + readSet.code,  " a un submissionState.code à la valeur " + readSet.submissionState.code);
					//throw new SraException("Le readSet " + readSet.code + " a un submissionState.code à la valeur " + readSet.submissionState.code);
				} else {
					Boolean alreadySubmit = experimentAPI.dao_checkObjectExist("readSetCode", readSet.code);		
					if ( alreadySubmit ) {
						// signaler erreur et passer au readSet suivant
//						countError++;
//						errorMessage  += "  - Experiment deja existant dans base pour : '" + readSet.code + "' \n";
//						contextValidation.addError("Le readSet " + readSet.code, "  - a deja un experiment dans la base de donnees");

						// Recuperer exp dans mongo
						continue;
					} 					
					// Verifier que ce readSet est bien valide avant soumission :
					if (! readSet.bioinformaticValuation.valid.equals(TBoolean.TRUE)) {
//						countError++;
//						errorMessage += "  - Soumission impossible pour le readset '" + readSet.code + "' parceque non valide pour la bioinformatique \n";
						contextValidation.addError("Le readSet " + readSet.code,"est non valide pour la bioinformatique");
						continue;
					} 
				}
			}
		}	
//		logger.debug("Dans services.ValidateReadSetCodesForSubmission, nombre d'erreur = " + countError);
//		logger.debug(errorMessage);
//		return errorMessage;
		//contextValidation.removeKeyFromRootKeyName("validateReadSetCodesForSubmission");

		
	}

	
	private Submission createSubmissionEntityForPrimarySubmission(Configuration config, 
																  String studyCode, 
																  String studyAc, 
																  String user) throws SraException{
		logger.debug("Dans createSubmissionEntityForPrimarySubmission");
		if (config == null) {
			throw new SraException("SubmissionServices::createSubmissionEntity::configuration : config null ???");
		}
		Submission submission = null;
//		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");	
		Date courantDate = new java.util.Date();
//		String st_my_date = dateFormat.format(courantDate);	
		submission = new Submission(user);
		submission.traceInformation.creationDate = courantDate;
		logger.debug("submissionCode="+ submission.code);
		submission.state = new State(SUB_N, user);
		submission.type = Submission.Type.CREATION;
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
					rawData.collabFileName = rawData.relatifName;					
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
	
	
	public ExternalSample fetchExternalSample(String sampleAc, String user) throws SraException {
		//logger.debug("Entree dans fetchExternalSample");

		ExternalSample externalSample;

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
			//logger.debug("Creation dans base du sample avec Ac " + sampleAc);
			String externalSampleCode = sraCodeHelper.generateExternalSampleCode(sampleAc);
			externalSample = new ExternalSample(); // objet avec state.code = submitted
			externalSample.accession = sampleAc;
			externalSample.code = externalSampleCode;
			externalSample.traceInformation.setTraceInformation(user);	
			externalSample.state = new State(SUB_F, user);
		}
		return externalSample;
	}

	public ExternalSample buildExternalSample(String sampleAc, String user) throws SraException {
		//logger.debug("Entree dans buildExternalSample");
		ExternalSample externalSample = new ExternalSample(); // objet avec state.code = submitted
		//logger.debug("Creation dans base du sample avec Ac " + sampleAc);
		String externalSampleCode = sraCodeHelper.generateExternalSampleCode(sampleAc);
		externalSample.accession = sampleAc;
		externalSample.code = externalSampleCode;
		externalSample.traceInformation.setTraceInformation(user);	
		externalSample.state = new State(SUB_F, user);
		//logger.debug("externalSample.accession = " + externalSample.accession);
		//logger.debug("externalSample.code = " + externalSample.code);
		//logger.debug("externalSample.state.code = " + externalSample.state.code);
		return externalSample;
	}
	
	private Sample fetchSample(ReadSet readSet, StrategySample strategySample, String scientificName, String user) throws SraException {
		// Recuperer pour chaque readSet les objets de laboratory qui existent forcemment dans mongoDB, 
		// et qui permettront de renseigner nos objets SRA :
		
		//logger.debug("!!!!!!!!!!!!!!!!!!!!!!! readsetCode = " + readSet.code);
		String codeSample = sraCodeHelper.generateSampleCode(readSet, readSet.projectCode, strategySample);
		Sample sample = null;
		if (readSet.sampleOnContainer == null) {
			throw new SraException("Absence du champs sampleOnContainer pour le readset " + readSet.code);
		}
        String taxonId = readSet.sampleOnContainer.taxonCode;
        
		
		// Si sample existe, prendre l'existant, sinon en creer un nouveau
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
			//sample.clone = laboratorySample.referenceCollab;
//			sample.refCollab = laboratorySample.referenceCollab;
			//sample.refCollab = readSet.sampleOnContainer.referenceCollab;
			sample.projectCode = readSet.projectCode;
			sample.state = new State(SUB_N, user);
			sample.traceInformation.setTraceInformation(user);			
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
		externalStudy.accession = studyAc;
		externalStudy.code = externalStudyCode;
		externalStudy.traceInformation.setTraceInformation(user);	
		externalStudy.state = new State(SUB_F, user);

		return externalStudy;
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
			
//			if ( ExternalStudy.class.isInstance(absStudy)) {
			if (absStudy instanceof ExternalStudy) {
				//logger.debug("Recuperation dans base du study avec Ac = " + studyAc +" qui est du type externalStudy ");
			} else {
				throw new SraException("Recuperation dans base du study avec Ac = " + studyAc +" qui n'est pas du type externalStudy ");
			}
			externalStudy = externalStudyAPI.dao_findOne(DBQuery.is("accession", studyAc));		
		} else {
			// Creer objet externalStudy
			String externalStudyCode = sraCodeHelper.generateExternalStudyCode(studyAc);
			externalStudy = new ExternalStudy(); // objet avec state.code = submitted
			externalStudy.accession = studyAc;
			externalStudy.code = externalStudyCode;
			externalStudy.traceInformation.setTraceInformation(user);	
			externalStudy.state = new State(SUB_F, user);
		}
		return externalStudy;
	}
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
	
	@Deprecated
	public void updateSubmissionState(Submission submission, State nextState) {
		submission.getTraceInformation().forceModificationStamp(nextState.user, nextState.date);
		submission.setState(nextState.createHistory(submission.getState()));
	}
	
	
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
	
	// methode à utiliser uniquement a la creation d'une soumission car met a jour 
	// le champs ReadSet.submissionUser qui ne doit pas etre ecrase par les differents
	// changement d'etat
	public void updateReadSetAndSaveIndbForCreation (List<ReadSet> readSets, String submissionStateCode, String user) throws SraException {
		// Updater les readSets pour le status dans la base: 
		for (ReadSet readSet : readSets) {
			if (readSet == null) {
				throw new SraException("null readSet in read sets (should not happen)");
			} else {		
				readSet.traceInformation.modificationStamp(user);
				State newState = IStateReference.createStateHistorique(readSet.submissionState, submissionStateCode, user);
				readSet.submissionState = newState;	
				readsetsAPI.dao_update(DBQuery.is("code", readSet.code), 
						DBUpdate.set("submissionState", newState)
						.set("submissionUser", user)
						.set("traceInformation.modifyUser", user)
						.set("traceInformation.modifyDate", new Date()));
			}
		}		
	}
}
