package models.sra.submit.sra.instance;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.util.StringUtil;

import fr.cea.ig.DBObject;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.dao.api.sra.AbstractSampleAPI;
import fr.cea.ig.ngl.dao.api.sra.AbstractStudyAPI;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import fr.cea.ig.ngl.dao.api.sra.ExperimentAPI;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;

import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.Comment;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
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
	
	public enum TypeRawDataSubmitted {
		bionanoRawData, // bionano
		defaultRawData, // illumina ou nanopore ou 454
		withoutRawData  // study ou samples sans données brutes.
	}	
	
	//public String alias;                  // required mais remplacé par code herité de DBObject, et valeur = CNS_projectCode_date_num
	//public String projectCode = null;     // required pour nos stats //Reference code de la collection project NGL             
	public List<String> projectCodes     = new ArrayList<>();
 	public String accession              = null;  // numeros d'accession attribué par ebi */;
	public Date submissionDate           = null;
	public List<String> refStudyCodes    = new ArrayList<>(); // Liste de tous les codes des AbstractStudy (ExternalStudy et Study) referencés par cette soumission, pas forcement à soumettre à l'EBI.
	public List<String> refSampleCodes   = new ArrayList<>(); // liste de tous les codes des AbstractSamples (ExternalSample ou Sample) references par cette soumission, pas forcement a soumettre à l'EBI.
	public String studyCode              = null;  // study à soumettre à l'ebi si strategy_internal_study ou bien study à rendre public 
	                                              // si study a mettre à jour, alors project correspondant mis à jour aussi avec xmlProjects
	public String analysisCode           = null;  // analysis à soumettre à l'ebi

	


	public String umbrellaCode = null;  // project à soumettre à l'ebi si creationProjectUmbrella
	// voir ticket 3102
	public List<String> sampleCodes     = new ArrayList<>(); // liste des codes des sample à soumettre à l'ebi
	public List<String> experimentCodes = new ArrayList<>(); // liste des codes des experiments à soumettre à l'ebi
	public List<String> runCodes        = new ArrayList<>(); // liste des codes des runs à soumettre à l'ebi
	public String       configCode      = null;
	public String       submissionDirectory = null;
	public Type         type;               // CREATION si soumission de données (toujours en confidential)  
									        // RELEASE  si release d'un study
									        // UPDATE   si modifications de données à l'EBI.
	
	public String xmlStudys      = null;    // nom relatif du fichier xml des studys rempli uniquement si le fichier existe.
	public String xmlProjects    = null;    // a conserver car si update du study alors ecriture du fichier study.xml mais aussi du fichier project.xml
	public String xmlSamples     = null;
	public String xmlExperiments = null;
	public String xmlRuns        = null;
	public String xmlSubmission  = null;
	public String xmlAnalysis    = null;
	public String xmlUmbrella    = null;
	public String ebiResult      = null; // Fichier resultat de la commande curl qui doit contenir les AC attribués par l'EBI
	public String adminComment;
    public List<Comment>comments = new ArrayList<Comment>();// commentaires des utilisateurs.

	public Map<String, UserRefCollabType> mapUserRefCollab; //= new HashMap<String, UserRefCollabType>();
	public TraceInformation traceInformation = new TraceInformation();  // Reference sur "models.laboratory.common.instance.TraceInformation" 
                                                                        // pour loguer les dernieres modifications utilisateurs
	public State state           = new State(); // Reference sur "models.laboratory.common.instance.state" 
	                                            // pour gerer les differents etats de l'objet en fonction de l'avancement dans le workflow de la soumission
	
	//NGL-2066 :
	public TypeRawDataSubmitted typeRawDataSubmitted; 

	
	public Submission() {
	}
	
	public Submission(String user) {
		this();
		this.traceInformation = new TraceInformation();
		this.traceInformation.setTraceInformation(user);
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
		//logger.debug("!!!!!!!!!!!!!! umbrellaCode = " + this.umbrellaCode);
		// verifier que (CNS)projectCodes est bien renseigné et existe dans NGL si soumission autre que projectumbrella
		if(StringUtil.isBlank(this.umbrellaCode)) {
			CommonValidationHelper.validateProjectCodes(contextValidation, projectCodes);
		}

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
				break;
			default:
				// Do nothing
			}
		}
		CommonValidationHelper.validateIdPrimary(contextValidation, this);
		//logger.debug("Submission.validate.validateInvariants: avant validateTraceInformationRequired");
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);			
		//logger.debug("Submission.validate.validateInvariants: apres validateTraceInformationRequired");
		CommonValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.SRA_SUBMISSION_COLL_NAME);

		// Dans le cas d'une soumission pour une release, un update ou une creation de type bionano ou sans rawData on ne valide pas de configuration :
		switch (type) {
		case RELEASE :	return; // ok - on n'applique pas le reste des validations
		case UPDATE   : return; // on n'applique pas le reste des validations 
		//		default       : break;  // mieux d'enumerer tous les cas car si ajout non geré ici, on sera avertit par eclipse.
		case CREATION  : 
			if (typeRawDataSubmitted.equals(TypeRawDataSubmitted.withoutRawData) ) {
				if (StringUtils.isBlank(umbrellaCode) && StringUtils.isBlank(studyCode) && sampleCodes.size() == 0 ) {
					contextValidation.addError("umbrellaCode, studyCode, sampleCodes ::", "Les 3 champs ne peuvent pas etre vides pour une soumission" + "taille des experiments = " +  this.experimentCodes.size() + ", taille des sample = "+ this.sampleCodes.size());
				}	
				return; // on n'applique pas verifs de la configuration
			} else if (typeRawDataSubmitted.equals(TypeRawDataSubmitted.bionanoRawData)) {
				if (StringUtils.isBlank(analysisCode)) {
					contextValidation.addError("submission avec type de donnees soumises bionanoRawData ", " sans analysisCode ???");
				}
				return; // on n'applique pas la validation sur configuration si soumission != de illumina (nanopore, 454)
			} else if (typeRawDataSubmitted.equals(TypeRawDataSubmitted.defaultRawData)) { 
				if(experimentCodes==null || experimentCodes.size()==0) {
					contextValidation.addError("submission avec defaultRawData  ", " sans experimentCodes ???");
				}
				break; // on applique le reste des validations sur la configuration
			} else {
				contextValidation.addError("submission avec typeRawDataSubmitted ", " non reconnu" + typeRawDataSubmitted + "'");
			}
		}

		// On devrait avoir un configCode pour les soumissions en creation sauf si study/project ou umbrella, mais il y a dans la base
		// des soumission sans configCode pour des creations de samples dues à des reprises d'historique.
		// verifier si la soumission contient configCode
		String adminComment = "Creation dans le cadre d'une reprise d'historique";

		if (StringUtil.isBlank(this.configCode)) {
			if (! adminComment.equals(this.adminComment)) {
				contextValidation.addError("submission non evaluable ", "sans configCode dans la soumission '" + code + "'");
				return;
			}
			return;
		} 

		if (this.configCode.startsWith(undifinedValueInRepriseHistorique)) {
			// pas de configuration associé pour une soumission hors ngl-sub
			return;
		}


		// Verifier si configCode dans submission existe dans base en mode CREATION
		ConfigurationAPI configurationAPI = ConfigurationAPI.get();
		//Configuration config = configurationAPI.dao_getObject(this.configCode);
		Configuration config = configurationAPI.get(this.configCode);

		if (config == null) {
			contextValidation.addError("submission.configCode", "objet configuration '" + configCode + "' qui n'existe pas dans base");
			// on ne verifie pas la configuration dans base
			return;
		}  

		// verifier config de la base :
		// pas de validation integrale de config qui a ete stocke dans la base donc valide mais verification
		// de quelques contraintes en lien avec soumission.
		if (StringUtils.isBlank(config.state.code)){
			contextValidation.addError("config.state.code", "'" + state.code + "' n'est pas à la valeur attendue 'used'");
		}			
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
			Experiment experiment = experimentAPI.get(experimentCode);
			if(experiment == null) {
				contextValidation.addError("experimentCodes", experimentCode + " n'existe pas dans la base de données et MODE UPDATE");
			} 
		}		
	}
	
	@Override
	public void validate(ContextValidation contextValidation) {
		String mess = "validation de la soumission ";
		if (StringUtils.isNotBlank(code)) {
			mess += "." + code;
		}
		//logger.debug(this.code + "  !!!!!!!!!!!!!!!!!!!!!!!!!!!");
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
//		
//		if (contextValidation.hasErrors()) {
//			contextValidation.displayErrors(logger, "debug");
//		} 
		contextValidation.removeKeyFromRootKeyName(mess);

	}
	
	
	/**
	 * Cree le repertoire de soumission indiqué dans le champs submissionDirectory.
	 * @param  submission   submission
	 * @throws SraException SRA exception
	 */
	public void createDirSubmission() throws SraException {
		//logger.debug("Dans createDirSubmission");
		if (this.submissionDirectory== null || StringUtils.isBlank(this.submissionDirectory)) {
			throw new SraException("La soumission " + this.code 
								 + " n'est pas renseigné pour le champs submissionDirectory");
		}
		//logger.debug("submissionDirectory = " + this.submissionDirectory);
		File dataRep = new File(this.submissionDirectory);
		//logger.info("Creation du repertoire de soumission" + this.submissionDirectory);
		if (dataRep.exists()) {
			//logger.debug("Le repertoire " + dataRep + " existe deja !!! (soumission concurrente ?)");
			throw new SraException("Le repertoire " + dataRep + " existe deja !!! (soumission concurrente ?)");
		} else {
			if (!dataRep.mkdirs()) {	
				//logger.debug("Impossible de creer le repertoire " + dataRep + " ");
				throw new SraException("Impossible de creer le repertoire " + dataRep + " ");
			}
		}
	}	
}
