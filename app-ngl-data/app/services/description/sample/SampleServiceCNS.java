package services.description.sample;

import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newSampleType;
// import static services.description.DescriptionFactory.newValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
//import models.laboratory.common.description.MeasureCategory;
//import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
//import services.description.common.MeasureService;

public class SampleServiceCNS extends AbstractSampleService {
	
	@Override
	public void saveSampleCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<SampleCategory> l = new ArrayList<>();
		
		l.add(newSampleCategory("ADN Cloné",                    "cloned-DNA"));
		l.add(newSampleCategory("Matériel Immunoprécipité",     "IP-sample"));
		l.add(newSampleCategory("Amplicon",                     "amplicon"));
		l.add(newSampleCategory("Défaut",                       "default"));
		l.add(newSampleCategory("Inconnu",                      "unknown"));
		l.add(newSampleCategory("ADN",                          "DNA"));
		l.add(newSampleCategory("ARN",                          "RNA"));
		l.add(newSampleCategory("cDNA",                         "cDNA"));
		l.add(newSampleCategory("ADN en plug",                  "DNAplug"));
		//FDS 05/06/2015 JIRA NGL-672: ajout des categories CNG qui n'existaient pas au CNS 
		l.add(newSampleCategory("FAIRE",                        "FAIRE"));  // manquant dans sample_parametrage_CNG.xls ( voir Julie)
		l.add(newSampleCategory("Methylated Base DNA (MBD)",    "methylated-base-DNA")); // manquant dans sample_parametrage_CNG.xls ( voir Julie)
		l.add(newSampleCategory("Bisulfite DNA",                "bisulfite-DNA")); // dans la spec mais inexistant a l'heure actuelle
		//FDS 16/16/2015 necessaire pour l'import des tubes...
		l.add(newSampleCategory("Control",                      "control"));
		l.add(newSampleCategory("Prélèvements environnementaux","environmental-samples"));
		
		DAOHelpers.saveModels(SampleCategory.class, l, errors);
	}
	
	private SampleType makeSampleType(String name, String code, String category, List<Institute> institutes) {
		return makeSampleType(name,code,category,null, institutes);
	}
	private SampleType makeSampleType(String name, String code, String category, List<PropertyDefinition> properties, List<Institute> institutes) {
		return newSampleType(name,code,SampleCategory.find.findByCode(category),properties,institutes);
	}
	
	@Override
	public void saveSampleTypes(Map<String, List<ValidationError>> errors) throws DAOException{
		List<Institute> CNS = getInstitutes(Constants.CODE.CNS);
		
		List<SampleType> l = new ArrayList<>();
		
		l.add(makeSampleType("BAC",               "BAC",         "cloned-DNA",            CNS));	
		l.add(makeSampleType("Plasmide",          "plasmid",     "cloned-DNA",            CNS));	
		l.add(makeSampleType("Fosmide",           "fosmid",      "cloned-DNA",            CNS));	
		// ajout CNG
		l.add(makeSampleType("ADN Génomique",     "gDNA",        "DNA",                   CNS)); 
		l.add(makeSampleType("ADN Métagénomique", "MeTa-DNA",    "DNA",                   CNS));	
		l.add(makeSampleType("Amplicon",          "amplicon",    "amplicon",              CNS));
		l.add(makeSampleType("ARN total",         "total-RNA",   "RNA",                   CNS));
		l.add(makeSampleType("ARNm",              "mRNA",        "RNA",                   CNS));
		l.add(makeSampleType("Small RNA",         "sRNA",        "RNA",                   CNS));
		l.add(makeSampleType("ARN déplété",       "depletedRNA", "RNA",                   CNS));
		l.add(makeSampleType("aARN",              "aRNA",        "RNA",                   CNS));		
		//l.add(newSampleType("ARN", "RNA", SampleCategory.find.findByCode("RNA"), getSampleCNGPropertyDefinitions(), getInstitutes(Institute.CODE.CNG)));
		l.add(makeSampleType("ADN en plug",       "DNAplug",     "DNAplug",               CNS));
		l.add(makeSampleType("cDNA",              "cDNA",        "cDNA",                  CNS));	
		l.add(makeSampleType("ChIP",              "chIP",        "IP-sample",             CNS));
		l.add(makeSampleType("ClIP",              "clIP",        "IP-sample",             CNS));
		l.add(makeSampleType("Inconnu",           "unknown",     "unknown",               CNS));
		//use only in  NGL-BI. Please not used in sample import !!!!!!!!!!!!!!!
		l.add(makeSampleType("Non défini",        "not-defined", "unknown",               CNS));
		l.add(makeSampleType("ADN",               "DNA",         "DNA",                   CNS));
		l.add(makeSampleType("ARN",               "RNA",         "RNA",                   CNS));
		l.add(makeSampleType("Plancton",          "plankton",    "environmental-samples", CNS));
		l.add(makeSampleType("Aérosol",           "aerosol",     "environmental-samples", CNS));
		l.add(makeSampleType("Sea grass",         "seagrass",    "environmental-samples", CNS));
 		l.add(makeSampleType("Corail",            "coral",       "environmental-samples", CNS));
		l.add(makeSampleType("Poisson",           "fish",        "environmental-samples", getFishPropertyDefinitions(), CNS));
		//l.add(newSampleType("Core", "core", SampleCategory.find.findByCode("environmental-samples"),null, getInstitutes(Constants.CODE.CNS)));
		l.add(makeSampleType("Culture d'échantillons marins", "culture-of-marine-samples", "environmental-samples", CNS));
		l.add(makeSampleType("Echantillon biologique",        "biological-sample",         "environmental-samples", CNS));
		
		DAOHelpers.saveModels(SampleType.class, l, errors);
	}	
	
	/* FDS 05/06/205 JIRA NGL-672: ajout Institute.CODE.CNG pour les sampleType communs CNS/CNG 
	 * NOTE: pour le CNG certains SampleCategory n'ont pas de SampleType definis=> utiliser le meme code (cf G.albini)
	 */
	
	/*public void saveSampleTypes(Map<String, List<ValidationError>> errors) throws DAOException{
		List<SampleType> l = new ArrayList<SampleType>();
		
		l.add(newSampleType("BAC",               "BAC",         SampleCategory.find.findByCode("cloned-DNA"), null, getInstitutes(Constants.CODE.CNS)));	
		l.add(newSampleType("Plasmide",          "plasmid",     SampleCategory.find.findByCode("cloned-DNA"), null, getInstitutes(Constants.CODE.CNS)));	
		l.add(newSampleType("Fosmide",           "fosmid",      SampleCategory.find.findByCode("cloned-DNA"), null, getInstitutes(Constants.CODE.CNS)));	
		
		// ajout CNG
		l.add(newSampleType("ADN Génomique",     "gDNA",        SampleCategory.find.findByCode("DNA"),       null, getInstitutes(Constants.CODE.CNS))); 
		l.add(newSampleType("ADN Métagénomique", "MeTa-DNA",    SampleCategory.find.findByCode("DNA"),       null, getInstitutes(Constants.CODE.CNS)));	
		
		l.add(newSampleType("Amplicon",          "amplicon",    SampleCategory.find.findByCode("amplicon"),  null, getInstitutes(Constants.CODE.CNS)));
		
		l.add(newSampleType("ARN total",         "total-RNA",   SampleCategory.find.findByCode("RNA"),       null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("ARNm",              "mRNA",        SampleCategory.find.findByCode("RNA"),       null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("Small RNA",         "sRNA",        SampleCategory.find.findByCode("RNA"),       null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("ARN déplété",       "depletedRNA", SampleCategory.find.findByCode("RNA"),       null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("aARN",              "aRNA",        SampleCategory.find.findByCode("RNA"),       null, getInstitutes(Constants.CODE.CNS)));		
		
		//l.add(newSampleType("ARN", "RNA", SampleCategory.find.findByCode("RNA"), getSampleCNGPropertyDefinitions(), getInstitutes(Institute.CODE.CNG)));
		
		l.add(newSampleType("ADN en plug",      "DNAplug",      SampleCategory.find.findByCode("DNAplug"),   null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("cDNA",             "cDNA",         SampleCategory.find.findByCode("cDNA"),      null, getInstitutes(Constants.CODE.CNS)));	

		l.add(newSampleType("ChIP",             "chIP",         SampleCategory.find.findByCode("IP-sample"), null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("ClIP",             "clIP",         SampleCategory.find.findByCode("IP-sample"), null, getInstitutes(Constants.CODE.CNS)));
		
		l.add(newSampleType("Inconnu",          "unknown",      SampleCategory.find.findByCode("unknown"),   null, getInstitutes(Constants.CODE.CNS)));
		
		//use only in  NGL-BI. Please not used in sample import !!!!!!!!!!!!!!!
		l.add(newSampleType("Non défini",       "not-defined",  SampleCategory.find.findByCode("unknown"),   null, getInstitutes(Constants.CODE.CNS)));

		l.add(newSampleType("ADN",              "DNA",          SampleCategory.find.findByCode("DNA"),                  null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("ARN",              "RNA",          SampleCategory.find.findByCode("RNA"),                  null, getInstitutes(Constants.CODE.CNS)));

		l.add(newSampleType("Plancton",         "plankton",     SampleCategory.find.findByCode("environmental-samples"), null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("Aérosol",          "aerosol",      SampleCategory.find.findByCode("environmental-samples"), null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("Sea grass",        "seagrass",     SampleCategory.find.findByCode("environmental-samples"), null, getInstitutes(Constants.CODE.CNS)));
 		l.add(newSampleType("Corail",           "coral",        SampleCategory.find.findByCode("environmental-samples"), null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("Poisson",          "fish",         SampleCategory.find.findByCode("environmental-samples"), getFishPropertyDefinitions(), getInstitutes(Constants.CODE.CNS)));
		
		//l.add(newSampleType("Core", "core", SampleCategory.find.findByCode("environmental-samples"),null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("Culture d'échantillons marins", "culture-of-marine-samples", SampleCategory.find.findByCode("environmental-samples"), null, getInstitutes(Constants.CODE.CNS)));
		l.add(newSampleType("Echantillon biologique",        "biological-sample",         SampleCategory.find.findByCode("environmental-samples"), null, getInstitutes(Constants.CODE.CNS)));
		
		
		DAOHelpers.saveModels(SampleType.class, l, errors);
	}*/
	
	private List<PropertyDefinition> getFishPropertyDefinitions() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Partie de l'anatomie", "fishAnatomy", LevelService.getLevels(Level.CODE.Sample,Level.CODE.Content), String.class, true, null,
				getFishAnatomyValues(), "single", 2, true, null, null));
		return propertyDefinitions;
	}
	
	private static List<Value> getFishAnatomyValues() {
		List<Value> values = new ArrayList<>();
		values.add(DescriptionFactory.newValue("MUC",   "mucus"));
		values.add(DescriptionFactory.newValue("GILLS", "gills"));
		values.add(DescriptionFactory.newValue("GT",    "Gut tractus"));
		values.add(DescriptionFactory.newValue("FIN",   "fin"));
		values.add(DescriptionFactory.newValue("OTO",   "otolith"));
		return values;	
	}
	
}
