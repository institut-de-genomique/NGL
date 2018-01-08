package services.description.process;

import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;// ajout FDS

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.laboratory.processes.description.ProcessCategory;
import models.laboratory.processes.description.ProcessType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import services.description.Constants;
import services.description.common.LevelService;
import services.description.common.MeasureService;

import com.typesafe.config.ConfigFactory;

public class ProcessServiceGET extends AbstractProcessService {


	public void saveProcessCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProcessCategory> l = new ArrayList<ProcessCategory>();
//		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Banque", "library"));
//		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Pre-Sequencage", "pre-sequencing"));
		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Sequençage", "sequencing"));	
		// Processus permet accéder aux experiments satellites en dehors tout autre processus
//		l.add(DescriptionFactory.newSimpleCategory(ProcessCategory.class, "Satellites", "satellites"));	
		
		DAOHelpers.saveModels(ProcessCategory.class, l, errors);

	}
	
	public void saveProcessTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProcessType> l = new ArrayList<ProcessType>();
		
		//if(	!ConfigFactory.load().getString("ngl.env").equals("PROD") ){
//		l.add(DescriptionFactory.newProcessType("Librairie PE sans sizing", "Lib-PE-NoSizing", ProcessCategory.find.findByCode("library"), 1,
//				
//				getPropertyDefinitionsLib300600(), 
//				Arrays.asList(
//						getPET("ext-to-library",-2),
//						getPET("fragmentation",-1),
//						getPET("librairie-indexing",0),
//						getPET("amplification",1)), 
//						
//				getExperimentTypes("fragmentation").get(0), 
//				getExperimentTypes("amplification").get(0), 
//				getExperimentTypes("ext-to-library").get(0), 
//				
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		

		
		//"Satellites"
//		l.add(DescriptionFactory.newProcessType("QC / TF / Purif", "qc-transfert-purif", ProcessCategory.find.findByCode("satellites"), 2,
//				
//				null,
//				Arrays.asList(getPET("ext-to-qc-transfert-purif",-1)), 
//				
//				getExperimentTypes("fluo-quantification").get(0), 
//				getExperimentTypes("ext-to-qc-transfert-purif").get(0), 
//				getExperimentTypes("ext-to-qc-transfert-purif").get(0), 
//			
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
//		l.add(DescriptionFactory.newProcessType("Normalisation", "normalisation-process", ProcessCategory.find.findByCode("pre-sequencing"), getPropertyDefinitionsQPCRQuantification(), Arrays.asList(getPET("solution-stock",0)), 
//					getExperimentTypes("solution-stock").get(0), getExperimentTypes("solution-stock").get(0), getExperimentTypes("ext-to-solution-stock").get(0), DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
//		//"Sequençage"
		l.add(DescriptionFactory.newProcessType("Run Illumina", "illumina-run", ProcessCategory.find.findByCode("sequencing"), 3,
				
				getPropertyDefinitionsIlluminaDepot() , 
				Arrays.asList(
						getPET("ext-to-prepa-flowcell",-1),
//						getPET("solution-stock",-1),
						getPET("prepa-flowcell",0),
						getPET("prepa-fc-ordered",0),
						getPET("illumina-depot",1)),
						
				//getExperimentTypes("prepa-flowcell").get(0), 
				getExperimentTypes("prepa-fc-ordered").get(0), 
				getExperimentTypes("illumina-depot").get(0),
				getExperimentTypes("ext-to-prepa-flowcell").get(0), 
				
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
//		//"Sequençage NovaSeq"
//		l.add(DescriptionFactory.newProcessType("Run NovaSeq", "novaseq-run", ProcessCategory.find.findByCode("sequencing"), 3,
//				
//				getPropertyDefinitionsNovaSeqDepot() , 
//				Arrays.asList(
//						getPET("ext-to-prepa-flowcell",-1),
//						getPET("prepa-fc-ns",0),
//						getPET("novaseq-depot",1)),
//						
//				getExperimentTypes("prepa-fc-ns").get(0), 
//				getExperimentTypes("novaseq-depot").get(0),
//				getExperimentTypes("ext-to-prepa-flowcell").get(0), 
//				
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
		DAOHelpers.saveModels(ProcessType.class, l, errors);
	}

	private static List<PropertyDefinition> getPropertyDefinitionsIlluminaDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();

		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Concentration dilution souhaitée", "finalConcentration1"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container), Double.class, true, null
						, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",11,true)); 
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("% PhiX à ajouter", "phixPercent", LevelService.getLevels(Level.CODE.Process, Level.CODE.Container), Double.class, true, null, null, null, null, "single",16,true,"1", null));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type séquencage","sequencingType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, getSequencingType(), "single",100));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type de lectures", "readType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("SR","PE"), "single",200));		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Longueur de lecture", "readLength"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("50","100","150","250","300","500","600"),"150", "single",300));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type d'analyse","analyseType"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container, Level.CODE.Content),String.class, true, getAnalyseType(),null,"single",301));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Espèce", "species"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container),String.class, false,"single",302));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Transcriptome de reference","reference_transcriptome", 
						LevelService.getLevels(Level.CODE.Process, Level.CODE.Container),String.class, false,null,null, null, null, "single",303,true,null, null)); 
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Genome de reference","reference_genome", 
						LevelService.getLevels(Level.CODE.Process, Level.CODE.Container),String.class, false,null,null, null, null, "single",304,true,null, null)); 

		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nombre de lanes", "nbrLanes"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container),String.class, true, DescriptionFactory.newValues("1","2","3","4","5","6","7","8"), "single",305));
		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("% à déposer prévisionnel", "estimatedPercentPerLane"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container),Double.class, true,"single",400,"100"));
		
		propertyDefinitions.add(
		DescriptionFactory.newPropertiesDefinition("Urgent", "urgent"
				, LevelService.getLevels(Level.CODE.Process),String.class, false, DescriptionFactory.newValues("oui","non"), "non", "single",404));
		
		propertyDefinitions.add(
		DescriptionFactory.newPropertiesDefinition("Conserver bam", "store_bam"
				, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("oui","non"), "non", "single",405));
		
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionsNovaSeqDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();

		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Concentration dilution souhaitée", "finalConcentration1"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container), Double.class, true, null
						, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",11,true)); 
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("% PhiX à ajouter", "phixPercent", LevelService.getLevels(Level.CODE.Process, Level.CODE.Container), Double.class, true, null, null, null, null, "single",16,true,"1", null));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type séquencage","sequencingType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, getSequencingType(), "single",100));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type de lectures", "readType"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("SR","PE"), "single",200));		
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Longueur de lecture", "readLength"
						, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("50","100","150","250","300","500","600"),"150", "single",300));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Type d'analyse","analyseType"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container, Level.CODE.Content),String.class, true, getAnalyseType(),null,"single",301));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Espèce", "species"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container),String.class, false,"single",302));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Transcriptome de reference","reference_transcriptome", 
						LevelService.getLevels(Level.CODE.Process, Level.CODE.Container),String.class, false,null,null, null, null, "single",303,true,null, null)); 
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Genome de reference","reference_genome", 
						LevelService.getLevels(Level.CODE.Process, Level.CODE.Container),String.class, false,null,null, null, null, "single",304,true,null, null)); 
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Nombre de reads", "nbrRead"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container, Level.CODE.Content),String.class, true, null, null, "single",305));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Kit utilisé", "kit"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container),Long.class, false, null, null, "single",306));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("Index", "tag"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container),String.class, false, null, null, "single",306));
		propertyDefinitions.add(
				DescriptionFactory.newPropertiesDefinition("% à déposer prévisionnel", "estimatedPercentPerLane"
						, LevelService.getLevels(Level.CODE.Process, Level.CODE.Container),Double.class, true,"single",400,"100"));
		
		propertyDefinitions.add(
		DescriptionFactory.newPropertiesDefinition("Urgent", "urgent"
				, LevelService.getLevels(Level.CODE.Process),String.class, false, DescriptionFactory.newValues("oui","non"), "non", "single",404));
		
		propertyDefinitions.add(
		DescriptionFactory.newPropertiesDefinition("Conserver bam", "store_bam"
				, LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("oui","non"), "non", "single",405));
		
		return propertyDefinitions;
	}


	private static List<Value> getSequencingType(){
		List<Value> values = new ArrayList<Value>();
//		values.add(DescriptionFactory.newValue("Hiseq 2500", "Hiseq 2500"));
//		values.add(DescriptionFactory.newValue("Hiseq 2500 Rapide", "Hiseq 2500 Rapide"));
		values.add(DescriptionFactory.newValue("Miseq", "Miseq"));
		values.add(DescriptionFactory.newValue("Hiseq 3000", "Hiseq 3000"));
		values.add(DescriptionFactory.newValue("NovaSeq", "NovaSeq"));
		return values;	
	}
	
	
//	private static List<ProcessExperimentType> getExperimentTypes(String...codes) throws DAOException {
//		return DAOHelpers.getModelByCodes(ExperimentType.class,ExperimentType.find, codes);
//	}


//	public static List<PropertyDefinition> getPropertyDefinitionsLib300600() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Robot à utiliser","autoVsManuel", LevelService.getLevels(Level.CODE.Process),String.class, true, DescriptionFactory.newValues("MAN","ROBOT"), "single",100));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Quantité à engager", "inputQuantity", LevelService.getLevels(Level.CODE.Process),Double.class, true, DescriptionFactory.newValues("250","500"), "single",200));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Objectif Taille de banque finale", "librarySizeGoal", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("300-600","300-800"), "single",300));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Catégorie Imputation", "imputationCat", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("PRODUCTION","DEVELOPPEMENT"), "single",400));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date limite", "deadline", LevelService.getLevels(Level.CODE.Process),Date.class,false, "single",500));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Indexing", "indexing", LevelService.getLevels(Level.CODE.Process),String.class,true,DescriptionFactory.newValues("Single indexing","Dual indexing","Pas d'indexing"), "single",600));
//		return propertyDefinitions;
//	}
	
//	public static List<PropertyDefinition> getPropertyDefinitionsQPCRQuantification() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();				
//		return propertyDefinitions;
//	}
	
	
//	public static List<PropertyDefinition> getPropertyDefinitionsNanoporeFragmentation() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();		
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Process,Level.CODE.Content),String.class, true, getLibProcessTypeCodeValues(), "ONT","single" ,1));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taille banque souhaitée","librarySize",LevelService.getLevels(Level.CODE.Process),Integer.class,true, DescriptionFactory.newValues("8","20")
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "kb"),MeasureUnit.find.findByCode( "kb"), "single",2));
//		
//		return propertyDefinitions;
//	}
	
//	public static List<PropertyDefinition> getPropertyDefinitionsNanoporeLibrary() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Type processus banque","libProcessTypeCode",LevelService.getLevels(Level.CODE.Process,Level.CODE.Content),String.class, true, getLibProcessTypeCodeValues(), "ONT","single" ,1));
//		
//		return propertyDefinitions;
//	}

//	private static List<Value> getLibProcessTypeCodeValues(){
//        List<Value> values = new ArrayList<Value>();
//         values.add(DescriptionFactory.newValue("ONT","ONT - Nanopore"));
//         return values;
//	}

	private static List<Value> getAnalyseType(){
        List<Value> analyseType = new ArrayList<Value>();
        analyseType.add(DescriptionFactory.newValue("Bisulfite","Bisulfite"));
        analyseType.add(DescriptionFactory.newValue("DNA","DNA"));
        analyseType.add(DescriptionFactory.newValue("DNA-MP","DNA-MP"));
        analyseType.add(DescriptionFactory.newValue("RNA","RNA"));
        analyseType.add(DescriptionFactory.newValue("10X-DeNovo","10X-DeNovo"));
        analyseType.add(DescriptionFactory.newValue("10X-WGS","10X-WGS"));
        analyseType.add(DescriptionFactory.newValue("10X-SingleCell","10X-SingleCell"));
        analyseType.add(DescriptionFactory.newValue("16S","16S"));
        return analyseType;
	}

	
}
