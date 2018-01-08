package services.description.experiment;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Institute;
import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.ProtocolCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
//import models.laboratory.parameter.NanoporeIndex;
import models.laboratory.processes.description.ExperimentTypeNode;
import models.utils.InstanceConstants;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.DescriptionFactory;
import services.description.Constants;
import services.description.common.LevelService;
import services.description.common.MeasureService;

import com.typesafe.config.ConfigFactory;

import play.Logger;


public class ExperimentServiceGET extends AbstractExperimentService {

	
	@SuppressWarnings("unchecked")
	public  void saveProtocolCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProtocolCategory> l = new ArrayList<ProtocolCategory>();
		l.add(DescriptionFactory.newSimpleCategory(ProtocolCategory.class, "Developpement", "development"));
		l.add(DescriptionFactory.newSimpleCategory(ProtocolCategory.class, "Production", "production"));
		DAOHelpers.saveModels(ProtocolCategory.class, l, errors);

	}
	/**
	 * Save all ExperimentCategory
	 * @param errors
	 * @throws DAOException 
	 */
	public  void saveExperimentCategories(Map<String,List<ValidationError>> errors) throws DAOException{
		List<ExperimentCategory> l = new ArrayList<ExperimentCategory>();


		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Purification", ExperimentCategory.CODE.purification.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Control qualité", ExperimentCategory.CODE.qualitycontrol.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Transfert", ExperimentCategory.CODE.transfert.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Transformation", ExperimentCategory.CODE.transformation.name()));
		l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, "Void process", ExperimentCategory.CODE.voidprocess.name()));
		
		DAOHelpers.saveModels(ExperimentCategory.class, l, errors);
	}


	public void saveExperimentTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ExperimentType> l = new ArrayList<ExperimentType>();
		
		// Enter into experiments
		l.add(newExperimentType("Ext to prepa flowcell","ext-to-prepa-flowcell",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), 
				null, 
				null,
				"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
//		l.add(newExperimentType("Ext to qPCR","ext-to-qpcr",null,-1,
//				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), 
//				null,  
//				null,
//				"OneToOne", 
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
//		l.add(newExperimentType("Ext to Solution-stock","ext-to-solution-stock",null,-1,
//				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()),
//				null,  
//				null,
//				"OneToOne", 
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		

		//QC provenant de collaborateur extérieur.
//		l.add(newExperimentType("QC Exterieur","external-qc", null,22000,
//				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), 
//				getPropertyDefinitionsExternalQC(), 
//				getInstrumentUsedTypes("hand"),
//				"OneToVoid", false, 
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		

//		l.add(newExperimentType("Solution stock","solution-stock",null,1000,
//				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
//				getPropertyDefinitionSolutionStock(),
//				getInstrumentUsedTypes("hand","tecan-evo-150"),
//				"OneToOne", 
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
		//preparation flowcells non ordonnées (MiSeq)
		l.add(newExperimentType("Preparation flowcell", "prepa-flowcell",null,1200, 
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
				getPropertyDefinitionsPrepaflowcell(),
				getInstrumentUsedTypes("cBot-interne","cBot"), 
				"ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		//preparation flowcells ordonnées (HiSeq3000, NovaSeq...)
		l.add(newExperimentType("Prep. flowcell ordonnée", "prepa-fc-ordered",null,1200, 
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
				getPropertyDefinitionsPrepaflowcellOrdered(),
				getInstrumentUsedTypes("cBot", "cBot-interne-novaseq"), 
				"ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//		
//		l.add(newExperimentType("Prep. flowcell NovaSeq", "prepa-fc-ns",null,1200, 
//				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), 
//				getPropertyDefinitionsPrepaflowcellOrdered(),
//				getInstrumentUsedTypes("cBot-interne-novaseq"), 
//				"ManyToOne", 
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
		l.add(newExperimentType("Depot Illumina", "illumina-depot",null, 1400,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyDefinitionsIlluminaDepot(),
				getInstrumentUsedTypes("MISEQ","HISEQ3000", "NOVASEQ"), 
				"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.GET)));

//		l.add(newExperimentType("Depot NovaSeq", "novaseq-depot",null, 1400,
//				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
//				getPropertyDefinitionsNovaSeqDepot(),
//				getInstrumentUsedTypes("NOVASEQ"), 
//				"OneToVoid", 
//				DescriptionFactory.getInstitutes(Constants.CODE.GET)));
		
		
		if(	!ConfigFactory.load().getString("ngl.env").equals("PROD") ){

		}
//			//library
//			l.add(newExperimentType("Fragmentation","fragmentation",null,200,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionFragmentation(),
//					getInstrumentUsedTypes("hand","covaris-s2"),"OneToOne", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET) ));
//			
//			l.add(newExperimentType("Librairie indexée","librairie-indexing",null,400,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsLibIndexing(),
//					getInstrumentUsedTypes("hand"),"OneToOne", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//			
//
//			l.add(newExperimentType("Amplification","amplification",null,800,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), null,
//					getInstrumentUsedTypes("hand","thermocycler"),"OneToOne", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//			
//			l.add(newExperimentType("Sizing sur gel","sizing",null,800,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), null,
//					null,"OneToOne", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//			
//			//quality control
//
//			//purif
//			l.add(newExperimentType("Ampure Non Ampli","ampure-na",null,-1,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()),
//					null, getInstrumentUsedTypes("hand"),"OneToOne", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//			
//			l.add(newExperimentType("Ampure Ampli","ampure-a",null,-1,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.purification.name()), null,
//					getInstrumentUsedTypes("hand"),"OneToOne", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//
//			//void
//			l.add(newExperimentType("Ext to Banque","ext-to-library",null,-1,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//					
//			l.add(newExperimentType("Migration sur puce (ampli)","chip-migration-post-pcr",null,650,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsChipMigration(), 
//					getInstrumentUsedTypes("agilent-2100-bioanalyzer"),"OneToVoid", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//			
//			l.add(newExperimentType("Migration sur puce (non ampli)","chip-migration-pre-pcr",null,250,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsChipMigration(), 
//					getInstrumentUsedTypes("agilent-2100-bioanalyzer"),"OneToVoid", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//			
//			l.add(newExperimentType("Dosage fluorimétrique","fluo-quantification",null,450,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), null, 
//					getInstrumentUsedTypes("qubit"),"OneToVoid", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//			
//			l.add(newExperimentType("Quantification qPCR","qPCR-quantification",null,850,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.qualitycontrol.name()), getPropertyDefinitionsQPCR(), 
//					getInstrumentUsedTypes("ABI7900HT","QS6"),"OneToVoid", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//
//			l.add(newExperimentType("Pool Tube","pool-tube",null,1200,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), getPropertyDefinitionPoolTube(),
//					getInstrumentUsedTypes("hand","tecan-evo-150","tecan-evo-200"),"ManyToOne", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//
//			l.add(newExperimentType("Pool x => tubes","pool-x-to-tubes",null,1,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), getPropertyDefinitionPoolTube(),
//					getInstrumentUsedTypes("tecan-evo-150"),"ManyToOne", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
//			
//			//satellite, exceptional OneToOne
//			l.add(newExperimentType("Ext to Eval / TF / purif","ext-to-qc-transfert-purif",null,-1,
//					ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
//					DescriptionFactory.getInstitutes(Constants.CODE.GET)));
			
			

		DAOHelpers.saveModels(ExperimentType.class, l, errors);

	}

	public void saveExperimentTypeNodes(Map<String, List<ValidationError>> errors) throws DAOException {

		//newExperimentTypeNode("ext-to-opgen-depot", getExperimentTypes("ext-to-opgen-depot").get(0), false, false, null, null, null).save();
		newExperimentTypeNode("ext-to-prepa-flowcell", getExperimentTypes("ext-to-prepa-flowcell").get(0), 
				false, false, false, //satellites
				null,  // no previous nodes
				null, null, null
				).save();
		
//		newExperimentTypeNode("ext-to-prepa-fc-ordered", getExperimentTypes("ext-to-prepa-fc-ordered").get(0), 
//				false, false, false, 
//				null, // no previous nodes
//				null, null, null
//				).save();
		
//		newExperimentTypeNode("ext-to-qpcr", getExperimentTypes("ext-to-qpcr").get(0), 
//				false, false, false, //satellites
//				null,  // no previous nodes
//				null, null, null
//				).save();	
		
//		newExperimentTypeNode("ext-to-solution-stock", getExperimentTypes("ext-to-solution-stock").get(0), 
//				false, false, false, //satellites
//				null,  // no previous nodes
//				null, null, null
//				).save();		
//		
		//preparation flowcells non ordonnées (MiSeq)
		newExperimentTypeNode("prepa-flowcell",getExperimentTypes("prepa-flowcell").get(0),
				false,false,false,
				getExperimentTypeNodes("ext-to-prepa-flowcell"),
				null,null,null
				).save();
		Logger.debug("Before saving prepa-fc-ordered");
		//preparation flowcells ordonnées (HiSeq3000, NovaSeq...)
		newExperimentTypeNode("prepa-fc-ordered",getExperimentTypes("prepa-fc-ordered").get(0),
				false, false, false,
				getExperimentTypeNodes("ext-to-prepa-flowcell"),
				null, null, null
				).save();
		Logger.debug("After saving prepa-fc-ordered");
//		newExperimentTypeNode("prepa-fc-ns",getExperimentTypes("prepa-fc-ns").get(0),
//				false, false, false,
//				getExperimentTypeNodes("ext-to-prepa-flowcell"),
//				null, null, null
//				).save();

		newExperimentTypeNode("illumina-depot",getExperimentTypes("illumina-depot").get(0),
				false,false,false,
				getExperimentTypeNodes("prepa-fc-ordered","prepa-flowcell"), // previous nodes
				null,
				null, // pas qc
				null  // pas tranfert
				).save();

//		newExperimentTypeNode("novaseq-depot",getExperimentTypes("novaseq-depot").get(0),
//				false,false,false,
//				getExperimentTypeNodes("prepa-fc-ns"), // previous nodes
//				null,
//				null, // pas qc
//				null  // pas tranfert
//				).save();
		 
//		newExperimentTypeNode("ext-to-library", getExperimentTypes("ext-to-library").get(0), false, false, null, null, null).save();
		
		//REM : experimentTypes list confirmées par Julie
//		newExperimentTypeNode("fragmentation", getExperimentTypes("fragmentation").get(0), false, false, getExperimentTypeNodes("ext-to-library"), 
//				getExperimentTypes("ampure-na"), getExperimentTypes("fluo-quantification","chip-migration-pre-pcr")).save();
		
//		newExperimentTypeNode("librairie-indexing", getExperimentTypes("librairie-indexing").get(0), false, false, getExperimentTypeNodes("fragmentation"), 
//				getExperimentTypes("ampure-na"), getExperimentTypes("fluo-quantification","chip-migration-pre-pcr")).save();
		
//		newExperimentTypeNode("amplification", getExperimentTypes("amplification").get(0), false, false, false, null, null, null, null).save();
		
//		newExperimentTypeNode("sizing", getExperimentTypes("sizing").get(0), false, false, false, null, null, null, null).save();
	
//		newExperimentTypeNode("ext-to-qc-transfert-purif", getExperimentTypes("ext-to-qc-transfert-purif").get(0), 
//				false, false, false, 
//				null, //pas d'exigences d'entrée
//				getExperimentTypes("ampure-na"), 
//				getExperimentTypes("qPCR-quantification", "fluo-quantification", "chip-migration-post-pcr", "chip-migration-pre-pcr"),
//				getExperimentTypes("pool-tube")).save();		
	
//		newExperimentTypeNode("solution-stock",getExperimentTypes("solution-stock").get(0),false, false,false,getExperimentTypeNodes("sizing","amplification"),null,null,getExperimentTypes("pool-tube", "pool-x-to-tubes")).save();
		
		Logger.debug("ExperimentServiceGET saveExperimentTypeNodes ");
			
	}

	
//	private List<PropertyDefinition> getPropertyDefinitionsQPCR() {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		
//		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null, 
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), MeasureUnit.find.findByCode( "nM"), MeasureUnit.find.findByCode("nM"),
//				"single", 11, true, null, "2"));		
//		
//		propertyDefinitions.add(newPropertiesDefinition("Concentration", "concentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, "F", null, 
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION), MeasureUnit.find.findByCode("ng/µl"), MeasureUnit.find.findByCode("ng/µl"),
//				"single", 12, true, null, "2"));		
//		
//		return propertyDefinitions;
//	}


//	private List<PropertyDefinition> getPropertyDefinitionDepotBionano() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single",100));
//
//		propertyDefinitions.add(newPropertiesDefinition("FC active","flowcellActiveOnChip", LevelService.getLevels(Level.CODE.ContainerIn),Boolean.class, false, null, null, null, null,"single", 8, true,"true", null));
//		propertyDefinitions.add(newPropertiesDefinition("Relance optimisation","optimizationRedo", LevelService.getLevels(Level.CODE.ContainerIn),Boolean.class, false, null, null, null, null,"single", 8, true,"false", null));
//
//		propertyDefinitions.add(newPropertiesDefinition("Laser","laser", LevelService.getLevels(Level.CODE.ContainerIn),String.class, false, DescriptionFactory.newValues("vert","rouge","vert et rouge"), null, null, null,"single", 9, true));
//
//
//		propertyDefinitions.add(newPropertiesDefinition("Temps de concentration recommandé","ptrConcentrationTime", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",10, true));
//		propertyDefinitions.add(newPropertiesDefinition("Temps de concentration réel","realConcentrationTime", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, DescriptionFactory.newValues("600","450","400","350","300","250","200","150"),
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",11, true));
//		propertyDefinitions.add(newPropertiesDefinition("Nombre de cycles","nbCycles", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, null, null, null, null,"single", 12, true));
//		//Image 
//		propertyDefinitions.add(newPropertiesDefinition("Photo zone pillar","pillarRegionPicture", LevelService.getLevels(Level.CODE.ContainerIn),Image.class, false, null, null, null, null,"img", 13, true));
//		propertyDefinitions.add(newPropertiesDefinition("Voltage","voltage", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, null, null, null, null,"single", 14, true));
//		propertyDefinitions.add(newPropertiesDefinition("Nb d'optimisations","nbOfOptimizations", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, false, DescriptionFactory.newValues("2","3","4"), null, null, null,"single", 15, true));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Optimized Bump Time","optimizedBumpTime", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",16, true));
//		propertyDefinitions.add(newPropertiesDefinition("Optimized Load Time","optimizedLoadTime", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"),"single",17, true)); 
//		propertyDefinitions.add(newPropertiesDefinition("Résumé manips","manipSummarize", LevelService.getLevels(Level.CODE.ContainerIn),File.class, false, null, null, null, null,"file", 20, true));
//		
//
//		return propertyDefinitions;
//	}


//	private List<PropertyDefinition> getPropertyDefinitionPreparationIrysChip() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",51,true,"8", null));		
//		return propertyDefinitions;
//	}


//	private List<PropertyDefinition> getPropertyDefinitionIrysPrepNLRS() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",8, true));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",9, true));
//
//		propertyDefinitions.add(newPropertiesDefinition("Quantité prévue par le protocole","requiredQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, true, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"),"single",11, true));
//		//File
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Tableau sélection enzyme","enzymeLabelDensity",LevelService.getLevels(Level.CODE.ContainerIn), File.class, false, "file", 12));
//
//		propertyDefinitions.add(newPropertiesDefinition("Enzyme de restriction", "restrictionEnzyme", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true,DescriptionFactory.newValues("BspQI","Bsm1","BbvCI","BsrD1"), null, "single",13));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Unités d'enzyme","enzymeUnit", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null, null, null, null,"single", 14, true));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Volume Enzyme","restrictionEnzymeVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",15, true));
//
//		propertyDefinitions.add(newPropertiesDefinition("Enzyme de restriction 2", "restrictionEnzyme2", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false,DescriptionFactory.newValues("BspQI","Bsm1","BbvCI","BsrD1"), null, "single",16));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Volume Enzyme 2","restrictionEnzyme2Volume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",17, true));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Unités d'enzyme 2","enzyme2Unit", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null, null, null, null,"single", 18, true));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Concentration 1", "measuredConc1", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",19, true));
//
//		propertyDefinitions.add(newPropertiesDefinition("Concentration 2", "measuredConc2", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",20, true));
//
//		propertyDefinitions.add(newPropertiesDefinition("Concentration 3", "measuredConc3", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",21, true));
//
//		propertyDefinitions.add(newPropertiesDefinition("Concentration arrondie", "nlrsConcentration", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"),"single",24, true));
//
//		propertyDefinitions.add(newPropertiesDefinition("CV","variationCoefficient", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null, null, null, null,"single", 25, true));
//
//		return propertyDefinitions;
//	}



	
//	private static List<PropertyDefinition> getPropertyAliquoting() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µl"),MeasureUnit.find.findByCode( "µl"), "single",10, false));
//		
//		return propertyDefinitions;
//	}
	
//	private static List<PropertyDefinition> getPropertyFragmentationNanopore() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb fragmentations","fragmentionNumber",LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, true, null
//				, null ,null,null, "single",11));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Qté totale dans frg","inputFrgQuantity",LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content), Double.class, true,  null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode("ng"),MeasureUnit.find.findByCode( "ng"), "single",12));
//		
//		
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Conc. finale FRG","postFrgConcentration",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",13));
//		
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Qté finale FRG","postFrgQuantity",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",14));
//		/*
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Profil","fragmentionProfile",LevelService.getLevels(Level.CODE.ContainerOut), Image.class, false, null
//				,null,null,null, "img",15));
//		 */
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taille réelle","measuredLibrarySize",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Integer.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"), "single",16));
//		
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb réparations","preCRNumber",LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, null
//				, null ,null,null, "single",17));
//		
//		return propertyDefinitions;
//
//	}

	
//	private static List<PropertyDefinition> getPropertyDepotNanopore() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single",300));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("PDF Report","report",LevelService.getLevels(Level.CODE.Experiment), File.class, false, "file", 400));
//
//        // Unite a verifier
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Date creation","loadingReport.creationDate",LevelService.getLevels(Level.CODE.ContainerIn), Date.class, false, "object_list",600));
//        propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Heure dépot","loadingReport.hour",LevelService.getLevels(Level.CODE.ContainerIn), String.class, false,"object_list",601));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Temps","loadingReport.time",LevelService.getLevels(Level.CODE.ContainerIn), Long.class, false,null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_TIME),MeasureUnit.find.findByCode( "h"),MeasureUnit.find.findByCode( "h"), "object_list",602));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Volume","loadingReport.volume",LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "object_list",603));
//
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Groupe","qcFlowcell.group",LevelService.getLevels(Level.CODE.ContainerOut), String.class, false, false, "object_list",700));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb pores actifs à réception","qcFlowcell.preLoadingNbActivePores",LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, "object_list",701));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb pores actifs lors du dépôt","qcFlowcell.postLoadingNbActivePores",LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, "object_list",702));
//		
//		return propertyDefinitions;
//	}

//	private static List<PropertyDefinition> getPropertyLibrairieNanopore() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µl"),MeasureUnit.find.findByCode( "µl"), "single",10));
//		propertyDefinitions.add(newPropertiesDefinition("Qté engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",11));
//		propertyDefinitions.add(newPropertiesDefinition("Catégorie tag","tagCategory", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class, false, getTagCategoriesNanopore(),"SINGLE-INDEX", "single",14));
//		
//		
//		propertyDefinitions.add(newPropertiesDefinition("Conc. finale End Repair","postEndRepairConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",15));
//		propertyDefinitions.add(newPropertiesDefinition("Qté finale End Repair","postEndRepairQuality", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",20));
//
//		propertyDefinitions.add(newPropertiesDefinition("Conc. finale dA tailing","postTailingConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",30));
//		propertyDefinitions.add(newPropertiesDefinition("Qté finale dA tailing","postTailingQuality", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",40));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Qté finale Ligation","ligationQuantity", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",60)); 
//
//		
//		return propertyDefinitions;
//	}

//	private static List<Value> getTagCategoriesNanopore(){
//		List<Value> values = new ArrayList<Value>();
//		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));		
//		return values;	
//	}

//	private static List<PropertyDefinition> getPropertyDefinitionFragmentation() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, "single"));
//		return propertyDefinitions;
//	}
	
	// GA separation getPropertyDefinitionsPrepaflowcellCNS / getPropertyDefinitionsPrepaflowcellCNG pour JIRA 676
	//  ==> feuille de calcul differentes pour la prepaflowcell entre CNS et CNG
//	private static List<PropertyDefinition> getPropertyDefinitionsPrepaflowcell() throws DAOException {
//	List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//	
//	//InputContainer
//	propertyDefinitions.add(newPropertiesDefinition("Volume sol. stock dans dénat.", "requiredVolume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
//			MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",11, false));
//	
//	propertyDefinitions.add(newPropertiesDefinition("Volume NaOH", "NaOHVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null 
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",12,true,"1", null));
//	
//	propertyDefinitions.add(newPropertiesDefinition("Conc. solution NaOH", "NaOHConcentration", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true,DescriptionFactory.newValues("1N","2N"), "2N", "single",13));
//	
//	propertyDefinitions.add(newPropertiesDefinition("Volume EB", "EBVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",14, false));
//
//	propertyDefinitions.add(newPropertiesDefinition("Conc. dénat. ", "finalConcentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",15,true,"2", null));
//	
//	propertyDefinitions.add(newPropertiesDefinition("Volume dénat.", "finalVolume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "20"
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",16));
//	
//	propertyDefinitions.add(newPropertiesDefinition("Volume dénat. dans dilution", "requiredVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",21, false));
//	
//	propertyDefinitions.add(newPropertiesDefinition("Volume HT1", "HT1Volume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",22, false));
//	
//	propertyDefinitions.add(newPropertiesDefinition("Volume Phix", "phixVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",23, false));
//
//	propertyDefinitions.add(newPropertiesDefinition("Conc. sol. mère Phix", "phixConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "pM"),MeasureUnit.find.findByCode( "nM"),"single",24, true,"0.02", null));
//
//	propertyDefinitions.add(newPropertiesDefinition("Conc. dilution", "finalConcentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null 
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "pM"),MeasureUnit.find.findByCode( "nM"), "single",25));
//
//	propertyDefinitions.add(newPropertiesDefinition("Volume dilution", "finalVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "1000"
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",26));
//	
//	propertyDefinitions.add(newPropertiesDefinition("Volume dilution sur la piste", "requiredVolume3", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",31, false));		
//	
//	
//	//Outputcontainer
//	propertyDefinitions.add(newPropertiesDefinition("% phiX", "phixPercent", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null, null, null, null, "single",51,false,"1", null));		
//	propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",52,false));
//	
//			
//	return propertyDefinitions;
//}

	
	private List<PropertyDefinition> getPropertyDefinitionsPrepaflowcellOrdered() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		 
		//

		propertyDefinitions.add(newPropertiesDefinition("Concentration dilution souhaitée", "finalConcentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",11,true)); 
		propertyDefinitions.add(newPropertiesDefinition("Volume dilution final", "finalVolume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "7"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",12)); //avec valeur par defaut
		propertyDefinitions.add(newPropertiesDefinition("Vol. échantillon engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
						MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",13, false));
		propertyDefinitions.add(newPropertiesDefinition("Vol. RSB", "rsbVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",14, false));
		propertyDefinitions.add(newPropertiesDefinition("Volume final prélever", "finalVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "7"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",15));
		propertyDefinitions.add(newPropertiesDefinition("% PhiX à ajouter", "phixPercent", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null, null, null, "single",16,true,null, null));

//		propertyDefinitions.add(newPropertiesDefinition("Conc. Phix", "phixConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",17, false, "0.3", "1"));
		propertyDefinitions.add(newPropertiesDefinition("Conc. Phix", "phixConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "0.3"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",17));
		propertyDefinitions.add(newPropertiesDefinition("Vol. PhiX à prélever", "phixVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",18, false));
		propertyDefinitions.add(newPropertiesDefinition("Prelever Vol. éch.+Phix", "echPhix", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",21));
		propertyDefinitions.add(newPropertiesDefinition("Ajouter Vol. NaOH", "NaOHVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",22));
		propertyDefinitions.add(newPropertiesDefinition("Conc. NaOH", "NaOHConcentration", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true,null, null, null, null, "single",23,true,"0.1N", null));
		
		propertyDefinitions.add(newPropertiesDefinition("Ajouter Vol. TrisHCL", "trisHCLVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",24));
		propertyDefinitions.add(newPropertiesDefinition("Conc. TrisHCL", "trisHCLConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "200000000" 
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "mM"),MeasureUnit.find.findByCode( "nM"), "single",25));
		propertyDefinitions.add(newPropertiesDefinition("Ajouter Vol. master EPX", "masterEPXVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "35"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",26));
		
		
		
//		//InputContainer 

//		propertyDefinitions.add(newPropertiesDefinition("Type librairie","Type_librairie", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class, true,getLibrairieType(),null, "single",300)); 
//		propertyDefinitions.add(newPropertiesDefinition("Type d'analyse","analyseType", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, true,getAnalyseType(),null, "single",301)); 
		
	
		return propertyDefinitions;
		
	}
	private List<PropertyDefinition> getPropertyDefinitionsPrepaflowcell() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		 
		//

		propertyDefinitions.add(newPropertiesDefinition("Concentration dilution souhaitée", "finalConcentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",11,true)); 
		propertyDefinitions.add(newPropertiesDefinition("Volume dilution final", "finalVolume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "7"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",12)); //avec valeur par defaut
		propertyDefinitions.add(newPropertiesDefinition("Vol. échantillon engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
						MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",13, false));
		propertyDefinitions.add(newPropertiesDefinition("Vol. RSB", "rsbVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",14, false));
		propertyDefinitions.add(newPropertiesDefinition("Volume final prélever", "finalVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "7"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",15));
		propertyDefinitions.add(newPropertiesDefinition("% PhiX à ajouter", "phixPercent", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null, null, null, "single",16,true,null, null));

//		propertyDefinitions.add(newPropertiesDefinition("Conc. Phix", "phixConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",17, false, "0.3", "1"));
		propertyDefinitions.add(newPropertiesDefinition("Conc. Phix", "phixConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "0.3"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",17));
		propertyDefinitions.add(newPropertiesDefinition("Vol. PhiX à prélever", "phixVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",18, false));
		propertyDefinitions.add(newPropertiesDefinition("Prelever Vol. éch.+Phix", "echPhix", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",21));
		propertyDefinitions.add(newPropertiesDefinition("Ajouter Vol. NaOH", "NaOHVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",22));
		propertyDefinitions.add(newPropertiesDefinition("Conc. NaOH", "NaOHConcentration", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true,null, null, null, null, "single",23,true,"0.1N", null));
		
		propertyDefinitions.add(newPropertiesDefinition("Ajouter Vol. TrisHCL", "trisHCLVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",24));
		propertyDefinitions.add(newPropertiesDefinition("Conc. TrisHCL", "trisHCLConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "200000000" 
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "mM"),MeasureUnit.find.findByCode( "nM"), "single",25));
		propertyDefinitions.add(newPropertiesDefinition("Ajouter Vol. master EPX", "masterEPXVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "35"
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",26));
		
		
		
//		//InputContainer 

//		propertyDefinitions.add(newPropertiesDefinition("Type librairie","Type_librairie", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),String.class, true,getLibrairieType(),null, "single",300)); 
//		propertyDefinitions.add(newPropertiesDefinition("Type d'analyse","analyseType", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, true,getAnalyseType(),null, "single",301)); 
		
	
		return propertyDefinitions;
		
	}
//	private static List<Value> getPhixConcentrationCodeValues(){
//        List<Value> values = new ArrayList<Value>();
//        values.add(DescriptionFactory.newValue("2","2.0"));
//        values.add(DescriptionFactory.newValue("0.2","0.2"));
//        return values;
//	}
	
	
	private static List<Value> getAnalyseType(){
        List<Value> analyseType = new ArrayList<Value>();
        analyseType.add(DescriptionFactory.newValue("Bisulfite","Bisulfite"));
        analyseType.add(DescriptionFactory.newValue("DNA","DNA"));
        analyseType.add(DescriptionFactory.newValue("DNA-MP","DNA-MP"));
        analyseType.add(DescriptionFactory.newValue("RNA","RNA"));
        analyseType.add(DescriptionFactory.newValue("16S","16S"));
        return analyseType;
	}

    protected static List<Value> getLibrairieType(){
            List<Value> values = new ArrayList<Value>();
            values.add(DescriptionFactory.newValue("Bisulfite-DNA","Bisulfite-DNA"));
            values.add(DescriptionFactory.newValue("ChIP-Seq","ChIP-Seq"));
            values.add(DescriptionFactory.newValue("DNA","DNA"));
            values.add(DescriptionFactory.newValue("DNA-MP","DNA-MP"));
            values.add(DescriptionFactory.newValue("MeDIP-Seq","MeDIP-Seq"));
            values.add(DescriptionFactory.newValue("RAD-Seq","RAD-Seq"));
            values.add(DescriptionFactory.newValue("ReadyToLoad","ReadyToLoad"));
            values.add(DescriptionFactory.newValue("RNA","RNA"));
            values.add(DescriptionFactory.newValue("RNA-Stranded","RNA-Stranded"));
            values.add(DescriptionFactory.newValue("10X","10X"));
            values.add(DescriptionFactory.newValue("16S","16S"));
            return values;
    }

	protected static List<Value> getTagCategories(){
		List<Value> values = new ArrayList<Value>();
		values.add(DescriptionFactory.newValue("SINGLE-INDEX", "SINGLE-INDEX"));	
		values.add(DescriptionFactory.newValue("DUAL-INDEX", "DUAL-INDEX"));	
		values.add(DescriptionFactory.newValue("NO-INDEX", "NO-INDEX"));
		values.add(DescriptionFactory.newValue("10X", "10X"));
		return values;	
	}
	
//	private static List<PropertyDefinition> getPropertyDefinitionsLibIndexing() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		//Ajouter la liste des index illumina
//		propertyDefinitions.add(newPropertiesDefinition("Tag","tag", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, false, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, "single"));
//		return propertyDefinitions;
//	}

	

	//TODO 
	// Propriete taille en output et non en input ?
	// Valider les keys
//	public static List<PropertyDefinition> getPropertyDefinitionsChipMigration() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		// A supprimer une fois le type de support category sera géré
//		propertyDefinitions.add(newPropertiesDefinition("Position","position", LevelService.getLevels(Level.CODE.ContainerIn),Integer.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, "single"));		
//		propertyDefinitions.add(newPropertiesDefinition("Taille", "size", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, true,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE), MeasureUnit.find.findByCode("kb"), MeasureUnit.find.findByCode("kb"), "single"));
//		// Voir avec Guillaume comment gérer les fichiers
//		propertyDefinitions.add(newPropertiesDefinition("Profil DNA HS", "fileResult", LevelService.getLevels(Level.CODE.ContainerOut),String.class, true, "single"));
//		return propertyDefinitions;
//	}
	
	private static List<PropertyDefinition> getPropertyDefinitionsIlluminaDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		//Utiliser par import ngl-data CNG de creation des depot-illumina
		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Espèce","species", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, false,"single",20)); 
		propertyDefinitions.add(newPropertiesDefinition("Transcriptome de reference","reference_transcriptome", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, false,"single",21)); 
		propertyDefinitions.add(newPropertiesDefinition("Genome de reference","reference_genome", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, false, "single",22)); 
		return propertyDefinitions;
	}
	
	private static List<PropertyDefinition> getPropertyDefinitionsNovaSeqDepot() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		//Utiliser par import ngl-data CNG de creation des depot-illumina
		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Espèce","species", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, false,"single",20)); 
		propertyDefinitions.add(newPropertiesDefinition("Transcriptome de reference","reference_transcriptome", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, false,"single",21)); 
		propertyDefinitions.add(newPropertiesDefinition("Genome de reference","reference_genome", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, false, "single",22)); 
		return propertyDefinitions;
	}
	
//	private static List<PropertyDefinition> getPropertyDefinitionExtToOpgenDepot() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, false, "single"));
//		return propertyDefinitions;
//	}	
	
//	private static List<PropertyDefinition> getPropertyDefinitionOpgenDepot() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single"));
//		return propertyDefinitions;
//	}
	
	private static List<PropertyDefinition> getPropertyDefinitionSolutionStock() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Volume à engager", "requiredVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, "IP",
				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",21, false,null, "1"));
		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, "IP"
				,null,MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",22, false,null,"1"));
		
		//Outputcontainer
		return propertyDefinitions; 
	}
	
//	private static List<PropertyDefinition> getPropertyDefinitionPoolTube() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		//InputContainer
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",8, false));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Label de travail", "workName", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Container), String.class, false, null, null, 
//				"single", 30, true, null,null));
//		
//		
//		return propertyDefinitions;
//	}	
	
	
//	private List<PropertyDefinition> getPropertyDefinitionsExternalQC() {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		
//		propertyDefinitions.add(newPropertiesDefinition("Volume fourni", "providedVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
//				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single", 11, true, null,null));
//		propertyDefinitions.add(newPropertiesDefinition("Concentration fournie", "providedConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
//				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode("ng/µl"),"single", 13, true, null,null));
//		propertyDefinitions.add(newPropertiesDefinition("Quantité fournie", "providedQuantity", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
//				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode("ng"),"single", 15, true, null,null));
//		propertyDefinitions.add(newPropertiesDefinition("Taille fournie", "providedSize", LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, false, null, 
//				null, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode("pb"),"single", 16, true, null,null));
//		
//		propertyDefinitions.add(newPropertiesDefinition("RIN", "providedRin", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null, 
//				null, "single", 17, false, null, null));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Commentaire", "comment", LevelService.getLevels(Level.CODE.ContainerIn), String.class, false, null, 
//				null, null, null, null,"single", 18, true, null,null));
//
//		
//		return propertyDefinitions;
//		
//	}

}
