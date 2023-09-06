package models.sra.submit.util;

import static fr.cea.ig.play.IGGlobals.configuration;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import fr.cea.ig.lfw.utils.HashMapBuilder;

public abstract class VariableSRA {
	
	private static final play.Logger.ALogger logger = play.Logger.of(VariableSRA.class);
	//public static final SraParameter sraParam = new SraParameter();
	
	public static final String centerName;
	public static final String laboratoryName;
	public static final String submissionRootDirectory;
	public static final String defaultLibraryConstructionProtocol;
	public static final String admin;
	public static final String xmlSubmission;
	public static final String xmlStudies;
	public static final String xmlAnalysis;
	public static final String xmlSamples;
	public static final String xmlExperiments;
	public static final String xmlRuns;
	public static final String xmlProjects;
	public static final String xmlUmbrella;


	// Il faut avoir un fichier /env/cns/apps/ngl/cns/conf/ngl-sub/application.conf avec ces variables definies:
	static {
		centerName              = getString("centerName");
		laboratoryName          = getString("laboratoryName");
		submissionRootDirectory = getString("submissionRootDirectory");
		defaultLibraryConstructionProtocol = getString("defaultLibraryConstructionProtocol");
		admin                   = getString("admin");
		xmlSubmission           = getString("xmlSubmission");
		xmlStudies              = getString("xmlStudies");
		xmlSamples              = getString("xmlSamples");
		xmlExperiments          = getString("xmlExperiments");
		xmlRuns                 = getString("xmlRuns");
		xmlProjects             = getString("xmlProjects");
		xmlUmbrella             = getString("xmlUmbrella");
		xmlAnalysis             = getString("xmlAnalysis");
	}

	public static final Map<String, String> mapSubmissionProjectType() {
		return SraParameter.getParameter("submissionProjectType");
	}
	
	public static final Map<String, String> mapCenterName() {
		return SraParameter.getParameter("centerName");
	}
	
	public static final Map<String, String> mapLaboratoryName() {
		return SraParameter.getParameter("laboratoryName");
	}
	
	public static final Map<String, String> mapLibProcessTypeCodeVal_orientation() {
		return SraParameter.getParameter("libProcessTypeCodeValue_orientation");
	}
	
	public static final Map<String, String> mapTypeReadset() { 
		return SraParameter.getParameter("typeReadset");
	}
	
	public static final Map<String, String> mapExistingStudyType() {
		return SraParameter.getParameter("existingStudyType");
	}
	
	
	public static final Map<String, String> mapLibraryLayoutOrientation() {
		return SraParameter.getParameter("libraryLayoutOrientation");
	}
	
	public static final Map<String, String> mapLibrarySource() {
		return SraParameter.getParameter("librarySource");
	}
	
	public static final Map<String, String> mapLibraryStrategy() {
		return SraParameter.getParameter("libraryStrategy");
	}
	
	public static final Map<String, String> mapLibrarySelection() {
		return SraParameter.getParameter("librarySelection");
	}
	
	public static final Map<String, String> mapTypePlatform() {
		return  SraParameter.getParameter("typePlatform");
	}
	
	public static final Map<String, String> mapLibraryLayout() {
		return SraParameter.getParameter("libraryLayout");
	}
	
	public static final Map<String, String> mapInstrumentModel() {
		return SraParameter.getParameter("instrumentModel"); 
	}
	
	public static final Map<String, String> mapAllInstrumentModel() {
		return SraParameter.getParameter("allInstrumentModel"); 
	}	
	
	public static final Map<String, String> mapAnalysisFileType() {
		return SraParameter.getParameter("analysisFileType"); 
	}

	// liste des state simplifiés pour affichage dans formulaire recherche de Submission et Study
	// Les states simplifiés ne sont pas dans la description. C'est juste pour affichage
	public static final Map<String, String> mapSimplifiedStates() {
		return SraParameter.getParameter("simplifiedStates"); 
	}	


	// correspondance entre simplifiedState.code(pas d'existence dans description) et liste des state.code(present dans description)
	public static final Map<String, ArrayList<String>> mapPseudoStateCodeToStateCodes() {
		return SraParameter.getParameters("pseudoStateCodeToStateCodes"); 
	}
	
//	public static final Map<String, String> mapStrategySample = new HashMap<String, String>() {
//		{
//			put("strategy_external_sample", "strategy_external_sample"); // Si pas de sample à creer parce que fournis par les collaborateurs
//			put("strategy_sample_taxon",    "strategy_sample_taxon");    // si sample specifique par code_projet et taxon
//			put("strategy_sample_clone",    "strategy_sample_clone");    // si sample specifique par code_projet et clone
//		}
//	};
//	
//	public static final Map<String, String> mapStrategyStudy = new HashMap<String, String>() {
//		{
//			put("strategy_external_study", "strategy_external_study"); // Si pas de study à creer parce que fournis par les collaborateurs
//			put("strategy_internal_study", "strategy_internal_study"); 
//		}
//	};	
//
	
	private static String getString(String key) {
		String result = configuration().getString(key);
		if (StringUtils.isBlank(result))
			logger.error("Absence dans la config play de la cle '{}'", key);
		return result;
	}
	
	@Deprecated
	public static final Map<String, String> mapStrategySample = 
			new HashMapBuilder<String,String>()
				.put("STRATEGY_AC_SAMPLE", "STRATEGY_AC_SAMPLE") // si pas de sample à creer parce que fournis par les collaborateurs
				.put("STRATEGY_CODE_SAMPLE_TAXON"    , "STRATEGY_CODE_SAMPLE_TAXON")    // si sample specifique par code_projet et taxon
				.put("STRATEGY_CODE_SAMPLE_REFCOLLAB", "STRATEGY_CODE_SAMPLE_REFCOLLAB")// si sample specifique par code_projet, taxon et refCollab
				.put("STRATEGY_CODE_SAMPLE_BANK"     , "STRATEGY_CODE_SAMPLE_BANK")     // si sample specifique par code_projet, taxon et type de banque
				.asMap();
	@Deprecated
	public static final Map<String, String> mapStrategyStudy = 
			new HashMapBuilder<String,String>()
				.put("STRATEGY_AC_STUDY", "STRATEGY_AC_STUDY") // Si pas de study à creer parce que fournis par les collaborateurs
				.put("STRATEGY_CODE_STUDY", "STRATEGY_CODE_STUDY")
				.asMap();
		
//	static {
//		Configuration conf      = Play.application().configuration();
//		centerName              = getString(conf, "centerName");
//		laboratoryName          = getString(conf, "laboratoryName");
//		submissionRootDirectory = getString(conf, "submissionRootDirectory");
//		defaultLibraryConstructionProtocol = getString(conf, "defaultLibraryConstructionProtocol");
//		admin                   = getString(conf, "admin");
//		xmlSubmission           = getString(conf, "xmlSubmission");
//		xmlStudies              = getString(conf, "xmlStudies");
//		xmlSamples              = getString(conf, "xmlSamples");
//		xmlExperiments          = getString(conf, "xmlExperiments");
//		xmlRuns                 = getString(conf, "xmlRuns");
//	}
	


}


