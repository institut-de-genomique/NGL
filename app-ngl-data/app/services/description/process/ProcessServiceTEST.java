package services.description.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.typesafe.config.ConfigFactory;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;

public class ProcessServiceTEST extends AbstractProcessService {

	@Override
	public void saveProcessCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProcessCategory> l = new ArrayList<>();
		if(	!ConfigFactory.load().getString("ngl.env").equals("PROD") ){
			l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Pre-Sequencage", "pre-sequencing"));
			l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Pre-Banque", "pre-library"));

		}
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Banque", "library"));
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Sequençage", "sequencing"));		
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Optical mapping", "mapping"));
		DAOHelpers.saveModels(ProcessCategory.class, l, errors);

	}

	@Override
	public void saveProcessTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProcessType> l = new ArrayList<>();
	
		/*
		l.add(DescriptionFactory.newProcessType("Frg, Lib ONT, Dépôt", "nanopore-process-library", ProcessCategory.find.findByCode("library"),getPropertyDefinitionsNanoporeFragmentation() , getExperimentTypes("nanopore-fragmentation","nanopore-library","nanopore-depot"), 
				getExperimentTypes("nanopore-fragmentation").get(0), getExperimentTypes("nanopore-depot").get(0),getExperimentTypes("ext-to-nanopore-frg-precr").get(0), DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		

		l.add(DescriptionFactory.newProcessType("Lib ONT, Dépôt", "nanopore-process-library-no-frg", ProcessCategory.find.findByCode("library"),getPropertyDefinitionsNanoporeLibrary() , getExperimentTypes("nanopore-library","nanopore-depot"), 
				getExperimentTypes("nanopore-library").get(0), getExperimentTypes("nanopore-depot").get(0),getExperimentTypes("ext-to-lib-ont").get(0), DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(DescriptionFactory.newProcessType("Run Nanopore", "nanopore-run", ProcessCategory.find.findByCode("sequencing"),null , getExperimentTypes("nanopore-depot"), 
				getExperimentTypes("nanopore-depot").get(0), getExperimentTypes("nanopore-depot").get(0),getExperimentTypes("ext-to-nanopore-depot").get(0), DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		
		l.add(DescriptionFactory.newProcessType("Run Illumina", "illumina-run", ProcessCategory.find.findByCode("sequencing"),getPropertyDefinitionsIlluminaDepotCNS() , getExperimentTypes("prepa-flowcell","prepa-fc-ordered","illumina-depot"), 
				getExperimentTypes("prepa-flowcell").get(0), getExperimentTypes("illumina-depot").get(0),getExperimentTypes("ext-to-prepa-flowcell").get(0), DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(DescriptionFactory.newProcessType("Run Opgen", "opgen-run", ProcessCategory.find.findByCode("mapping"),null , getExperimentTypes("opgen-depot"), getExperimentTypes("opgen-depot").get(0), getExperimentTypes("opgen-depot").get(0),getExperimentTypes("ext-to-opgen-depot").get(0), DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		*/
		l.add(DescriptionFactory.newProcessType("Test", "test", ProcessCategory.find.findByCode("library"),null , 
				getTestProperties(),
				Arrays.asList(getPET("ext-to-test-one-to-one",-1),getPET("test-one-to-one",0),getPET("test-one-to-one-2",0)), getExperimentTypes("test-one-to-one").get(0),getExperimentTypes("test-one-to-one-2").get(0), getExperimentTypes("ext-to-test-one-to-one").get(0), DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		
		
		DAOHelpers.saveModels(ProcessType.class, l, errors);
	}
	public static List<PropertyDefinition> getTestProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Prop. Content 1","propContent1",LevelService.getLevels(Level.CODE.Process,Level.CODE.Content),String.class, true, getLibProcessTypeCodeValues(), "ONT","single" ,1));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Prop. 1","prop1",LevelService.getLevels(Level.CODE.Process),String.class, true, "single" ,1));
		
		return propertyDefinitions;
	}

//	private static List<PropertyDefinition> getPropertyDefinitionsIlluminaDepotCNS() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//
//		//TO do multi value
//		propertyDefinitions.add(
//				DescriptionFactory.newPropertiesDefinition("Type séquencage","sequencingType"
//						, LevelService.getLevels(Level.CODE.Process),String.class, true, getSequencingType(), "single",100));
//		propertyDefinitions.add(
//				DescriptionFactory.newPropertiesDefinition("Type de lectures", "readType"
//						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("SR","PE"), "single",200));		
//		propertyDefinitions.add(
//				DescriptionFactory.newPropertiesDefinition("Longueur de lecture", "readLength"
//						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("50","100","150","250","300","500","600"), "single",300));
//		
//		propertyDefinitions.add(
//				DescriptionFactory.newPropertiesDefinition("% à déposer prévisionnel", "estimatedPercentPerLane"
//						, LevelService.getLevels(Level.CODE.Process),Double.class, true,"single",400));	
//		return propertyDefinitions;
//	}

//	private static List<Value> getSequencingType(){
//		List<Value> values = new ArrayList<Value>();
//		values.add(DescriptionFactory.newValue("Hiseq 2000/2500N", "Hiseq 2000 / 2500 N"));
//		values.add(DescriptionFactory.newValue("Hiseq 2500 Rapide", "Hiseq 2500 Rapide"));
//		values.add(DescriptionFactory.newValue("Miseq", "Miseq"));
//		values.add(DescriptionFactory.newValue("Hiseq 4000", "Hiseq 4000"));
//		return values;	
//	}
	
	
	public static List<PropertyDefinition> getPropertyDefinitionsLib300600() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Robot à utiliser","autoVsManuel", LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("MAN","ROBOT"), "single",100));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Quantité à engager", "inputQuantity", LevelService.getLevels(Level.CODE.Process),Double.class, true, DescriptionFactory.newValues("250","500"), "single",200));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Objectif Taille de banque finale", "librarySizeGoal", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("300-600","300-800"), "single",300));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Catégorie Imputation", "imputationCat", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("PRODUCTION","DEVELOPPEMENT"), "single",400));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date limite", "deadline", LevelService.getLevels(Level.CODE.Process),Date.class,false, "single",500));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Indexing", "indexing", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("Single indexing","Dual indexing","Pas d'indexing"), "single",600));
		return propertyDefinitions;
	}
	
	public static List<PropertyDefinition> getPropertyDefinitionsQPCRQuantification() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();				
		return propertyDefinitions;
	}
	
	
	public static List<PropertyDefinition> getPropertyDefinitionsNanoporeFragmentation() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();		
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Process,Level.CODE.Content),String.class, true, getLibProcessTypeCodeValues(), "ONT","single" ,1));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taille banque souhaitée","librarySize",LevelService.getLevels(Level.CODE.Process),Integer.class,true, DescriptionFactory.newValues("8","20")
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "kb"),MeasureUnit.find.findByCode( "kb"), "single",2));
		
		return propertyDefinitions;
	}
	
	public static List<PropertyDefinition> getPropertyDefinitionsNanoporeLibrary() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Process,Level.CODE.Content),String.class, true, getLibProcessTypeCodeValues(), "ONT","single" ,1));
		
		return propertyDefinitions;
	}

	private static List<Value> getLibProcessTypeCodeValues(){
        List<Value> values = new ArrayList<>();
         values.add(DescriptionFactory.newValue("ONT","ONT - Nanopore"));
         return values;
	}



	
}
