package models.sra.submit.common.instance;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.DBObject;
import fr.cea.ig.MongoDBDAO;
import fr.cea.ig.lfw.utils.Iterables;
import fr.cea.ig.ngl.dao.api.sra.SubmissionAPI;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.sra.instance.Configuration;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.utils.ValidationHelper;
import workflows.sra.submission.StateContainer;

// objet qui decrit ce qu'on soumet à l'EBI à l'instant t.
public class Submission extends DBObject implements IValidation, StateContainer {

	//public String alias;         // required mais remplacé par code herité de DBObject, et valeur = CNS_projectCode_date_num
	//public String projectCode = null;     // required pour nos stats //Reference code de la collection project NGL
	public List<String> projectCodes = new ArrayList<>();
 	public String accession = null;       // numeros d'accession attribué par ebi */
	public Date creationDate = null;
	public Date submissionDate = null;
	public String creationUser = null;
	public List<String> refStudyCodes = new ArrayList<>();  // Liste de tous les codes des AbstractStudy (ExternalStudy et Study) referencés par cette soumission, pas forcement à soumettre à l'EBI.
	public List<String> refSampleCodes = new ArrayList<>(); // liste de tous les codes des AbstractSamples (ExternalSample ou Sample) references par cette soumission, pas forcement a soumettre à l'EBI.
	//public List<String> refReadSetCodes = new ArrayList<String>(); // liste des codes des readSet references par cette soumission(pas de soumission).
	public String studyCode = null;          // study à soumettre à l'ebi si strategy_internal_study ou bien study à rendre public
	public String analysisCode = null;       // analysis à soumettre à l'ebi
	public List<String> sampleCodes = new ArrayList<>(); // liste des codes des sample à soumettre à l'ebi
	public List<String> experimentCodes = new ArrayList<>(); // liste des codes des experiments à soumettre à l'ebi
	public List<String> runCodes = new ArrayList<>(); // liste des codes des runs à soumettre à l'ebi
	public String configCode = null;
	public String submissionDirectory = null;
	//public String submissionTmpDirectory = null;
	public Boolean release = false; // si true : soumission pour levée de confidentialite d'un study, 
	                                // si false :  soumission de données (toujours en confidential)
		
	public String xmlStudys = null;  // nom relatif du fichier xml des studys rempli uniquement si le fichier existe.
	public String xmlSamples = null;
	public String xmlExperiments = null;
	public String xmlRuns = null;
	public String xmlSubmission = null;
	public String xmlAnalysis = null;
	public String resultSendXml = null; // Fichier resultat de la commande curl qui doit contenir les AC attribués par l'EBI
	//public String userSubmission; // login du bioinfo qui a creer ticket.
	
	public Map<String, UserCloneType> mapUserClone = new HashMap<>();

	public State state = new State(); // Reference sur "models.laboratory.common.instance.state" 
	// pour gerer les differents etats de l'objet en fonction de l'avancement dans le workflow de la soumission
	
	public TraceInformation traceInformation = new TraceInformation();// .Reference sur "models.laboratory.common.instance.TraceInformation" 
//	private Object validationDate; // aucune utilite et normalement champs jamais present en prod.
		// pour loguer les dernieres modifications utilisateurs
	public String adminComment;
	public Submission(String user, List<String>projectCodes) {
		//DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");	
		//String st_my_date = dateFormat.format(courantDate);	
		// determination du repertoire de soumission dans methode activate SubmissionServices
//		Date courantDate = new Date();
//		this.creationDate = courantDate;
		this();
		this.traceInformation = new TraceInformation();
		this.traceInformation.setTraceInformation(user);
		this.creationUser = user;
		for (String projectCode : projectCodes) {
			if (StringUtils.isNotBlank(projectCode)) {
				this.projectCodes.add(projectCode);
			}
		}
		this.projectCodes = Iterables.filter(projectCodes, StringUtils::isNotBlank).toList();  
	}

	public Submission() {
////		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");	
//		Date courantDate = new java.util.Date();
////		String st_my_date = dateFormat.format(courantDate);	
//		this.creationDate = courantDate;
		creationDate = new Date();
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
		String undifinedValueInRepriseHistorique = "undifinedValueRepriseHistorique";
		contextValidation = contextValidation.appendPath("submission");
		if (contextValidation.isUpdateMode()) {
			ValidationHelper  .validateNotEmpty    (contextValidation, creationUser, "creationUser");
		}
		// verifier que projectCode est bien renseigné et existe dans lims :
		CommonValidationHelper.validateProjectCodes(contextValidation, projectCodes);

		// Verifier que status est bien renseigné avec valeurs autorisees et que submissionDirectory est bien renseigné une
		// fois que l'objet est en status "inWaiting" (etape activate de la soumission)
		CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.SRASubmission, state);

		if (StringUtils.isNotBlank(state.code)) {
			switch (state.code.toUpperCase()) {
			case "IW-SUB"   :
			case "IW-SUB-R" :
			case "IP-SUB"   :
			case "IP-SUB-R" :
			case "F-SUB"    :
				ValidationHelper.validateNotEmpty(contextValidation, this.submissionDirectory , "submissionDirectory");
				ValidationHelper.validateNotEmpty(contextValidation, this.creationDate ,        "creationDate");
			}
		}
		CommonValidationHelper.validateIdPrimary(contextValidation, this);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);			

		// Dans le cas d'une soumission pour une release, on n'applique pas le reste des validations
		if (release)
			return;
		if (StringUtils.isBlank(this.configCode)) {
			contextValidation.addError("submission non evaluable ", "sans configCode dans la soumission '" + code + "'");
			return;
		} 
		
		Configuration config = MongoDBDAO.findByCode(InstanceConstants.SRA_CONFIGURATION_COLL_NAME, Configuration.class, configCode);
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
			if (StringUtils.isBlank(config.strategyStudy)){
				contextValidation.addError("strategy_study", "champs qui doit etre renseigne dans la configuration passee dans le contexte de validation");
			} else if (config.strategyStudy.equalsIgnoreCase("strategy_internal_study")) {
				// Le champs studyCode est rempli ssi le study est à soumettre (state = N)
				/*if (StringUtils.isBlank(this.studyCode)) {
						contextValidation.addErrors("strategy_study", "strategy_internal_study incompatible avec studyCode vide");
					}*/
			} else if (config.strategyStudy.equalsIgnoreCase("strategy_external_study")) {
				if (StringUtils.isNotBlank(studyCode)){
					contextValidation.addError("strategy_study", "strategy_external_study incompatible avec studyCode renseigne : '" + studyCode +"'");
				}
				// On peut avoir donné un seul studyAc externe pour toute la soumission via l'interface
			} else {
				contextValidation.addError("strategy_study", "valeur non attendue '" + config.strategyStudy + "'");
			}
			if (StringUtils.isBlank(config.strategySample)){
				contextValidation.addError("strategy_sample", "champs qui doit etre renseigne dans la configuration passee dans le contexte de validation");
			} else {
				if (config.strategySample.equalsIgnoreCase("strategy_external_sample")) {
					if (sampleCodes.size() != 0) {
						contextValidation.addError("strategy_external_sample incompatible avec samples à soumettre : ", "taille sampleCode = "  + sampleCodes.size());
					}
					// On peut avoir donné un seul sampleAc externe pour toute la soumission via l'interface
				}
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
//		if(! MongoDBDAO.checkObjectExist(InstanceConstants.SRA_EXPERIMENT_COLL_NAME, Experiment.class, "code", code)) {
//			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE DELETE");
//		}	
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
	}

}
