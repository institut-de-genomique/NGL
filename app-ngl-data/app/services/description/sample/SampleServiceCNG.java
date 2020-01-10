package services.description.sample;

//import static services.description.DescriptionFactory.newPropertiesDefinition;
import static services.description.DescriptionFactory.newSampleType;
//import static services.description.DescriptionFactory.newValues;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.sample.description.SampleCategory;
import models.laboratory.sample.description.SampleType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
//import services.description.common.LevelService;
//import services.description.common.MeasureService;

public class SampleServiceCNG extends AbstractSampleService {
	
	
	@Override
	public  void saveSampleCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<SampleCategory> l = new ArrayList<>();
		
		l.add(newSampleCategory("Défaut", "default"));
		l.add(newSampleCategory("Inconnu", "unknown"));
		l.add(newSampleCategory("Matériel Immunoprécipité","IP-sample"));
		l.add(newSampleCategory("ARN", "RNA"));
		l.add(newSampleCategory("ADN", "DNA"));
		l.add(newSampleCategory("FAIRE", "FAIRE"));  // manquant dans sample_parametrage_CNG.xls ( voir Julie)
		l.add(newSampleCategory("Methylated Base DNA (MBD)","methylated-base-DNA")); // manquant dans sample_parametrage_CNG.xls ( voir Julie)
		l.add(newSampleCategory("Bisulfite DNA","bisulfite-DNA")); // dans la spec mais inexistant a l'heure actuelle
		l.add(newSampleCategory("Control","control"));
		
		DAOHelpers.saveModels(SampleCategory.class, l, errors);
	}
	
	// 28/03/2018 copié depuis SampleServiceCNS.java
	private SampleType makeSampleType(String name, String code, String category, List<PropertyDefinition> properties, List<Institute> institutes) {
		return newSampleType(name,code,SampleCategory.find.findByCode(category),properties,institutes);
	}

	@Override
	public void saveSampleTypes(Map<String, List<ValidationError>> errors) throws DAOException{
		List<Institute> CNG = getInstitutes(Constants.CODE.CNG); // 28/03/2018 faire comme au CNS...
		List<SampleType> l = new ArrayList<>();
				
		// 28/03/2018 passer a 	makeSampleType rend la creation de sampleTypes plus lisible !!
		//					name				code			category		properties	institutes
		
		l.add(makeSampleType("ARN",				"RNA",			"RNA",			null,		CNG));   
		l.add(makeSampleType("ARN totaux",		"total-RNA",	"RNA",			null,		CNG));    // ajout 28/03/2018 NGL-1969
		l.add(makeSampleType("ARN small",		"small-RNA",	"RNA",			null,		CNG));    // ajout 28/03/2018 NGL-1969

		l.add(makeSampleType("ADN",				"DNA",			"DNA",			null,		CNG));
		l.add(makeSampleType("ADN Génomique",	"gDNA",			"DNA",			null,		CNG));
		
		l.add(makeSampleType("IP",				"IP",			"IP-sample",	null,		CNG));

		/* pas de subdivisions dans la base solexa...=> SampleType=SampleCategory*/
		l.add(makeSampleType("FAIRE",			 "FAIRE",		 "FAIRE", 		null, 		CNG));
		l.add(makeSampleType("methylated base DNA (mbd)", "methylated-base-DNA", "methylated-base-DNA", null, CNG));
		l.add(makeSampleType("bisulfite DNA", "bisulfite-DNA", "bisulfite-DNA", null, 		CNG));
		l.add(makeSampleType("Control", 		"CTRL", 		"control", 		null, 		CNG));
		
		l.add(makeSampleType("Défaut", 	"default-sample-cng", 	"default", 		null, 		CNG));
		l.add(makeSampleType("Inconnu", 		"unknown", 		"unknown", 		null,		CNG));
		//use only in  NGL-BI. Please not used in sample import !!!!!!!!!!!!!!!
		l.add(makeSampleType("Non défini", 		"not-defined", 	"unknown",		null,		CNG));
		
		DAOHelpers.saveModels(SampleType.class, l, errors);
	}
	
}