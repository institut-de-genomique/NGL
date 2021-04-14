package models.sra.submit.common.instance;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;

import fr.cea.ig.DBObject;
//import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
//import fr.cea.ig.ngl.dao.api.sra.StudyAPI;

import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Configuration;
import models.sra.submit.sra.instance.Experiment;
import models.sra.submit.util.SraException;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;
// objet qui decrit ce qu'on soumet à l'EBI à l'instant t.
public class Submission extends DBObject implements IValidation, IStateReference {

	public enum Type {
		CREATION,
		RELEASE,
		UPDATE
	}
	
	//public String alias;         // required mais remplacé par code herité de DBObject, et valeur = CNS_projectCode_date_num
	//public String projectCode = null;     // required pour nos stats //Reference code de la collection project NGL
	public List<String> projectCodes = new ArrayList<>();
	public Object ok = null;
 	public String accession = null;       // numeros d'accession attribué par ebi */
 	@Deprecated
 	public Date creationDate = null;
	public Date submissionDate = null;
	@Deprecated
	public String creationUser = null;
	public List<String> refStudyCodes = new ArrayList<>();  // Liste de tous les codes des AbstractStudy (ExternalStudy et Study) referencés par cette soumission, pas forcement à soumettre à l'EBI.
	public List<String> refSampleCodes = new ArrayList<>(); // liste de tous les codes des AbstractSamples (ExternalSample ou Sample) references par cette soumission, pas forcement a soumettre à l'EBI.
	//public List<String> refReadSetCodes = new ArrayList<String>(); // liste des codes des readSet references par cette soumission(pas de soumission).
	public String studyCode = null;          // study à soumettre à l'ebi si strategy_internal_study ou bien study à rendre public
	public String analysisCode = null;       // analysis à soumettre à l'ebi
	public String ebiProjectCode = null;        // project à soumettre à l'ebi si update du project pour locus_tag
	// voir ticket 3102
	public List<String> sampleCodes = new ArrayList<>(); // liste des codes des sample à soumettre à l'ebi
	public List<String> experimentCodes = new ArrayList<>(); // liste des codes des experiments à soumettre à l'ebi
	public List<String> runCodes = new ArrayList<>(); // liste des codes des runs à soumettre à l'ebi
	public String configCode = null;
	public String submissionDirectory = null;
	//public String submissionTmpDirectory = null;
	@Deprecated
	public Boolean release; // si true  : soumission pour levée de confidentialite d'un study, 
//	                                // si false : soumission de données (toujours en confidential)
//		
//	public String mode;
	public Type type;               // CREATION si soumission de données (toujours en confidential)  
									// RELEASE  si release d'un study
									// UPDATE   si modifications de données à l'EBI.
	
	public String xmlStudys = null;  // nom relatif du fichier xml des studys rempli uniquement si le fichier existe.
	public String xmlSamples = null;
	public String xmlExperiments = null;
	public String xmlRuns = null;
	public String xmlSubmission = null;
	public String xmlAnalysis = null;
	public String xmlProjects = null;
	public String ebiResult = null; // Fichier resultat de la commande curl qui doit contenir les AC attribués par l'EBI
	//public String userSubmission; // login du bioinfo qui a creer ticket.
	@Deprecated
	public Map<String, UserRefCollabType> mapUserClone;
	
	public Map<String, UserRefCollabType> mapUserRefCollab;//= new HashMap<String, UserRefCollabType>();

	public State state = new State(); // Reference sur "models.laboratory.common.instance.state" 
	// pour gerer les differents etats de l'objet en fonction de l'avancement dans le workflow de la soumission
	
	public TraceInformation traceInformation = new TraceInformation();// .Reference sur "models.laboratory.common.instance.TraceInformation" 
//	private Object validationDate; // aucune utilite et normalement champs jamais present en prod.
		// pour loguer les dernieres modifications utilisateurs
	public String adminComment;
	private static final play.Logger.ALogger logger = play.Logger.of(Submission.class);

	public Submission() {
		//creationDate = new Date();
	}
	
	public Submission(String user) {
		//DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");	
		//String st_my_date = dateFormat.format(courantDate);	
		// determination du repertoire de soumission dans methode activate SubmissionServices
//		Date courantDate = new Date();
//		this.creationDate = courantDate;
		this();
		this.traceInformation = new TraceInformation();
		this.traceInformation.setTraceInformation(user);
		//this.creationUser = user;
	}
	
	
	
	public Submission(String user, List<String>projectCodes) {
		this(user);
		for (String projectCode : projectCodes) {
			if (StringUtils.isNotBlank(projectCode)) {
				this.projectCodes.add(projectCode);
			}
		}
		this.projectCodes = Iterables.filter(projectCodes, StringUtils::isNotBlank).toList();  
	}


	@Override
	public State getState() {
		return state;
	}

	@Override
	public void setState(State state) {
		this.state = state;
	}

	
	@Override
	public TraceInformation getTraceInformation() {
		return traceInformation;
	}
	

	
	public void validateInvariants(ContextValidation contextValidation) {
		String undifinedValueInRepriseHistorique = "undefinedValue_repriseHistorique";
		contextValidation = contextValidation.appendPath("submission");
//		if (contextValidation.isUpdateMode()) {
//			ValidationHelper  .validateNotEmpty    (contextValidation, creationUser, "creationUser");
//		}
		// verifier que projectCode est bien renseigné et existe dans lims :
		CommonValidationHelper.validateProjectCodes(contextValidation, projectCodes);

		// Verifier que status est bien renseigné avec valeurs autorisees et que submissionDirectory est bien renseigné une
		// fois que l'objet est en status "inWaiting" (etape activate de la soumission)
		CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.SRASubmission, state);

		if (StringUtils.isNotBlank(state.code)) {
			switch (state.code.toUpperCase()) {
			case "SUB-SRD-IW"   :
			case "SUBR-SMD-IW"  :
			case "IP-SUB"       :
			case "IP-SUB-R"     :
			case "SUB-F"        :
				ValidationHelper.validateNotEmpty(contextValidation, this.submissionDirectory , "submissionDirectory");
				//ValidationHelper.validateNotEmpty(contextValidation, this.creationDate ,        "creationDate");
				break;
			default:
				// Do nothing
			}
		}
		CommonValidationHelper.validateIdPrimary(contextValidation, this);
		logger.debug("Submission.validate.validateInvariants: avant validateTraceInformationRequired");
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);			
		logger.debug("Submission.validate.validateInvariants: apres validateTraceInformationRequired");
	
		// Dans le cas d'une soumission pour une release, on n'applique pas le reste des validations
//		if (release)
		switch (type) {
		case RELEASE :	return; // ok - on n'applique pas le reste des validations
		case UPDATE   : return; // on n'applique pas le reste des validations 
//		default       : break;  // mieux d'enumerer tous les cas car si ajout non geré ici, 
		//on sera avertit par eclipse.
		case CREATION  : break; // on applique le reste des validations
		}
		if (StringUtils.isBlank(this.configCode)) {
//			contextValidation.addError("submission non evaluable ", "sans configCode dans la soumission '" + code + "'");
			return;
		} 
		
//		Configuration config = MongoDBDAO.findByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, configCode);
		ConfigurationAPI configurationAPI = ConfigurationAPI.get();
		Configuration config = configurationAPI.dao_getObject(configCode);
		if (config == null) {
			if (this.configCode.equals(undifinedValueInRepriseHistorique)) {
				// pas de configuration associé pour une soumission hors ngl-sub
			} else {
				contextValidation.addError("submission.configCode", "objet configuration '" + configCode + "' qui n'existe pas dans base");
			}
		} else { 
			// pas de validation integrale de config qui a ete stocke dans la base donc valide mais verification
			// de quelques contraintes en lien avec soumission.
			if (StringUtils.isBlank(config.state.code)){
				contextValidation.addError("config.state.code", "'" + state.code + "' n'est pas à la valeur attendue 'used'");
			}
//			if (StringUtils.isBlank(config.strategyStudy)){
//				contextValidation.addError("strategy_study", "champs qui doit etre renseigne dans la configuration passee dans le contexte de validation");
//			} else if (config.strategyStudy.equalsIgnoreCase("strategy_internal_study")) {
//				// Le champs studyCode est rempli ssi le study est à soumettre (state = N)
//				/*if (StringUtils.isBlank(this.studyCode)) {
//						contextValidation.addErrors("strategy_study", "strategy_internal_study incompatible avec studyCode vide");
//					}*/
//			} else if (config.strategyStudy.equalsIgnoreCase("strategy_external_study")) {
//				if (StringUtils.isNotBlank(studyCode)){
//					contextValidation.addError("strategy_study", "strategy_external_study incompatible avec studyCode renseigne : '" + studyCode +"'");
//				}
//				// On peut avoir donné un seul studyAc externe pour toute la soumission via l'interface
//			} else {
//				contextValidation.addError("strategy_study", "valeur non attendue '" + config.strategyStudy + "'");
//			}
			
			switch(config.strategyStudy) {
				case  STRATEGY_CODE_STUDY :
					break; // ok on est dans le bon cas.
				case  STRATEGY_AC_STUDY :	
					if (StringUtils.isNotBlank(studyCode)){
						contextValidation.addError("strategy_study", " STRATEGY_AC_STUDY incompatible avec studyCode renseigne : '" + studyCode +"'");
					}
					break;
				default:
					contextValidation.addError("strategy_study", "valeur non attendue '" + config.strategyStudy + "'");
			}
			
//			if (StringUtils.isBlank(config.strategySample)){
//				contextValidation.addError("strategy_sample", "champs qui doit etre renseigne dans la configuration passee dans le contexte de validation");
//			} else {
//				if (config.strategySample.equalsIgnoreCase("strategy_external_sample")) {
//					if (sampleCodes.size() != 0) {
//						contextValidation.addError("strategy_external_sample incompatible avec samples à soumettre : ", "taille sampleCode = "  + sampleCodes.size());
//					}
//					// On peut avoir donné un seul sampleAc externe pour toute la soumission via l'interface
//				}
//			}
			
			switch(config.strategySample) {
				case  STRATEGY_CODE_SAMPLE_TAXON :
					break; // ok on est dans le bon cas.
				case  STRATEGY_CODE_SAMPLE_BANK :	
					break;
				case  STRATEGY_CODE_SAMPLE_REFCOLLAB :	
					break;
				case  STRATEGY_AC_SAMPLE :
					if (sampleCodes.size() != 0) {
						contextValidation.addError("STRATEGY_AC_SAMPLE incompatible avec samples à soumettre : ", "taille sampleCode = "  + sampleCodes.size());
					}
					break;
				default:
					contextValidation.addError("strategy_sample", "valeur non attendue '" + config.strategySample + "'");
			}
		}
		
		if (StringUtils.isBlank(studyCode) && sampleCodes.size() == 0 &&  experimentCodes.size() == 0) {
			contextValidation.addError("studyCode, sampleCodes et experimentCodes ::", "Les 3 champs ne peuvent pas etre vides pour une soumission" + "taille des experiments = " +  this.experimentCodes.size() + ", taille des sample = "+ this.sampleCodes.size());
		}		
		CommonValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.SRA_SUBMISSION_COLL_NAME);		
	}
	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("submission");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		SubmissionAPI submissionAPI = SubmissionAPI.get();
		if(submissionAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " existe deja dans la base de données et MODE CREATION");
		}	
	}

	private void validateDelete(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("submission");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		SubmissionAPI submissionAPI = SubmissionAPI.get();
		if(! submissionAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE DELETE");
		}	
	}
	

	private void validateUpdate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("submission");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);	
		SubmissionAPI submissionAPI = SubmissionAPI.get();
		if(! submissionAPI.dao_checkObjectExist("code", code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE UPDATE");
		}
		// Ajouter contrainte pour verifier qu'aucun objet à updater à l'EBI ne soit de type external
		if (StringUtil.isNotBlank(studyCode)) {
			AbstractStudyAPI abstStudyAPI = AbstractStudyAPI.get();
			AbstractStudy abstStudy = abstStudyAPI.dao_getObject(studyCode);
			if(abstStudy == null) {
				contextValidation.addError("studycode", studyCode + " n'existe pas dans la base de données et MODE UPDATE");
			} else {
				if (abstStudy instanceof ExternalStudy) {
					contextValidation.addError("studycode", studyCode + " ne peut pas etre mis à jour à l'EBI car ExternalStudy");
				}
			}
		}
		AbstractSampleAPI abstSampleAPI = AbstractSampleAPI.get();
		for(String sampleCode : sampleCodes) {
			AbstractSample abstSample = abstSampleAPI.dao_getObject(sampleCode);
			if(abstSample == null) {
				contextValidation.addError("sampleCodes", sampleCode + " n'existe pas dans la base de données et MODE UPDATE");
			} else {
				if (abstSample instanceof ExternalSample) {
					contextValidation.addError("sampleCodes", sampleCode + " ne peut pas etre mis à jour à l'EBI car ExternalSample");
				}
			}
		}
		ExperimentAPI experimentAPI = ExperimentAPI.get();
		for(String experimentCode : experimentCodes) {
			Experiment experiment = experimentAPI.dao_getObject(experimentCode);
			if(experiment == null) {
				contextValidation.addError("experimentCodes", experimentCode + " n'existe pas dans la base de données et MODE UPDATE");
			} 
		}		
	}
	
	@Override
	public void validate(ContextValidation contextValidation) {
		switch (contextValidation.getMode()) {
		case CREATION:
			validateCreation(contextValidation);
			break;
		case UPDATE:
			validateUpdate(contextValidation);
			break;
		case DELETE:
			validateDelete(contextValidation);
			break;
		default: // autre cas undefined notamment
			contextValidation.addError("ERROR", "contextValidation.getMode() != de CREATION, UPDATE ou DELETE (undefined ?)");
			break;	
		} 
		validateInvariants(contextValidation);
		
		//logger.debug("ZZZZZZZZZZ   texte de validation = "+ contextValidation.getErrors().toString());
		if (contextValidation.hasErrors()) {
			//logger.debug("ZZZZZZZZZZZZ  Et dans hasError() :");
			contextValidation.displayErrors(logger, "debug");
		} else {
			//logger.debug("ZZZZZZZZZZZZZZ  Aucune erreur dans hasError:");
		}
		
		
	}
	
	/**
	 * Cree le repertoire de soumission indiqué dans le champs submissionDirectory.
	 * @param  submission   submission
	 * @throws SraException SRA exception
	 */
	public void createDirSubmission() throws SraException {
		logger.debug("Dans createDirSubmission");
		if (this.submissionDirectory== null || StringUtils.isBlank(this.submissionDirectory)) {
			throw new SraException("La soumission " + this.code 
								 + " n'est pas renseigné pour le champs submissionDirectory");
		}
		logger.debug("submissionDirectory = " + this.submissionDirectory);
		File dataRep = new File(this.submissionDirectory);
		logger.info("Creation du repertoire de soumission" + this.submissionDirectory);
		if (dataRep.exists()) {
			logger.debug("Le repertoire " + dataRep + " existe deja !!! (soumission concurrente ?)");
			throw new SraException("Le repertoire " + dataRep + " existe deja !!! (soumission concurrente ?)");
		} else {
			if (!dataRep.mkdirs()) {	
				logger.debug("Impossible de creer le repertoire " + dataRep + " ");
				throw new SraException("Impossible de creer le repertoire " + dataRep + " ");
			}
		}
	}	
}
