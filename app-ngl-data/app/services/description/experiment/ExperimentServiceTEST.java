package services.description.experiment;

import static services.description.DescriptionFactory.newExperimentType;
import static services.description.DescriptionFactory.newExperimentTypeNode;
import static services.description.DescriptionFactory.newPropertiesDefinition;

//import java.io.File;
import java.util.ArrayList;
//import java.util.Date;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
//import models.laboratory.common.description.Value;
import models.laboratory.experiment.description.ExperimentCategory;
import models.laboratory.experiment.description.ExperimentType;
import models.laboratory.experiment.description.ProtocolCategory;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;

// import com.typesafe.config.ConfigFactory;

public class ExperimentServiceTEST extends AbstractExperimentService {
	
	// @SuppressWarnings("unchecked")
	@Override
	public  void saveProtocolCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ProtocolCategory> l = new ArrayList<>();
		l.add(DescriptionFactory.newSimpleCategory(ProtocolCategory.class, "Developpement", "development"));
		l.add(DescriptionFactory.newSimpleCategory(ProtocolCategory.class, "Production", "production"));
		DAOHelpers.saveModels(ProtocolCategory.class, l, errors);
	}
	
	/**
	 * Save all ExperimentCategory.
	 * @param errors        error manager
	 * @throws DAOException DAO problem
	 */
	@Override
	public  void saveExperimentCategories(Map<String,List<ValidationError>> errors) throws DAOException {
		List<ExperimentCategory> l = new ArrayList<>();
		for (ExperimentCategory.CODE code : ExperimentCategory.CODE.values()) {
			l.add(DescriptionFactory.newSimpleCategory(ExperimentCategory.class, code.name(), code.name()));
		}
		DAOHelpers.saveModels(ExperimentCategory.class, l, errors);
	}

	@Override
	public void saveExperimentTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		List<ExperimentType> l = new ArrayList<>();
/*
		l.add(newExperimentType("Ext to dépôt opgen","ext-to-opgen-depot",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), getPropertyDefinitionExtToOpgenDepot(), null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Depot Opgen", "opgen-depot",null,1600
				, ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionOpgenDepot(), 
				getInstrumentUsedTypes("ARGUS"), "ManyToOne", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		//Prepaflowcell : to finish
		l.add(newExperimentType("Ext to prepa flowcell","ext-to-prepa-flowcell",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Ext to qPCR","ext-to-qpcr",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Ext to Solution-stock","ext-to-solution-stock",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		
		l.add(newExperimentType("Preparation flowcell", "prepa-flowcell",null,1200, 
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsPrepaflowcellCNS(),
				getInstrumentUsedTypes("cBot-interne","cBot"), "ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Prep. flowcell ordonnée", "prepa-fc-ordered",null,1200, 
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionsPrepaflowcellOrdered(),
				getInstrumentUsedTypes("cBot"), "ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		

		l.add(newExperimentType("Solution stock","solution-stock",null,1000,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDefinitionSolutionStock(),
				getInstrumentUsedTypes("hand","tecan-evo-100"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Depot Illumina", "illumina-depot",null, 1400,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),getPropertyDefinitionsIlluminaDepot(),
				getInstrumentUsedTypes("MISEQ","HISEQ2000","HISEQ2500","HISEQ4000"), "OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		
		//Nanopore
		l.add(newExperimentType("Ext to Fragmentation-réparation","ext-to-nanopore-frg-precr",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Ext to Librairie ONT","ext-to-lib-ont",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Ext to Depot Nanopore","ext-to-nanopore-depot",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null,  null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Fragmentation-réparation","nanopore-fragmentation",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyFragmentationNanopore(), getInstrumentUsedTypes("eppendorf-mini-spin-plus"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Aliquot","aliquoting",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()),
				getPropertyAliquoting(), getInstrumentUsedTypes("hand"),"OneToMany", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));

		l.add(newExperimentType("Librairie ONT","nanopore-library",null,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyLibrairieNanopore(), getInstrumentUsedTypes("hand"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Depot Nanopore","nanopore-depot",null,200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()), getPropertyDepotNanopore(),
				getInstrumentUsedTypes("minion","mk1"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST) ));

		l.add(newExperimentType("Pool Tube","pool-tube",null,1200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), getPropertyDefinitionPoolTube(),
				getInstrumentUsedTypes("hand","tecan-evo-100"),"ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		*/
		l.add(newExperimentType("Ext to Test One To One","ext-to-test-one-to-one",null,-1,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.voidprocess.name()), null, null,"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Test One To One","test-one-to-one",null,2,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyTestOneToOne(), getInstrumentUsedTypes("one-to-one"),"OneToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		
		l.add(newExperimentType("Test One To One 2","test-one-to-one-2",null,3,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transformation.name()),
				getPropertyTestOneToOne(), getInstrumentUsedTypes("one-to-one"),"OneToVoid", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		
		
		l.add(newExperimentType("Aliquot","aliquoting",null,4,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()),
				getPropertyAliquoting(), getInstrumentUsedTypes("hand"),"OneToMany", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newExperimentType("Pool Tube","pool-tube",null,1200,
				ExperimentCategory.find.findByCode(ExperimentCategory.CODE.transfert.name()), 
				getPropertyDefinitionPoolTube(), getInstrumentUsedTypes("hand"),"ManyToOne", 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));

		DAOHelpers.saveModels(ExperimentType.class, l, errors);

	}


	

	


	@Override
	public void saveExperimentTypeNodes(Map<String, List<ValidationError>> errors) throws DAOException {
		newExperimentTypeNode("ext-to-test-one-to-one", getExperimentTypes("ext-to-test-one-to-one").get(0), false, false, null, null, null).save();
		newExperimentTypeNode("test-one-to-one",getExperimentTypes("test-one-to-one").get(0),false,false,false,getExperimentTypeNodes("ext-to-test-one-to-one"),null,null,getExperimentTypes("aliquoting","pool-tube")).save();
		newExperimentTypeNode("test-one-to-one-2",getExperimentTypes("test-one-to-one-2").get(0),false,false,false,getExperimentTypeNodes("test-one-to-one"),null,null,null).save();
		
		/*
		
		newExperimentTypeNode("ext-to-opgen-depot", getExperimentTypes("ext-to-opgen-depot").get(0), false, false, null, null, null).save();
		newExperimentTypeNode("ext-to-prepa-flowcell", getExperimentTypes("ext-to-prepa-flowcell").get(0), false, false, null, null, null).save();
		newExperimentTypeNode("ext-to-qpcr", getExperimentTypes("ext-to-qpcr").get(0), false, false, null, null, null).save();	
		newExperimentTypeNode("ext-to-solution-stock", getExperimentTypes("ext-to-solution-stock").get(0), false, false, null, null, null).save();	

		newExperimentTypeNode("opgen-depot",getExperimentTypes("opgen-depot").get(0),false,false,getExperimentTypeNodes("ext-to-opgen-depot"),null,null).save();
		
		
		newExperimentTypeNode("solution-stock",getExperimentTypes("solution-stock").get(0),false,false,getExperimentTypeNodes("ext-to-qpcr"),null,null).save();
		newExperimentTypeNode("prepa-flowcell",getExperimentTypes("prepa-flowcell").get(0),false,false,getExperimentTypeNodes("ext-to-prepa-flowcell","solution-stock"),null,null).save();
		newExperimentTypeNode("prepa-fc-ordered",getExperimentTypes("prepa-fc-ordered").get(0),false,false,getExperimentTypeNodes("ext-to-prepa-flowcell","solution-stock"),null,null).save();
		newExperimentTypeNode("illumina-depot",getExperimentTypes("illumina-depot").get(0),false,false,getExperimentTypeNodes("prepa-flowcell","prepa-fc-ordered"),	null,null).save();
		
		//Nanopore
		newExperimentTypeNode("ext-to-nanopore-depot", getExperimentTypes("ext-to-nanopore-depot").get(0), false, false, null, null, null).save();
		newExperimentTypeNode("ext-to-nanopore-frg-precr", getExperimentTypes("ext-to-nanopore-frg-precr").get(0), false, false, null, null, null).save();
		newExperimentTypeNode("ext-to-lib-ont", getExperimentTypes("ext-to-lib-ont").get(0), false, false, null, null, null).save();
		newExperimentTypeNode("nanopore-fragmentation",getExperimentTypes("nanopore-fragmentation").get(0),false,false,getExperimentTypeNodes("ext-to-nanopore-frg-precr"),null,null).save();
		newExperimentTypeNode("nanopore-library",getExperimentTypes("nanopore-library").get(0),false,false,getExperimentTypeNodes("ext-to-lib-ont","nanopore-fragmentation"),null,null).save();
		newExperimentTypeNode("nanopore-depot",getExperimentTypes("nanopore-depot").get(0),false,false,getExperimentTypeNodes("nanopore-library","ext-to-nanopore-depot"),null,null).save();
		
		
		newExperimentTypeNode("pool-tube",getExperimentTypes("pool-tube").get(0),false,false,getExperimentTypeNodes("solution-stock","nanopore-library"),null,null).save();		
		newExperimentTypeNode("aliquoting",getExperimentTypes("aliquoting").get(0),false,false,getExperimentTypeNodes("nanopore-fragmentation"),null,null).save();
		
		*/
		
		


	}

	private static List<PropertyDefinition> getPropertyTestOneToOne() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();				
		propertyDefinitions.add(newPropertiesDefinition("Exp Content 1", "expContent1", LevelService.getLevels(Level.CODE.Experiment,Level.CODE.Content), String.class, false, "single"));
		propertyDefinitions.add(newPropertiesDefinition("Exp 1", "exp1", LevelService.getLevels(Level.CODE.Experiment), String.class, false, "single"));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cont. In Content 1","contInContent1",LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content), String.class, true, null, null ,null,null, "single",11));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cont. In 1","contIn1",LevelService.getLevels(Level.CODE.ContainerIn), String.class, true, null, null ,null,null, "single",12));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cont. Out Content 1","contOutContent1",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), String.class, true, null, null ,null,null, "single",13));
		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Cont. Out 1","contOut1",LevelService.getLevels(Level.CODE.ContainerOut), String.class, true, null, null ,null,null, "single",14));		
		return propertyDefinitions;
	}

	private static List<PropertyDefinition> getPropertyAliquoting() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",10, false));
		
		return propertyDefinitions;
	}
	
//	private static List<PropertyDefinition> getPropertyFragmentationNanopore() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb fragmentations","fragmentionNumber",LevelService.getLevels(Level.CODE.ContainerIn), Integer.class, true, null
//				, null ,null,null, "single",11));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Qté totale dans frg","inputFrgQuantity",LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content), Double.class, true,  null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode("ng"),MeasureUnit.find.findByCode( "ng"), "single",12));
//		
//		
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Conc. finale FRG","postFrgConcentration",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",13));
//		
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Qté finale FRG","postFrgQuantity",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",14));
//		/*
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Profil","fragmentionProfile",LevelService.getLevels(Level.CODE.ContainerOut), Image.class, false, null
//				,null,null,null, "img",15));
//		 */
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Taille réelle","measuredLibrarySize",LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content), Integer.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"), "single",16));
//		
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Nb réparations","preCRNumber",LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, false, null
//				, null ,null,null, "single",17));
//		/*propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Conc. finale preCR","postPreCRConcentration",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",8));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Qté finale preCR","postPreCRQuantity",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false,null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",9));
//		
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Volume final","measuredVolume",LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",10));
//		*/
//		return propertyDefinitions;
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
//		//propertyDefinitions.add(newPropertiesDefinition("Channels with Reads", "minknowChannelsWithReads", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",301));
//        //propertyDefinitions.add(newPropertiesDefinition("Events in Reads", "minknowEvents", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, "single",302));
//        //propertyDefinitions.add(newPropertiesDefinition("Complete reads", "minknowCompleteReads", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",303));
//        //propertyDefinitions.add(newPropertiesDefinition("Read count", "metrichorReadCount", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, "single",304));
//        //propertyDefinitions.add(newPropertiesDefinition("Total 2D yield", "metrichor2DReadsYield", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, null
//		//		, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"), "single",305));
//        //propertyDefinitions.add(newPropertiesDefinition("Longest 2D read", "metrichorMax2DRead", LevelService.getLevels(Level.CODE.ContainerOut),Integer.class, false, null
//		//		, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SIZE),MeasureUnit.find.findByCode( "pb"),MeasureUnit.find.findByCode( "pb"),"single",306));
//        //propertyDefinitions.add(newPropertiesDefinition("Peak 2D quality score", "metrichorMax2DQualityScore", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, "single",307));
//
//		return propertyDefinitions;
//	}

//	private static List<PropertyDefinition> getPropertyLibrairieNanopore() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",10));
//		propertyDefinitions.add(newPropertiesDefinition("Qté engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",11));
////		propertyDefinitions.add(newPropertiesDefinition("Taille", "librarySize", LevelService.getLevels(Level.CODE.ContainerOut), Integer.class, true, null
//	//			, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "kb"),MeasureUnit.find.findByCode( "kb"), "single",8));
//		propertyDefinitions.add(newPropertiesDefinition("Conc. finale End Repair","postEndRepairConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",15));
//		propertyDefinitions.add(newPropertiesDefinition("Qté finale End Repair","postEndRepairQuality", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",20));
//
//		propertyDefinitions.add(newPropertiesDefinition("Conc. finale dA tailing","postTailingConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",30));
//		propertyDefinitions.add(newPropertiesDefinition("Qté finale dA tailing","postTailingQuality", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",40));
//
//		
///*		propertyDefinitions.add(newPropertiesDefinition("Conc. finale Ligation","measuredConcentration", LevelService.getLevels(Level.CODE.ContainerOut),Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "ng/µl"),MeasureUnit.find.findByCode( "ng/µl"), "single",50));*/
//		// Problème code car doit etre measuredquantity
//		
//		propertyDefinitions.add(newPropertiesDefinition("Qté finale Ligation","ligationQuantity", LevelService.getLevels(Level.CODE.ContainerOut,Level.CODE.Content),Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_QUANTITY),MeasureUnit.find.findByCode( "ng"),MeasureUnit.find.findByCode( "ng"), "single",60)); 
//
//		return propertyDefinitions;
//	}

//	private static List<PropertyDefinition> getPropertyDefinitionFragmentation() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(newPropertiesDefinition("Quantité engagée","inputQuantity", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Volume engagé","inputVolume", LevelService.getLevels(Level.CODE.ContainerIn),Double.class, true, "single"));
//		return propertyDefinitions;
//	}
	
	// GA separation getPropertyDefinitionsPrepaflowcellCNS / getPropertyDefinitionsPrepaflowcellCNG pour JIRA 676
	//  ==> feuille de calcul differentes pour la prepaflowcell entre CNS et CNG
//	private static List<PropertyDefinition> getPropertyDefinitionsPrepaflowcellCNS() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		
//		//InputContainer
//		propertyDefinitions.add(newPropertiesDefinition("Volume sol. stock dans dénat.", "requiredVolume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",11, false));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Volume NaOH", "NaOHVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null 
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",12,true,"1", null));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Conc. solution NaOH", "NaOHConcentration", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true,DescriptionFactory.newValues("1N","2N"), "2N", "single",13));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Volume EB", "EBVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",14, false));
//
//		propertyDefinitions.add(newPropertiesDefinition("Conc. dénat. ", "finalConcentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",15,true,"2", null));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Volume dénat.", "finalVolume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "20"
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",16));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Volume dénat. dans dilution", "requiredVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",21, false));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Volume HT1", "HT1Volume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",22, false));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Volume Phix", "phixVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",23, false));
//
//		propertyDefinitions.add(newPropertiesDefinition("Conc. sol. mère Phix", "phixConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "pM"),MeasureUnit.find.findByCode( "nM"),"single",24, true,"0.02", null));
//
//		propertyDefinitions.add(newPropertiesDefinition("Conc. dilution", "finalConcentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null 
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "pM"),MeasureUnit.find.findByCode( "nM"), "single",25));
//
//		propertyDefinitions.add(newPropertiesDefinition("Volume dilution", "finalVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "1000"
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",26));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Volume dilution sur la piste", "requiredVolume3", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",31, false));		
//		
//		
//		//Outputcontainer
//		propertyDefinitions.add(newPropertiesDefinition("% phiX", "phixPercent", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null, null, null, null, "single",51,false,"1", null));		
//		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",52,false));
//		
//				
//		return propertyDefinitions;
//	}
	
//	private List<PropertyDefinition> getPropertyDefinitionsPrepaflowcellOrdered() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		
//		//InputContainer
//		propertyDefinitions.add(newPropertiesDefinition("Vol. engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
//						MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",11, false));
//		propertyDefinitions.add(newPropertiesDefinition("Vol. PhiX", "phixVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",12, false));
//		propertyDefinitions.add(newPropertiesDefinition("Vol. RSB", "rsbVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",13, false));
//		//Add list value
//		propertyDefinitions.add(newPropertiesDefinition("Conc. Phix", "phixConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, getPhixConcentrationCodeValues(), null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",14));
//		propertyDefinitions.add(newPropertiesDefinition("Concentration dilution", "finalConcentration1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",15,true));
//		propertyDefinitions.add(newPropertiesDefinition("Volume dilution", "finalVolume1", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",16));
//		propertyDefinitions.add(newPropertiesDefinition("Vol. dil. ds dénat", "inputVolume2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",21));
//
//		propertyDefinitions.add(newPropertiesDefinition("Vol. NaOH", "NaOHVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",22));
//		
//		propertyDefinitions.add(newPropertiesDefinition("Conc. NaOH", "NaOHConcentration", LevelService.getLevels(Level.CODE.ContainerIn), String.class, true,null, null, null, null, "single",23,true,"0.1N", null));
//		propertyDefinitions.add(newPropertiesDefinition("Vol. TrisHCL", "trisHCLVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "5"
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",24));
//		propertyDefinitions.add(newPropertiesDefinition("Conc. TrisHCL", "trisHCLConcentration", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "200000000" 
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "mM"),MeasureUnit.find.findByCode( "nM"), "single",25));
//		propertyDefinitions.add(newPropertiesDefinition("Vol. master EPX", "masterEPXVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, true, null, "35"
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",26));
//		propertyDefinitions.add(newPropertiesDefinition("Concentration finale", "finalConcentration2", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false,  null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "pM"),MeasureUnit.find.findByCode( "nM"),"single",27,false));
//
//		
//		//OuputContainer
//		propertyDefinitions.add(newPropertiesDefinition("% phiX", "phixPercent", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null, null, null, null, "single",51,false,"1", null));		
//		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",28,false));
//		
//		return propertyDefinitions;
//		
//	}
	
//	private static List<Value> getPhixConcentrationCodeValues(){
//        List<Value> values = new ArrayList<Value>();
//        values.add(DescriptionFactory.newValue("0.1","100"));
//        values.add(DescriptionFactory.newValue("0.2","200"));
//        values.add(DescriptionFactory.newValue("0.3","300"));
//        return values;
//	}
	
//	private static List<PropertyDefinition> getPropertyDefinitionsLibIndexing() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		//Ajouter la liste des index illumina
//		propertyDefinitions.add(newPropertiesDefinition("Tag","tag", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Catégorie tag","tagCategory", LevelService.getLevels(Level.CODE.ContainerIn,Level.CODE.Content),String.class, true, getTagCategories(),"single"));
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
	
//	private static List<PropertyDefinition> getPropertyDefinitionsIlluminaDepot() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		//Utiliser par import ngl-data CNG de creation des depot-illumina
//		//propertyDefinitions.add(newPropertiesDefinition("Code LIMS", "limsCode", LevelService.getLevels(Level.CODE.Experiment), Integer.class, false, "single"));	
//		propertyDefinitions.add(newPropertiesDefinition("Date réelle de dépôt", "runStartDate", LevelService.getLevels(Level.CODE.Experiment), Date.class, true, "single"));
//		return propertyDefinitions;
//	}
	
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
	
//	private static List<PropertyDefinition> getPropertyDefinitionSolutionStock() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		//InputContainer
//		propertyDefinitions.add(newPropertiesDefinition("Volume à engager", "requiredVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",21, false));
//		propertyDefinitions.add(newPropertiesDefinition("Volume tampon", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
//				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",22, false));
//		//Outputcontainer
///*		propertyDefinitions.add(newPropertiesDefinition("Concentration finale", "finalConcentration", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode("nM"),"single",7,true,"10.0"));		
//		propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
//				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",8,true)); */
//		return propertyDefinitions; 
//	}
	
	private static List<PropertyDefinition> getPropertyDefinitionPoolTube() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
		//InputContainer
		propertyDefinitions.add(newPropertiesDefinition("Volume engagé", "inputVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",8, false));
	/*	propertyDefinitions.add(newPropertiesDefinition("Volume tampon à rajouter", "bufferVolume", LevelService.getLevels(Level.CODE.ContainerIn), Double.class, false, null,
				MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"),"single",9, false)); */
		//Outputcontainer
		/*	propertyDefinitions.add(newPropertiesDefinition("Volume final", "finalVolume", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, true, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_VOLUME),MeasureUnit.find.findByCode( "µL"),MeasureUnit.find.findByCode( "µL"), "single",13,true));
		propertyDefinitions.add(newPropertiesDefinition("Concentration finale", "finalConcentration", LevelService.getLevels(Level.CODE.ContainerOut), Double.class, false, null
				, MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_CONCENTRATION),MeasureUnit.find.findByCode( "nM"),MeasureUnit.find.findByCode( "nM"),"single",14,true));	*/	
		
		return propertyDefinitions;
	}	

}
