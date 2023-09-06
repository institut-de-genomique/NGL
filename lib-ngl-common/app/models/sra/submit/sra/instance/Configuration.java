package models.sra.submit.sra.instance;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.DBObject;
import fr.cea.ig.ngl.dao.api.sra.ConfigurationAPI;
import models.laboratory.common.description.ObjectType;
import models.laboratory.common.instance.State;
import models.laboratory.common.instance.TraceInformation;
import models.sra.submit.util.VariableSRA;
import models.utils.InstanceConstants;
import validation.ContextValidation;
import validation.IValidation;
import validation.common.instance.CommonValidationHelper;
import validation.sra.SraValidationHelper;
import validation.utils.ValidationHelper;


//Declaration d'une collection Configuration (herite de DBObject)
public class Configuration extends DBObject implements IValidation {

	public static final String initialStateCode = "NONE";
	
	public  enum StrategyStudy {
		strategy_internal_study, //obsolete
		strategy_external_study, //obsolete

		STRATEGY_CODE_STUDY,  // internal_study
		STRATEGY_AC_STUDY     // external_study
	}
	
	public enum StrategySample {
		strategy_sample_clone,    //obsolete
		strategy_sample_bank,     //obsolete
		strategy_sample_taxon,    //obsolete
		strategy_external_sample, //obsolete

		STRATEGY_CODE_SAMPLE_TAXON,
		STRATEGY_CODE_SAMPLE_REFCOLLAB,
		STRATEGY_CODE_SAMPLE_BANK,
		STRATEGY_AC_SAMPLE         // external_sample
	}
	
		
	//public String alias;            // required mais remplacé par code herité de DBObject, et valeur = conf_projectCode_num
	public List<String> projectCodes = new ArrayList<>(); // de type BAT, required pour nos stats
	//public String studyCode = null;
//	public String strategySample = null;  // required et constraint :
	public StrategySample strategySample = null;  // required et constraint :
	//("STRATEGY_SAMPLE_TAXON", "STRATEGY_SAMPLE_CLONE", "STRATEGY_NO_SAMPLE");
//	public String strategyStudy = "strategy_internal_study"; // required et contraint 
	public StrategyStudy strategyStudy = Configuration.StrategyStudy.STRATEGY_CODE_STUDY; // required et contraint 
	// Whole Genome Sequencing, Metagenomics, transcriptome analysis

	//public String studyCode = null;       // study à soumettre à l'ebi si strategyStudy==strategy_internal_study

	// Informations de library obligatoires qui sont prises dans le lims mais qui peuvent etre surchargées 
	// par le la configuration ou encore par le  fichier User_Experiments.csv si les valeurs sont specifiques 
	// de chaque experiment:

	public String librarySelection = null;// required et constraint: champs de la proc surchargeable dans conf ou par utilisateurs */
	public String libraryStrategy = null; // required et constraint. Valeur mise par defaut à "WGS" mais surchargeable */
	public String librarySource = null;   // required et constraint. Valeur mise par defaut à "GENOMIC" mais surchargeable */
	public String libraryConstructionProtocol = null; //facultatif et texte libre mais assez court.
	//public String userFileExperiments = null; // Nom complet du fichier donnant les informations de librairies specifiques de chaque experiment
	// si les données ne doivent pas etre prises dans le lims ou dans la configuration.


	public State state = new State(); // Reference sur "models.laboratory.common.instance.state" 
	// pour gerer les differents etats de l'objet.

	public TraceInformation traceInformation = new TraceInformation(); // Reference sur "models.laboratory.common.instance.TraceInformation" 
	// pour loguer les dernieres modifications utilisateurs


	public void validateInvariants(ContextValidation contextValidation) {
		// Ajouter l'objet au contexte de validation seulement si objet ext
		// pour ex pour validation de submission qui va avoir besoin d'ajouter des objets 

		//contextValidation.putObject("configuration", this);	   
		contextValidation = contextValidation.appendPath("configuration");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		// verifier que projectCode est bien renseigné et existe dans lims :
		CommonValidationHelper.validateProjectCodes(contextValidation, this.projectCodes);
		// Verifier que les projects ne sont pas archivés :
		for (String projectCode: this.projectCodes) {
			if (StringUtils.isNotBlank(projectCode)) {
				// verifier si archivage ou non:
			}
		}
		// verifier que champs contraints presents avec valeurs autorisees:
		SraValidationHelper.requiredAndConstraint(contextValidation, librarySelection, VariableSRA.mapLibrarySelection(), "librarySelection");
		SraValidationHelper.requiredAndConstraint(contextValidation, libraryStrategy,  VariableSRA.mapLibraryStrategy(),  "libraryStrategy");
		SraValidationHelper.requiredAndConstraint(contextValidation, librarySource,    VariableSRA.mapLibrarySource(),    "librarySource");
		if (state != null && StringUtils.isNotBlank(state.code) && !initialStateCode.equals(state.code)) {
			CommonValidationHelper.validateStateRequired(contextValidation, ObjectType.CODE.SRASubmission, this.state);
		}
		

//		SraValidationHelper.requiredAndConstraint(contextValidation, this.strategySample, VariableSRA.mapStrategySample, "strategySample");
//		SraValidationHelper.requiredAndConstraint(contextValidation, this.strategyStudy, VariableSRA.mapStrategyStudy, "strategyStudy");
		// verifier que code est bien renseigné
		//SraValidationHelper.validateCode(this, InstanceConstants.SRA_CONFIGURATION_COLL_NAME, contextValidation);
		CommonValidationHelper.validateIdPrimary(contextValidation, this);
		CommonValidationHelper.validateCodePrimary(contextValidation, this, InstanceConstants.SRA_CONFIGURATION_COLL_NAME);
		CommonValidationHelper.validateTraceInformationRequired(contextValidation, traceInformation);
	}
	
	private void validateCreation(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("configuration");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ConfigurationAPI configurationAPI = ConfigurationAPI.get();
		//if(configurationAPI.dao_checkObjectExist("code", code)) {
		if(configurationAPI.isObjectExist(code)) {

			contextValidation.addError("code", code + " existe deja dans la base de données et MODE CREATION");
		}	

		// On teste la création selon le mode copy :
		Boolean copy = false;
		if (contextValidation.getObject("copy") != null && contextValidation.getObject("copy") == (Boolean) true) {
			copy = true;
		}	
	}
	
	private void validateDelete(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("configuration");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ConfigurationAPI configurationAPI = ConfigurationAPI.get();
		if(! configurationAPI.isObjectExist(code)) {
			contextValidation.addError("code", code + " n'existe pas dans la base de données et MODE DELETE");
		}	
	}
	
	private void validateUpdate(ContextValidation contextValidation) {
		contextValidation = contextValidation.appendPath("configuration");
		if (ValidationHelper.validateNotEmpty(contextValidation, code, "code")) 
			contextValidation = contextValidation.appendPath(code);
		ConfigurationAPI configurationAPI = ConfigurationAPI.get();
		if(! configurationAPI.isObjectExist(code)) {
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
