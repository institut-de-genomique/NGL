package services.description.instrument;

import static services.description.DescriptionFactory.newInstrumentCategory;
import static services.description.DescriptionFactory.newInstrumentUsedType;
import static services.description.DescriptionFactory.newPropertiesDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.laboratory.common.description.Level;
import models.laboratory.common.description.MeasureCategory;
import models.laboratory.common.description.MeasureUnit;
import models.laboratory.common.description.PropertyDefinition;
import models.laboratory.instrument.description.Instrument;
import models.laboratory.instrument.description.InstrumentCategory;
import models.laboratory.instrument.description.InstrumentUsedType;
import models.utils.dao.DAOException;
import models.utils.dao.DAOHelpers;
import play.data.validation.ValidationError;
import services.description.Constants;
import services.description.DescriptionFactory;
import services.description.common.LevelService;
import services.description.common.MeasureService;

public class InstrumentServiceTEST extends AbstractInstrumentService {
	
	@Override
	public void saveInstrumentCategories(Map<String, List<ValidationError>> errors) throws DAOException {
		List<InstrumentCategory> l = new ArrayList<>();
		l.add(newInstrumentCategory("Covaris","covaris"));
		l.add(newInstrumentCategory("Spri","spri"));
		l.add(newInstrumentCategory("Thermocycleur","thermocycler"));
		l.add(newInstrumentCategory("Centrifugeuse","centrifuge"));

		
		l.add(newInstrumentCategory("Quantification par fluorométrie","fluorometer"));
		l.add(newInstrumentCategory("Appareil de qPCR","qPCR-system"));
		l.add(newInstrumentCategory("Electrophorèse sur puce","chip-electrophoresis"));
		
		l.add(newInstrumentCategory("Main","hand"));
		l.add(newInstrumentCategory("CBot","cbot"));
		
		l.add(newInstrumentCategory("Séquenceur Illumina","illumina-sequencer"));
		l.add(newInstrumentCategory("Cartographie Optique Opgen","opt-map-opgen"));
		l.add(newInstrumentCategory("Séquenceur Nanopore","nanopore-sequencer"));
		l.add(newInstrumentCategory("Extérieur","extseq"));
		
		l.add(newInstrumentCategory("Robot pipetage","liquid-handling-robot"));
		l.add(newInstrumentCategory("Appareil de sizing","sizing-system"));
				
		DAOHelpers.saveModels(InstrumentCategory.class, l, errors);
	}
	
	@Override
	public void saveInstrumentUsedTypes(Map<String, List<ValidationError>> errors) throws DAOException {
		
		List<InstrumentUsedType> l = new ArrayList<>();
		
		//CNS
		/*
		l.add(newInstrumentUsedType("Covaris S2", "covaris-s2", InstrumentCategory.find.findByCode("covaris"), getCovarisProperties(), 
				getInstruments(
						createInstrument("Covaris1", "Covaris1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)), 
						createInstrument("Covaris2", "Covaris2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)) ) ,
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("Spri", "spri", InstrumentCategory.find.findByCode("spri"), getSpriProperties(), 
				getInstruments(
						createInstrument("Spri1", "Spri1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)), 
						createInstrument("Spri2", "Spri2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)), 
						createInstrument("Spri3", "Spri3", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)) ), 
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("Fluoroskan", "fluoroskan", InstrumentCategory.find.findByCode("fluorometer"),null, 
				getInstruments(
						createInstrument("Fluoroskan1", "Fluoroskan1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST))), 
				getContainerSupportCategories(new String[]{"tube"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST))); 
		
		
		l.add(newInstrumentUsedType("Stratagene qPCR system", "stratagene-qPCR", InstrumentCategory.find.findByCode("qPCR-system"), getQPCRProperties(), 
				getInstruments(
						createInstrument("Stratagene1", "Stratagene1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST))),
				getContainerSupportCategories(new String[]{"tube","96-well-plate"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("cBot-interne", "cBot-interne", InstrumentCategory.find.findByCode("cbot"), getCBotInterneProperties(), 
				getInstruments(
						createInstrument("cBot Fluor A", "cBot-Fluor-A", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)),
						createInstrument("cBot Fluor B", "cBot-Fluor-B", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)),
						createInstrument("cBot Platine A", "cBot-Platine-A", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)),
						createInstrument("cBot Platine B", "cBot-Platine-B", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)),
						createInstrument("cBot Mimosa", "cBot-Mimosa", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)),
						createInstrument("cBot Melisse", "cBot-Melisse", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST))), 
				getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"flowcell-2","flowcell-1"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));

		
		l.add(newInstrumentUsedType("ARGUS", "ARGUS", InstrumentCategory.find.findByCode("opt-map-opgen"), getArgusProperties(), 
				getInstrumentOpgen(),
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"mapcard"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));	
		
		l.add(newInstrumentUsedType("EXTSOLEXA", "EXTSOLEXA", InstrumentCategory.find.findByCode("extseq"), null, 
				getInstrumentExtSolexa(),
				getContainerSupportCategories(new String[]{"flowcell-2","flowcell-1","flowcell-8"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("Biomek FX", "biomekFX", InstrumentCategory.find.findByCode("liquid-handling-robot"), null, 
				getInstruments(
						createInstrument("walle", "WALLE", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)), 
						createInstrument("r2d2", "R2D2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)) ) ,
				getContainerSupportCategories(new String[]{"96-well-plate"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));

		l.add(newInstrumentUsedType("TECAN evo 100", "tecan-evo-100", InstrumentCategory.find.findByCode("liquid-handling-robot"), null, 
				getInstruments(
						createInstrument("wolverine", "Wolverine", null, true, "/bureautique/atelier/SOLEXA/Solstock_TECAN/", DescriptionFactory.getInstitutes(Constants.CODE.TEST)),
						createInstrument("arrow", "Arrow", null, true, "/bureautique/atelier/SOLEXA/Solstock_TECAN/", DescriptionFactory.getInstitutes(Constants.CODE.TEST))),
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));

	l.add(newInstrumentUsedType("Blue pippin", "blue-pippin", InstrumentCategory.find.findByCode("sizing-system"), null, 
				getInstruments(
						createInstrument("BluePippin1", "BluePippin1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST))),
						getContainerSupportCategories(new String[]{"96-well-plate"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));

		l.add(newInstrumentUsedType("Main", "hand", InstrumentCategory.find.findByCode("hand"), null, 
				getInstruments(
						createInstrument("hand", "Main", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)) ),
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("QuBit", "qubit", InstrumentCategory.find.findByCode("fluorometer"), getQuBitProperties(), 
				getInstruments(
						createInstrument("quBit1", "QuBit1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)),
						createInstrument("QuBit2", "QuBit2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)),
						createInstrument("QuBit3", "QuBit3", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST))
						), 
				getContainerSupportCategories(new String[]{"tube"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST))); //ok
		
		l.add(newInstrumentUsedType("GAIIx", "GAIIx", InstrumentCategory.find.findByCode("illumina-sequencer"), null, 
				getInstrumentGAII(),
				getContainerSupportCategories(new String[]{"flowcell-8"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("MISEQ", "MISEQ", InstrumentCategory.find.findByCode("illumina-sequencer"), getMiseqProperties(), 
				getInstrumentMiSeq(),
				getContainerSupportCategories(new String[]{"flowcell-1"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("HISEQ2000", "HISEQ2000", InstrumentCategory.find.findByCode("illumina-sequencer"), getHiseq2000Properties(), 
				getInstrumentHiseq2000(),
				getContainerSupportCategories(new String[]{"flowcell-8"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("HISEQ2500", "HISEQ2500", InstrumentCategory.find.findByCode("illumina-sequencer"), getHiseq2500Properties(), 
				getInstrumentHiseq2500(),
				getContainerSupportCategories(new String[]{"flowcell-8","flowcell-2"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("HISEQ4000", "HISEQ4000", InstrumentCategory.find.findByCode("illumina-sequencer"), getHiseq4000Properties(), 
				getInstrumentHiseq4000(),
				getContainerSupportCategories(new String[]{"flowcell-8"}), null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("Agilent 2100 bioanalyzer", "agilent-2100-bioanalyzer", InstrumentCategory.find.findByCode("chip-electrophoresis"), getChipElectrophoresisProperties(), 
				getInstruments(
						createInstrument("bioAnalyzer1", "BioAnalyzer1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST )), 
						createInstrument("bioAnalyzer2", "BioAnalyzer2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)) ), 
				getContainerSupportCategories(new String[]{"tube"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("Thermocycleur", "thermocycler", InstrumentCategory.find.findByCode("thermocycler"), getThermocyclerProperties(), 
				getInstruments(
						createInstrument("thermoS1", "ThermoS1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)), 
						createInstrument("thermoS2", "ThermoS2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)), 
						createInstrument("thermoS3", "ThermoS3",  null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)) 
						), 
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("cBot", "cBot", InstrumentCategory.find.findByCode("cbot"), getCBotProperties(), 
				getInstruments(
						createInstrument("cBot1", "cBot1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)),
						createInstrument("cBot2", "cBot2", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)),
						createInstrument("cBot3", "cBot3", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)),
						createInstrument("cBot4", "cBot4", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST))), 
				getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"flowcell-8","flowcell-2"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("Covaris E210", "covaris-e210", InstrumentCategory.find.findByCode("covaris"), getCovarisProperties(), 
				getInstruments(
						createInstrument("covaris3", "Covaris3", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)), 
						createInstrument("covaris4", "Covaris4", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST))						) , 
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		
		l.add(newInstrumentUsedType("LabChip GX", "labChipGX", InstrumentCategory.find.findByCode("chip-electrophoresis"), null, 
				getInstruments(
						createInstrument("labChip1", "LabChip1", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)) ) ,
				getContainerSupportCategories(new String[]{"384-well-plate","96-well-plate"}),null, 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
				
		
		l.add(newInstrumentUsedType("Eppendorf MiniSpin plus", "eppendorf-mini-spin-plus", InstrumentCategory.find.findByCode("centrifuge"), getNanoporeMiniSpinProperties(),  getInstrumentEppendorfMiniSpinPlus()
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"tube"}), DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		DAOHelpers.saveModels(InstrumentUsedType.class, l, errors);
		
		
		l.add(newInstrumentUsedType("MinION", "minION", InstrumentCategory.find.findByCode("nanopore-sequencer"), getNanoporeSequencerProperties(),getInstrumentMinIon() 
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"flowcell-1"}), DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("Mk1", "mk1", InstrumentCategory.find.findByCode("nanopore-sequencer"), getNanoporeSequencerProperties(),getInstrumentMKI() 
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"flowcell-1"}), DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		*/
		l.add(newInstrumentUsedType("Test One To One", "one-to-one", InstrumentCategory.find.findByCode("centrifuge"), getTestOneToOneProperties(),  getInstrumentTestOneToOne()
				,getContainerSupportCategories(new String[]{"tube"}), getContainerSupportCategories(new String[]{"tube"}), DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		l.add(newInstrumentUsedType("Main", "hand", InstrumentCategory.find.findByCode("hand"), null, 
				getInstruments(
						createInstrument("hand", "Main", null, true, null, DescriptionFactory.getInstitutes(Constants.CODE.TEST)) ),
				getContainerSupportCategories(new String[]{"tube"}),getContainerSupportCategories(new String[]{"tube"}), 
				DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		
		
		DAOHelpers.saveModels(InstrumentUsedType.class, l, errors);
	}

	private static List<PropertyDefinition> getTestOneToOneProperties() throws DAOException {
		List<PropertyDefinition> propertyDefinitions = new ArrayList<>();
        propertyDefinitions.add(newPropertiesDefinition("Inst. Content 1", "instCont1", LevelService.getLevels(Level.CODE.Instrument),String.class, true,
        		DescriptionFactory.newValues("G-TUBE"), "G-TUBE", null, null, null, "single", 1));
        propertyDefinitions.add(newPropertiesDefinition("Inst. 1", "inst1", LevelService.getLevels(Level.CODE.Instrument),String.class, false,
        		null, "8000", MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "rpm"),MeasureUnit.find.findByCode( "rpm"), "single", 2));
        // unite s
        propertyDefinitions.add(newPropertiesDefinition("Inst. Container 1", "instContainer1", LevelService.getLevels(Level.CODE.Instrument),String.class, false, 
        		null, "60",MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_TIME),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"), "single", 3));
		return propertyDefinitions;
	}
	
	private static List<Instrument> getInstrumentTestOneToOne() throws DAOException {
		List<Instrument> instruments = new ArrayList<>();
		instruments.add(createInstrument("inst-one-to-one", "One To One", null, true, "path", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		return instruments;
	}
	
//	private static List<PropertyDefinition> getNanoporeMiniSpinProperties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//        propertyDefinitions.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument),String.class, true,
//        		DescriptionFactory.newValues("G-TUBE"), "G-TUBE", null, null, null, "single", 1));
//        propertyDefinitions.add(newPropertiesDefinition("Vitesse", "speed", LevelService.getLevels(Level.CODE.Instrument),String.class, false,
//        		null, "8000", MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_SPEED),MeasureUnit.find.findByCode( "rpm"),MeasureUnit.find.findByCode( "rpm"), "single", 2));
//        // unite s
//        propertyDefinitions.add(newPropertiesDefinition("Durée", "duration", LevelService.getLevels(Level.CODE.Instrument),String.class, false, 
//        		null, "60",MeasureCategory.find.findByCode(MeasureService.MEASURE_CAT_CODE_TIME),MeasureUnit.find.findByCode( "s"),MeasureUnit.find.findByCode( "s"), "single", 3));
//		return propertyDefinitions;
//	}

//	private static List<PropertyDefinition> getNanoporeSequencerProperties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//        propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",100));
//        propertyDefinitions.add(newPropertiesDefinition("Version Flowcell", "flowcellChemistry", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",200));
//        //Liste a definir
//        propertyDefinitions.add(newPropertiesDefinition("Numero PC", "pcNumber", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single",300));
//       // propertyDefinitions.add(newPropertiesDefinition("Version MinKNOW", "minKnowVersion",LevelService.getLevels(Level.CODE.Instrument),String.class,false,"single",400));
//	//	propertyDefinitions.add(newPropertiesDefinition("Version Metrichor", "metrichorVersion",LevelService.getLevels(Level.CODE.Instrument),String.class,false,"single",500));
//	//	propertyDefinitions.add(newPropertiesDefinition("Metrichor run ID", "metrichorRunId",LevelService.getLevels(Level.CODE.Instrument),String.class,false,"single",600));
//
//		return propertyDefinitions;
//	}

//	private static List<PropertyDefinition> getCBotProperties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//        propertyDefinitions.add(newPropertiesDefinition("Type lectures","sequencingProgramType"
//        		, LevelService.getLevels(Level.CODE.Instrument,Level.CODE.ContainerSupport),String.class, true,DescriptionFactory.newValues("SR","PE"),"single"));
//      //  propertyDefinitions.add(newPropertiesDefinition("Type flowcell","flowcellType"
//       // 		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("Paired End FC Hiseq-v3","Single FC Hiseq-v3","Rapid FC PE HS 2500-v1","Rapid FC SR HS 2500-v1"),"single"));
//        propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single"));
//        propertyDefinitions.add(newPropertiesDefinition("Piste contrôle","controlLane", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)","1",
//        		"2","3","4","5","6","7","8"),"Pas de piste contrôle (auto-calibrage)","single"));
//        return propertyDefinitions;
//	}
	
//	private static List<PropertyDefinition> getCBotInterneProperties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//        propertyDefinitions.add(newPropertiesDefinition("Type lectures","sequencingProgramType"
//        		, LevelService.getLevels(Level.CODE.Instrument,Level.CODE.ContainerSupport),String.class, true,DescriptionFactory.newValues("SR","PE"),"single"));
//     //   propertyDefinitions.add(newPropertiesDefinition("Type flowcell","flowcellType"
//        //		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("Rapid FC PE HS 2500-v1","Rapid FC SR HS 2500-v1",
//        	//			"FC Miseq-v2","FC Miseq-v3"),"single"));
//        propertyDefinitions.add(newPropertiesDefinition("Code Flowcell", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single"));
//        propertyDefinitions.add(newPropertiesDefinition("Piste contrôle","controlLane", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)","1",
//        		"2"),"Pas de piste contrôle (auto-calibrage)","single"));
//        return propertyDefinitions;
//	}
	
//	private List<PropertyDefinition> getHiseq4000Properties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		 propertyDefinitions.add(newPropertiesDefinition("Position","position"
//        		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("A","B"), "single",100));
//		
//		 propertyDefinitions.add(newPropertiesDefinition("Type lectures", "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",200));
//		
//		 propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1", "nbCyclesRead1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
//	        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
//	        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2", "nbCyclesRead2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
//	        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
//	        
//	        return propertyDefinitions;
//	}
	
//	private static List<PropertyDefinition> getHiseq2000Properties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//        propertyDefinitions.add(newPropertiesDefinition("Position","position"
//        		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("A","B"), "single",200));
//        propertyDefinitions.add(newPropertiesDefinition("Type lectures", "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",300));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1", "nbCyclesRead1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2", "nbCyclesRead2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",700));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
//        propertyDefinitions.add(newPropertiesDefinition("Piste contrôle","controlLane", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValuesWithDefault("Pas de piste contrôle (auto-calibrage)","Pas de piste contrôle (auto-calibrage)","1",
//        		"2","3","4","5","6","7","8"),"Pas de piste contrôle (auto-calibrage)","single",100));
//        return propertyDefinitions;
//	}

//	private static List<PropertyDefinition> getMiseqProperties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//		propertyDefinitions.add(newPropertiesDefinition("Nom cassette Miseq", "miseqReagentCassette",LevelService.getLevels(Level.CODE.Instrument),String.class,true,"single",100));
//        propertyDefinitions.add(newPropertiesDefinition("Type lectures", "sequencingProgramType", LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("SR","PE"), "single",200));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read1", "nbCyclesRead1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",300));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index1", "nbCyclesReadIndex1", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",400));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read2", "nbCyclesRead2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",600));
//        propertyDefinitions.add(newPropertiesDefinition("Nb cycles Read Index2", "nbCyclesReadIndex2", LevelService.getLevels(Level.CODE.Instrument),Integer.class, true, "single",500));
//        return propertyDefinitions;
//	}
	
//	private static List<PropertyDefinition> getArgusProperties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = new ArrayList<PropertyDefinition>();
//        propertyDefinitions.add(newPropertiesDefinition("Type de MapCard", "mapcardType", LevelService.getLevels(Level.CODE.Instrument),String.class, true, newValues("standard","HD"), "single"));
//        propertyDefinitions.add(newPropertiesDefinition("Référence Carte", "containerSupportCode", LevelService.getLevels(Level.CODE.Instrument),String.class, true, "single"));
//		propertyDefinitions.add(newPropertiesDefinition("Enzyme de restriction", "restrictionEnzyme", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("AfIII","ApaLI","BamHI","BgIII","EcoRI","HindIII","KpnI","MIuI","Ncol","NdeI","NheI","NotI","PvuII","SpeI","XbaI","XhoI"), "single"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Tableau sélection enzyme","enzymeChooser",LevelService.getLevels(Level.CODE.Instrument), Image.class, false, "img"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Photo digestion","digestionForTracking",LevelService.getLevels(Level.CODE.Instrument), Image.class, false, "img"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Metrix","anlyseMetrics",LevelService.getLevels(Level.CODE.Instrument), Image.class, false, "img"));
//		propertyDefinitions.add(DescriptionFactory.newPropertiesDefinition("Statistiques Contigs","contigStatistics",LevelService.getLevels(Level.CODE.Instrument), Image.class, false, "img"));
//        return propertyDefinitions;
//	}

//	private static List<PropertyDefinition> getHiseq2500Properties() throws DAOException {
//		List<PropertyDefinition> propertyDefinitions = getHiseq2000Properties();		
//	   propertyDefinitions.add(0, newPropertiesDefinition("Mode run","runMode"
//	        		, LevelService.getLevels(Level.CODE.Instrument),String.class, true,DescriptionFactory.newValues("normal","rapide"), "single",50));
//        return propertyDefinitions;
//	}
	
//	private static List<PropertyDefinition> getCovarisProperties() throws DAOException {
//		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
//		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("Frag_PE300","Frag_PE400","Frag_PE500","Frag_cDNA_Solexa"), "single"));
//		return l;
//	}
	
//	private static List<PropertyDefinition> getSpriProperties() throws DAOException {
//		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
//		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("300-600"), "single"));		
//		return l;
//	}
	
//	private static List<PropertyDefinition> getThermocyclerProperties() throws DAOException {
//		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
//		l.add(newPropertiesDefinition("Programme", "program", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("15","18"), "single"));		
//		return l;
//	}
	
//	private static List<PropertyDefinition> getChipElectrophoresisProperties() throws DAOException {
//		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
//		l.add(newPropertiesDefinition("Type puce", "chipType", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("DNA HS", "DNA 12000", "RNA"), "single"));		
//		return l;
//	}
	
//	private static List<PropertyDefinition> getQuBitProperties() throws DAOException {
//		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
//		l.add(newPropertiesDefinition("Kit", "kit", LevelService.getLevels(Level.CODE.Instrument), String.class, true, newValues("HS", "BR"), "single"));		
//		return l;
//	}
	
//	private static List<PropertyDefinition> getQPCRProperties() throws DAOException {
//		List<PropertyDefinition> l = new ArrayList<PropertyDefinition>();
//		l.add(newPropertiesDefinition("Nb. Echantillon", "sampleNumber", LevelService.getLevels(Level.CODE.Instrument), Integer.class, true, "single"));		
//		return l;
//	}
			
	public static List<Instrument> getInstrumentOpgen()throws DAOException{
		List<Instrument> instruments=new ArrayList<>();
		instruments.add( createInstrument("APOLLON", "APOLLON", null, true, "/env/ig/atelier/opgen/cns/APOLLON", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		return instruments;
	}
	
//	private static List<Instrument> getInstrumentGAII() throws DAOException {
//		List<Instrument> instruments=new ArrayList<Instrument>();
//		instruments.add(createInstrument("BISMUTH", "BISMUTH", "G3", false, "/env/ig/atelier/illumina_BISMUTH", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
//		instruments.add(createInstrument("HELIUM", "HELIUM", "G1", false, "/env/ig/atelier/illumina_HELIUM", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
//		instruments.add(createInstrument("AZOTE", "AZOTE", "G2", false, "/env/ig/atelier/illumina_AZOTE", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
//		
//		return instruments;
//	}

//	private static List<Instrument> getInstrumentMiSeq() throws DAOException {
//		List<Instrument> instruments=new ArrayList<Instrument>();
//		instruments.add(createInstrument("MELISSE", "MELISSE", "M2", true, "/env/ig/atelier/illumina/cns/MELISSE", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
//		instruments.add(createInstrument("MIMOSA", "MIMOSA", "M1", true, "/env/ig/atelier/illumina/cns/MIMOSA", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
//		instruments.add(createInstrument("MISEQ1", "MISEQ1", "M1C", false, "/env/ig/atelier/illumina/cng/MISEQ1", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
//		return instruments;
//	}
	
	public static List<Instrument> getInstrumentHiseq2000() throws DAOException{
		List<Instrument> instruments=new ArrayList<>();
		instruments.add(createInstrument("HISEQ2", "HISEQ2", "H2C", false, "/env/ig/atelier/illumina/cng/HISEQ2/", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		instruments.add(createInstrument("HISEQ4", "HISEQ4", "H4C", false, "/env/ig/atelier/illumina/cng/HISEQ4/", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		instruments.add(createInstrument("HISEQ7", "HISEQ7", "H7C", false, "/env/ig/atelier/illumina/cng/HISEQ7/", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		instruments.add(createInstrument("CARBONE", "CARBONE", "H6", true, "/env/ig/atelier/illumina/cns/CARBONE", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
		instruments.add(createInstrument("CHROME", "CHROME", "H1", false, "/env/ig/atelier/illumina_CHROME", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
		instruments.add(createInstrument("MERCURE", "MERCURE", "H2", true, "/env/ig/atelier/illumina/cns/MERCURE", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
		instruments.add(createInstrument("SOUFRE", "SOUFRE", "H4", true, "/env/ig/atelier/illumina/cns/SOUFRE", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
		instruments.add( createInstrument("PHOSPHORE", "PHOSPHORE", "H3", true, "/env/ig/atelier/illumina/cns/PHOSPHORE", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		return instruments;
	}
	
	public static List<Instrument> getInstrumentHiseq2500() throws DAOException{
		List<Instrument> instruments=new ArrayList<>();
		instruments.add( createInstrument("HISEQ9", "HISEQ9", "H9C", false, "/env/ig/atelier/illumina/cng/HISEQ9/", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		instruments.add( createInstrument("HISEQ10", "HISEQ10", "H10C", false, "/env/ig/atelier/illumina/cng/HISEQ10/", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		instruments.add( createInstrument("HISEQ11", "HISEQ11", "H11C", false, "/env/ig/atelier/illumina/cng/HISEQ11/", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));		
		instruments.add( createInstrument("FLUOR", "FLUOR", "H8", true, "/env/ig/atelier/illumina/cns/FLUOR", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		instruments.add( createInstrument("PLATINE", "PLATINE", "H7", true, "/env/ig/atelier/illumina/cns/PLATINE", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		return instruments;
	}
	
//	private List<Instrument> getInstrumentHiseq4000() throws DAOException {
//		List<Instrument> instruments=new ArrayList<Instrument>();
//		instruments.add(createInstrument("TORNADE", "TORNADE", "H5", true, "/env/ig/atelier/illumina/cns/TORNADE", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
//		instruments.add(createInstrument("RAFALE", "RAFALE", "H9", true, "/env/ig/atelier/illumina/cns/RAFALE", DescriptionFactory.getInstitutes(Constants.CODE.TEST)) );
//		return instruments;
//	}
	
	public static List<Instrument> getInstrumentExtSolexa()throws DAOException{
		List<Instrument> instruments=new ArrayList<>();
		instruments.add( createInstrument("EXTGAIIX", "EXTGAIIX", "G0", true, "/env/atelier", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		instruments.add( createInstrument("EXTHISEQ", "EXTHISEQ", "H0", true, "/env/atelier", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		instruments.add( createInstrument("EXTMISEQ", "EXTMISEQ", "M0", true, "/env/atelier", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
		return instruments;
	}
		
//	private List<Instrument> getInstrumentMKI() throws DAOException {
//		List<Instrument> instruments=new ArrayList<Instrument>();
//		instruments.add(createInstrument("MN15456", "MK15456", null, true, "/env/ig/atelier/nanopore/cns/MN15456", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		instruments.add(createInstrument("MN15302", "MK15302", null, true, "/env/ig/atelier/nanopore/cns/MN15302", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		instruments.add(createInstrument("MN15382", "MK15382", null, true, "/env/ig/atelier/nanopore/cns/MN15382", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		instruments.add(createInstrument("MN15407", "MK15407", null, true, "/env/ig/atelier/nanopore/cns/MN15407", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		instruments.add(createInstrument("MN15464", "MK15464", null, true, "/env/ig/atelier/nanopore/cns/MN15464", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		instruments.add(createInstrument("MN15336", "MK15336", null, true, "/env/ig/atelier/nanopore/cns/MN15336", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		instruments.add(createInstrument("MN15782", "MK15782", null, true, "/env/ig/atelier/nanopore/cns/MN15782", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		instruments.add(createInstrument("MN15794", "MK15794", null, true, "/env/ig/atelier/nanopore/cns/MN15794", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		return instruments;
//	}
	
//	private static List<Instrument> getInstrumentMinIon() throws DAOException {
//		List<Instrument> instruments=new ArrayList<Instrument>();
//		instruments.add(createInstrument("MN02528", "MN02528", null, true, "/env/ig/atelier/nanopore/cns/MN02528", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		instruments.add(createInstrument("MN02670", "MN02670", null, true, "/env/ig/atelier/nanopore/cns/MN02670", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		instruments.add(createInstrument("MN02280", "MN02280", null, true, "/env/ig/atelier/nanopore/cns/MN02280", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		instruments.add(createInstrument("MN02259", "MN02259", null, true, "/env/ig/atelier/nanopore/cns/MN02259", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		instruments.add(createInstrument("MN02833", "MN02833", null, true, "/env/ig/atelier/nanopore/cns/MN02833", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		return instruments;
//	}
	
//	private static List<Instrument> getInstrumentEppendorfMiniSpinPlus() throws DAOException {
//		List<Instrument> instruments=new ArrayList<Instrument>();
//		instruments.add(createInstrument("MiniSpin plus 1", "miniSpinPlus1", null, true, "path", DescriptionFactory.getInstitutes(Constants.CODE.TEST)));
//		return instruments;
//	}
	
}
