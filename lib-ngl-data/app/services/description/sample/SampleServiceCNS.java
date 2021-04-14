package services.description.sample;

import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newSampleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
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

public class SampleServiceCNS extends AbstractSampleService {
	
	public List<SampleCategory> getSampleCategories() {
		List<SampleCategory> l = new ArrayList<>();
		
		l.add(new SampleCategory("cloned-DNA",            "ADN Cloné"));
		l.add(new SampleCategory("IP-sample",             "Matériel Immunoprécipité"));
		l.add(new SampleCategory("amplicon",              "Amplicon"));
		l.add(new SampleCategory("default",               "Défaut"));
		l.add(new SampleCategory("unknown",               "Inconnu"));
		l.add(new SampleCategory("DNA",                   "ADN"));
		l.add(new SampleCategory("RNA",                   "ARN"));
		l.add(new SampleCategory("cDNA",                  "cDNA"));
		l.add(new SampleCategory("DNAplug",               "ADN en plug"));
		// FDS 05/06/2015 JIRA NGL-672: ajout des categories CNG qui n'existaient pas au CNS 
		l.add(new SampleCategory("FAIRE",                 "FAIRE"));  // manquant dans sample_parametrage_CNG.xls ( voir Julie)
		l.add(new SampleCategory("methylated-base-DNA",   "Methylated Base DNA (MBD)")); // manquant dans sample_parametrage_CNG.xls ( voir Julie)
		l.add(new SampleCategory("bisulfite-DNA",         "Bisulfite DNA")); // dans la spec mais inexistant a l'heure actuelle
		// FDS 16/16/2015 necessaire pour l'import des tubes...
		l.add(new SampleCategory("control",               "Control"));
		l.add(new SampleCategory("environmental-samples", "Prélèvements environnementaux"));
		
		return l;
	}
	
	@Override
	public void saveSampleCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(SampleCategory.class, getSampleCategories(), errors);
	}
	
	// Institutes list is implicit (CNS)
	private SampleType makeSampleType(String name, String code, String category, List<Institute> institutes) {
		return makeSampleType(name, code, category, null, institutes);
	}
	
	// Institutes list is implicit (CNS)
	private SampleType makeSampleType(String name, String code, String category, List<PropertyDefinition> properties, List<Institute> institutes) {
		return newSampleType(name, code, SampleCategory.find.get().findByCode(category), properties, institutes);
	}
	
	public List<SampleType> getSampleTypes() {
		List<Institute>  CNS = DescriptionFactory.getInstitutes(Constants.CODE.CNS);
		List<SampleType> l   = new ArrayList<>();
		
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
		return l;
	}
	
	@Override
	public void saveSampleTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		DAOHelpers.saveModels(SampleType.class, getSampleTypes(), errors);
	}	
	

	private List<PropertyDefinition> getFishPropertyDefinitions() {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Partie de l'anatomie", "fishAnatomy", LevelService.getLevels(Level.CODE.Sample, Level.CODE.Content), String.class, true, null,
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
