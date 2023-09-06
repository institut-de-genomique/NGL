package models.sra.submit.util;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.DBObject;
import models.laboratory.common.instance.PropertyValue;
import models.laboratory.run.instance.ReadSet;
import models.sra.submit.sra.instance.IStateReference;
import models.sra.submit.sra.instance.Configuration.StrategySample;
import models.utils.code.DefaultCodeImpl;
import validation.ContextValidation;

public class SraCodeHelper extends DefaultCodeImpl {
	
	private static final play.Logger.ALogger logger = play.Logger.of(SraCodeHelper.class);

	// Comme SraCodeHelper est suceptible d'etre injecté, il ne faut pas
	// declarer de constructeur privé et donc si on veut avoir un singleton,
	// soit on ne declare pas de constructeur, si injection, alors utilisation 
	// du constructeur parent, soit declaration d'un constructeur public avec 
	// annotation singleton mais qui est purement informative, les developeurs pourront utiliser ce constructeur
	// preferencielement a la method getInstance meme avec cette Annotation

	@Inject
	@Singleton
	public SraCodeHelper() { 
	}

	public synchronized String generateConfigurationCode(List<String> projectCodes) throws SraException {
		// conf_projectCode1_projectCode_2_YYYYMMDDHHMMSSSS
		String code = "";
		if(projectCodes==null || projectCodes.size()==0) {
			throw new SraException("generateConfigurationCode : pas de projectCodes ???");			
		}
		for (String projectCode: projectCodes) {
			if (StringUtils.isNotBlank(projectCode)) {
				 code += "_" + projectCode;
			}
		}
		if (StringUtils.isBlank(code)) {
			throw new SraException("generateConfigurationCode : argument sans aucune valeur ???");
		}
		return ("conf" + code + "_" + generateBarCode()).toUpperCase();
	}	
	
	public synchronized String generateUmbrellaCode() throws SraException {
		return ("umbrella_" + generateBarCode()).toUpperCase();
	}	
		
//	public synchronized String generateStudyCode(List<String> projectCodes) throws SraException {
//		// study_projectCode1_projectCode_2_YYYYMMDDHHMMSSSS
//		String code = "";
//		for (String projectCode: projectCodes) {
//			if (StringUtils.isNotBlank(projectCode)) {
//				 code += "_" + projectCode;
//			}
//		}
//		if (StringUtils.isBlank(code)){
//			throw new SraException("generateStudyCode : argument sans aucune valeur ???");
//		}
//		return ("study" + code + "_" + generateBarCode()).toUpperCase();
//	}	
	
	public synchronized String generateStudyCode() throws SraException {
		return ("study" + "_" + generateBarCode()).toUpperCase();
	}	
	
	public synchronized String generateAnalysisCode() throws SraException {		
		return ("analysis" + "_" + generateBarCode()).toUpperCase();
	}	
	
	public synchronized String generateSubmissionCode(List<String> projectCodes) throws SraException {
		// GSC_CCM_57FG3H8GK
		String code = "";
		for (String projectCode: projectCodes) {
			if (StringUtils.isNotBlank(projectCode)) {
				 code += "_" + projectCode;
			}
		}
		if (StringUtils.isBlank(code)){
			throw new SraException("generateSubmissionCode : argument sans aucune valeur ???");
		}
		return ("GSC" + code + "_" + generateBarCode()).toUpperCase();
	}


	public synchronized String generateSubmissionCodeForUmbrella() throws SraException {
		// GSC_umbrella_57FG3H8GK	
		return ("GSC_umbrella_" + generateBarCode()).toUpperCase();
	}
	
	@Override
	public synchronized String generateExperimentCode(String readSetCode) {
		// exp_readSetCode
		return "exp_" + readSetCode;
	}
	
	public String generateRunCode(String readSetCode) {
		// run_readSetCode
		return "run_" + readSetCode;
	}
	
	public String generateExternalSampleCode(String sampleAc) throws SraException {
		return "externalSample_" + sampleAc;
	}
	
	
	public String generateExternalStudyCode(String studyAc) throws SraException {
		return "externalStudy_" + studyAc;
	}
	
	public String generateSampleCode(String projectCode, int taxonId, String refCollab) {
		if (StringUtils.isBlank(projectCode)) {
			throw new SraException("Aucun projectCode en argument");
		}
		String codeSample = "";
		if(StringUtils.isNotBlank(refCollab)) {
			codeSample = "sample_" + projectCode + "_" + taxonId + "_" + refCollab;
		} else {
			codeSample = "sample_" + projectCode + "_" + taxonId;
		}
		return codeSample;
	}
	
	public String generateSampleCode(ReadSet readSet, String projectCode, StrategySample strategySample) throws SraException {
		logger.debug("!!!!!!!!!!!!!Dans generateSampleCode");
		if (readSet == null)
			throw new SraException("Aucun readSet en argument");
		if (StringUtils.isBlank(projectCode))
			throw new SraException("Aucun projectCode en argument");
		String taxonId = readSet.sampleOnContainer.taxonCode;
		String codeSample = null;

		switch (strategySample) {
		case STRATEGY_CODE_SAMPLE_REFCOLLAB :
			String clone = readSet.sampleOnContainer.referenceCollab;
			// ajout pour ticket NGL-3413
			if(readSet.properties.containsKey("refCollabSub")) {
				PropertyValue propertyValue = readSet.properties.get("refCollabSub");
				String refCollabSub = (String) propertyValue.value;
				if(StringUtils.isNotBlank(refCollabSub)) {
					clone = refCollabSub;
				}
			}
			
			if (! isGoodNameClone(clone)) {
				throw new SraException("STRATEGY_CODE_SAMPLE_REFCOLLAB avec refCollaborateur '" + clone +"' qui ne repond pas à la regEXp '"+ cloneRegExp + "'");
			}
			codeSample = "sample_" + projectCode + "_" + taxonId + "_" + clone;
			break;
		case STRATEGY_CODE_SAMPLE_BANK :
			if (readSet.sampleOnContainer.properties.isEmpty()) {
				throw new SraException("STRATEGY_CODE_SAMPLE_BANK avec pour le readset '" + readSet.code + "' un sampleOnContainer sans properties");		
			} 
			if (!readSet.sampleOnContainer.properties.containsKey("libProcessTypeCode")) {
				throw new SraException("STRATEGY_CODE_SAMPLE_BANK avec pour le readset '" + readSet.code + "' un sampleOnContainer sans la propriete libProcessTypeCode");		
			}
			String libProcessTypeCodeVal = (String) readSet.sampleOnContainer.properties.get("libProcessTypeCode").getValue();
			if (! isGoodNameClone(libProcessTypeCodeVal)) {
				throw new SraException("STRATEGY_CODE_SAMPLE_BANK avec typeBank '" + libProcessTypeCodeVal +"' qui ne repond pas à la regEXp '"+ cloneRegExp + "'");
			}
			codeSample = "sample_" + projectCode + "_" + taxonId + "_" + libProcessTypeCodeVal;
			break;
		case STRATEGY_CODE_SAMPLE_TAXON :
			codeSample = "sample_" + projectCode + "_" + taxonId;
			break;
		case STRATEGY_AC_SAMPLE :
			throw new SraException("STRATEGY_AC_SAMPLE :  + utiliser generateExternalSampleCode");		
		default:
			throw new SraException("StrategySample inconnu : " + strategySample);		
		}	
		return codeSample;
	}
	
	public static boolean isGoodNameClone(String clone) {
		return clone.matches(cloneRegExp);
	}
	public static boolean isGoodTaxonId(String clone) {
		return clone.matches(taxonIdRegExp);
	}	
	public static final String cloneRegExp ="^[\\.A-Za-z0-9_-]+$";
	public static final String taxonIdRegExp ="^[0-9]+$";
	
	
	public static <T  extends DBObject & IStateReference> void assertObjectStateCode (T t, String stateCode, ContextValidation contextValidation)  {
		if (! t.getState().code.equals(stateCode)) {
			String message = "L'objet " + t.getState().code + " est invalide, "
			    + " etat attendu = " + stateCode 
				+ " ,etat de l'objet = " + t.getState().code;
			contextValidation.addError("assertSubmissionStateCode : ", message);
		}		
	}	

}
	
