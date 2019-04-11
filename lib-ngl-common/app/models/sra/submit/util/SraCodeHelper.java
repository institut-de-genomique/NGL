package models.sra.submit.util;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;

import models.laboratory.run.instance.ReadSet;
import models.utils.code.DefaultCodeImpl;

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

	// TODO: check synchronized keyword as it seems overkill
	public synchronized String generateConfigurationCode(List<String> projectCodes) throws SraException {
		// conf_projectCode1_projectCode_2_YYYYMMDDHHMMSSSS
		String code = "";
		for (String projectCode: projectCodes) {
			if (StringUtils.isNotBlank(projectCode)) {
				 code += "_" + projectCode;
			}
		}
		if (StringUtils.isBlank(code)) {
			throw new SraException("generateConfigurationCode : argument sans aucune valeur ???");
		}
//		return ("conf" + code + "_" + this.getInstance().generateBarCode()).toUpperCase();
		return ("conf" + code + "_" + generateBarCode()).toUpperCase();
	}	
		
	// TODO: check synchronized keyword as it seems overkill
	public synchronized String generateStudyCode(List<String> projectCodes) throws SraException {
		// conf_projectCode1_projectCode_2_YYYYMMDDHHMMSSSS
		String code = "";
		for (String projectCode: projectCodes) {
			if (StringUtils.isNotBlank(projectCode)) {
				 code += "_" + projectCode;
			}
		}
		if (StringUtils.isBlank(code)){
			throw new SraException("generateStudyCode : argument sans aucune valeur ???");
		}
//		return ("study" + code + "_" + this.getInstance().generateBarCode()).toUpperCase();
		return ("study" + code + "_" + generateBarCode()).toUpperCase();
	}	
	
	// TODO: check synchronized keyword as it seems overkill
	public synchronized String generateSubmissionCode(List<String> projectCodes) throws SraException {
		// cns_projectCode1_projectCode_2_YYYYMMDDHHMMSSSS
		String code = "";
		for (String projectCode: projectCodes) {
			if (StringUtils.isNotBlank(projectCode)) {
				 code += "_" + projectCode;
			}
		}
		if (StringUtils.isBlank(code)){
			throw new SraException("generateSubmissionCode : argument sans aucune valeur ???");
		}
//		return ("GSC" + code + "_" + this.getInstance().generateBarCode()).toUpperCase();
		return ("GSC" + code + "_" + generateBarCode()).toUpperCase();
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
		
	public String generateSampleCode(ReadSet readSet, String projectCode, String strategySample) throws SraException {
		logger.debug("!!!!!!!!!!!!!Dans generateSampleCode");
		if (readSet == null)
			throw new SraException("Aucun readSet en argument");
		if (StringUtils.isBlank(projectCode))
			throw new SraException("Aucun projectCode en argument");
		if (StringUtils.isBlank(strategySample))
			throw new SraException("Aucun strategySample en argument");
		//logger.debug("!!!!!!!!!!!!!Dans generateSampleCode");

		//logger.debug("!!!!!!!!!!!!!Dans generateSampleCode codeSample="+readSet.sampleCode);
	
		//String laboratorySampleCode = readSet.sampleCode;
		//models.laboratory.sample.instance.Sample laboratorySample = MongoDBDAO.findByCode(InstanceConstants.SAMPLE_COLL_NAME, models.laboratory.sample.instance.Sample.class, laboratorySampleCode);
		//String laboratorySampleName = laboratorySample.name;
		//String taxonId = laboratorySample.taxonCode;
		String taxonId = readSet.sampleOnContainer.taxonCode;
		//String laboratoryRunCode = readSet.runCode;
		//models.laboratory.run.instance.Run  laboratoryRun = MongoDBDAO.findByCode(InstanceConstants.RUN_ILLUMINA_COLL_NAME, models.laboratory.run.instance.Run.class, laboratoryRunCode);

		String codeSample = null;
		switch (strategySample.toUpperCase()) {
		case "STRATEGY_SAMPLE_CLONE":
//			String clone = laboratorySample.referenceCollab;
			String clone = readSet.sampleOnContainer.referenceCollab;
			if (! isGoodNameClone(clone)) {
				throw new SraException("STRATEGY_SAMPLE_CLONE avec clone '" + clone +"' qui ne repond pas à la regEXp '"+ cloneRegExp + "'");
			}
			codeSample = "sample_" + projectCode + "_" + taxonId + "_" + clone;
			break;
		case "STRATEGY_SAMPLE_BANK":
			if (readSet.sampleOnContainer.properties.isEmpty()) {
				throw new SraException("STRATEGY_SAMPLE_BANK avec pour le readset '" + readSet.code + "' un sampleOnContainer sans properties");		
			} 
			if (!readSet.sampleOnContainer.properties.containsKey("libProcessTypeCode")) {
				throw new SraException("STRATEGY_SAMPLE_BANK avec pour le readset '" + readSet.code + "' un sampleOnContainer sans la propriete libProcessTypeCode");		
			}
			String libProcessTypeCodeVal = (String) readSet.sampleOnContainer.properties.get("libProcessTypeCode").getValue();
			if (! isGoodNameClone(libProcessTypeCodeVal)) {
				throw new SraException("STRATEGY_SAMPLE_BANK avec typeBank '" + libProcessTypeCodeVal +"' qui ne repond pas à la regEXp '"+ cloneRegExp + "'");
			}
			codeSample = "sample_" + projectCode + "_" + taxonId + "_" + libProcessTypeCodeVal;
			break;
		case"STRATEGY_SAMPLE_TAXON":
			codeSample = "sample_" + projectCode + "_" + taxonId;
			break;
		case"STRATEGY_EXTERNAL_SAMPLE":
			throw new SraException("STRATEGY_EXTERNAL_SAMPLE :  + utiliser generateExternalSampleCode");		
		default:
			throw new SraException("StrategySample inconnu : " + strategySample);		
		}	
		return codeSample;
	}
	
	public static boolean isGoodNameClone(String clone) {
		return clone.matches(cloneRegExp);
	}
	
	public static final String cloneRegExp ="^[A-Za-z0-9_-]+$";
	
}
